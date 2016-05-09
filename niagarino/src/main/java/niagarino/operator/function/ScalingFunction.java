/*
 * @(#)ScalingFunction.java   1.0   Jul 11, 2015
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
 * Derivation function to scale an attribute in the schema by a given factor.
 * 
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * 
 * @version 1.0
 */
public class ScalingFunction extends AbstractDerivationFunction {

   /** Position of the attribute in the Schema. */
   private final int attribute;
   
   /** Scaling factor. */
   private final double scalingFactor;
   
   /** Position of the attribute in the Schema. */
   private final String derivedAttributeName;
   
   /**
    * @param attr
    *          Attribute to be scaled.
    * @param factor
    *          Scaling Factor.
    * @param name
    *          Name of the derived(scaled) attribute.
    */
   public ScalingFunction(final int attr, final double factor, final String name) {
      this.attribute = attr;
      this.scalingFactor = factor;
      this.derivedAttributeName = name;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      final Class< ? > typeOne = schema.get(this.attribute).getType();
      if (TypeSystem.isNumeric(typeOne)) {
         return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final Number attributeValue = (Number) tuple.get(this.attribute);
      if (null != attributeValue) {
         final double scaledAttribute = attributeValue.doubleValue() * this.scalingFactor;
         return TypeSystem.convertNumber(this.getDerivedAttribute().getType(), scaledAttribute);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Attribute initDerivedAttribute(final List<Attribute> schema) {
      final Attribute attribute = schema.get(this.attribute);
      final String name = null != this.derivedAttributeName ? this.derivedAttributeName : attribute.getName();
      final Class< ? > type = attribute.getType();
      return new Attribute(name, type);
   }
}
