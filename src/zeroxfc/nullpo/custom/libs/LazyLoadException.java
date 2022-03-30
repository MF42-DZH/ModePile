package zeroxfc.nullpo.custom.libs;

/** Signifies that an exception has occurred while lazy-loading a value. */
public class LazyLoadException extends RuntimeException {

  public LazyLoadException() {}

  public LazyLoadException(String message) {
    super(message);
  }

  public LazyLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public LazyLoadException(Throwable cause) {
    super(cause);
  }
}
