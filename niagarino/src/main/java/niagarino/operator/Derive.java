/*
 * @(#)Derive.java   1.0   Feb 22, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import niagarino.operator.function.DerivationFunction;
import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.ElementMetadata;
import niagarino.stream.Schema;

/**
 * Adds derived attributes to a tuple using the given derivation functions.
 *
 * @author Michael Grossniklas &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Derive extends AbstractOperator {

   /** Derivation functions applied by this derive operator. */
   private final DerivationFunction[] functions;
   /** Output schema of this derive operator. */
   private final Schema outputSchema;

   /**
    * Constructs a new derive operator that applies the given derivation functions to data tuples described by
    * the given schema.
    *
    * @param operatorId
    *           name of operator
    * @param inputSchema
    *           schema of input data tuples
    * @param functions
    *           derivation functions to apply
    */
   public Derive(final String operatorId, final Schema inputSchema, final DerivationFunction... functions) {
      super(operatorId, Arrays.asList(inputSchema));
      this.functions = functions;
      this.outputSchema = this.initSchema();
   }

   /**
    * Constructs a new derive operator that applies the given derivation functions to data tuples described by
    * the given schema.
    *
    * @param inputSchema
    *           schema of input data tuples
    * @param functions
    *           derivation functions to apply
    */
   public Derive(final Schema inputSchema, final DerivationFunction... functions) {
      this(Derive.class.getSimpleName(), inputSchema, functions);
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final ElementMetadata metadata = tuple.getElementMetadata();
      final List<Object> values = new ArrayList<Object>();
      values.addAll(tuple.getValues());
      for (final DerivationFunction function : this.functions) {
         values.add(function.derive(values, metadata));
      }
      this.pushTuple(new DataTuple(this.getOutputSchema(), values, metadata));
   }

   @Override
   public Schema getOutputSchema() {
      return this.outputSchema;
   }

   /**
    * Initializes and extends the schema to reflect the additional tuple attributes introduced by the
    * derivation functions.
    *
    * @return tuple schema
    */
   private Schema initSchema() {
      final List<Attribute> attributes = new ArrayList<Attribute>();
      final Schema schema = this.getInputSchemas().get(0);
      attributes.addAll(schema.getAttributes());
      for (final DerivationFunction function : this.functions) {
         if (function.isApplicable(attributes)) {
            function.setSchema(attributes);
            attributes.add(function.getDerivedAttribute());
         } else {
            throw new IllegalArgumentException("Function " + function + " is not applicable to schema "
                  + attributes + ".");
         }
      }
      return new Schema(schema.getProgressingAttribute(), attributes.toArray(new Attribute[0]));
   }

   /**
    * Factory for new instances of the Derive operator.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Factory implements PlanOperatorByParametersFactory {

      @Override
      public Derive getOperatorByParameters(final String operatorId, final Schema inputSchema,
            final Parameter parameters) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
         final String derivationFunctionName = parameters.get("object").get("class").getString();
         final DerivationFunction function = (DerivationFunction) Class.forName(derivationFunctionName)
               .newInstance();
         return new Derive(operatorId, inputSchema, function);
      }
   }
}
