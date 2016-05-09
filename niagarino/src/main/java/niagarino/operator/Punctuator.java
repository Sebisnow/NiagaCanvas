/*
 * @(#)Punctuator.java   1.0   Apr 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;
import java.util.Date;

import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.PunctuationControl.Type;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * A {@link Punctuator} emits punctuations in intervals after seeing all tuples belonging to the interval.<br />
 * <br />
 * Other operators may profit from punctuations by knowing that no value lower than that of the punctuation
 * can occur anymore.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class Punctuator extends AbstractOperator {

   /** Interval size. */
   private final long interval;

   /** First punctuation mark of this operator. */
   private long startValue;

   /** Mark for the latest punctuation. */
   private double currentOffset;

   /** Whether the initial offset is set. */
   private boolean offsetSet;

   /** The current segment's ID. */
   private long segmentId;

   /**
    * Constructs a new Punctuator.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    * @param interval
    *           size of the intervals
    * @param start
    *           initial offset for the punctuator
    */
   public Punctuator(final String operatorId, final Schema inputSchema, final long interval, final long start) {
      super(operatorId, Arrays.asList(inputSchema));
      this.interval = interval;
      this.startValue = start;
      this.currentOffset = start;
      this.offsetSet = true;
      this.segmentId = 0;
   }

   /**
    * Constructs a new Punctuator with initial offset of 0.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    * @param interval
    *           size of the intervals
    */
   public Punctuator(final String operatorId, final Schema inputSchema, final long interval) {
      super(operatorId, Arrays.asList(inputSchema));
      this.interval = interval;
      this.currentOffset = -1;
      this.offsetSet = false;
      this.segmentId = -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      // extract progressing value
      final Object obj = tuple.getProgressingValue();

      double value = Double.NaN;
      if (obj instanceof Number) {
         value = ((Number) obj).doubleValue();
      } else if (obj instanceof Date) {
         value = ((Date) obj).getTime();
      }
      // adjust offset (this relies on the stream being ordered)
      if (!this.offsetSet) {
         this.currentOffset = value;
         this.startValue = (long) value;
         this.offsetSet = true;
      }
      // check whether next interval has arrived
      while (value - this.currentOffset + 1 > this.interval) {
         final PunctuationControl ctrl = new PunctuationControl(Type.INTERVAL, this.startValue,
               this.interval, this.segmentId, (long) this.currentOffset);
         this.currentOffset += this.interval;
         this.segmentId++;
         this.pushControl(Flow.FORWARD, ctrl);
      }
      this.pushTuple(tuple);
   }
}
