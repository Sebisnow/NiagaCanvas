/*
 * @(#)TupleWindow.java   1.0   Feb 16, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Slide-by-tuple window.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class TupleWindow extends Window {

   /** Count of seen tuples. */
   private long tupleCount = 0;

   /**
    * Constructs a new tuple-based window operator with the given input schema, which generates windows that
    * contain the given number of tuples and slide by the given value.
    *
    * @param inputSchema
    *           input schema
    * @param size
    *           size of the window
    * @param slide
    *           slide of the window
    */
   public TupleWindow(final Schema inputSchema, final long size, final long slide) {
      this(TupleWindow.class.getSimpleName(), inputSchema, size, slide);
   }

   /**
    * Constructs a new tuple-based window operator with the given input schema, which generates windows that
    * contain the given number of tuples and slide by the given value.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           input schema
    * @param size
    *           size of the window
    * @param slide
    *           slide of the window
    */
   public TupleWindow(final String operatorId, final Schema inputSchema, final long size, final long slide) {
      super(operatorId, inputSchema, size, slide);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void assignSegments(final DataTuple tuple) {
      final long start;
      if (this.tupleCount < this.getSize()) {
         start = 0;
      } else {
         start = (long) Math.floor((this.tupleCount - this.getSize()) / this.getSlide()) + 1;
      }
      if (this.getSegmentId() < start) {
         this.incrementSegmentId();
      }
      final long end = (int) Math.floor(this.tupleCount / this.getSlide());
      for (long i = start; i <= end; i++) {
         tuple.getElementMetadata().addSegmentId(i);
      }
      this.tupleCount++;
   }
}
