package zeroxfc.nullpo.custom.libs.annotations;

import java.lang.annotation.*;

/**
 * Annotation used for marking that an object instance contains debug code.
 * Due to the way Java annotations work, the usage of the annotation looks something like:
 *
 * <pre>
 *     final Object foo = new @Debug Object() {};
 *     foo.getClass().isAnnotationPresent(Debug.class); // true</pre>
 *
 * Which does create an anonymous class instance of the type you are trying to debug.
 * This means the {@code @Debug} annotation <b>WILL NOT WORK</b> with {@code final}
 * classes, as you will not be able to make an anonymous instance of that class.
 * <p>
 * Use {@link DebugOps} for helpers for using this annotation, and ensure to remove all
 * instances of this annotation being used before making a public release!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.TYPE_USE })
public @interface Debug {}
