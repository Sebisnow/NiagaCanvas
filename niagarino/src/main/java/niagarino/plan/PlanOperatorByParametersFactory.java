/*
 * @(#)PlanOperatorByParametersFactory.java   1.0   Jan 20, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.plan;

import niagarino.operator.Operator;
import niagarino.stream.Schema;

/**
 * Factory for creating operators by parameters retrieved in the {@link inf.uni.kn.niagarino.plan} package.
 * 
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public interface PlanOperatorByParametersFactory {

   /**
    * Returns a new instance of the operator of this factory, based on the given parameters.
    * 
    * @param operatorId
    *           the name of the new operator instance
    * @param inputSchema
    *           the schema with which to work
    * @param parameters
    *           the parameters based on which the operator is to create
    * @return the new operator
    * @throws Exception
    *            if an exception occurs during the creation of the operator
    */
   Operator getOperatorByParameters(final String operatorId, final Schema inputSchema,
         final Parameter parameters) throws Exception;
}
