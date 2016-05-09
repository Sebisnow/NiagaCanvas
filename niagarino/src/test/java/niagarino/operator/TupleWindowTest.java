/*
 * @(#)TupleWindowTest.java   1.0   Feb 16, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;

import org.junit.Test;

import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Unit tests for class {@link TupleWindow}.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class TupleWindowTest {

   /** Window size. */
   private static final long SIZE = 10;
   /** Window slide. */
   private static final long SLIDE = 1;
   /** Schema of test tuples. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("Value", Integer.class));

   /**
    * Test case for method {@link TupleWindow#processTuple(int, DataTuple)}.
    */
   @Test
   public void testProcessTuple() {
      final TupleWindow window = new TupleWindow(TupleWindowTest.SCHEMA, TupleWindowTest.SIZE,
            TupleWindowTest.SLIDE);
      for (int i = 0; i < 100; i++) {
         final DataTuple tuple = new DataTuple(TupleWindowTest.SCHEMA, Arrays.asList(new Object[] { Integer
               .valueOf(i) }));
         window.processTuple(0, tuple);
         System.out.println(tuple.getElementMetadata().getSegmentIds());
      }
   }
}
