/*
 * @(#)Selection.java   1.0   Mar 1, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagacanvas.operator;

import java.util.Arrays;

import niagarino.operator.AbstractOperator;
import niagarino.operator.predicate.Predicate;
import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Selection operator that uses a selection predicate to filter a stream.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
@OperatorAnnotation
public class Selection extends AbstractOperator {

	/** Predicate used by this selection operator. */
	private final Predicate predicate;

	/**
	 * Constructs a new selection operator with the given input schema that uses
	 * the given predicate.
	 * 
	 * @param operatorId
	 *            id of operator
	 * @param inputSchema
	 *            input schema
	 * @param predicate
	 *            selection predicate
	 */

	public Selection(final String operatorId, final Schema inputSchema, final Predicate predicate) {
		super(operatorId, Arrays.asList(inputSchema));
		if (predicate.isApplicable(inputSchema)) {
			this.predicate = predicate;
		} else {
			throw new IllegalArgumentException(
					"Predicate " + predicate + " not applicable to schema " + inputSchema + ".");
		}
	}

	/**
	 * Constructs a new selection operator with the given input schema that uses
	 * the given predicate.
	 * 
	 * @param inputSchema
	 *            input schema
	 * @param predicate
	 *            selection predicate
	 */
	public Selection(final Schema inputSchema, final Predicate predicate) {
		this(Selection.class.getSimpleName(), inputSchema, predicate);
	}

	/**
	 * This Method is used to tell others what is needed to instantiate a new
	 * Selection Operator.
	 * 
	 * @return An Array of ParamDescription
	 */
	@GetParamDesc(numberOfParams = 1)
	public static ParamDescription[] getParams() {
		ParamDescription[] params = new ParamDescription[1];
		params[0] = new ParamDescription("Predicate", ParamDescription.PREDICATE, null, "The Predicate to select by.");
		return params;
	}

	/**
	 * This Method is used to tell others what is needed to instantiate a new
	 * Selection Operator.
	 * 
	 * @return An Array of ParamDescription
	 */
	@GetParamsOfInstance
	public static ParamDescription[] getParamsofInstance(Selection sel) {

		ParamDescription[] params = new ParamDescription[1];
		params[0] = new ParamDescription("Predicate", ParamDescription.PREDICATE, sel.predicate,
				"The Predicate to select by.");
		return params;
	}

	/**
	 * Factory Method to generate a new Selection Operator.
	 * 
	 * @param inputSchema
	 *            The Schema to do the Selection on.
	 * 
	 * @param predicate
	 *            The predicate for the selection.
	 * 
	 * @return returns a new instance of Selection
	 */
	@NewInstance
	public static Selection newInstance(final Schema inputSchema, final Predicate predicate) {
		return new Selection(inputSchema, predicate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Schema getOutputSchema() {
		return this.getInputSchemas().get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTuple(final int input, final DataTuple tuple) {
		if (this.predicate.evaluate(tuple)) {
			this.pushTuple(tuple);
		}
	}

	/**
	 * Factory for new instances of the Selection operator.
	 * 
	 * @author Florian 'Sammy' Junghanns
	 *         &lt;florian.junghanns@uni-konstanz.de&gt;
	 * @version 1.0
	 */
	public static class Factory implements PlanOperatorByParametersFactory {

		@Override
		public Selection getOperatorByParameters(final String operatorId, final Schema inputSchema,
				final Parameter parameters)
						throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			final String predicateName = parameters.get("object").get("class").getString();
			final Predicate predicate = (Predicate) Class.forName(predicateName).newInstance();
			return new Selection(operatorId, inputSchema, predicate);
		}
	}

}
