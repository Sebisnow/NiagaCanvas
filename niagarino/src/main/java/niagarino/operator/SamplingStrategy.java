/*
 * @(#)SamplingStrategy.java   1.0   May 29, 2015
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
 * Generic Interface for Sampling Algorithms.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public interface SamplingStrategy {

   /**
    * Applies sampling to tuples and returns the result.
    *
    * @param input
    *           input Tuple for Sampling
    * @return boolean
    */
   boolean sample(DataTuple input);

   /**
    * Returns the Insertion Position for the Sampled Tuple in the StreamSegment.
    *
    * @return boolean
    */
   int getInsertPosition();

   /**
    * Resets the current counter after a Punctuation is sent.
    */
   void resetCurrCounter();
   
   /**
    * Returns the Window Size.
    *
    * @return long
    */
   long getWindowSize();
   
   /**
    * Returns the Sample Size.
    *
    * @return long
    */
   long getSampleSize();
}
