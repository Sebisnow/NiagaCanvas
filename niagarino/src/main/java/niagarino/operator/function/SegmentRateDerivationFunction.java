/*
 * @(#)SegmentRateDerivationFunction.java   1.0   Mar 8, 2011
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
 * Reads out the segment rate from a tuple's meta-data.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class SegmentRateDerivationFunction extends AbstractDerivationFunction {

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final List<Long> ids = metadata.getSegmentIds();
      if (ids.size() > 0) {
         return Long.valueOf(ids.get(0).longValue() + 1);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Attribute initDerivedAttribute(final List<Attribute> schema) {
      return new Attribute("segment-rate", Long.class);
   }
}
