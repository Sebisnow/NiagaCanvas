/*
 * @(#)NoOp.java   1.0   Aug 19, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;

import niagarino.stream.Schema;
/**
 * An Operator for testing if in- and output are the same, this operator does nothing.
 * 
 * @author Maximilian Ortwein
 * @version 1.0
 */
public class NoOp extends AbstractOperator {
   /** Outputschema.*/
   private final Schema outputSchema;

   /**
    * Constructs a new NoOp.
    * 
    * @param schema
    *        the schema
    */
   public NoOp(final Schema schema) {
      super("NoOp", Arrays.asList(schema));
      this.outputSchema = schema;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getOutputSchema() {
      return this.outputSchema;
   }
}
