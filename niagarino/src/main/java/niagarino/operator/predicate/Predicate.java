/*
 * @(#)Predicate.java   1.0   Mar 1, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.predicate;

import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Selection predicate for the selection operator.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public interface Predicate {

   /**
    * Tests whether this predicate is applicable to the given schema.
    *
    * @param schema
    *           schema definition
    * @return <code>true</code> if this predicate is applicable to the given schema, <code>false</code>
    *         otherwise
    */
   boolean isApplicable(Schema schema);

   /**
    * Evaluates this predicate on the given tuple and returns the result.
    * 
    * @param tuple
    *           data tuple
    * @return <code>true</code> if this predicate holds for the given tuple, <code>false</code> otherwise
    */
   boolean evaluate(DataTuple tuple);
}
