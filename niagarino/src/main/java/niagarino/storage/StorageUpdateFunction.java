/*
 * @(#)StorageUpdateFunction.java   1.0   Feb 22, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

import niagarino.stream.DataTuple;

/**
 * The StorageUpdateFunction provides a way to update a Storage's value based on the old value and a given
 * tuple.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @param <V>
 *           the value type of the Storage object
 */
public interface StorageUpdateFunction<V> {

   /**
    * Retrieves the numerical key of the tuple. If the key is an object, it simply returns the hash code.
    *
    * @param tuple
    *           the tuple providing the key
    * @return the numeric key for the given tuple
    */
   int getTupleNumericKey(final DataTuple tuple);

   /**
    * Retrieves the key of the tuple. If the key is numeric, it simply returns the corresponding object to the
    * number.
    *
    * @param tuple
    *           the tuple providing the key
    * @return the key for the given tuple
    */
   Object getTupleKey(final DataTuple tuple);

   /**
    * Calculates the new value based on the current value and the tuple value.
    *
    * @param oldValue
    *           the old value to base the calculation on
    * @param tuple
    *           the tuple to retrieve the value to update with from
    * @return the value
    */
   V getUpdatedValue(final V oldValue, final DataTuple tuple);
}
