/*
 * @(#)SumAggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Implementation of the {@code SUM} built-in aggregation function.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SumAggregationFunction extends AbstractAggregationFunction {

   /**
    * Constructs a new {@code SUM} built-in aggregation function.
    */
   SumAggregationFunction() {
      super(Type.SUM, Double.class);
   }

   @Override
   public SumAggregator get() {
      return new SumAggregator();
   }

   /**
    * Sum aggregator that uses Kahan's method to incrementally compute a numerically stable sum.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class SumAggregator implements Aggregator {

      /** Sum value. */
      private double sum = 0.0;
      /** Kahan's 'c' value. */
      private double c = 0.0;
      /** Flag to check whether the sum value is {@code null}. */
      private boolean isNull = true;

      @Override
      public boolean update(final Object value) {
         if (value != null) {
            final double old = this.sum;
            final double v = ((Number) value).doubleValue();
            final double y = v - this.c;
            final double t = this.sum + y;
            this.c = t - this.sum - y;
            this.sum = t;
            this.isNull = false;
            return old != this.sum;
         }
         return false;
      }

      @Override
      public Double getValue() {
         return this.isNull ? null : Double.valueOf(this.sum);
      }
   }
}
