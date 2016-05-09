/*
 * @(#)BuiltInAggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

import niagarino.operator.function.AggregationFunction.Type;

/**
 * Utility class providing constants for the built-in aggregation functions of Niagarino.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public final class BuiltInAggregationFunction {

   /** Sum aggregation function type. */
   public static final AggregationFunction SUM = new SumAggregationFunction();
   /** Average aggregation function type. */
   public static final AggregationFunction AVG = new AvgAggregationFunction();
   /** Count aggregation function type. */
   public static final AggregationFunction COUNT = new CountAggregationFunction();
   /** Minimum aggregation function type. */
   public static final AggregationFunction MIN = new MinMaxAggregationFunction(true);
   /** Maximum aggregation function type. */
   public static final AggregationFunction MAX = new MinMaxAggregationFunction(false);

   /**
    * Returns the built-in aggregation function corresponding to the given aggregation function type.
    * 
    * @param type
    *           aggregation function type
    * @return aggregation function
    */
   public static AggregationFunction forType(final Type type) {
      switch (type) {
         case SUM:
            return SUM;
         case AVG:
            return AVG;
         case COUNT:
            return COUNT;
         case MIN:
            return MIN;
         case MAX:
            return MAX;
         default:
            throw new IllegalArgumentException("Type " + type.name()
                  + " is not a built-in aggregation function.");
      }
   }

   /**
    * Hidden constructor.
    */
   private BuiltInAggregationFunction() {
      // hidden constructor
   }
}
