/*
 * @(#)Parameter.java   1.0   Jan 14, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Describes a parameter for an operator.
 * 
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class Parameter {

   /**
    * Describes different types of parameters.
    * 
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   enum ParameterType {
      /** The parameter is a list of values. */
      LIST,
      /** The parameter is an attribute of another parameter. */
      ATTRIBUTE,
      /** The parameter is a default parameter. */
      DEFAULT;
   }

   /** The name of this parameter. */
   private final String name;

   /** The value of this parameter, if existent. */
   private final String value;

   /** A list of values of this parameter, if it is a list. */
   private final List<Parameter> valueList;

   /** A map of parameters contained by this parameter. */
   private final HashMap<String, Parameter> valueMap;

   /** The type of this parameter. */
   private final Parameter.ParameterType type;

   /**
    * Constructs a new Parameter object with the given name, the given value and the given type.
    * 
    * @param name
    *           the parameter name
    * @param value
    *           the parameter value
    * @param type
    *           the parameter type
    */
   Parameter(final String name, final String value, final Parameter.ParameterType type) {
      this.name = name;
      this.value = value;
      this.valueList = new ArrayList<>();
      this.valueMap = new HashMap<>();
      this.type = type;
   }

   /**
    * Constructs a new Parameter object with the given name, the given value and the default type.
    * 
    * @param name
    *           the parameter name
    * @param value
    *           the parameter value
    */
   Parameter(final String name, final String value) {
      this(name, value, Parameter.ParameterType.DEFAULT);
   }

   /**
    * Constructs a new Parameter object with the given name, no value and the given type.
    * 
    * @param name
    *           the parameter name
    * @param type
    *           the parameter type
    */
   Parameter(final String name, final Parameter.ParameterType type) {
      this(name, null, type);
   }

   /**
    * Constructs a new Parameter object with the given name, no value and the default type.
    * 
    * @param name
    *           the parameter name
    */
   Parameter(final String name) {
      this(name, null, Parameter.ParameterType.DEFAULT);
   }

   /**
    * Returns the name of this parameter.
    * 
    * @return the name
    */
   public String getName() {
      return this.name;
   }

   /**
    * Returns whether this parameter has a string value.
    * 
    * @return <code>true</code> if this parameter contains a string value, <code>false</code> otherwise
    */
   public boolean hasString() {
      return this.value != null
            || this.type == ParameterType.LIST && this.valueList.get(0).hasString();
   }

   /**
    * Returns the string value of this parameter.
    * 
    * @return the string value, or <code>null</code> if there is none
    */
   public String getString() {
      if (this.type == ParameterType.LIST) {
         return this.valueList.get(0).getString();
      } else {
         return this.value;
      }
   }

   /**
    * Inserts a parameter with the given name.
    * 
    * @param name
    *           the name of the parameter
    * @param parameter
    *           the parameter to insert
    */
   public void put(final String name, final Parameter parameter) {
      if (ParameterType.ATTRIBUTE.equals(parameter.getType())) {
         this.valueMap.put(name, parameter);
      } else {
         Parameter wrapParam = this.valueMap.get(name);
         if (wrapParam == null) {
            wrapParam = new Parameter(name, ParameterType.LIST);
            this.valueMap.put(name, wrapParam);
         }
         wrapParam.add(parameter);
      }
      this.valueList.add(parameter);
   }

   /**
    * Inserts a parameter into the list of this parameter.
    * 
    * @param parameter
    *           the parameter to insert
    */
   public void add(final Parameter parameter) {
      this.valueList.add(parameter);
   }

   /**
    * Returns the parameter with the given name.
    * 
    * @param name
    *           the name of the parameter to return
    * @return the requested Parameter object
    */
   public Parameter get(final String name) {
      if (this.type == ParameterType.LIST) {
         return this.valueList.get(0).get(name);
      } else {
         return this.valueMap.get(name);
      }
   }

   /**
    * Returns the parameter at the given position.
    * 
    * @param index
    *           the position of the parameter
    * @return the requested Parameter object
    */
   public Parameter get(final int index) {
      return this.valueList.get(index);
   }

   /**
    * Returns an iterator over the parameter list contained by this Parameter.
    * 
    * @return the iterator
    */
   public Iterator<Parameter> getListIterator() {
      return this.valueList.iterator();
   }

   /**
    * Returns the type of this Parameter.
    * 
    * @return the type
    */
   public Parameter.ParameterType getType() {
      return this.type;
   }

   /**
    * Returns the parameter list contained by this Parameter.
    * 
    * @return the list
    */
   public List<Parameter> getList() {
      return this.valueList;
   }

   /**
    * Returns the map of parameters contained by this Parameter.
    * 
    * @return the map
    */
   public HashMap<String, Parameter> getMap() {
      if (this.type == ParameterType.LIST) {
         return this.valueList.get(0).getMap();
      } else {
         return this.valueMap;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      if (this.type == Parameter.ParameterType.LIST) {
         return this.valueList.toString();
      } else if (this.valueMap.size() == 0) {
         return this.value;
      } else {
         return this.valueMap.toString();
      }
   }
}
