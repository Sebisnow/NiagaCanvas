/*
 * @(#)TupleGenerator.java   1.0   Feb 7, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.simulator;

import java.util.ArrayList;
import java.util.List;

import niagarino.operator.Operator;
import niagarino.operator.OperatorEventListener;
import niagarino.operator.OperatorEventListenerList;
import niagarino.stream.ControlTuple;
import niagarino.stream.ControlTuple.Type;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;
import niagarino.stream.Stream;
import niagarino.stream.Stream.Flow;

/**
 * Abstract super class for tuple generators.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public abstract class TupleGenerator implements Operator, Runnable {

   /** Output streams of this operator. */
   private final List<Stream> outputStreams;
   /** Schema of the generated tuples. */
   private final Schema schema;
   /** Maximum timestamp. */
   private final long maxTs;
   /** Current timestamp. */
   private long ts;
   /** Indicates whether this generator is running. */
   private boolean running;
   /** Indicates whether this generator is a sink. */
   private boolean sink;
   /** Statistics collected during execution of this operator. */
   private final OperatorEventListenerList listeners;

   /**
    * Creates a new tuple generator that generates tuples for the given schema and up to the given maximum
    * timestamp.
    *
    * @param schema
    *           output schema
    * @param maxTs
    *           maximum timestamp
    */
   public TupleGenerator(final Schema schema, final long maxTs) {
      this.outputStreams = new ArrayList<Stream>();
      this.schema = schema;
      this.maxTs = maxTs;
      this.ts = 0;
      this.running = false;
      this.sink = false;
      this.listeners = new OperatorEventListenerList();
   }

   @Override
   public String getName() {
      return this.getClass().getSimpleName();
   }

   /**
    * Returns the next timestamp.
    *
    * @return next timestamp
    */
   protected long nextTs() {
      return this.ts++;
   }

   @Override
   public Schema getOutputSchema() {
      return this.schema;
   }

   @Override
   public void addOutputStream(final Stream out) {
      this.outputStreams.add(out);
   }

   @Override
   public void addInputStream(final Stream in) {
      throw new UnsupportedOperationException("A tuple generator cannot have an input stream.");
   }

   @Override
   public int getOutputArity() {
      return this.outputStreams.size();
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
      while (this.ts < this.maxTs && this.running) {
         final DataTuple tuple = this.next();
         this.listeners.fireOnOutputTuple(this, tuple);
         for (final Stream out : this.outputStreams) {
            out.pushElement(Flow.FORWARD, tuple);
         }
      }
      final ControlTuple eof = new ControlTuple(Type.EOS);
      for (final Stream out : this.outputStreams) {
         out.pushElement(Flow.FORWARD, eof);
      }
      this.running = false;
      this.listeners.fireOnShutdown(this);
   }

   @Override
   public void addOperatorEventListener(final OperatorEventListener listener) {
      this.listeners.addOperatorEventListener(listener);
   }

   @Override
   public boolean removeOperatorEventListener(final OperatorEventListener listener) {
      return this.listeners.removeOperatorEventListener(listener);
   }

   /**
    * Generates and returns the next tuple.
    *
    * @return next tuple
    */
   public abstract DataTuple next();
}
