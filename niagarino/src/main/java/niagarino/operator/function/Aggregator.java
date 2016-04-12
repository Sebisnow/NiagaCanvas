/*
 * @(#)Aggregator.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Interface describing an aggregator. Aggregators are used by aggregation functions to compute the
 * aggregation result.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public interface Aggregator {

   /**
    * Returns the current aggregation value.
    *
    * @return current aggregation value
    */
   Object getValue();

   /**
    * Incrementally updates the aggregated value with the given value.
    *
    * @param value
    *           value object
    * @return {@code true} if the aggregation value has changed, {@code false} otherwise
    */
   boolean update(final Object value);
}
