/*
 * @(#)SegmentChangePredicate.java   1.0   Jul 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import niagarino.operator.predicate.Predicate;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Determines whether a position report indicates a segment change, using previously calculated values.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SegmentChangePredicate implements Predicate {

   @Override
   public boolean isApplicable(final Schema schema) {
      return schema.getAttributeIndex("old_segid") > -1 && schema.getAttributeIndex("segid") > -1;
   }

   @Override
   public boolean evaluate(final DataTuple tuple) {
      final int segId = (Integer) tuple.getAttributeValue("segid");
      final int oldSegId = (Integer) tuple.getAttributeValue("old_segid");
      return segId != oldSegId;
   }
}
