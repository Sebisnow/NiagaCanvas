/*
 * @(#)SamplingOperator.java   1.0   May 22, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.SampleStreamSegment;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;
import niagarino.stream.StreamSegment;
import niagarino.util.TypeSystem;

/**
 * SamplingOperator operator which samples the input tuples based on the Sampling Strategy.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */

public class SamplingOperator extends AbstractOperator {

   /** SamplingOperator Type indicating the SamplingOperator Algorithm. */
   private final SamplingStrategy samplingStrategy;

   /** Insert Position in the sampleList. */
   private int insertionIndex;

   /** Segments that are currently open. */
   private final Set<Long> openSegments;

   /** Segments that are currently being aggregated. */
   private final SortedMap<Long, SampleStreamSegment> segments;

   /** Last seen punctuation. */
   private PunctuationControl lastPunctuation;

   /**
    * Constructs a new SamplingOperator operator with the given input schema.
    *
    * @param operatorId
    *           name of operator
    * @param inputSchema
    *           input schema
    * @param samplingStrategy
    *           sampling Strategy
    */
   public SamplingOperator(final String operatorId, final Schema inputSchema,
         final SamplingStrategy samplingStrategy) {
      super(operatorId, Arrays.asList(inputSchema));
      this.samplingStrategy = samplingStrategy;
      this.insertionIndex = -1;
      this.openSegments = new HashSet<Long>();
      this.segments = new TreeMap<Long, SampleStreamSegment>();
      this.lastPunctuation = null;
   }

   /**
    * Constructs a new SamplingOperator operator with the given input schema.
    *
    * @param inputSchema
    *           input Schema
    * @param samplingStrategy
    *           type of Sampling to be applied to the input Schema
    */
   public SamplingOperator(final Schema inputSchema, final SamplingStrategy samplingStrategy) {
      this(SamplingOperator.class.getSimpleName(), inputSchema, samplingStrategy);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processTuple(final int input, final DataTuple tuple) {

      if (this.samplingStrategy.sample(tuple)) {

         final List<Long> currentSegments = tuple.getElementMetadata().getSegmentIds();
         this.insertionIndex = this.samplingStrategy.getInsertPosition();
         // compute open segments
         this.openSegments.addAll(currentSegments);
         // update current segments
         for (final Long l : currentSegments) {
            SampleStreamSegment segment = this.segments.get(l);
            if (segment == null) {
               segment = new SampleStreamSegment(this.getInputSchemas().get(0));
               this.segments.put(l, segment);
            }
            segment.insertTuple(tuple, this.insertionIndex);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processForwardControl(final int input, final ControlTuple message) {
      switch (message.getType()) {
         case PUNCTUATION:
            final PunctuationControl pctrl = (PunctuationControl) message;
            if (PunctuationControl.Type.WINDOW.equals(pctrl.getPunctuationType())) {
               this.lastPunctuation = pctrl;
               this.pushSegment(pctrl.getSegmentId());
               this.openSegments.remove(pctrl.getSegmentId());
            }
            break;
         default:
      }
      this.pushControl(Flow.FORWARD, message);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Socket.INPUT.equals(socket)) {
         while (!this.segments.isEmpty()) {
            final Long l = this.segments.firstKey();
            this.pushSegment(l);
         }
      }
      super.handleEoS(socket, input, message);
   }

   /**
    * Push the sampled segment to the stream and removing the segments from the list of currently processed
    * segments.
    *
    * @param l
    *           segment number
    */
   private void pushSegment(final Long l) {
      final StreamSegment segment = this.segments.get(l);
      if (segment != null) {
         final SampleStreamSegment sampledStreamSegment = (SampleStreamSegment) segment;
         if (this.lastPunctuation != null) {
            final long difference = (l - this.lastPunctuation.getSegmentId())
                  * this.lastPunctuation.getStepSize();
            final Object min;
            final Object max;
            final Schema s = this.getOutputSchema();
            final Class< ? > progAttrType = s.getAttribute(s.getProgressingAttribute()).getType();
            if (Date.class.equals(progAttrType)) {
               min = new Date(this.lastPunctuation.getSegmentStart() + difference);
               max = new Date(this.lastPunctuation.getSegmentEnd() + difference);
            } else {
               min = TypeSystem.convertNumber(progAttrType, this.lastPunctuation.getSegmentStart()
                     + difference);
               max = TypeSystem
                     .convertNumber(progAttrType, this.lastPunctuation.getSegmentEnd() + difference);
            }
            sampledStreamSegment.setMinMax(min, max);
         }
         final List<DataTuple> tuples = segment.reportTuples();
         for (final DataTuple tuple : tuples) {
            tuple.getElementMetadata().clearSegmentIds();
            tuple.getElementMetadata().addSegmentId(l.longValue());
            this.pushTuple(tuple);
         }
         this.segments.remove(l);
         // Reset the current counter(current tuple count in the window)
         this.samplingStrategy.resetCurrCounter();
      }
   }

   /**
    * @return the samplingStrategy
    */
   public SamplingStrategy getSamplingStrategy() {
      return this.samplingStrategy;
   }

   /**
    * @return the insertionIndex
    */
   public int getInsertionIndex() {
      return this.insertionIndex;
   }

   /**
    * @param insertionIndex
    *           Insertion Index in the sample list.
    */
   public void setInsertionIndex(final int insertionIndex) {
      this.insertionIndex = insertionIndex;
   }

   /**
    * @return the openSegments
    */
   public Set<Long> getOpenSegments() {
      return this.openSegments;
   }

   /**
    * @return the segments
    */
   public SortedMap<Long, SampleStreamSegment> getSegments() {
      return this.segments;
   }
}
