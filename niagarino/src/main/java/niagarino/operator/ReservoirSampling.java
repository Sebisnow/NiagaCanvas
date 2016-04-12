/*
 * @(#)ReservoirSampling.java   1.0   Jul 05, 2015
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
 * Implementation of Reservoir Sampling.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public class ReservoirSampling implements SamplingStrategy {

   /** Insert at any position in SampleList(append preferred). */
   private static final int INSERT_INDEX_LAST = -1;
   
   /** No seed for the random generator. */
   private static final int NO_SEED = -1;
   
   /** Starting index of a window. */
   private static final int START_INDEX = 1;

   /** Size of the reservoir. */
   private final int reservoirSize;

   /** Size of the window. */
   private final long windowSize;

   /** Current number of seen tuples. */
   private long curr;

   /** Random number generated for each tuple. */
   private double tupleRandom;

   /** Probability for each tuple. */
   private double tupleProbability;

   /** Insert Position for a tuple in a segment. */
   private int insertPos;

   /** Random Number Generator for Tuple. */
   private Random randomGenerator;

   /** Random Number Generator to determine position of tuple in a SampleSegment. */
   private final Random randomPosGenerator;
   
   /** Random Number Generator seed. */
   private final long randomSeed;


   /**
    * @param reservoirSize
    *           Size of the reservoir
    * @param windowSize
    *           Size of the window
    */
   public ReservoirSampling(final int reservoirSize, final long windowSize) {
      if (reservoirSize > windowSize) {
         throw new IllegalArgumentException("Reservoir Size(" + reservoirSize + ") must be smaller "
               + "than Window Size(" + windowSize + ").");
      }
      this.reservoirSize = reservoirSize;
      this.curr = 0;
      this.insertPos = -1;
      this.randomGenerator = new Random();
      this.randomPosGenerator = new Random();
      this.windowSize = windowSize;
      this.randomSeed = -1;
   }
   
   /**
    * @param reservoirSize
    *           Size of the reservoir
    * @param windowSize
    *           Size of the window
    * @param seed
    *           Seed for the random number generator
    */
   public ReservoirSampling(final int reservoirSize, final long windowSize, final long seed) {
      if (reservoirSize > windowSize) {
         throw new IllegalArgumentException("Reservoir Size(" + reservoirSize + ") must be smaller "
               + "than Window Size(" + windowSize + ").");
      }
      this.reservoirSize = reservoirSize;
      this.curr = 0;
      this.insertPos = -1;
      this.randomGenerator = new Random(seed);
      this.randomPosGenerator = new Random();
      this.windowSize = windowSize;
      this.randomSeed = seed;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean sample(final DataTuple tuple) {
      this.curr++;
      // Resets the current tuple count at the beginning of every window.
      if (this.curr > this.windowSize) {
         this.resetCurrCounter();
         //Initialize the random Variable again if a seed is present
         if (this.randomSeed != ReservoirSampling.NO_SEED) {
            this.randomGenerator = new Random(this.randomSeed);
         }
      }
      if (this.curr <= this.reservoirSize) {
         return true;
      } else {
         this.tupleProbability = (float) this.reservoirSize / this.curr;
         this.tupleRandom = this.randomGenerator.nextFloat();
         if (this.tupleRandom < this.tupleProbability) {
            return true;
         }
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getInsertPosition() {
      if (this.curr <= this.reservoirSize) {
         // Just append to the list.
         this.insertPos = ReservoirSampling.INSERT_INDEX_LAST;
      } else {
         // Choose a random insert position within the reservoir size.
         this.insertPos = this.randomPosGenerator.nextInt(this.reservoirSize);
      }
      return this.insertPos;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void resetCurrCounter() {
      this.curr = ReservoirSampling.START_INDEX;
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
      return this.reservoirSize;
   } 
}
