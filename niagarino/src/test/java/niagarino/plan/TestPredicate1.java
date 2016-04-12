/*
 * @(#)TestPredicate1.java   1.0   Jan 13, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.plan;

import niagarino.operator.predicate.Predicate;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Predicate for testing an example query plan.
 * 
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TestPredicate1 implements Predicate {

   @Override
   public boolean isApplicable(final Schema schema) {
      return true;
   }

   @Override
   public boolean evaluate(final DataTuple tuple) {
      return ((Integer) tuple.getAttributeValue(0)).equals(0);
   }
}
