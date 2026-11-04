package co.casterlabs.flv4j.documentation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The method copies its data into freshly-allocated storage; the result does
 * not alias the receiver's backing buffer (no view/slice). This can have
 * performance implications depending on the amount of data being copied.
 */
@Documented
@Target(ElementType.METHOD)
public @interface CreatesCopy {

}
