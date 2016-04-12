/*
 * @(#)CountAggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Implementation of the {@code COUNT} built-in aggregation function.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class CountAggregationFunction extends AbstractAggregationFunction {

   /**
    * Constructs a new {@code COUNT} built-in aggregation function.
    */
   CountAggregationFunction() {
      super(Type.COUNT, Long.class);
   }

   @Override
   public CountAggregator get() {
      return new CountAggregator();
   }

   /**
    * Count aggregator that counts all non-{@code null} values.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class CountAggregator implements Aggregator {

      /** Count value. */
      private long count = 0;

      @Override
      public boolean update(final Object value) {
         if (value != null) {
            this.count++;
            return true;
         }
         return false;
      }

      @Override
      public Long getValue() {
         return Long.valueOf(this.count);
      }
   }
}
