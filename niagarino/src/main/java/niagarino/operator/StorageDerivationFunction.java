/*
 * @(#)StorageDerivationFunction.java   1.0   Apr 13, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import niagarino.storage.StorageReader;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;

/**
 * Storage derivation functions are used in operators reading from storages.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @param <V>
 *           the value type of the involved storage
 */
public interface StorageDerivationFunction<V> {

   /**
    * Returns the attribute added by this derivation function.
    *
    * @return the new attribute
    */
   Attribute getDerivedAttribute();

   /**
    * Derives the new value for the tuple using the incoming tuple and the given storage's content.
    *
    * @param tuple
    *           the incoming tuple
    * @param reader
    *           the reader for the storage to use
    * @return the new value
    */
   Object derive(final DataTuple tuple, final StorageReader<V> reader);
}
