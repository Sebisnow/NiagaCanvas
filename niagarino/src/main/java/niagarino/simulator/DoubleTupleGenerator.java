/*
 * @(#)DoubleTupleGenerator.java   1.0   Feb 7, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.simulator;

import java.util.Arrays;
import java.util.Random;

import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * A tuple generator that generates tuples with a double value.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class DoubleTupleGenerator extends TupleGenerator {

   /** Schema of this tuple generator. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("Timestamp", Long.class), new Attribute(
         "Valeue", Double.class));
   /** Trend value used by this tuple generator. */
   private static final double P_TREND = 0.75;
   /** Maximum fluctuation of the generated values. */
   private static final double FLUCTUATION = 10.0;

   /** Current trend (increasing or decreasing). */
   private int trend;
   /** Previous tuple. */
   private double previous;
   /** Random number generator. */
   private final Random random;

   /**
    * Creates a new tuple generator that generates tuple consisting of a time stamp and a double value.
    *
    * @param seed
    *           seed for random number generator
    * @param maxTs
    *           maximum generated timestamp
    */
   public DoubleTupleGenerator(final long seed, final long maxTs) {
      super(DoubleTupleGenerator.SCHEMA, maxTs);
      this.trend = 1;
      this.random = new Random(seed);
      this.previous = this.random.nextDouble() * 100;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DataTuple next() {
      final double next = this.previous + this.getTrend() * this.random.nextDouble()
            * DoubleTupleGenerator.FLUCTUATION;
      this.previous = next;
      return new DataTuple(DoubleTupleGenerator.SCHEMA, Arrays.asList(new Object[] {
            Long.valueOf(this.nextTs()), Double.valueOf(next) }));
   }

   /**
    * Probabilistically decides whether the current trend (increasing or decreasing) will change.
    *
    * @return new trend
    */
   private int getTrend() {
      if (this.random.nextDouble() < DoubleTupleGenerator.P_TREND) {
         this.trend = (int) (Math.round(this.random.nextDouble() * 2) + this.trend) % 3;
      }
      return this.trend - 1;
   }
}
