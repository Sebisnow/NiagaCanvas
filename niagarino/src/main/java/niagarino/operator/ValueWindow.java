/*
 * @(#)ValueWindow.java   1.0   Feb 16, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.PunctuationControl.Type;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * Slide-by-value window.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public class ValueWindow extends Window {

   /** Logger for this class. */
   private static final Logger LOG = LogManager.getLogger(ValueWindow.class);

   /** Start of the first window. */
   private long start;

   /**
    * Constructs a new value-based window operator with the given input schema, which generates windows that
    * contain the given number of tuples and slide by the given value.
    *
    * @param inputSchema
    *           input schema
    * @param size
    *           size of the window
    * @param slide
    *           slide of the window
    */
   public ValueWindow(final Schema inputSchema, final long size, final long slide) {
      this(ValueWindow.class.getSimpleName(), inputSchema, size, slide);
   }

   /**
    * Constructs a new value-based window operator with the given input schema, which generates windows that
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
   public ValueWindow(final String operatorId, final Schema inputSchema, final long size, final long slide) {
      this(operatorId, inputSchema, size, slide, Long.MIN_VALUE, false);
   }

   /**
    * Constructs a new value-based window operator with the given input schema, which generates windows that
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
    * @param start
    *           starting offset for this window operator
    */
   public ValueWindow(final String operatorId, final Schema inputSchema, final long size, final long slide,
         final long start) {
      this(operatorId, inputSchema, size, slide, start, true);
   }

   /**
    * Constructs a new value-based window operator with the given input schema, which generates windows that
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
    * @param start
    *           starting offset for this window operator
    * @param startSet
    *           whether start value is set already
    */
   private ValueWindow(final String operatorId, final Schema inputSchema, final long size, final long slide,
         final long start, final boolean startSet) {
      super(operatorId, inputSchema, size, slide);
      if (startSet) {
         if (start < 0) {
            throw new IllegalArgumentException("Start must be non-negative, was: " + start);
         } else {
            this.start = start;
         }
      } else {
         this.start = Long.MIN_VALUE;
      }
   }

   @Override
   protected void assignSegments(final DataTuple tuple) {
      // extract progressing value
      final Object obj = tuple.getProgressingValue();
      long value;
      if (obj instanceof Number) {
         value = ((Number) obj).longValue();
      } else if (obj instanceof Date) {
         value = ((Date) obj).getTime();
      } else {
         throw new OperatorException(this, "Unsupported progressing attribute type: "
               + obj.getClass().getSimpleName() + ".");
      }

      // adjust offset (this relies on the stream being ordered)
      if (this.start == Long.MIN_VALUE) {
         this.start = value;
      }

      // compute segment ids
      final long loID = (value - this.start) / this.getSlide();
      final long hiID = (value - this.start + this.getSize()) / this.getSlide() - 1;

      // assign segment ids to tuple
      for (long id = loID; id <= hiID; id++) {
         tuple.getElementMetadata().addSegmentId(id);
      }

      // check if base segment id needs to be updated
      final long baseID = this.getSegmentId();
      if (loID > baseID) {
         this.incrementSegmentId(loID - baseID);
      }
   }

   @Override
   protected void incrementSegmentId() {
      LOG.debug("Increasing segment ID from {}.", this.getSegmentId());
      final ControlTuple msg = new PunctuationControl(Type.WINDOW, this.start, this.getSlide(),
            this.getSegmentId(), this.getCurrentStart(), this.getSize());
      this.pushControl(Flow.FORWARD, msg);
      super.incrementSegmentId(1);
   }

   /**
    * Returns the start of the current segment.
    *
    * @return the start of the current segment
    */
   private long getCurrentStart() {
      return this.start - this.getSize() + this.getSlide() + this.getSegmentId() * this.getSlide();
   }

   @Override
   protected void incrementSegmentId(final long increment) {
      for (long i = 0; i < increment; i++) {
         this.incrementSegmentId();
      }
   }

   @Override
   protected void processForwardControl(final int input, final ControlTuple message) {
      switch (message.getType()) {
         case ENDOFSEGMENT:
            // Drop end-of-segment controls because they get invalidated by this very segment.
            break;
         case PUNCTUATION:
            final PunctuationControl pc = (PunctuationControl) message;
            // The punctuation can be used if the current segment ends at the same point as the
            // punctuation.
            long currentSegmentEnd = this.getCurrentStart() + this.getSize() - 1;
            final long punctuationValue = pc.getSegmentEnd();
            while (currentSegmentEnd <= punctuationValue) {
               // End the current segment
               this.incrementSegmentId();
               currentSegmentEnd += this.getSlide();
            }
            // Only forward INTERVAL punctuations
            // If WINDOW punctuations are forwarded, in certain scenarios they might pile up and
            // unnecessarily use up resources
            if (pc.getPunctuationType() == PunctuationControl.Type.INTERVAL) {
               this.pushControl(Flow.FORWARD, message);
            }
            break;
         default:
            this.pushControl(Flow.FORWARD, message);
      }
   }
}
