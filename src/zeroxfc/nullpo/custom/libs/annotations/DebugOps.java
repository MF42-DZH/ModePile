package zeroxfc.nullpo.custom.libs.annotations;

import org.apache.log4j.Logger;

/**
 * Operations which streamline the use of {@link Debug}.
 */
public final class DebugOps {
    /** If the passed in object is annotated with {@code Debug}, perform an action. */
    public static <T> void ifDebug(T object, Runnable action) {
        if (object.getClass().isAnnotationPresent(Debug.class)) action.run();
    }

    /** Trace an object if the context object is annotated with {@code Debug} */
    public static <C, T> T trace(Logger log, C object, T toTrace) {
        ifDebug(object, () -> log.trace(toTrace.toString()));
        return toTrace;
    }

    // Don't need to instantiate this class.
    private DebugOps() {}
}
