/*
 * @(#)AggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

import java.util.function.Supplier;

import niagarino.stream.Attribute;

/**
 * Interface describing an aggregation function. Aggregation function are used by the aggregate operator.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public interface AggregationFunction extends Supplier<Aggregator> {

   /**
    * Returns the name of this aggregation function.
    *
    * @return aggregation function name
    */
   String getName();

   /**
    * Returns the type of this aggregation function.
    *
    * @return aggregation function type
    */
   Type getType();

   /**
    * Returns the attribute that is added to the schema by this aggregation function.
    *
    * @param inputAttribute
    *           input attribute of the aggregation function
    * @return aggregated output attribute
    */
   Attribute getAggregatedAttribute(Attribute inputAttribute);

   /**
    * Aggregation types that are supported by Niagarino.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   public enum Type {

      /** Sum aggregation function type. */
      SUM,
      /** Average aggregation function type. */
      AVG,
      /** Count aggregation function type. */
      COUNT,
      /** Minimum aggregation function type. */
      MIN,
      /** Maximum aggregation function type. */
      MAX,
      /** User-defined aggregation function type. */
      UDF;
   }
}
