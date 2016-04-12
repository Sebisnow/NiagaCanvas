/*
 * @(#)PageElement.java   1.0   Apr 26, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;


/**
 * Common interface of all paging stream elements.
 *
 * @author Maximilian Ortwein &lt;maximilian.ortwein@uni.kn&gt;
 */
public interface PageElement extends StreamElement {

   /**
    * Inserts the given data tuple into this page.
    *
    * @param tuple
    *           data tuple
    */
   void put(final DataTuple tuple);

   /**
    * Returns the tuples that are currently in this page.
    *
    * @return array of tuples
    */
   DataTuple[] getTuples();

   /**
    * Returns if this page is full.
    *
    * @return {@code true} if the page is full, {@code false} otherwise
    */
   boolean isFull();

   /**
    * Returns if this page is empty.
    *
    * @return {@code true} if the page is empty {@code false} otherwise
    */
   boolean isEmpty();

   /**
    * Returns the current size of this page.
    *
    * @return page size
    */
   int size();

   /**
    * Clears all tuples from the page.
    */
   void clear();
}
