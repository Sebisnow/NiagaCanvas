package niagacanvas.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author sebastian
 * 
 *         This is used to annotate the static method of a Niagarino Operator to
 *         return all the parameters needed for instantiation. This static
 *         method has to return an array of ParamDescription. The number of
 *         Parameters needed can be specified in brackets as well:
 *         "@GetParamDesc(numberOfParams = 2)"
 *
 */

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface GetParamDesc {
	// if the number of parameters needed to instantiate this operator is not
	// needed it can be removed, so far it is unused
	int numberOfParams();

}
