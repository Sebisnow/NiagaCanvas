/*
 * @(#)RealTupleGenerator.java   1.0   Oct 22, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.simulator;

import java.util.Arrays;

import niagarino.operator.Operator;
import niagarino.operator.OperatorEventListener;
import niagarino.operator.OperatorEventListenerList;
import niagarino.operator.OperatorException;
import niagarino.stream.ControlTuple;
import niagarino.stream.ControlTuple.Type;
import niagarino.stream.DataTuple;
import niagarino.stream.Page;
import niagarino.stream.Schema;
import niagarino.stream.Stream;
import niagarino.stream.Stream.Flow;
import niagarino.stream.StreamElement;
import niagarino.util.PropertiesReader;

/**
 * Builds a stream of tuples.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class RealTupleGenerator implements Operator, Runnable {

   /** Name of the operator. */
   private final String operatorId;
   /** Schema of this scan operator. */
   private final Schema schema;
   /** Output stream of this scan operator. */
   private Stream stream;
   /** Indicates whether this operator is a sink. */
   private boolean sink;
   /** Indicates whether this operator is running. */
   private boolean running;
   /** Statistics collected by this operator. */
   private final OperatorEventListenerList listeners;
   /** Page. **/
   private Page page;
   /** Current window value. */
   private int window = 0;
   /** Maximum window value. */
   private final int maxWindows;
   /** Current group value. */
   private int group = 0;
   /** Is this operator paging? **/
   private final boolean isPaging = Boolean.parseBoolean(
         PropertiesReader.getPropertiesReader().getProperties().getProperty(PropertiesReader.PAGING_ENABLED));
   /** Number of tuples in a page. */
   private final int pageSize = Integer.parseInt(PropertiesReader.getPropertiesReader().getProperties()
         .getProperty(PropertiesReader.PAGING_PAGESIZE));

   /**
    * Constructs a new generator operator with the given output schema that creates tuples from the next()
    * function.
    *
    * @param schema
    *           output schema
    * @param maxWindowValue
    *           the maximum value the window value may have
    */
   public RealTupleGenerator(final Schema schema, final int maxWindowValue) {
      this(RealTupleGenerator.class.getSimpleName(), schema, maxWindowValue);
   }

   /**
    * Constructs a new generator operator with the given output schema that creates tuples from the next()
    * function.
    *
    * @param operatorId
    *           name of operator
    * @param schema
    *           output schema
    * @param maxWindowValue
    *           the maximum value the window value may have
    */
   public RealTupleGenerator(final String operatorId, final Schema schema, final int maxWindowValue) {
      this.operatorId = operatorId;
      this.stream = null;
      this.schema = schema;
      this.sink = false;
      this.running = false;
      this.listeners = new OperatorEventListenerList();
      this.page = null;
      this.maxWindows = maxWindowValue;
   }

   @Override
   public String getName() {
      return this.operatorId;
   }

   @Override
   public Schema getOutputSchema() {
      return this.schema;
   }

   @Override
   public void addOutputStream(final Stream stream) {
      if (this.stream == null) {
         this.stream = stream;
      } else {
         throw new IllegalStateException("Maximum number of output streams exceeded.");
      }
   }

   @Override
   public void addInputStream(final Stream in) {
      throw new UnsupportedOperationException("A generator operator cannot have an input stream.");
   }

   @Override
   public int getOutputArity() {
      return 1;
   }

   @Override
   public int getInputArity() {
      return 0;
   }

   @Override
   public void setSink(final boolean sink) {
      this.sink = sink;
   }

   @Override
   public boolean isSink() {
      return this.sink;
   }

   @Override
   public void stop() {
      this.running = false;
   }

   @Override
   public boolean isRunning() {
      return this.running;
   }

   @Override
   public void run() {
      this.running = true;
      DataTuple tuple = null;
      while ((tuple = this.next()) != null) {
         if (this.isPaging) {
            if (this.page == null) {
               this.page = new Page(this.pageSize);
            }
            this.page.put(tuple);
            if (this.page.isFull()) {
               this.stream.pushElement(Flow.FORWARD, this.page);
               this.page = new Page(this.pageSize);
            }
         } else {
            this.stream.pushElement(Flow.FORWARD, tuple);
         }
         this.listeners.fireOnInputTuple(this, tuple);
      }
      if (this.isPaging) {
         this.stream.pushElement(Flow.FORWARD, this.page);
      }
      this.stream.pushElement(Flow.FORWARD, new ControlTuple(Type.EOS));
      boolean eof = false;
      do {
         final StreamElement element = this.stream.pullElement(Flow.BACKWARD);
         if (element != null && element instanceof ControlTuple) {
            final ControlTuple control = (ControlTuple) element;
            if (Type.EOS.equals(control.getType())) {
               eof = true;
            }
         }
      } while (!eof);
      this.running = false;
      // System.out.println(this.statistics);
      // sleep needed to be sure console output makes it in time
      try {
         Thread.sleep(100);
      } catch (final InterruptedException e) {
         throw new OperatorException(this, e);
      }
   }

   /**
    * Returns the next generated tuple.
    *
    * @return the next generated tuple
    */
   private DataTuple next() {
      if (this.group == 100) {
         this.group = 0;
         this.window++;
      }
      if (this.window > this.maxWindows) {
         return null;
      }
      return new DataTuple(this.schema, Arrays.asList(Integer.valueOf(this.window),
            Integer.valueOf(this.group++), Integer.valueOf((int) (Math.random() * 1000))));
   }

   @Override
   public void addOperatorEventListener(final OperatorEventListener listener) {
      this.listeners.addOperatorEventListener(listener);
   }

   @Override
   public boolean removeOperatorEventListener(final OperatorEventListener listener) {
      return this.listeners.removeOperatorEventListener(listener);
   }
}
