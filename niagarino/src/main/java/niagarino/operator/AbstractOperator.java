/*
 * @(#)AbstractOperator.java   1.0   Feb 14, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.stream.ControlTuple;
import niagarino.stream.ControlTuple.Type;
import niagarino.stream.DataTuple;
import niagarino.stream.Page;
import niagarino.stream.Schema;
import niagarino.stream.Stream;
import niagarino.stream.Stream.Flow;
import niagarino.stream.StreamElement;
import niagarino.stream.TupleElement;
import niagarino.util.PropertiesReader;

/**
 * Abstract operator implementation.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public abstract class AbstractOperator implements Operator, Runnable {

   /** Logger for this class. */
   private static final Logger LOG = LogManager.getLogger(AbstractOperator.class);
   /** Initial value for exponential back-off in milliseconds. */
   private static final long BACK_OFF = 1;
   /** Maximum back-off value in milliseconds. */
   private static final long MAX_BACK_OFF = 128;

   /** Name of the operator. */
   private final String operatorId;
   /** Schema definitions of operator inputs. */
   private final List<Schema> inputSchemas;
   /** Map of all input and output streams. */
   private final Map<Socket, List<Stream>> streams;
   /** Map of bit vectors that indicate which streams have ended. */
   private final Map<Socket, boolean[]> eos;
   /** Current sleep duration in milliseconds used in exponential back-off. */
   private long sleep;
   /** Indicates whether this operator is currently running. */
   private boolean running;
   /** Indicates whether this operator is a sink of the query plan. */
   private boolean sink;
   /** Indicates whether this operator uses paging. */
   private final boolean paging;
   /** Statistics collected during execution of this operator. */
   private final OperatorEventListenerList listeners;
   /** List of Pages for every stream. */
   private final ArrayList<Page> pages;
   /** Number of Tuples in One Page. */
   private final int pageSize;

   /**
    * Constructs a new abstract operator with the given input schemas, ignoring input and output arity.
    *
    * @param operatorId
    *           name of operator
    * @param inputSchemas
    *           input schema definition
    * @param inputArity
    *           operator input arity
    * @param outputArity
    *           operator output arity
    */
   @Deprecated
   protected AbstractOperator(final String operatorId, final List<Schema> inputSchemas, final int inputArity,
         final int outputArity) {
      this(operatorId, inputSchemas);
   }

   /**
    * Constructs a new abstract operator with the given input schemas.
    *
    * @param operatorId
    *           name of operator
    * @param inputSchemas
    *           input schema definition
    */
   protected AbstractOperator(final String operatorId, final List<Schema> inputSchemas) {
      this.operatorId = operatorId;
      this.inputSchemas = inputSchemas;
      this.sink = false;
      this.running = false;
      this.sleep = AbstractOperator.BACK_OFF;
      this.streams = new HashMap<Socket, List<Stream>>();
      this.streams.put(Socket.INPUT, new ArrayList<Stream>());
      this.streams.put(Socket.OUTPUT, new ArrayList<Stream>());
      this.eos = new HashMap<Socket, boolean[]>();
      this.paging = Boolean.parseBoolean(PropertiesReader.getPropertiesReader().getProperties()
            .getProperty(PropertiesReader.PAGING_ENABLED));
      this.listeners = new OperatorEventListenerList();
      this.pages = new ArrayList<Page>();
      this.pageSize = Integer.parseInt(PropertiesReader.getPropertiesReader().getProperties()
            .getProperty(PropertiesReader.PAGING_PAGESIZE));
   }

   @Override
   public String getName() {
      return this.operatorId;
   }

   @Override
   public int getInputArity() {
      return this.streams.get(Socket.INPUT).size();
   }

   @Override
   public int getOutputArity() {
      return this.isSink() ? 1 : this.streams.get(Socket.OUTPUT).size();
   }

   @Override
   public void addInputStream(final Stream stream) {
      final List<Stream> inputStreams = this.streams.get(Socket.INPUT);
      if (!this.running) {
         inputStreams.add(stream);
      } else {
         throw new UnsupportedOperationException("Operator is running and cannot be changed.");
      }
   }

   @Override
   public void addOutputStream(final Stream stream) {
      final List<Stream> outputStreams = this.streams.get(Socket.OUTPUT);
      if (!this.running) {
         outputStreams.add(stream);
      } else {
         throw new UnsupportedOperationException("Operator is running and cannot be changed.");
      }
   }

   @Override
   public void setSink(final boolean sink) {
      if (!this.running) {
         this.sink = sink;
      } else {
         throw new UnsupportedOperationException("Operator is running and cannot be changed.");
      }
   }

   @Override
   public boolean isSink() {
      return this.sink;
   }

   @Override
   public void stop() {
      this.running = false;
      // Clear all streams this AbstractOperator reads from to prevent blocking other operators.
      for (final Stream stream : this.streams.get(Socket.INPUT)) {
         stream.clearStream(Socket.INPUT.read());
      }
      for (final Stream stream : this.streams.get(Socket.OUTPUT)) {
         stream.clearStream(Socket.OUTPUT.read());
      }
   }

   @Override
   public boolean isRunning() {
      return this.running;
   }

   /**
    * Executes actions that need to be done after all configuration has completed.
    */
   private void startActions() {
      this.eos.put(Socket.INPUT, new boolean[this.getInputArity()]);
      this.eos.put(Socket.OUTPUT, new boolean[this.getOutputArity()]);
      Arrays.fill(this.eos.get(Socket.INPUT), false);
      Arrays.fill(this.eos.get(Socket.OUTPUT), false);
   }

   @Override
   public synchronized void run() {
      this.running = true;
      this.startActions();
      do {
         final boolean found = this.processStreams();
         if (!found) {
            // exponential back-off
            try {
               Thread.sleep(this.sleep);
               // record total sleep time
               if (this.sleep < MAX_BACK_OFF) {
                  this.sleep *= 2;
               }
            } catch (final InterruptedException e) {
               throw new OperatorException(this, e);
            }
            // record the scheduling miss in the statistics
         } else {
            this.sleep = BACK_OFF;
         }
         if (this.isEoS(Socket.OUTPUT)) {
            this.running = false;
         } else if (this.sink && this.isEoS(Socket.INPUT)) {
            this.handleEoS(Socket.OUTPUT, 0, new ControlTuple(Type.EOS));
            this.running = false;
         }
      } while (this.running);
      this.listeners.fireOnShutdown(this);
      this.shutDown();
   }

   /**
    * Pushes the given stream element with the given flow direction.
    *
    * @param flow
    *           forward or backward stream flow direction
    * @param element
    *           stream element
    */
   private void pushElement(final Flow flow, final StreamElement element) {
      final Socket socket = Socket.getPushSocket(flow);
      final List<Stream> streams = this.streams.get(socket);
      // forward stream element to all streams
      StreamElement out = element;
      for (int i = 0; i < streams.size(); i++) {
         // check if element needs to be cloned
         if (i > 0 && element instanceof TupleElement) {
            try {
               out = ((TupleElement) element).clone();
            } catch (final CloneNotSupportedException e) {
               throw new OperatorException(this, e);
            }
         }
         final Stream stream = streams.get(i);
         if (element instanceof DataTuple) {
            final DataTuple tuple = (DataTuple) out;
            // handle tuple stream elements
            if (this.paging) {
               // make sure there are enough output pages
               if (this.pages.size() <= i) {
                  this.pages.add(new Page(this.pageSize));
               }
               final Page page = this.pages.get(i);
               page.put(tuple);
               if (page.isFull()) {
                  stream.pushElement(socket.write(), page);
                  this.pages.set(i, new Page(this.pageSize));
               }
            } else {
               stream.pushElement(socket.write(), out);
            }
            this.listeners.fireOnOutputTuple(this, tuple);
         } else if (element instanceof ControlTuple) {
            // handle control stream elements
            if (this.paging) {
               // flush pages before forwarding control stream element
               this.flushPages();
            }
            stream.pushElement(socket.write(), out);
         }
      }
   }

   /**
    * Flush out all pages to the corresponding streams of the given socket.
    */
   private void flushPages() {
      final List<Stream> streams = this.streams.get(Socket.OUTPUT);
      int i = 0;
      if (!this.pages.isEmpty()) {
         for (final Stream stream : streams) {
            final Page page = this.pages.get(i);
            if (!page.isEmpty()) {
               stream.pushElement(Socket.OUTPUT.write(), page);
               this.pages.set(i, new Page(this.pageSize));
            }
            i++;
         }
      }
   }

   /**
    * Returns the schema definition of the inputs of this operator.
    *
    * @return input schema definitions
    */
   protected List<Schema> getInputSchemas() {
      return Collections.unmodifiableList(this.inputSchemas);
   }

   /**
    * Checks if all streams at the given socket have ended.
    *
    * @param socket
    *           input or output socket
    * @return {@code true} if all streams at the given socket have ended, {@code false} otherwise
    */
   private boolean isEoS(final Socket socket) {
      boolean eos = true;
      final boolean[] streamEos = this.eos.get(socket);
      for (final boolean element : streamEos) {
         eos = eos && element;
      }
      return eos;
   }

   /**
    * Reads from all output streams, then from all input streams, and processes any stream element it
    * encounters.
    *
    * @return {@code true} if a stream element was read and processed, {@code false} otherwise
    */
   protected boolean processStreams() {
      // read from output streams
      final boolean found = this.processStreams(Socket.OUTPUT);
      // read from input streams
      return this.processStreams(Socket.INPUT) || found;
   }

   /**
    * Reads from all streams at the given socket and processes any stream element it encounters.
    *
    * @param socket
    *           input or output socket
    * @return {@code true} if a stream element was read and processed, {@code false} otherwise
    */
   private boolean processStreams(final Socket socket) {
      final List<Stream> streams = this.streams.get(socket);
      final boolean[] streamEos = this.eos.get(socket);
      boolean found = false;
      for (int input = 0; input < streams.size(); input++) {
         if (!streamEos[input]) {
            final Stream stream = streams.get(input);
            final StreamElement element = stream.pullElement(socket.read());
            if (element != null) {
               this.processElement(socket, input, element);
               found = true;
            }
         }
      }
      return found;
   }

   /**
    * Processes the given stream element that was read from the given socket and input.
    *
    * @param socket
    *           input or output socket
    * @param input
    *           input number
    * @param element
    *           stream element
    */
   private void processElement(final Socket socket, final int input, final StreamElement element) {
      if (element instanceof DataTuple) {
         if (Socket.INPUT.equals(socket)) {
            final DataTuple tuple = (DataTuple) element;
            this.processTuple(input, tuple);
            this.listeners.fireOnInputTuple(this, tuple);
         }
      } else if (element instanceof ControlTuple) {
         final ControlTuple message = (ControlTuple) element;
         if (Type.EOS.equals(message.getType())) {
            this.handleEoS(socket, input, message);
         } else {
            switch (socket) {
               case INPUT:
                  this.processForwardControl(input, message);
                  break;
               case OUTPUT:
                  this.processBackwardControl(input, message);
                  break;
               default:
                  // do nothing
            }
         }
      } else if (element instanceof Page) {
         final Page p = (Page) element;
         final DataTuple[] tuples = p.getTuples();
         for (final DataTuple tuple : tuples) {
            if (Socket.INPUT.equals(socket) && tuple != null) {
               this.processTuple(input, tuple);
               this.listeners.fireOnInputTuple(this, tuple);
            }
         }
      }
   }

   /**
    * Pushes the given data tuple forward.
    *
    * @param tuple
    *           data tuple
    */
   protected void pushTuple(final DataTuple tuple) {
      this.pushElement(Flow.FORWARD, tuple);
   }

   /**
    * Processes the given data tuple from the given input. This default implementation simply pushes the data
    * tuple forward.
    *
    * @param input
    *           input number
    * @param tuple
    *           data tuple
    */
   protected void processTuple(final int input, final DataTuple tuple) {
      this.pushTuple(tuple);
   }

   /**
    * Pushes the given control message with the given stream flow direction.
    *
    * @param flow
    *           forward or backward stream flow direction.
    * @param message
    *           control message
    */
   protected void pushControl(final Flow flow, final ControlTuple message) {
      this.pushElement(flow, message);
   }

   /**
    * Processes the given control message from the given input along the forward stream flow. This default
    * implementation simply pushes the control messages forward.
    *
    * @param input
    *           input number
    * @param message
    *           control message
    */
   protected void processForwardControl(final int input, final ControlTuple message) {
      this.pushControl(Flow.FORWARD, message);
   }

   /**
    * Processes the given control message from the given input along the backward stream flow. This default
    * implementation simply pushes the control messages backward.
    *
    * @param input
    *           input number
    * @param message
    *           control message
    */
   protected void processBackwardControl(final int input, final ControlTuple message) {
      this.pushControl(Flow.BACKWARD, message);
   }

   /**
    * Handles EoS messages specifically. If this implementation is overwritten, the behaviour of this
    * implementation needs to be reimplemented in a compatible manner in the overwriting method, or this
    * imlementation just be called upon using {@code super.handleEoS}.
    *
    * @param socket
    *           which socket the EoS message originated from
    * @param input
    *           which input id the message originated from
    * @param message
    *           the message itself
    */
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Type.EOS.equals(message.getType())) {
         // mark input as EoS
         this.eos.get(socket)[input] = true;
         // find out whether entire socket is EoS
         if (this.isEoS(socket)) {
            if (Socket.INPUT.equals(socket)) {
               LOG.debug("Pushing EoS forward.");
               this.pushControl(Flow.FORWARD, new ControlTuple(Type.EOS));
            } else if (Socket.OUTPUT.equals(socket)) {
               LOG.debug("Pushing EoS backward.");
               this.pushControl(Flow.BACKWARD, new ControlTuple(Type.EOS));
            }
         } else {
            LOG.debug("Not ready to push EoS on yet.");
         }
      }
   }

   /**
    * Check if paging is enabled.
    *
    * @return boolean is Paging enabled?
    */
   protected boolean isPaging() {
      return this.paging;
   }

   /**
    * Shut down method that is called before the operator is completely stopped.
    */
   protected void shutDown() {
      // sleep needed to be sure console output makes it in time
      try {
         Thread.sleep(100);
      } catch (final InterruptedException e) {
         throw new OperatorException(this, e);
      }
   }

   /**
    * Enumeration to describe the input and output socket of a stream operator.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   protected enum Socket {

      /** Input socket. */
      INPUT,

      /** Output socket. */
      OUTPUT;

      /**
       * Returns the read flow of this socket. For an input socket, the read flow direction is forward. For an
       * output socket, the read flow direction is backward.
       *
       * @return read flow direction
       */
      Flow read() {
         switch (this) {
            case INPUT:
               return Flow.FORWARD;
            case OUTPUT:
               return Flow.BACKWARD;
            default:
               return null;
         }
      }

      /**
       * Returns the write flow of this socket. For an input socket, the write flow direction is backward. For
       * an output socket, the write flow direction is forward.
       *
       * @return write flow direction
       */
      Flow write() {
         switch (this) {
            case INPUT:
               return Flow.BACKWARD;
            case OUTPUT:
               return Flow.FORWARD;
            default:
               return null;
         }
      }

      /**
       * Returns the push socket for the given flow direction. For the forward flow, the push socket is the
       * output socket. For the backward flow, the push socket is the input socket.
       *
       * @param flow
       *           stream flow direction
       * @return push stream socket
       */
      static Socket getPushSocket(final Flow flow) {
         switch (flow) {
            case FORWARD:
               return OUTPUT;
            case BACKWARD:
               return INPUT;
            default:
               return null;
         }
      }

      /**
       * Returns the pull socket for the given flow direction. For the forward flow, the pull socket is the
       * input socket. For the backward flow, the pull socket is the output socket.
       *
       * @param flow
       *           stream flow direction
       * @return pull stream socket
       */
      static Socket getPullSocket(final Flow flow) {
         switch (flow) {
            case FORWARD:
               return INPUT;
            case BACKWARD:
               return OUTPUT;
            default:
               return null;
         }
      }
   };

   @Override
   public void addOperatorEventListener(final OperatorEventListener listener) {
      this.listeners.addOperatorEventListener(listener);
   }

   @Override
   public boolean removeOperatorEventListener(final OperatorEventListener listener) {
      return this.listeners.removeOperatorEventListener(listener);
   }
}
