/*
 * @(#)AvgAggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Implementation of the {@code AVG} built-in aggregation function.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class AvgAggregationFunction extends AbstractAggregationFunction {

   /**
    * Constructs a new {@code AVG} built-in aggregation function.
    */
   AvgAggregationFunction() {
      super(Type.AVG, Double.class);
   }

   @Override
   public AvgAggregator get() {
      return new AvgAggregator();
   }

   /**
    * Average aggregator that uses Kahan's method to incrementally compute a numerically stable average.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class AvgAggregator implements Aggregator {

      /** Sum value. */
      private double avg = 0.0;
      /** Kahan's 'c' value. */
      private double c = 0.0;
      /** Count value. */
      private long count = 0;
      /** Flag to check whether the sum value is {@code null}. */
      private boolean isNull = true;

      @Override
      public boolean update(final Object value) {
         if (value != null) {
            final double old = this.avg;
            final double v = ((Number) value).doubleValue();
            final double delta = (v - this.avg) / (this.count + 1);
            final double y = delta - this.c;
            final double t = this.avg + y;
            this.c = t - this.avg - y;
            this.avg = t;
            this.isNull = false;
            this.count++;
            return old != this.avg;
         }
         return false;
      }

      @Override
      public Double getValue() {
         return this.isNull ? null : Double.valueOf(this.avg);
      }
   }
}
