/*
 * @(#)DeltaFunction.java   1.0   Feb 22, 2011
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
 * Computes the delta between two attribute values.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class DeltaFunction extends AbstractDerivationFunction {

   /** Position of first attribute. */
   private final int attributeOne;
   /** Position of second attribute. */
   private final int attributeTwo;

   /**
    * Creates a new derivation function that computes the delta between the two attributes
    * given by their positions.
    *
    * @param attributeOne
    *           first attribute position
    * @param attributeTwo
    *           second attribute position
    */
   public DeltaFunction(final int attributeOne, final int attributeTwo) {
      this.attributeOne = attributeOne;
      this.attributeTwo = attributeTwo;
   }

   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final Number numberOne = (Number) tuple.get(this.attributeOne);
      final Number numberTwo = (Number) tuple.get(this.attributeTwo);
      if (numberOne != null && numberTwo != null) {
         final double delta = Math.abs(numberOne.doubleValue() - numberTwo.doubleValue());
         return TypeSystem.convertNumber(this.getDerivedAttribute().getType(), delta);
      }
      return null;
   }

   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      final Class< ? > typeOne = schema.get(this.attributeOne).getType();
      final Class< ? > typeTwo = schema.get(this.attributeTwo).getType();
      if (TypeSystem.isNumeric(typeOne) && TypeSystem.isNumeric(typeTwo)) {
         return true;
      }
      return false;
   }

   @Override
   protected Attribute initDerivedAttribute(final List<Attribute> schema) {
      final Attribute attributeOne = schema.get(this.attributeOne);
      final Attribute attributeTwo = schema.get(this.attributeTwo);
      final String name = "|" + attributeOne.getName() + "-" + attributeTwo.getName() + "|";
      final Class< ? > type = TypeSystem.commonType(attributeOne.getType(),
            attributeTwo.getType());
      return new Attribute(name, type);
   }
}
