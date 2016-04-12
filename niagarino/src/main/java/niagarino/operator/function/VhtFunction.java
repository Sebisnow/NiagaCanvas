/*
 * @(#)VhtFunction.java   1.0   Mar 1, 2011
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
 * Computes the function <code>vht = (vol * length) / speed</code>.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class VhtFunction extends AbstractDerivationFunction {

   /** Position of the volume attribute in the tuple. */
   private final int volumeAttribute;
   /** Position of the speed attribute in the tuple. */
   private final int speedAttribute;
   /** Length parameter of this vht derivation function. */
   private final double length;

   /**
    * Creates a new vht derivation function that uses the volume and speed attributes given
    * by their position and the given length parameter.
    *
    * @param volumeAttribute
    *           volume attribute position
    * @param speedAttribute
    *           speed attribute position
    * @param length
    *           length parameter
    */
   public VhtFunction(final int volumeAttribute, final int speedAttribute,
         final double length) {
      this.volumeAttribute = volumeAttribute;
      this.speedAttribute = speedAttribute;
      this.length = length;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final Number volume = (Number) tuple.get(this.volumeAttribute);
      final Number speed = (Number) tuple.get(this.speedAttribute);
      if (volume != null && speed != null && speed.doubleValue() != 0) {
         final double vht = volume.doubleValue() * this.length / speed.doubleValue();
         return TypeSystem.convertNumber(this.getDerivedAttribute().getType(), vht);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      final Class< ? > volumeType = schema.get(this.volumeAttribute).getType();
      final Class< ? > speedType = schema.get(this.speedAttribute).getType();
      if (TypeSystem.isNumeric(volumeType) && TypeSystem.isNumeric(speedType)) {
         return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Attribute initDerivedAttribute(final List<Attribute> schema) {
      final Attribute volumeAttribute = schema.get(this.volumeAttribute);
      final Attribute speedAttribute = schema.get(this.speedAttribute);
      final String name = "vht(" + volumeAttribute.getName() + "*" + this.length + "/"
            + speedAttribute.getName() + ")";
      final Class< ? > type = Double.class;
      return new Attribute(name, type);
   }
}
