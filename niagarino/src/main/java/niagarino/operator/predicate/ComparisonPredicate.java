/*
 * @(#)ComparisonPredicate.java   1.0   Oct 22, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.predicate;

import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Comparison predicate that compares an attribute value to a constant value.
 *
 * @author Maximilian Ortwein &lt;maximilian.ortwein@uni-konstanz.de&gt;
 */
public class ComparisonPredicate implements Predicate {

   /** Position of the attribute to which the predicate is applied. */
   private final int attributePosition;
   /** Value to compare with. */
   private final double value;
   /** Comparison operator used in this predicate. */
   private final ComparisonOperator operator;

   /**
    * Constructs a comparison predicate that compares the attribute value at the given
    * position to the given constant value using the given comparison operator.
    *
    * @param attributePosition
    *           position of the attribute
    * @param operator
    *           comparison operator
    * @param value
    *           constant value
    */
   public ComparisonPredicate(final int attributePosition,
         final ComparisonOperator operator, final double value) {
      this.attributePosition = attributePosition;
      this.operator = operator;
      this.value = value;
   }

   /**
    * Constructs a comparison predicate that compares the attribute value at the given
    * position to the given constant value using the given comparison operator.
    *
    * @param attributePosition
    *           position of the attribute
    * @param operator
    *           comparison operator
    * @param value
    *           constant value
    */
   public ComparisonPredicate(final int attributePosition, final String operator,
         final double value) {
      this(attributePosition, ComparisonOperator.valueOf(operator), value);
   }

   @Override
   public boolean isApplicable(final Schema schema) {
      final Class< ? > type = schema.getAttribute(this.attributePosition).getType();
      return Number.class.isAssignableFrom(type);
   }

   @Override
   public boolean evaluate(final DataTuple tuple) {
      final Number number = (Number) tuple.getAttributeValue(this.attributePosition);
      if (number != null) {
         switch (this.operator) {
            case LT:
               return number.doubleValue() < this.value;
            case GT:
               return number.doubleValue() > this.value;
            case LEQ:
               return number.doubleValue() <= this.value;
            case GEQ:
               return number.doubleValue() >= this.value;
            case NEQ:
               return number.doubleValue() != this.value;
            case EQ:
               return number.doubleValue() == this.value;
            default:
               throw new RuntimeException("Operator not Supportet");
         }
      }
      return false;
   }

   /**
    * Enumeration of comparison operators.
    *
    * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
    */
   enum ComparisonOperator {
      /** Less-than operator. */
      LT,
      /** Greater-than operator. */
      GT,
      /** Less-or-equals operator. */
      LEQ,
      /** Greater-or-equals operator. */
      GEQ,
      /** Not-equals operator. */
      NEQ,
      /** Equals operator. */
      EQ;
   }
}
