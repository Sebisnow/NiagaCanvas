/*
 * @(#)TestDerivationFunction1.java   1.0   Jan 20, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.plan;

import java.util.List;

import niagarino.operator.function.DerivationFunction;
import niagarino.stream.Attribute;
import niagarino.stream.ElementMetadata;

/**
 * Derivation function for testing an example query plan.
 * 
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TestDerivationFunction1 implements DerivationFunction {

   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      return true;
   }

   @Override
   public void setSchema(final List<Attribute> schema) {
      // Nothing to do.
   }

   @Override
   public Attribute getDerivedAttribute() {
      return new Attribute("segid", Integer.class);
   }

   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final int xway = (int) tuple.get(2);
      final int dir = (int) tuple.get(3);
      final int seg = (int) tuple.get(4);
      return xway << 8 | dir << 7 | seg;
   }
}
