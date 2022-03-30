package zeroxfc.nullpo.custom.libs;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * A wrapper for values which allows the values to be lazy-loaded rather than strictly loaded. The
 * intrinsic memory overhead for this class comes from the fact that it itself takes up space,
 * along with a reference to the internal value supplier The lazy-loaded value can throw
 * exceptions, but those will be wrapped with a special exception, {@link LazyLoadException}.
 *
 * <p>Lazy values are only ever loaded once. Every call of <code>get()</code> after the first will
 * load the same instance of the value.
 *
 * @param <T> Apparent type of value being lazy-loaded.
 */
public class LazyReference<T> {

  private final Callable<? extends T> getter;
  private T value;
  private boolean evaluated = false;

  // No outside instantiation is needed. The static methods perform all of the instantiation.
  private LazyReference(Callable<T> getter) {
    this.getter = getter;
  }

  /**
   * Make a lazy-loaded value from a callable object. In general, once can use a callable object
   * directly, or use a lambda instead:
   *
   * <pre>{@code () -> value
   *
   * or
   *
   * () -> {
   *   ...
   *   return value;
   * }}</pre>
   *
   * @param callable Callable object to load from.
   * @param <T> Apparent type of lazy-loaded value.
   * @return Lazy-loader that loads from the given callable.
   */
  public static <T> LazyReference<T> fromCallable(Callable<? extends T> callable) {
    return new LazyReference<>(callable::call);
  }

  /**
   * Make a lazy-loaded value from a supplier object. In general, once can use a supplier object
   * directly, or use a lambda instead:
   *
   * <pre>{@code () -> value
   *
   * or
   *
   * () -> {
   *   ...
   *   return value;
   * }}</pre>
   *
   * @param supplier Supplier object to load from.
   * @param <T> Apparent type of lazy-loaded value.
   * @return Lazy-loader that loads from the given supplier.
   */
  public static <T> LazyReference<T> fromSupplier(Supplier<? extends T> supplier) {
    return new LazyReference<>(supplier::get);
  }

  /**
   * Lazy-load the value and return it. If an error occurs, a {@link LazyLoadException} will be
   * thrown, with the cause exception nested inside.
   *
   * @return The lazily-loaded value. If this has already been called before, gets the
   * already-loaded value.
   */
  public T getValue() {
    if (value == null) {
      evaluated = true;

      try {
        value = getter.call();
      } catch (Exception e) {
        throw new LazyLoadException("Exception when lazy-loading object.", e);
      }
    }

    return value;
  }

  /** Utility for checking if the object has already been evaluated and loaded. */
  public boolean isEvaluated() {
    return evaluated;
  }
}
