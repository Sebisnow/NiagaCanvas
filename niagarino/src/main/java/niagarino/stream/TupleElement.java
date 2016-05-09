/*
 * @(#)TupleElement.java   1.0   Apr 26, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;


/**
 * Common interface for all stream elements.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public interface TupleElement extends StreamElement, Cloneable {

   /**
    * Returns the stream element metadata of this tuple stream element.
    *
    * @return stream element metadata
    */
   ElementMetadata getElementMetadata();

   /**
    * Returns a cloned tuple element.
    *
    * @return tuple element
    * @throws CloneNotSupportedException
    *            if the tuple element cannot be cloned
    */
   TupleElement clone() throws CloneNotSupportedException;
}
