/*
 * @(#)AccumulatorFunction.java   1.0   Feb 23, 2011
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
import niagarino.util.TypeSystem;

/**
 * A function that uses an internal aggregator to derive accumulated values from another attribute.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class AccumulatorFunction extends AbstractDerivationFunction {

   /** Position of aggregated attribute. */
   private final int attribute;
   /** Function used to aggregated. */
   private final AggregationFunction function;
   /** Incrementally aggregated values. */
   private final Aggregator aggregate;

   /**
    * Creates a new accumulator function that uses the given function to aggregate the attribute at the given
    * position.
    *
    * @param attribute
    *           attribute position
    * @param function
    *           aggregation function
    */
   public AccumulatorFunction(final int attribute, final AggregationFunction function) {
      this.attribute = attribute;
      this.function = function;
      this.aggregate = function.get();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final Number number = (Number) tuple.get(this.attribute);
      this.aggregate.update(number);
      final Object value = this.aggregate.getValue();
      if (value != null) {
         return TypeSystem.convertNumber(this.getDerivedAttribute().getType(), (Double) value);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      return TypeSystem.isNumeric(schema.get(this.attribute).getType());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Attribute initDerivedAttribute(final List<Attribute> schema) {
      final String name = this.function.getType().name() + "(" + schema.get(this.attribute).getName() + ")";
      final Class< ? > type = schema.get(this.attribute).getType();
      return new Attribute(name, type);
   }
}
