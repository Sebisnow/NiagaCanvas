/*
 * @(#)EarthMoversDistanceTest.java   1.0   Aug 21, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.util;

import org.junit.Test;

/**
 * Unit tests for class {@link EarthMoversDistance}.
 *
 * @author Michael Grossniklaus &lt;grossniklaus@cs.pdx.edu&gt;
 * @version 1.0
 */
public class EarthMoversDistanceTest {

   /**
    * Test case for method {@link EarthMoversDistance#oneDimensional(double[], double[])}.
    */
   @Test
   public void testOneDimensional() {
      final EarthMoversDistance emd = new EarthMoversDistance();
      double[] reference = new double[] { 5.0, 3.0, 6.0, 7.0 };
      System.out.println(emd.oneDimensional(reference, reference));
      double[] histogram = new double[] { 9.0, 2.0, 5.0, 5.0 };
      System.out.println(emd.oneDimensional(histogram, histogram));
      System.out.println(emd.oneDimensional(reference, histogram));
      System.out.println(emd.oneDimensional(histogram, reference));

      histogram = new double[] { 4.0, 6.0, 7.0, 4 };
      System.out.println(emd.oneDimensional(reference, histogram));

      reference = new double[] { 9.0, 2.0, 5.0, 5.0 };
      System.out.println(emd.oneDimensional(reference, histogram));
   }
}
