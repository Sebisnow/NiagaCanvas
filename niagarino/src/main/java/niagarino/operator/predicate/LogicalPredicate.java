/*
 * @(#)LogicalPredicate.java   1.0   Mar 9, 2011
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
 * Predicate implementation that evaluates one or more sub-predicate based on the given logical operator.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class LogicalPredicate implements Predicate {

   /** Logical operator used by this predicate. */
   private LogicalOperator operator;
   /** Sub-predicates of this predicate. */
   private Predicate[] predicates;

   /**
    * Constructs a new logical predicate that combines the given sub-predicates by applying the given
    * operator.
    *
    * @param operator
    *           operator of this predicate
    * @param predicates
    *           sub-predicates of this predicate
    */
   public LogicalPredicate(final LogicalOperator operator, final Predicate... predicates) {
      if (predicates.length == operator.getArity()) {
         this.operator = operator;
         this.predicates = predicates;
      } else {
         throw new IllegalArgumentException("Operator arity does not match number of predicates.");
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final Schema schema) {
      for (final Predicate predicate : this.predicates) {
         if (!predicate.isApplicable(schema)) {
            return false;
         }
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean evaluate(final DataTuple tuple) {
      switch (this.operator) {
         case NOT:
            return !this.predicates[0].evaluate(tuple);
         case AND:
            return this.predicates[0].evaluate(tuple) && this.predicates[1].evaluate(tuple);
         case OR:
            return this.predicates[0].evaluate(tuple) || this.predicates[1].evaluate(tuple);
         default:
            return false;
      }
   }

   /**
    * Enumeration of logical operators supported by this predicate.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   public enum LogicalOperator {
      /** Logical not operator. */
      NOT(1),
      /** Logical and operator. */
      AND(2),
      /** Logical or operator. */
      OR(2);

      /** Arity of the logical operator. */
      private int arity;

      /**
       * Constructs a new logical operator that has the given arity.
       *
       * @param arity
       *           logical operator arity
       */
      private LogicalOperator(final int arity) {
         this.arity = arity;
      }

      /**
       * Returns the arity of this logical operator.
       * 
       * @return logical operator arity
       */
      private int getArity() {
         return this.arity;
      }
   }
}
