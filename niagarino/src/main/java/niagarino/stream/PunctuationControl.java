/*
 * @(#)PunctuationControl.java   1.0   May 13, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;


/**
 * A PunctuationControl is sent as a notification that all tuples with a certain criterion have been seen.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class PunctuationControl extends ControlTuple {

   /**
    * The type of a PunctuationControl object.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public enum Type {
      /** End of an interval. */
      INTERVAL,
      /** End of a window. */
      WINDOW,
      /** End of an entity. */
      END_OF_ENTITY;
   }

   /** The type of this PunctuationControl. */
   private final Type type;

   /** Value at which the punctuations started. */
   private final long startValue;

   /** Step size of the punctuations. */
   private final long stepSize;

   /** The segment id that is closed at this point. */
   private final long segmentId;

   /** The start of the segment that is closed at this point. */
   private final long segmentStart;

   /** The size of the segment that is closed at this point. */
   private final long segmentSize;

   /**
    * Constructs a new PunctuationControl with the specified type.
    *
    * @param type
    *           the type of the new PunctuationControl
    * @param startValue
    *           start of punctuations of the emitting operator
    * @param stepSize
    *           interval size between punctuations
    * @param segmentId
    *           the segment id of the latest interval
    * @param segmentStart
    *           the start of the latest segment/interval
    * @param segmentSize
    *           the size of the latest segment
    */
   public PunctuationControl(final Type type, final long startValue, final long stepSize,
         final long segmentId, final long segmentStart, final long segmentSize) {
      super(ControlTuple.Type.PUNCTUATION);
      this.type = type;
      this.startValue = startValue;
      this.stepSize = stepSize;
      this.segmentId = segmentId;
      this.segmentStart = segmentStart;
      this.segmentSize = segmentSize;
   }

   /**
    * Constructs a new PunctuationControl with the specified type.
    *
    * @param type
    *           the type of the new PunctuationControl
    * @param startValue
    *           start of punctuations of the emitting operator
    * @param stepSize
    *           interval size between punctuations
    * @param segmentId
    *           the segment id of the last interval
    * @param segmentStart
    *           the start of the last segment/interval
    */
   public PunctuationControl(final Type type, final long startValue, final long stepSize,
         final long segmentId, final long segmentStart) {
      this(type, startValue, stepSize, segmentId, segmentStart, stepSize);
   }

   /**
    * Returns the type of this PunctuationControl.
    *
    * @return the type of this PunctuationControl
    */
   public Type getPunctuationType() {
      return this.type;
   }

   /**
    * Returns the initial offset of the emitting operator.
    *
    * @return the start value
    */
   public long getStartValue() {
      return this.startValue;
   }

   /**
    * Returns the interval size for intervals between punctuations of the emitting operator.
    *
    * @return the interval size
    */
   public long getStepSize() {
      return this.stepSize;
   }

   /**
    * Returns the segment id of the closed interval.
    *
    * @return segment id
    */
   public long getSegmentId() {
      return this.segmentId;
   }

   /**
    * Returns the start value of the closed interval/segment.
    *
    * @return segment start
    */
   public long getSegmentStart() {
      return this.segmentStart;
   }

   /**
    * Returns the end value of the closed segment.
    *
    * @return segment end
    */
   public long getSegmentEnd() {
      return this.segmentStart + this.segmentSize - 1;
   }
}
