/*
 * @(#)BernoulliSampling.java   1.0   May 29, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Random;

import niagarino.stream.DataTuple;

/**
 * Implementation of Bernoulli Sampling (Simple Random Sampling).
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public class BernoulliSampling implements SamplingStrategy {

  /** Sample none of the tuples. */
   private static final int SAMPLE_NONE = 0;

   /** Return all the tuples in the Sample. */
   private static final int SAMPLE_ALL = 100;

   /** Insert position in StreamSegment. */
   private static final int INSERT_POSITION = -1;

   /** Sampling Rate used by the Sampling operator. */
   private final int samplingRate;

   /** Sampling Rate used by the Sampling operator. */
   private final float samplingProbability;

   /** Random Number Generator. */
   private final Random randomGenerator;

   /** Current number of seen tuples. */
   private long curr;
   
   /** Size of the window. */
   private long windowSize;
   
   /** Sample Size for a window(reservoir size). */
   private long sampleSize;

   /**
    * @param samplingRate
    *           Sampling Rate for the Simple Random Sampling operator.
    */
   public BernoulliSampling(final int samplingRate) {
      if (samplingRate < 0 | samplingRate > 100) {
         throw new IllegalArgumentException("Invalid Sampling Rate " + samplingRate + ".");
      }
      this.samplingRate = samplingRate;
      this.randomGenerator = new Random();
      this.samplingProbability = (float) samplingRate / 100;
   }
   
   /**
    * @param samplingRate
    *           Sampling Rate for the Simple Random Sampling operator.
    * @param seed
    *           Seed for the Random Number generator.
    */
   public BernoulliSampling(final int samplingRate, final long seed) {
      if (samplingRate < 0 | samplingRate > 100) {
         throw new IllegalArgumentException("Invalid Sampling Rate " + samplingRate + ".");
      }
      this.samplingRate = samplingRate;
      this.randomGenerator = new Random(seed);
      this.samplingProbability = (float) samplingRate / 100;
   }
   
   /**
    * @param samplingRate
    *           Sampling Rate for the Simple Random Sampling operator.
    * @param windowSize
    *          Size of the window.
    * @param sampleSize
    *          Size of Reservoir(number of samples).
    * @param seed
    *           Seed for the Random Number generator.
    */
   public BernoulliSampling(final int samplingRate, final long windowSize, final int sampleSize, final long seed) {
      this(samplingRate, seed);
      this.windowSize = windowSize;
      this.sampleSize = sampleSize;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean sample(final DataTuple tuple) {

      float randomProbability = 0;
      if (this.samplingRate == SAMPLE_NONE) {
         return false;
      } else if (this.samplingRate == SAMPLE_ALL) {
         return true;
      } else {
         randomProbability = this.randomGenerator.nextFloat();
         if (randomProbability <= this.samplingProbability) {
            return true;
         }
         return false;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getInsertPosition() {
      return BernoulliSampling.INSERT_POSITION;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void resetCurrCounter() {
      // Do nothing as curr is not relevant here
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public long getWindowSize() {
      return this.windowSize;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public long getSampleSize() {
      return this.sampleSize;
   }
   
   /**
    * @return the curr
    */
   public long getCurr() {
      return this.curr;
   }

   /**
    * @param curr
    *           the curr to set
    */
   public void setCurr(final long curr) {
      this.curr = curr;
   }

   /**
    * @return the randomGenerator
    */
   public Random getRandomGenerator() {
      return this.randomGenerator;
   }

   /**
    * @return the samplingProbability
    */
   public float getSamplingProbability() {
      return this.samplingProbability;
   }
}
