/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
