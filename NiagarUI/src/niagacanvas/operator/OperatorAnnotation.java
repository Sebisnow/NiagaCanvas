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
 *         This is used to annotate the NON - static method of a Niagarino
 *         Operator to take an Operator instance to hand back the parameters.
 *
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface OperatorAnnotation {

}
