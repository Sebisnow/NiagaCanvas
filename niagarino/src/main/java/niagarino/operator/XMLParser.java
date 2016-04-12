/*
 * @(#)XMLParser.java   1.0   May 28, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * XML Parser that gives tuples according to given SAX handler, which operates on incoming tuples.
 *
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class XMLParser extends AbstractOperator {

   /** Logger. */
   private static final Logger LOG_MP = LogManager.getLogger(MiniParser.class.getSimpleName());

   /** Parsing instructions for the SAX parser, and Tuple creator. */
   private final XMLTupleEmitter emitter;

   /** SAX parser. */
   private final MiniParser xThread;

   /** The buffer in which to cache the document. */
   private PipedWriter buffer;

   // XMLParser
   // - creates SAXParser
   // - puts Tuples into inputstream/outputstream-combo
   // - requests a tuple after every input tuple

   /**
    * Constructs XMLParser.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           input schema
    * @param emitter
    *           emits tuples based on incoming XML data
    * @throws SAXException
    *            some SAX error
    * @throws ParserConfigurationException
    *            if SAX is not configured properly
    */
   public XMLParser(final String operatorId, final Schema inputSchema, final XMLTupleEmitter emitter)
         throws ParserConfigurationException, SAXException {
      super(operatorId, Arrays.asList(inputSchema));
      this.emitter = emitter;

      this.xThread = new MiniParser(this.emitter);
      this.xThread.start();
      this.resetDocument();
   }

   /**
    * Resets everything for the processing of the next document.
    */
   private void resetDocument() {
      final PipedReader reader = new PipedReader(10000);
      try {
         this.buffer = new PipedWriter(reader);
      } catch (final IOException e) {
         // Cannot happen.
         e.printStackTrace();
      }
      this.emitter.reset();
      this.xThread.resetSource(reader);
   }

   /**
    * Finishes the document parsing and emits remaining tuples.
    */
   private void finishDocument() {
      try {
         LOG_MP.debug("Closing output stream.");
         this.buffer.flush();
         this.buffer.close();
      } catch (final IOException e) {
         // Cannot happen.
         e.printStackTrace();
      }
      this.buffer = null;
      // Blocks until done with parsing.
      this.xThread.finishDocument();
      this.emitTuples();
   }

   /**
    * Emits all tuples and controls that are available right now.
    */
   private void emitTuples() {
      while (this.emitter.tupleAvailable()) {
         this.pushTuple(this.emitter.nextTuple());
         final ControlTuple[] controls = this.emitter.nextControls();
         for (final ControlTuple control : controls) {
            this.pushControl(Flow.FORWARD, control);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      // TODO make it possible to specify the attribute containing the XML data
      // We start a new document if not yet started. Necessary because else at the end there might be an
      // empty document which the parser doesn't like.
      if (this.buffer == null) {
         this.resetDocument();
      }
      try {
         this.buffer.append(tuple.getValues().get(0).toString() + System.lineSeparator());
         // LOG_MP.debug(tuple.getValues().get(0).toString());
      } catch (final IOException e) {
         // Cannot happen.
         e.printStackTrace();
      }
      this.emitTuples();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processForwardControl(final int input, final ControlTuple message) {
      // Act only on punctuations that interest you.
      if (ControlTuple.Type.PUNCTUATION.equals(message.getType())) {
         final PunctuationControl punct = (PunctuationControl) message;
         if (PunctuationControl.Type.END_OF_ENTITY.equals(punct.getPunctuationType())) {
            this.finishDocument();
            LOG_MP.debug("Entity ended: " + punct.getSegmentId());
         }
      }
      super.processForwardControl(input, message);
      // TODO what happens with the end of xml or end of readfile punctuations? currently it gets the default
      // handling (probably "carry on") of the AbstractOperator
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Socket.INPUT.equals(socket)) {
         if (this.buffer != null) {
            // Previous document was not finished, finish it now.
            this.finishDocument();
         }
         // Stop the extra thread.
         this.xThread.close();
      }
      super.handleEoS(socket, input, message);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getOutputSchema() {
      return this.emitter.getOutputSchema();
   }

   /**
    * Factory or new instances of the XMLParser operator.
    *
    * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Factory implements PlanOperatorByParametersFactory {

      @Override
      public Operator getOperatorByParameters(final String operatorId, final Schema inputSchema,
            final Parameter parameters) throws Exception {
         final String emitterName = parameters.get("object").get("class").getString();
         final XMLTupleEmitter emitter = (XMLTupleEmitter) Class.forName(emitterName).newInstance();
         return new XMLParser(operatorId, inputSchema, emitter);
      }
   }

   /**
    * Parses given input sources using a given SAX handler in an extra thread.
    *
    * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public class MiniParser extends Thread {

      /** The SAX handler. */
      private final XMLTupleEmitter emitter;

      /** The SAX parser. */
      private final XMLReader sax;

      /** Whether this {@link MiniParser} is running. */
      private boolean running;

      /** Wether this {@link MiniParser} is parsing. */
      private boolean parsing;

      /** The current input source for the SAX parser. */
      private LinkedList<InputSource> sources;

      /**
       * Constructs a new {@link MiniParser} with the given SAX handler.
       *
       * @param emitter
       *           the SAX handler
       * @throws ParserConfigurationException
       *            if the SAX configuration is faulty
       * @throws SAXException
       *            if the SAX configuration is faulty
       */
      public MiniParser(final XMLTupleEmitter emitter) throws ParserConfigurationException, SAXException {
         this.emitter = emitter;
         this.sax = this.prepareSAX();
         this.running = true;
         this.parsing = false;
         this.sources = new LinkedList<>();
         this.setName("MiniParser");
      }

      @Override
      public void run() {
         while (this.running) {
            // Parse only if a source is available.
            if (this.sources.size() > 0) {
               final InputSource source = this.sources.poll();
               // Start parsing.
               synchronized (this) {
                  LOG_MP.debug("Start parsing.");
                  this.parsing = true;
               }
               try {
                  this.sax.parse(source);
               } catch (final IOException | SAXException e) {
                  // Stuff fails? Let it fail, we throw away.
                  e.printStackTrace();
               }
               synchronized (this) {
                  this.parsing = false;
                  LOG_MP.debug("Notify: Parsing finished.");
                  // Notify other threads that we're not parsing at the moment.
                  this.notifyAll();
               }
            } else {
               synchronized (this) {
                  // Wait for the source to become available.
                  if (this.sources.size() == 0) {
                     LOG_MP.debug("Waiting for source to become available.");
                     try {
                        this.wait();
                     } catch (final InterruptedException e) {
                        // Nothing to do here.
                     }
                     LOG_MP.debug("Woken: Looking for source.");
                  }
               }
            }
         }
      }

      /**
       * Initializes the SAX parser.
       *
       * @return the XMLReader of the SAX parser
       * @throws ParserConfigurationException
       *            if SAX is not configured properly
       * @throws SAXException
       *            some SAX error
       */
      private XMLReader prepareSAX() throws ParserConfigurationException, SAXException {
         final SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setNamespaceAware(true);
         final SAXParser saxParser = spf.newSAXParser();

         final XMLReader sax = saxParser.getXMLReader();
         sax.setContentHandler(this.emitter);
         sax.setErrorHandler(this.emitter);
         return sax;
      }

      /**
       * Sends signal and waits until the current document is done parsing.
       */
      public void finishDocument() {
         // this.source = null;
         // Block until parsing is finished.
         synchronized (this) {
            while (this.parsing) {
               LOG_MP.debug("Waiting for parsing to finish.");
               try {
                  this.wait();
               } catch (final InterruptedException e) {
                  // Nothing to do here.
               }
               LOG_MP.debug("Woken (finish): Continuing if parsing finished.");
            }
         }
      }

      /**
       * Sets a new source for this {@link MiniParser}.
       *
       * @param reader
       *           the new input source
       */
      public void resetSource(final Reader reader) {
         if (reader != null) {
            synchronized (this) {
               this.sources.add(new InputSource(reader));
               LOG_MP.debug("Notify: New source available.");
               this.notifyAll();
            }
         }
      }

      /**
       * Makes this {@link MiniParser} terminate as soon as all pending work is done.
       */
      public synchronized void close() {
         this.running = false;
         this.notifyAll();
      }
   }
}
