/*
 * @(#)AbstractDerivationFunction.java   1.0   Feb 22, 2011
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

/**
 * Common abstract superclass for derivation function.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public abstract class AbstractDerivationFunction implements DerivationFunction {

   /** Schema to which this derivation function is applied. */
   private List<Attribute> schema;
   /** Attribute that is appended to the schema by this derivation function. */
   private Attribute outAttribute;

   /**
    * {@inheritDoc}
    */
   @Override
   public void setSchema(final List<Attribute> schema) {
      this.schema = schema;
      this.outAttribute = this.initDerivedAttribute(schema);
   }

   /**
    * Returns the schema of the input tuples to which this derivation is applied.
    *
    * @return tuple schema
    */
   public List<Attribute> getSchema() {
      return this.schema;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Attribute getDerivedAttribute() {
      if (this.outAttribute == null) {
         throw new IllegalStateException("Derivation function has not been properly initiated; "
               + "use DerivationFunction#setSchema().");
      }
      return this.outAttribute;
   }

   /**
    * Initializes the derived attribute based on the given input schema.
    *
    * @param schema
    *           tuple schema
    * @return derived attribute
    */
   protected abstract Attribute initDerivedAttribute(List<Attribute> schema);
}
