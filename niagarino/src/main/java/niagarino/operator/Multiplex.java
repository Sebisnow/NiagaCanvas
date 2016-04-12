/*
 * @(#)Multiplex.java   1.0   Feb 21, 2011
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
 * Multiplex operator that replicates one input stream onto several output streams.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Multiplex extends AbstractOperator {

   /**
    * Constructs a new multiplex operator with the given input schema that has the given number of output
    * streams.
    *
    * @param operatorId
    *           name of operator
    * @param inputSchema
    *           input schema
    * @param arity
    *           number of output streams
    */
   @Deprecated
   public Multiplex(final String operatorId, final Schema inputSchema, final int arity) {
      super(operatorId, Arrays.asList(inputSchema));
   }

   /**
    * Constructs a new multiplex operator with the given input schema that has the given number of output
    * streams.
    *
    * @param inputSchema
    *           input schema
    * @param arity
    *           number of output streams
    */
   public Multiplex(final Schema inputSchema, final int arity) {
      this(Multiplex.class.getSimpleName(), inputSchema, arity);
   }

   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }
}
