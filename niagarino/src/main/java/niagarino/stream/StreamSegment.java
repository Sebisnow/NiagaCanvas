/*
 * @(#)StreamSegment.java   1.0   Feb 15, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.List;

/**
 * A stream segment represents a consecutive region over a stream, identified by the minimal and maximal value
 * of the progressing attribute. Both windows and frames are stream segments.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public interface StreamSegment {

   /**
    * Returns the minimum value, i.e., the lower boundary, of this stream segment.
    *
    * @return minimum value
    */
   Object getMinimalValue();

   /**
    * Returns the maximum value, i.e., the upper boundary, of this stream segment.
    *
    * @return maximum value
    */
   Object getMaximalValue();

   /**
    * Inserts the given tuple into this stream segment and update the lower or upper boundary, if necessary.
    *
    * @param tuple
    *           data tuple
    */
   void insertTuple(DataTuple tuple);

   /**
    * Retrieves all the tuples that are currently in this stream segment.
    *
    * @return data tuples
    */
   List<DataTuple> reportTuples();

   /**
    * Returns the schema of the tuples in this stream segment.
    * 
    * @return schema definition
    */
   Schema getSchema();
}
