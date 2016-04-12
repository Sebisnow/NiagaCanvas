/*
 * @(#)BernoulliSamplingEfficient.java   1.0   Jul 07, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import niagarino.stream.DataTuple;

/**
 * Implementation of efficient version of Bernoulli Sampling Algorithm.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public class BernoulliSamplingEfficient extends BernoulliSampling {

   /** Size of the skip. */
   private int skipSize;

   /** Index to next element to be included in the sample. */
   private int nextElement;

   /** Size of the window. */
   private final long windowSize;
   
   /** Sample Size for a window(reservoir size). */
   private long sampleSize;

   /**
    * @param samplingRate
    *           Sampling Rate.
    * @param windowSize
    *           Size of the window.
    */
   public BernoulliSamplingEfficient(final int samplingRate, final long windowSize) {
      super(samplingRate);
      this.skipSize = 0;
      this.nextElement = 0;
      this.windowSize = windowSize;
   }
   
   /**
    * @param samplingRate
    *           Sampling Rate.
    * @param windowSize
    *           Size of the window.
    * @param seed
    *           Seed for the Random Number generator.
    */
   public BernoulliSamplingEfficient(final int samplingRate, final long windowSize, final long seed) {
      super(samplingRate, seed);
      this.skipSize = 0;
      this.nextElement = 0;
      this.windowSize = windowSize;
   }
   
   /**
    * @param samplingRate
    *           Sampling Rate.
    * @param windowSize
    *           Size of the window.
    * @param sampleSize
    *           Sample Size for a window(reservoir size).
    */
   public BernoulliSamplingEfficient(final int samplingRate, final long windowSize, final int sampleSize) {
      this(samplingRate, windowSize);
      this.sampleSize = sampleSize;
   }

   @Override
   public boolean sample(final DataTuple tuple) {
      this.setCurr(this.getCurr() + 1);
      // Reset current tuple count at the beginning of each window.
      if (this.getCurr() > this.windowSize) {
         this.resetCurrCounter();
         this.nextElement = 0;
      }
      float randomProbability = 0;
      boolean pushTuple = false;
      // Initial Skip
      if (this.nextElement == 0) {
         randomProbability = this.getRandomGenerator().nextFloat();
         this.skipSize = (int) (Math.ceil(Math.log(randomProbability)
               / Math.log(1 - this.getSamplingProbability())) - 1);
         this.nextElement = this.skipSize + 1;
      }
      // Sample the current element and determine the skip size.
      if (this.getCurr() == this.nextElement) {
         randomProbability = this.getRandomGenerator().nextFloat();
         this.skipSize = (int) (Math.ceil(Math.log(randomProbability)
               / Math.log(1 - this.getSamplingProbability())) - 1);
         this.nextElement += this.skipSize + 1;
         pushTuple = true;
      }
      return pushTuple;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void resetCurrCounter() {
      this.setCurr(1);
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
}
