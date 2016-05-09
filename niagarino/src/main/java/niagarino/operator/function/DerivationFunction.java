/*
 * @(#)DerivationFunction.java   1.0   Feb 22, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

import java.util.List;

import niagarino.stream.Attribute;
import niagarino.stream.ElementMetadata;

/**
 * Interface describing a derivation function. Derivation functions are used in the derive operator.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public interface DerivationFunction {

   /**
    * Tests whether this derivation function is applicable to the given schema and returns <code>true</code>
    * if it is, or <code>false</code> otherwise.
    *
    * @param schema
    *           tuple schema
    * @return <code>true</code> if this derivation is applicable, <code>false</code> otherwise
    */
   boolean isApplicable(List<Attribute> schema);

   /**
    * Sets the schema of the input tuples of this derivation function.
    *
    * @param schema
    *           tuple schema
    */
   void setSchema(List<Attribute> schema);

   /**
    * Returns the attribute that is added to the schema by this derivation function.
    *
    * @return derived output attribute
    */
   Attribute getDerivedAttribute();

   /**
    * Executes the derivation function on the given tuple with the given metadata and returns the resulting
    * value.
    *
    * @param tuple
    *           data tuple
    * @param metadata
    *           metadata about tuple
    * @return derivation function result
    */
   Object derive(List<Object> tuple, ElementMetadata metadata);
}
