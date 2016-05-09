/*
 * @(#)OperatorException.java   1.0   Jun 27, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

/**
 * Exception thrown by operators during query execution.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 */
public class OperatorException extends RuntimeException {

   /** Serial version UID. */
   private static final long serialVersionUID = -6896650930581323291L;

   /** Operator that threw this exception. */
   private final Operator operator;

   /**
    * Constructs a new operator exception for the given operator, message, and cause.
    *
    * @param operator
    *           operator throwing the exception
    * @param message
    *           exception message
    * @param cause
    *           cause of the exception
    */
   public OperatorException(final Operator operator, final String message,
         final Throwable cause) {
      super(message, cause);
      this.operator = operator;
   }

   /**
    * Constructs a new operator exception for the given operator and message.
    *
    * @param operator
    *           operator throwing the exception
    * @param message
    *           exception message
    */
   public OperatorException(final Operator operator, final String message) {
      super(message);
      this.operator = operator;
   }

   /**
    * Constructs a new operator exception for the given operator and cause.
    *
    * @param operator
    *           operator throwing the exception
    * @param cause
    *           cause of the exception
    */
   public OperatorException(final Operator operator, final Throwable cause) {
      super(cause);
      this.operator = operator;
   }

   /**
    * Returns the operator that threw this exception.
    *
    * @return operator throwing the exception
    */
   public Operator getOperator() {
      return this.operator;
   }
}
