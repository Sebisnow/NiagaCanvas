/*
 * @(#)CrossProduct.java   1.0   Jun 16, 2014
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

import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * CrossProduct operator that produces multiple tuples for a combination of two attributes.
 * 
 * @author Andreas Weiler &lt;andreas.weiler@uni.kn&gt;
 * @version 1.0
 */
public class CrossProduct extends AbstractOperator {
   
   /** Position of attribute to combine. */
   private final int attributePosition;
   /** Separator to use for tokenization. */
   private final String separator;
   /** OutputSchema. */
   private final Schema outputSchema;
   /** Name of new attribute. */
   private final String attributeName;
   
   /**
    * Constructs a new crossproduct operator with the given input schema that has the given number of output
    * streams.
    * 
    * @param inputSchema
    *           input schema
    * @param attributePosition
    *          attributePosition to combine
    * @param separator
    *          separator to use for tokenization
    * @param attributeName
    *          name of new attribute
    */
   public CrossProduct(final Schema inputSchema, final int attributePosition, final String separator,
         final String attributeName) {
      this(CrossProduct.class.getSimpleName(), inputSchema, attributePosition, separator, attributeName);
   }

   /**
    * Constructs a new crossproduct operator with the given input schema.
    * 
    * @param operatorId
    *           name of operator
    * @param inputSchema
    *           input schema
    * @param attributePosition
    *          attributePosition to combine
    * @param separator
    *          separator to use for tokenization
    * @param attributeName
    *          name of new attribute
    */
   public CrossProduct(final String operatorId, final Schema inputSchema, final int attributePosition,
         final String separator, final String attributeName) {
      super(operatorId, Arrays.asList(inputSchema));
      this.attributePosition = attributePosition;
      this.separator = separator;
      this.attributeName = attributeName;
      this.outputSchema = this.initSchema();
   }

   /**
    * {@inheritDoc}
    */
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
      attributes.add(new Attribute(this.attributeName, String.class));
      return new Schema(schema.getProgressingAttribute(), attributes.toArray(new Attribute[0]));
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final String[] values = (tuple.getAttributeValue(this.attributePosition) + "").split(this.separator);
      final List<Object> tupleValues = new ArrayList<Object>(tuple.getValues());
      tupleValues.add("");
      for (String v : values) {
         tupleValues.set(tupleValues.size() - 1, v);
         this.pushTuple(new DataTuple(this.getOutputSchema(), tupleValues));
      }
   }
}
