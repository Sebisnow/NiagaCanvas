/*
 * @(#)EarthMoversDistance.java   1.0   Aug 21, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.util;

/**
 * Calculates the Earth Mover's Distance measure to compare two histograms.
 *
 * @author Michael Grossniklaus &lt;grossniklaus@cs.pdx.edu&gt;
 * @version 1.0
 */
public class EarthMoversDistance {

   /**
    * Computes and returns the earth mover's distance between a one-dimensional reference and
    * actual histogram.
    *
    * @param reference
    *           reference histogram
    * @param histogram
    *           actual histogram
    * @return earth mover's distance
    */
   public double oneDimensional(final double[] reference, final double[] histogram) {
      if (reference == null || histogram == null) {
         throw new IllegalArgumentException("Histogram cannot be null.");
      }
      if (reference.length != histogram.length) {
         throw new IllegalArgumentException("Histograms must be of the same size.");
      }
      double emd = 0.0;
      final int size = reference.length;
      final double[] temp = new double[size];
      temp[0] = histogram[0];
      for (int i = 1; i < size; i++) {
         final double diff = temp[i - 1] - reference[i - 1];
         temp[i] = histogram[i] + diff;
         emd += Math.abs(diff);
      }
      return emd;
   }

   /**
    * Computes and returns the earth mover's distance between a multi-dimensional reference
    * and actual histogram.
    *
    * @return earth mover's distance
    */
   public double multiDimensional() {
      throw new UnsupportedOperationException(
            "Currently, only one-dimensional histograms are supported.");
   }
}
