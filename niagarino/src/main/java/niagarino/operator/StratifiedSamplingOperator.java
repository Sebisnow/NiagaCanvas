/*
 * @(#)StratifiedSamplingOperator.java   1.0   Jul 08, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.List;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.SampleStreamSegment;
import niagarino.stream.Schema;


/**
 * Implementation of Stratified Sampling.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public class StratifiedSamplingOperator extends SamplingOperator {

   /** Insert at any position in SampleList(append preferred). */
   private static final int INSERT_INDEX_LAST = -1;
   
   /** Insert at any position in SampleList(append preferred). */
   private static final int START_INDEX = 1;
   
   /** Number of Strata. */
   private final long strataCount;

   /** Stratum Length. */
   private final long stratumLength;
   
   /** Stratum Length. */
   private final long stratumSampleSize;

   /** Current Stratum. */
   private long currStratumNo;
   
   /** Insert index in the current Strata Sample. */
   private long strataSampleInsertIndex;
   
   /** Current number of seen tuples in the strata. */
   private long strataCurr;

   /** Current number of seen tuples in the window. */
   private long windowCurr;
   
   /** Size of the window. */
   private long windowSize;
   
   /**
    * @param inputSchema
    *          Input Schema for the Operator.
    * @param numberOfStrata
    *          Number of Strata.
    * @param samplingStrategy
    *          Sampling Strategy.
    *          
    */
   public StratifiedSamplingOperator(final Schema inputSchema, final long numberOfStrata, 
         final SamplingStrategy samplingStrategy) {
      this(StratifiedSamplingOperator.class.getName(), inputSchema, numberOfStrata, samplingStrategy);
   }

   /**
    * @param inputSchema
    *          Input Schema for the Operator.
    * @param numberOfStrata
    *          Number of Strata.
    * @param samplingStrategy
    *          Sampling Strategy.
    * @param operatorId
    *          Id of the Operator       
    */
   public StratifiedSamplingOperator(final String operatorId, final Schema inputSchema, final long numberOfStrata, 
         final SamplingStrategy samplingStrategy) {
      super(operatorId, inputSchema, samplingStrategy);
      this.strataCount = numberOfStrata;
      this.currStratumNo = 1;
      this.stratumLength = samplingStrategy.getWindowSize();
      this.stratumSampleSize = samplingStrategy.getSampleSize();
      this.windowSize = this.strataCount * this.stratumLength;
      this.strataSampleInsertIndex = -1;
   }
   
   /**
    * @param inputSchema
    *          Input Schema for the Operator.
    * @param numberOfStrata
    *          Number of Strata.
    * @param samplingStrategy
    *          Sampling Strategy.
    * @param operatorId
    *          Id of the Operator
    * @param seed
    *          Seed of the random number generator      
    */
   public StratifiedSamplingOperator(final String operatorId, final Schema inputSchema, final long numberOfStrata, 
         final SamplingStrategy samplingStrategy, final long seed) {
      super(operatorId, inputSchema, samplingStrategy);
      this.strataCount = numberOfStrata;
      this.currStratumNo = 1;
      this.stratumLength = samplingStrategy.getWindowSize();
      this.stratumSampleSize = samplingStrategy.getSampleSize();
      this.windowSize = this.strataCount * this.stratumLength;
      this.strataSampleInsertIndex = -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      this.strataCurr++;
      this.windowCurr++;
      // Reset current number of seen tuples for a window and strata at the beginning of each window.
      if (this.windowCurr > this.windowSize) {
         this.resetCurrCounter();
         this.resetStrataCurrCounter();
         this.currStratumNo = StratifiedSamplingOperator.START_INDEX;
      }
      // Reset current number of seen tuples at the beginning of each strata.
      if (this.strataCurr > this.stratumLength) {
         this.resetStrataCurrCounter();
         this.currStratumNo++;
      }
      // Samples the tuples of each Strata according to the sampling strategy specified.
      if (this.getSamplingStrategy().sample(tuple)) {
         this.strataSampleInsertIndex = this.getSamplingStrategy().getInsertPosition();
         if (this.strataSampleInsertIndex == StratifiedSamplingOperator.INSERT_INDEX_LAST) {
            this.setInsertionIndex((int) this.strataSampleInsertIndex);
         } else {
            this.setInsertionIndex((int) ((this.currStratumNo - 1) * this.stratumSampleSize
                  + this.strataSampleInsertIndex));
         }
         final List<Long> currentSegments = tuple.getElementMetadata().getSegmentIds();
         // compute open segments
         this.getOpenSegments().addAll(currentSegments);
         // update current segments
         for (final Long l : currentSegments) {
            SampleStreamSegment segment = this.getSegments().get(l);
            if (segment == null) {
               segment = new SampleStreamSegment(this.getInputSchemas().get(0));
               this.getSegments().put(l, segment);
            }
            segment.insertTuple(tuple, this.getInsertionIndex());
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processForwardControl(final int input, final ControlTuple message) {
      super.processForwardControl(input, message);
   } 
   
   /** 
    * Resets the current number of seen tuples in the window.
    */
   public void resetCurrCounter() {
      this.windowCurr = StratifiedSamplingOperator.START_INDEX;
   }

   /**
    * Reset the current tuple in the strata.
    */
   public void resetStrataCurrCounter() {
      this.strataCurr = StratifiedSamplingOperator.START_INDEX;
   }
}
