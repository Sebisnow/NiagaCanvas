/*
 * @(#)MinMaxAggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Implementation of the {@code MIN} and {@code MAX} built-in aggregation function.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class MinMaxAggregationFunction extends AbstractAggregationFunction {

   /**
    * Constructs a new {@code MIN} or {@code MAX} built-in aggregation function.
    *
    * @param min
    *           {@code true} to find the minimum, {@code false} to find the maximum
    */
   MinMaxAggregationFunction(final boolean min) {
      super(min ? Type.MIN : Type.MAX, Double.class);
   }

   @Override
   public Aggregator get() {
      switch (this.getType()) {
         case MIN:
            return new MinAggregator();
         case MAX:
            return new MaxAggregator();
         default:
            throw new IllegalStateException("Aggregation function type " + this.getType()
                  + " is neither MIN nor MAX.");
      }
   }

   /**
    * Minimum aggregator.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class MinAggregator implements Aggregator {

      /** Minimum value. */
      private double minimum = Double.POSITIVE_INFINITY;
      /** Flag to check whether the minimum value is {@code null}. */
      private boolean isNull = true;

      @Override
      public boolean update(final Object value) {
         if (value != null) {
            final double old = this.minimum;
            final double v = ((Number) value).doubleValue();
            this.minimum = Math.min(this.minimum, v);
            this.isNull = false;
            return old != this.minimum;
         }
         return false;
      }

      @Override
      public Double getValue() {
         return this.isNull ? null : Double.valueOf(this.minimum);
      }
   }

   /**
    * Maximum aggregator.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class MaxAggregator implements Aggregator {

      /** Maximum value. */
      private double maximum = Double.NEGATIVE_INFINITY;
      /** Flag to check whether the maximum value is {@code null}. */
      private boolean isNull = true;

      @Override
      public boolean update(final Object value) {
         if (value != null) {
            final double old = this.maximum;
            final double v = ((Number) value).doubleValue();
            this.maximum = Math.max(this.maximum, v);
            this.isNull = false;
            return old != this.maximum;
         }
         return false;
      }

      @Override
      public Double getValue() {
         return this.isNull ? null : Double.valueOf(this.maximum);
      }
   }
}
