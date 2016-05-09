/*
 * @(#)TypeSystemTest.java   1.0   May 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.util;

import java.util.Date;

import org.junit.Test;

/**
 * Test cases for {@link TypeSystem}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TypeSystemTest {

   /**
    * Tests date conversion.
    */
   @Test
   public void testDateConversion() {
      System.out.println(TypeSystem.convertValue(Date.class, "2015-05-21 20:57:13-01"));
      System.out.println(TypeSystem.convertValue(Date.class, "2015-05-21 14:57:13-07"));
      System.out.println(TypeSystem.convertValue(Date.class, "2015-05-21 13:57:13-08"));
      System.out.println(TypeSystem.convertValue(Date.class, "2015-05-21 12:57:13-09"));
      System.out.println(TypeSystem.convertValue(Date.class, "2015-05-22 00:57:13+03"));
      System.out.println(TypeSystem.convertValue(Date.class, "Thu May 21 23:57:13 CEST 2015"));
      System.out.println(TypeSystem.convertValue(Date.class, "2015-05-21 23:57:13"));
   }
}
