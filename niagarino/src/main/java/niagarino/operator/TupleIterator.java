/*
 * @(#)TupleIterator.java   1.0   May 20, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;

/**
 * An iterator over tuples.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public interface TupleIterator extends Iterator<DataTuple> {

   /**
    * Whether this {@link TupleIterator} has tuples left to emit.
    *
    * @return <code>true</code> if tuples are left, <code>false</code> otherwise
    */
   boolean hasNext();

   /**
    * The next tuple of this {@link TupleIterator}.
    *
    * @return the next tuple
    * @throws NoSuchElementException
    *            if no more tuples are available
    */
   DataTuple next() throws NoSuchElementException;

   /**
    * The next {@link ControlTuple} messages of this {@link TupleIterator}. Is called after every call to
    * {@link TupleIterator#next()}.
    *
    * @return an array containing pending {@link ControlTuple} messages
    */
   ControlTuple[] nextControls();
}
