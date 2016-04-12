/*
 * @(#)Frame.java   1.0   Mar 9, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import niagarino.operator.predicate.Predicate;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Generic and thus inefficient implementation of a predicate-based frame.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Frame extends Segment {

   /** Predicate used by this frame operator. */
   private final Predicate predicate;
   /** Indicates whether this frame operator produces adjacent stream segments. */
   private final boolean adjacent;
   /** Indicates whether the satisfying tuple will be included in the frame. */
   private final boolean includeSatisfyingTuple;

   /**
    * Constructs a new frame operator with the given input schema and predicate.
    *
    * @param inputSchema
    *           input schema
    * @param predicate
    *           frame predicate
    * @param adjacent
    *           indicates whether adjacent stream segments will be produced or not
    */
   public Frame(final Schema inputSchema, final Predicate predicate, final boolean adjacent) {
      super(inputSchema);
      this.includeSatisfyingTuple = false;
      if (predicate.isApplicable(inputSchema)) {
         this.predicate = predicate;
         this.adjacent = adjacent;
      } else {
         throw new IllegalArgumentException(
               "The predicate is not applicable to the input schema.");
      }
   }

   /**
    * Constructs a new frame operator with the given input schema and predicate.
    *
    * @param inputSchema
    *           input schema
    * @param predicate
    *           frame predicate
    * @param adjacent
    *           indicates whether adjacent stream segments will be produced or not
    * @param includeSatisfyingTuple
    *           indicates whether the satisfying tuple will be included in the frame or not
    */
   public Frame(final Schema inputSchema, final Predicate predicate, final boolean adjacent,
         final boolean includeSatisfyingTuple) {
      super(inputSchema);
      this.includeSatisfyingTuple = includeSatisfyingTuple;
      if (predicate.isApplicable(inputSchema)) {
         this.predicate = predicate;
         this.adjacent = adjacent;
      } else {
         throw new IllegalArgumentException(
               "The predicate is not applicable to the input schema.");
      }
   }

   /**
    * Returns the predicate used by this frame operator.
    *
    * @return frame predicate
    */
   public Predicate getPredicate() {
      return this.predicate;
   }

   /**
    * Returns whether this frame operator produces adjacent stream segments.
    *
    * @return <code>true</code> if the produced stream segments are adjacent, <code>false</code> otherwise
    */
   public boolean isAdjacent() {
      return this.adjacent;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void assignSegments(final DataTuple tuple) {
      // tag it with the current segment id
      if (!this.includeSatisfyingTuple) {
         tuple.getElementMetadata().addSegmentId(this.getSegmentId());
      }
      // check condition
      this.checkCondition(tuple);
      if (this.includeSatisfyingTuple) {
         tuple.getElementMetadata().addSegmentId(this.getSegmentId());
      }
   }

   /**
    * Checks if the given tuple satisfies the frame predicate and moves to the next frame if it does.
    *
    * @param tuple
    *           data tuple
    */
   private void checkCondition(final DataTuple tuple) {
      if (this.predicate.evaluate(tuple)) {
         this.incrementSegmentId();
         if (this.adjacent) {
            // also tag current tuple with new segment id
            tuple.getElementMetadata().addSegmentId(this.getSegmentId());
         }
      }
   }
}
