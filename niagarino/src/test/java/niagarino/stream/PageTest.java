/*
 * @(#)PageTest.java   1.0   Mar 03, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Maximilian Ortwein &lt;maximilian.ortwein@uni.kn&gt;
 * @version 1.0
 */
public class PageTest {

   /** Test Data. **/
   private final List<Object> data = new ArrayList<Object>();
   /** Test Tuple. **/
   private DataTuple t;

   /**
    * Generate some Testdata.
    */
   @Before
   public void generateData() {
      this.data.add(2);
      this.data.add(3);
      this.data.add(1);
      final Schema s = new Schema(2, new Attribute("test1", Integer.class), new Attribute(
            "test2", Integer.class), new Attribute("test3", Integer.class));
      this.t = new DataTuple(s, this.data);
   }

   /**
    * Test if empty Page creation is working.
    */
   @Test
   public void createEmptyPage() {
      final Page p = new Page(200);
      assertTrue(p.isEmpty());
      assertTrue(p.size() == 0);
   }

   /**
    * Test if encuing tuples is working.
    */
   @Test
   public void testEncueueTuple() {
      final Page p = new Page(200);
      p.put(this.t);
      assertFalse(p.isEmpty());
      assertTrue(p.getTuples()[0].equals(this.t));
      assertTrue(p.size() == 1);
   }

   /**
    * Test if filling page to maximum is working.
    */
   @Test
   public void fillPage() {
      final Page p = new Page(20);
      while (!p.isFull()) {
         p.put(this.t);
      }
      assertTrue(p.isFull());
      assertTrue(p.size() == 20);
   }
}
