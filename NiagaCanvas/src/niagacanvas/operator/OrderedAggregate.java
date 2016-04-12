/*
 * @(#)OrderedAggregate.java   1.0   Feb 7, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagacanvas.operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import niagarino.operator.AbstractOperator;
import niagarino.operator.function.AggregationFunction;
import niagarino.operator.function.AggregationFunction.Type;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.GroupedStreamSegment;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;
import niagarino.stream.StreamSegment;
import niagarino.util.TypeSystem;

/**
 * Aggregation operator that works over ordered streams.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class OrderedAggregate extends AbstractOperator {

	/** Positions of grouping attributes. */
	private final int[] groupAttributes;
	/** Positions of aggregated attributes. */
	private final int[] aggregatedAttributes;
	/** Aggregation functions that are applied by this operator. */
	private final AggregationFunction[] functions;
	/** Segments that are currently open. */
	private final Set<Long> openSegments;
	/** Segments that are currently being aggregated. */
	private final SortedMap<Long, StreamSegment> segments;
	/** Flag if values of segment are sorted by an attribute value. */
	private final int sortValues;
	/** Number of values in the result set. */
	private final int limit;
	/** Position of attribute to order the values. */
	private final int attributePosition;
	/** Last seen punctuation. */
	private PunctuationControl lastPunctuation;

	/**
	 * Constructs a new aggregation operator for ordered streams with the given
	 * input schema that applies the given aggregation functions to the given
	 * aggregate attributes based on the given grouping attributes.
	 *
	 * @param inputSchema
	 *            input schema
	 * @param groupAttributes
	 *            grouping attributes
	 * @param aggregatedAttributes
	 *            aggregate attributes
	 * @param sortValues
	 *            <code>1</code> if the values should be sorted in descending
	 *            order, <code>2</code> if ascending order, <code>0</code>
	 *            otherwise
	 * @param limit
	 *            number of values per segment, 0 = unlimited
	 * @param attributePosition
	 *            attributePosition to order the values
	 * @param functions
	 *            aggregation functions
	 */
	public OrderedAggregate(final Schema inputSchema, final int[] groupAttributes, final int[] aggregatedAttributes,
			final int sortValues, final int limit, final int attributePosition,
			final AggregationFunction... functions) {
		this(OrderedAggregate.class.getSimpleName(), inputSchema, groupAttributes, aggregatedAttributes, sortValues,
				limit, attributePosition, functions);
	}

	/**
	 * Constructs a new aggregation operator for ordered streams with the given
	 * input schema that applies the given aggregation functions to the given
	 * aggregate attributes based on the given grouping attributes.
	 *
	 * @param operatorId
	 *            id of operator
	 * @param inputSchema
	 *            input schema
	 * @param groupAttributes
	 *            grouping attributes
	 * @param aggregatedAttributes
	 *            aggregate attributes
	 * @param sortValues
	 *            <code>1</code> if the values should be sorted in descending
	 *            order, <code>2</code> if ascending order, <code>0</code>
	 *            otherwise
	 * @param limit
	 *            number of values per segment, 0 = unlimited
	 * @param attributePosition
	 *            attributePosition to order the values
	 * @param functions
	 *            aggregation functions
	 */
	public OrderedAggregate(final String operatorId, final Schema inputSchema, final int[] groupAttributes,
			final int[] aggregatedAttributes, final int sortValues, final int limit, final int attributePosition,
			final AggregationFunction... functions) {
		super(operatorId, Arrays.asList(inputSchema));
		final int progressingAttibute = inputSchema.getProgressingAttribute();
		if (this.arrayContains(groupAttributes, progressingAttibute)
				|| this.arrayContains(aggregatedAttributes, progressingAttibute)) {
			throw new IllegalArgumentException("The progressing attribute cannot be used in groups or aggregates.");
		}
		for (final int groupAttribute : groupAttributes) {
			if (this.arrayContains(aggregatedAttributes, groupAttribute)) {
				throw new IllegalArgumentException("Group and aggregated attributes cannot overlap.");
			}
		}
		this.groupAttributes = groupAttributes;
		this.aggregatedAttributes = aggregatedAttributes;
		this.functions = functions;
		this.openSegments = new HashSet<Long>();
		this.segments = new TreeMap<Long, StreamSegment>();
		this.sortValues = sortValues;
		this.limit = limit;
		this.attributePosition = attributePosition;
		this.lastPunctuation = null;
	}

	/**
	 * Constructs a new aggregation operator for ordered streams with the given
	 * input schema that applies the given aggregation functions to the given
	 * aggregate attributes based on the given grouping attributes.
	 *
	 * @param inputSchema
	 *            input schema
	 * @param groupAttributes
	 *            grouping attributes
	 * @param aggregatedAttributes
	 *            aggregate attributes
	 * @param functions
	 *            aggregation functions
	 */
	public OrderedAggregate(final Schema inputSchema, final int[] groupAttributes, final int[] aggregatedAttributes,
			final AggregationFunction... functions) {
		this(inputSchema, groupAttributes, aggregatedAttributes, 1, 0, 0, functions);
	}

	/**
	 * Constructs a new aggregation operator for ordered streams with the given
	 * input schema that applies the given aggregation functions to the given
	 * aggregate attributes based on the given grouping attributes.
	 *
	 * @param operatorId
	 *            id of operator
	 * @param inputSchema
	 *            input schema
	 * @param groupAttributes
	 *            grouping attributes
	 * @param aggregatedAttributes
	 *            aggregate attributes
	 * @param functions
	 *            aggregation functions
	 */
	public OrderedAggregate(final String operatorId, final Schema inputSchema, final int[] groupAttributes,
			final int[] aggregatedAttributes, final AggregationFunction... functions) {
		this(operatorId, inputSchema, groupAttributes, aggregatedAttributes, 1, 0, 0, functions);
	}

	/**
	 * Returns the input schema of this operator.
	 *
	 * @return input schema
	 */
	private Schema getInputSchema() {
		return this.getInputSchemas().get(0);
	}

	@Override
	public Schema getOutputSchema() {
		final List<Schema> inputSchemas = this.getInputSchemas();
		final GroupedStreamSegment segment = new GroupedStreamSegment(inputSchemas.get(0), this.groupAttributes,
				this.aggregatedAttributes, this.functions);
		return segment.getSchema();
	}

	/**
	 * Checks whether the given integer array contains the given integer key.
	 *
	 * @param array
	 *            integer array
	 * @param key
	 *            integer key
	 * @return <code>true</code> if the array contains the key,
	 *         <code>false</code> otherwise
	 */
	private boolean arrayContains(final int[] array, final int key) {
		for (final int i : array) {
			if (i == key) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void processTuple(final int input, final DataTuple tuple) {
		final List<Long> currentSegments = tuple.getElementMetadata().getSegmentIds();
		// compute open segments
		this.openSegments.addAll(currentSegments);
		// update current segments
		for (final Long l : currentSegments) {
			StreamSegment segment = this.segments.get(l);
			if (segment == null) {
				segment = new GroupedStreamSegment(this.getInputSchema(), this.groupAttributes,
						this.aggregatedAttributes, this.functions);
				this.segments.put(l, segment);
			}
			segment.insertTuple(tuple);
		}
	}

	@Override
	protected void processForwardControl(final int input, final ControlTuple message) {
		switch (message.getType()) {
		case PUNCTUATION:
			final PunctuationControl pctrl = (PunctuationControl) message;
			if (PunctuationControl.Type.WINDOW.equals(pctrl.getPunctuationType())) {
				this.lastPunctuation = pctrl;
				this.reportSegment(pctrl.getSegmentId());
				this.openSegments.remove(pctrl.getSegmentId());
			}
			break;
		default:
		}
		this.pushControl(Flow.FORWARD, message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
		if (Socket.INPUT.equals(socket)) {
			while (!this.segments.isEmpty()) {
				final Long l = this.segments.firstKey();
				this.reportSegment(l);
			}
		}
		super.handleEoS(socket, input, message);
	}

	/**
	 * Reports the given segment by pushing the aggregated result tuple to the
	 * stream and removing the segments from the list of currently processed
	 * segments.
	 *
	 * @param l
	 *            segment number
	 */
	private void reportSegment(final Long l) {
		final StreamSegment segment = this.segments.get(l);
		if (segment != null) {
			final GroupedStreamSegment gss = (GroupedStreamSegment) segment;
			if (this.lastPunctuation != null) {
				final long difference = (l - this.lastPunctuation.getSegmentId()) * this.lastPunctuation.getStepSize();
				final Object min;
				final Object max;
				final Schema s = this.getOutputSchema();
				final Class<?> progAttrType = s.getAttribute(s.getProgressingAttribute()).getType();
				if (Date.class.equals(progAttrType)) {
					min = new Date(this.lastPunctuation.getSegmentStart() + difference);
					max = new Date(this.lastPunctuation.getSegmentEnd() + difference);
				} else {
					min = TypeSystem.convertNumber(progAttrType, this.lastPunctuation.getSegmentStart() + difference);
					max = TypeSystem.convertNumber(progAttrType, this.lastPunctuation.getSegmentEnd() + difference);
				}
				gss.setMinMax(min, max);
			}
			List<DataTuple> tuples = segment.reportTuples();
			if (this.sortValues > 0) {
				Collections.sort(tuples, compareTuples(this.attributePosition, this.sortValues));
			}
			if (this.limit > 0) {
				tuples = tuples.subList(0, Math.min(this.limit, tuples.size()));
			}
			for (final DataTuple tuple : tuples) {
				tuple.getElementMetadata().addSegmentId(l.longValue());
				this.pushTuple(tuple);
			}
			this.segments.remove(l);
		}
	}

	/**
	 * Comparator for tuples.
	 *
	 * @param attributePosition
	 *            position of attribute
	 * @param descending
	 *            <code>1</code> if the values should be sorted in descending
	 *            order, <code>2</code> otherwise
	 * @return comparison result between tuples
	 */
	protected static Comparator<DataTuple> compareTuples(final int attributePosition, final int descending) {
		return new Comparator<DataTuple>() {

			@Override
			public int compare(final DataTuple first, final DataTuple second) {
				if (first.getSchema().getAttribute(attributePosition).getType().equals(String.class)
						&& second.getSchema().getAttribute(attributePosition).getType().equals(String.class)) {
					return ((String) first.getAttributeValue(attributePosition))
							.compareTo((String) second.getAttributeValue(attributePosition));
				}
				if (first.getSchema().getAttribute(attributePosition).getType().equals(Long.class)
						&& second.getSchema().getAttribute(attributePosition).getType().equals(Long.class)) {
					final long a = (Long) first.getAttributeValue(attributePosition);
					final long b = (Long) second.getAttributeValue(attributePosition);
					if (descending != 1) {
						return a > b ? 1 : a < b ? -1 : 0;
					} else {
						return a > b ? -1 : a < b ? 1 : 0;
					}
				}
				return 0;
			}
		};
	}

	/**
	 * Factory for new instances of the OrderedAggregate operator.
	 *
	 * @author Florian 'Sammy' Junghanns
	 *         &lt;florian.junghanns@uni-konstanz.de&gt;
	 * @version 1.0
	 */
	public static class Factory implements PlanOperatorByParametersFactory {

		@Override
		public OrderedAggregate getOperatorByParameters(final String operatorId, final Schema inputSchema,
				final Parameter parameters) {
			final Parameter groupParam = parameters.get("group").get("attribute");
			final int[] groupAttributes = new int[groupParam.getList().size()];
			int counter = 0;
			for (final Parameter param : groupParam.getList()) {
				groupAttributes[counter++] = inputSchema.getAttributeIndex(param.get("name").getString());
				// OrderedAggregate.getAttributeNumber(
				// inputSchema, param.get("name").getString());
			}
			final Parameter funcParam = parameters.get("functions").get(0);
			final int[] funcAttributes = new int[funcParam.getList().size()];
			final AggregationFunction[] funcFuncs = new AggregationFunction[funcParam.getList().size()];
			counter = 0;
			for (final Parameter param : funcParam.getList()) {
				funcAttributes[counter] = inputSchema.getAttributeIndex(param.get("attribute").getString());
				funcFuncs[counter] = BuiltInAggregationFunction.forType(Type.valueOf(param.getName().toUpperCase()));
				counter++;
			}
			int limit = 0;
			if (parameters.get("limit") != null) {
				limit = Integer.parseInt(parameters.get("limit").getString());
			}
			int sort = 0;
			int sortAttribute = -1;
			final Parameter sortParam = parameters.get("sort");
			if (sortParam != null) {
				final Parameter order = sortParam.get("order");
				if (order != null && order.getString().equalsIgnoreCase("desc")) {
					sort = 1;
				} else {
					sort = 2;
				}
				final String sortAttrName = sortParam.get("by").getString();
				sortAttribute = inputSchema.getAttributeIndex(sortAttrName);
				if (sortAttribute == -1) {
					// sort attribute is in generated attributes, iterate over
					// functions
					for (int i = 0; i < funcAttributes.length; i++) {
						// This is rather ugly because we assume the naming of
						// generated attributes
						// will never change.
						final Attribute inputAttribute = inputSchema.getAttribute(funcAttributes[i]);
						final Attribute outputAttribute = funcFuncs[i].getAggregatedAttribute(inputAttribute);
						final String genAttrName = outputAttribute.getName();
						if (genAttrName.equalsIgnoreCase(sortAttrName)) {
							sortAttribute = 1 + groupAttributes.length + i;
							break;
						}
					}
					if (sortAttribute == -1) {
						throw new IllegalArgumentException("Sort attribute " + sortAttrName + " was not found.");
					}
				}
			}
			return new OrderedAggregate(operatorId, inputSchema, groupAttributes, funcAttributes, sort, limit,
					sortAttribute, funcFuncs);
		}
	}
}
