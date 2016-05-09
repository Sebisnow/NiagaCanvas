/*
 * @(#)ReversalRateAggregationFunction.java   1.0   Jul 25, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Implementation of the reversal-rate user-defined aggregation function. The reversal-rate calculates the
 * number of trend changes per aggregated value. A trend change is detected if a series of increasing values
 * begins to decrease or vice-versa. Therefore, the reversal-rate is a measure for the degree of oscillation
 * within a time-series.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class ReversalRateAggregationFunction extends AbstractAggregationFunction {

   /**
    * Constructs a new reversal-rate aggregation function.
    */
   public ReversalRateAggregationFunction() {
      super("REVERSALRATE", Double.class);
   }

   @Override
   public ReversalRateAggregator get() {
      return new ReversalRateAggregator();
   }

   /**
    * Reversal-rate aggregator.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class ReversalRateAggregator implements Aggregator {

      /** Sum value. */
      private double rate = 0.0;
      /** Count value. */
      private long count = 0;
      /** Previous value. */
      private double previousValue = 0.0;
      /** Previous trend. */
      private double previousTrend = 0.0;
      /** Flag to check whether the reversal-rate value is {@code null}. */
      private boolean isNull = true;

      @Override
      public boolean update(final Object value) {
         if (value != null) {
            final double old = this.rate;
            final double v = ((Number) value).doubleValue();
            final double trend = v - this.previousValue;
            int increment = 0;
            if (trend > 0 && this.previousTrend < 0 || trend < 0 && this.previousTrend > 0) {
               // trend has changed
               increment = 1;
            }
            this.rate = this.rate + (increment - this.rate) / (this.count + 1);
            this.previousTrend = trend;
            this.previousValue = v;
            this.count++;
            this.isNull = false;
            return old != this.rate;
         }
         return false;
      }

      @Override
      public Double getValue() {
         return this.isNull ? null : Double.valueOf(this.rate);
      }
   }
}
