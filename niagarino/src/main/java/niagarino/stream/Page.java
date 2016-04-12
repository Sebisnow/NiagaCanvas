/*
 * @(#)Page.java   1.0   Apr 26, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.Arrays;

/**
 * Implementation of a paging stream element.
 *
 * @author Maximilian Ortwein &lt;maximilian.ortwein@uni.kn&gt;
 */

public class Page implements PageElement {

   /** The Queue where the tuples are stored. **/
   private final DataTuple[] tuples;
   /** Size of the Queue. **/
   private final int size;
   /** Current count of Tuples. **/
   private int tupleCount;

   /**
    * Constructor of the Page.
    *
    * @param capacity
    *           capacity
    */
   public Page(final int capacity) {
      this.size = capacity;
      this.tuples = new DataTuple[this.size];
      this.tupleCount = 0;
   }

   @Override
   public final void put(final DataTuple tuple) {
      this.tuples[this.tupleCount] = tuple;
      this.tupleCount++;
   }

   @Override
   public final DataTuple[] getTuples() {
      return this.tuples;
   }

   @Override
   public final boolean isFull() {
      return this.tupleCount == this.size;
   }

   @Override
   public final int size() {
      return this.tupleCount;
   }

   @Override
   public final boolean isEmpty() {
      return this.tupleCount == 0;
   }

   @Override
   public void clear() {
      Arrays.fill(this.tuples, null);
      this.tupleCount = 0;
   }

   @Override
   public String toString() {
      return Page.class.getSimpleName() + "[" + this.size() + "]";
   }
}
