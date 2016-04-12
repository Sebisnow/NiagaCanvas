/*
 * @(#)XMLReader.java   1.0   Jan 9, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2014-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.plan;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import niagarino.operator.Multiplex;
import niagarino.operator.Operator;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * The XMLReader is capable of reading a file containing XML code describing a query plan. TODO correct
 * exception types
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class XMLReader {

   /** Path to the file containing the query plan description. */
   private final String planFile;

   /** All schemas given in the XML file. */
   private final HashMap<String, Schema> schemas;

   /** The first stream segment of the query plan. */
   private Stream firstStream = null;

   /**
    * Constructs a new XMLReader for the plan description in the specified file.
    *
    * @param planFile
    *           the path to the file containing the query plan description in XML
    */
   public XMLReader(final String planFile) {
      this.planFile = planFile;
      this.schemas = new HashMap<>();
   }

   /**
    * Reads the query plan description from the file of this XMLReader and creates the actual query plan from
    * it.
    *
    * @throws JAXBException
    *            if the XML file is malformed
    * @throws ClassNotFoundException
    *            if a non-existent operator is used in the plan description
    */
   public void read() throws JAXBException, ClassNotFoundException {
      final File xml = new File(this.planFile);

      final JAXBContext jc = JAXBContext.newInstance(Plan.class);

      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      final Plan plan = (Plan) unmarshaller.unmarshal(xml);

      // Create schemas.
      for (final TypeListType typeList : plan.getSchema()) {
         this.schemas.put(typeList.getName(), this.createSchema(typeList));
      }

      // Create operator order.
      final HashMap<String, Stream> streams = new HashMap<>();
      for (final StreamType streamT : plan.getStream()) {
         // Order streams.
         final String name = streamT.getName();
         Stream stream = streams.get(name);
         if (stream == null) {
            stream = new Stream();
            streams.put(name, stream);
         }
         final String previousName = streamT.getPrevious();
         if (previousName != null) {
            Stream previousStream = streams.get(previousName);
            if (previousStream == null) {
               // If the previous stream doesn't exist yet, create it - it'll come along
               // eventually
               previousStream = new Stream();
               streams.put(previousName, previousStream);
            }
            previousStream.getFollowingStreams().add(stream);
         } else {
            this.firstStream = stream;
         }

         // Get operators for current stream.
         for (final OperatorType operatorT : streamT.getOperators()) {
            final OperatorSpecification operator = this.parseOperator(operatorT);
            stream.getOperators().add(operator);
         }
      }
      // In case we miss a stream segment without a preceding stream segment: fail
      if (this.firstStream == null) {
         throw new IllegalArgumentException("There is no beginning for the plan in the plan description.");
      }
   }

   /**
    * Creates the query plan of the XML file of this XMLReader object using the information parsed beforehand.
    *
    * @return the created PhysicalQueryPlan object
    */
   public PhysicalQueryPlan createPlan() {
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      this.createPlan(plan, this.firstStream);
      return plan;
   }

   /**
    * Creates the query plan of this XMLReader object using information parsed beforehand.
    *
    * @param plan
    *           the PhysicalQueryPlan to use for the actual query plan
    * @param stream
    *           the stream segment that is currently to process
    */
   private void createPlan(final PhysicalQueryPlan plan, final Stream stream) {
      this.createPlan(plan, stream, null);
   }

   /**
    * Creates the query plan of this XMLReader object using information parsed beforehand.
    *
    * @param plan
    *           the PhysicalQueryPlan to use for the actual query plan
    * @param stream
    *           the stream segment that is currently to process
    * @param previousStreamOperator
    *           the last operator from the previous stream segment, or, if missing, <code>null</code>
    */
   private void createPlan(final PhysicalQueryPlan plan, final Stream stream,
         final Operator previousStreamOperator) {
      Operator previousOperator = previousStreamOperator;
      for (int i = 0; i < stream.getOperators().size(); i++) {
         final OperatorSpecification opSpec = stream.getOperators().get(i);
         // Determine input schema
         final Schema inputSchema;
         if (previousOperator == null) {
            final String schemaName = opSpec.get("schema").get("name").getString();
            inputSchema = this.schemas.get(schemaName);
         } else {
            inputSchema = previousOperator.getOutputSchema();
         }

         // Treat special case multiplex
         if (opSpec.getOperatorType().equals(Multiplex.class)) {
            final Parameter param = new Parameter("arity",
                  String.valueOf(stream.getFollowingStreams().size()));
            opSpec.put("arity", param);
         }

         // Create operator
         final Operator operator = this.getOperatorInstance(opSpec, inputSchema);

         // Add operator to the plan
         if (previousOperator == null) {
            plan.addOperator(operator, PhysicalQueryPlan.OperatorType.SOURCE);
         } else if (i == stream.getOperators().size() - 1 && stream.getFollowingStreams().size() == 0) {
            plan.addOperator(operator, PhysicalQueryPlan.OperatorType.SINK);
         } else {
            plan.addOperator(operator);
         }
         if (previousOperator != null) {
            plan.addStream(previousOperator, operator);
         }

         previousOperator = operator;
      }

      // Process following stream segments
      for (final Stream nextStream : stream.getFollowingStreams()) {
         this.createPlan(plan, nextStream, previousOperator);
      }
   }

   /**
    * Creates a new instance of the operator specified by <i>op</i>, using <i>inputSchema</i> as input schema.
    *
    * @param op
    *           the operator specification to create an instance from
    * @param inputSchema
    *           the input schema to use for the new operator
    * @return a new operator according to the given specifications
    */
   private Operator getOperatorInstance(final OperatorSpecification op, final Schema inputSchema) {
      final Class< ? > opClass = op.getOperatorType();

      // If simple (non-nested) parameters
      boolean isSimple = true;
      for (final Parameter param : op.getList()) {
         if (!param.hasString()) {
            if (!param.getName().equalsIgnoreCase("schema")) {
               isSimple = false;
               break;
            }
         }
      }
      Operator operatorInstance = null;
      if (isSimple) {
         // Build parameter sets for search for correct constructor
         final HashSet<String> operatorParamNames = new HashSet<>(op.getMap().keySet());
         operatorParamNames.add("inputSchema");
         operatorParamNames.add("operatorId");

         final PriorityQueue<Constructor< ? >> constructors = new PriorityQueue<>(
               new Comparator<Constructor< ? >>() {

                  @Override
                  public int compare(final Constructor< ? > o1, final Constructor< ? > o2) {
                     return -(o1.getParameterCount() - o2.getParameterCount());
                  }
               });

         // Check all constructors
         for (final Constructor< ? > constr : opClass.getConstructors()) {
            // Build parameter set for this constructor
            final HashSet<String> constrParamNames = new HashSet<>();
            for (final java.lang.reflect.Parameter param : constr.getParameters()) {
               constrParamNames.add(param.getName());
            }
            // Check constructor and add to list of candidates
            if (operatorParamNames.containsAll(constrParamNames)) {
               constructors.add(constr);
            }
         }

         // Try all candidates, starting with the one with the highest parameter count
         for (final Constructor< ? > constr : constructors) {
            try {
               final Object[] objects = this.createConstructorParameterObjects(op, inputSchema, constr);
               final Object instance = constr.newInstance(objects);
               operatorInstance = (Operator) instance;
               break;
            } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                  | InvocationTargetException | SecurityException e) {
               // If this fails, just try with the next one.
            }
         }
      }

      // If no suitable constructor is found: try factory
      if (operatorInstance == null) {
         try {
            // Search for factory
            Class< ? > factoryClass = null;
            for (final Class< ? > subClass : op.getOperatorType().getDeclaredClasses()) {
               for (final Class< ? > potFactoryInterface : subClass.getInterfaces()) {
                  if (potFactoryInterface.getName().equals("niagarino.plan.PlanOperatorByParametersFactory")) {
                     factoryClass = subClass;
                     break;
                  }
               }
            }
            if (factoryClass == null) {
               throw new UnsupportedOperationException("Could not find a factory for operator "
                     + op.getOperatorType().getCanonicalName()
                     + " (interface niagarino.plan.PlanOperatorByParametersFactory).");
            }

            final PlanOperatorByParametersFactory factory = (PlanOperatorByParametersFactory) factoryClass
                  .newInstance();
            operatorInstance = factory.getOperatorByParameters(op.getName(), inputSchema, op);
         } catch (final Exception e) {
            // If factory fails as well: give up
            if (e instanceof UnsupportedOperationException) {
               throw (UnsupportedOperationException) e;
            } else {
               throw new UnsupportedOperationException("Operator " + op.getOperatorType().getCanonicalName()
                     + " does not comply to the niagarino query plan rules.", e);
            }
         }
      }
      return operatorInstance;
   }

   /**
    * Returns an Object array containing parameters for the given constructor in the right order and with the
    * right data type. The data is taken from the given operator specification and the given schema.
    *
    * @param operator
    *           the operator specification from which to take the data from
    * @param inputSchema
    *           the input schema to use in the Object array
    * @param constr
    *           the constructor to whose parameters the Object array has to comply
    * @return the Object array to use as parameters for the given constructor
    */
   private Object[] createConstructorParameterObjects(final OperatorSpecification operator,
         final Schema inputSchema, final Constructor< ? > constr) {
      final ArrayList<Object> params = new ArrayList<>();
      for (final java.lang.reflect.Parameter p : constr.getParameters()) {
         // First a few special cases
         if (p.getName().equalsIgnoreCase("operatorId")) {
            params.add(operator.getName());
         } else if (Schema.class.equals(p.getType())) {
            params.add(inputSchema);
         } else {
            // Then the generic cases
            final String value = operator.get(p.getName()).getString();
            final Class< ? > pType = p.getType();
            if (pType.equals(Integer.class) || pType.equals(Integer.TYPE)) {
               params.add(Integer.parseInt(value));
            } else if (pType.equals(Long.class) || pType.equals(Long.TYPE)) {
               params.add(Long.parseLong(value));
            } else if (pType.equals(Double.class) || pType.equals(Double.TYPE)) {
               params.add(Double.parseDouble(value));
            } else if (pType.equals(Boolean.class) || pType.equals(Boolean.TYPE)) {
               params.add(Boolean.parseBoolean(value));
            } else {
               params.add(value);
            }
         }
      }
      return params.toArray(new Object[params.size()]);
   }

   /**
    * Converts a TypeListType object to a Schema object.
    *
    * @param typeList
    *           the TypeListType object to convert
    * @return the new Schema object
    */
   private Schema createSchema(final TypeListType typeList) {
      final List<Attribute> attributes = new ArrayList<>();
      int progressing = -1;
      int counter = 0;
      for (final JAXBElement<ValueTypeType> singleType : typeList.getTypes()) {
         final String name = singleType.getValue().getName();
         final Class< ? > classType;
         switch (singleType.getName().getLocalPart()) {
            case "int":
               classType = Integer.class;
               break;
            case "double":
               classType = Double.class;
               break;
            case "string":
            default:
               classType = String.class;
               break;
         }
         attributes.add(new Attribute(name, classType));
         if (singleType.getValue().isProgressing() != null && singleType.getValue().isProgressing()) {
            progressing = counter;
         }
         counter++;
      }
      return new Schema(Math.max(progressing, 0), attributes.toArray(new Attribute[attributes.size()]));
   }

   /**
    * Parses the given OperatorType obtained from XML and creates an OperatorSpecification object from it.
    *
    * @param operatorObject
    *           the OperatorType object to take the data from
    * @return an OperatorSpecification object describing the given OperatorType object's operator
    * @throws ClassNotFoundException
    *            if the given OperatorType specifies a non-existent operator
    */
   private OperatorSpecification parseOperator(final OperatorType operatorObject)
         throws ClassNotFoundException {
      OperatorSpecification operator;
      if (operatorObject instanceof GenericOperatorType) {
         final Class< ? > operatorType = Class.forName(((GenericOperatorType) operatorObject).getClassSpec());
         final String operatorName = operatorObject.getName();
         operator = new OperatorSpecification(operatorType, operatorName);

         // Process parameters and store them in a hash map.
         for (final Object element : ((GenericOperatorType) operatorObject).getParameters()) {
            final Parameter param = this.parseParameter((Node) element);
            operator.put(param.getName(), param);
         }
      } else {
         throw new UnsupportedOperationException("Type "
               + ((GenericOperatorType) operatorObject).getClassSpec() + " is not yet supported.");
      }
      return operator;
   }

   /**
    * Creates a Parameter object from the given DOM object.
    *
    * @param node
    *           the DOM object to parse the Parameter information from
    * @return the Parameter object describing specifications taken from the given Node object
    */
   private Parameter parseParameter(final Node node) {
      // Get name and text value of parameter
      final String name = node.getLocalName();
      final String value;
      if (node.getChildNodes().getLength() == 1
            && node.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
         value = node.getChildNodes().item(0).getNodeValue().trim();
      } else {
         value = null;
      }
      final Parameter parameter = new Parameter(name, value);

      // Parse attributes if existent
      final NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
         final String aName = attributes.item(i).getLocalName();
         final String aValue = attributes.item(i).getNodeValue();
         final Parameter attribute = new Parameter(aName, aValue, Parameter.ParameterType.ATTRIBUTE);
         parameter.put(aName, attribute);
      }

      // Parse child elements if existent
      // Each child element goes into a list in case of multiple of the same name
      if (value == null) {
         final NodeList subParams = node.getChildNodes();
         for (int i = 0; i < subParams.getLength(); i++) {
            if (subParams.item(i).getNodeName().equalsIgnoreCase("#text")) {
               continue;
            }
            final Parameter subParam = this.parseParameter(subParams.item(i));
            parameter.put(subParam.getName(), subParam);
         }
      }
      return parameter;
   }

   /**
    * Represents a stream segment and organises the stream segments following this one.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class Stream {

      /** The stream segments following this stream segment. */
      private final Set<Stream> followingStreams;

      /** The operators of this stream segment. */
      private final List<OperatorSpecification> operators;

      /**
       * Constructs a new empty Stream segment.
       */
      public Stream() {
         this.followingStreams = new HashSet<>();
         this.operators = new ArrayList<>();
      }

      /**
       * Returns the set of stream segments following this one.
       *
       * @return a set of Stream objects
       */
      public Set<Stream> getFollowingStreams() {
         return this.followingStreams;
      }

      /**
       * Returns the specification of all operators of this stream segment.
       *
       * @return a list of the operator specifications
       */
      public List<OperatorSpecification> getOperators() {
         return this.operators;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String toString() {
         return this.operators.toString();
      }
   }

   /**
    * Contains all information regarding a single operator in the query plan.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class OperatorSpecification extends Parameter {

      /** The class of this operator. */
      private final Class< ? > type;

      /**
       * Creates a new OperatorSpecification object describing an operator with the given class and name.
       *
       * @param type
       *           the class of the operator described by this OperatorSpecification
       * @param name
       *           the name of the operator
       */
      public OperatorSpecification(final Class< ? > type, final String name) {
         super(name);
         this.type = type;
      }

      /**
       * Returns the name of this operator.
       *
       * @return the name
       */
      @Override
      public String getName() {
         return super.getName();
      }

      /**
       * Returns the class used by this operator.
       *
       * @return the class of this operator
       */
      public Class< ? > getOperatorType() {
         return this.type;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String toString() {
         return "\"" + super.getName() + "\": " + super.toString();
      }
   }
}
