/*
 * @(#)Segment.java   1.0   Feb 16, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * Common abstract superclass of all operators that segment a stream.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public abstract class Segment extends AbstractOperator {

   /** Current segment number. */
   private long segmentId;

   /**
    * Constructs a new segmenting operator with the given schema.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           input schema
    */
   protected Segment(final String operatorId, final Schema inputSchema) {
      super(operatorId, Arrays.asList(inputSchema));
      this.segmentId = 0;
   }

   /**
    * Constructs a new segmenting operator with the given schema.
    *
    * @param inputSchema
    *           input schema
    */
   protected Segment(final Schema inputSchema) {
      this(Segment.class.getSimpleName(), inputSchema);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected final void processTuple(final int input, final DataTuple tuple) {
      tuple.getElementMetadata().clearSegmentIds();
      this.assignSegments(tuple);
      // check if tuple was assigned to any windows
      if (tuple.getElementMetadata().getSegmentIds().size() > 0) {
         this.pushTuple(tuple);
      }
   }

   /**
    * Assigns the processed tuple to segment ids.
    *
    * @param tuple
    *           the tuple which is to assign to segments
    */
   protected abstract void assignSegments(final DataTuple tuple);

   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   /**
    * Returns the number of the segment that is currently being processed.
    *
    * @return segment number
    */
   public long getSegmentId() {
      return this.segmentId;
   }

   /**
    * Increments the segment number by 1.
    */
   protected void incrementSegmentId() {
      this.incrementSegmentId(1);
   }

   /**
    * Increments the segment number by the given value.
    *
    * @param increment
    *           increment value
    */
   protected void incrementSegmentId(final long increment) {
      this.segmentId += increment;
   }

   @Override
   protected void processForwardControl(final int input, final ControlTuple message) {
      switch (message.getType()) {
         case ENDOFSEGMENT:
            // Drop end-of-segment controls because they get invalidated by this very segment.
            break;
         default:
            this.pushControl(Flow.FORWARD, message);
      }
   }
}
