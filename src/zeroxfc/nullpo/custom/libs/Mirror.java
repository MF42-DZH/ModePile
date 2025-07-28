package zeroxfc.nullpo.custom.libs;

import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.log4j.Logger;

/**
 * The {@code Mirror} helps in providing cleaner, less verbose access to Java's reflection API.
 * It handles most errors in attempting to gain access to fields or methods via reflection.
 */
public class Mirror {
    private static final Logger log = Logger.getLogger(Mirror.class);

    // This class does not need instantiation.
    private Mirror() {}

    private static <T> T handleErrors(ReflectiveCallable<T> task) {
        try {
            return task.call();
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            log.error("Inner member does not exist:");
            log.error(e);
        } catch (SecurityException e) {
            log.error("Encountered security exception:");
            log.error(e);
        } catch (ClassCastException | IllegalArgumentException e) {
            log.error("Wrong type:");
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error("Failed to make field accessible, so illegal access performed:");
            log.error(e);
        } catch (NullPointerException e) {
            log.error("Null value caught:");
            log.error(e);
        } catch (InvocationTargetException e) {
            log.error("Cannot invoke method on target:");
            log.error(e);
        } catch (Exception e) {
            log.error("Other exception occurred:");
            log.error(e);
        }

        return null;
    }

    private static void handleErrorsVoid(ReflectiveRunnable task) {
        try {
            task.run();
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            log.error("Inner member does not exist:");
            log.error(e);
        } catch (SecurityException e) {
            log.error("Encountered security exception:");
            log.error(e);
        } catch (ClassCastException | IllegalArgumentException e) {
            log.error("Wrong type:");
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error("Failed to make field accessible, so illegal access performed:");
            log.error(e);
        } catch (NullPointerException e) {
            log.error("Null value caught:");
            log.error(e);
        } catch (InvocationTargetException e) {
            log.error("Cannot invoke method on target:");
            log.error(e);
        } catch (Exception e) {
            log.error("Other exception occurred:");
            log.error(e);
        }
    }

    // For primitives:
    public static <T> byte getByte(Class<T> targetClass, T target, String fieldName) {
        final Byte value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getByte(target);
        });

        return value == null ? 0 : value;
    }

    public static <T> void setByte(Class<T> targetClass, T target, String fieldName, byte newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setByte(target, newValue);
        });
    }

    public static <T> short getShort(Class<T> targetClass, T target, String fieldName) {
        final Short value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getShort(target);
        });

        return value == null ? 0 : value;
    }

    public static <T> void setShort(Class<T> targetClass, T target, String fieldName, short newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setShort(target, newValue);
        });
    }

    public static <T> int getInt(Class<T> targetClass, T target, String fieldName) {
        final Integer value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getInt(target);
        });

        return value == null ? 0 : value;
    }

    public static <T> void setInt(Class<T> targetClass, T target, String fieldName, int newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setInt(target, newValue);
        });
    }

    public static <T> long getLong(Class<T> targetClass, T target, String fieldName) {
        final Long value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getLong(target);
        });

        return value == null ? 0 : value;
    }

    public static <T> void setLong(Class<T> targetClass, T target, String fieldName, long newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setLong(target, newValue);
        });
    }

    public static <T> char getChar(Class<T> targetClass, T target, String fieldName) {
        final Character value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getChar(target);
        });

        return value == null ? '\0' : value;
    }

    public static <T> void setChar(Class<T> targetClass, T target, String fieldName, char newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setChar(target, newValue);
        });
    }

    public static <T> boolean getBoolean(Class<T> targetClass, T target, String fieldName) {
        final Boolean value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getBoolean(target);
        });

        return value == null ? false : value;
    }

    public static <T> void setBoolean(Class<T> targetClass, T target, String fieldName, boolean newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setBoolean(target, newValue);
        });
    }

    public static <T> float getFloat(Class<T> targetClass, T target, String fieldName) {
        final Float value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getFloat(target);
        });

        return value == null ? 0f : value;
    }

    public static <T> void setFloat(Class<T> targetClass, T target, String fieldName, float newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setFloat(target, newValue);
        });
    }

    public static <T> double getDouble(Class<T> targetClass, T target, String fieldName) {
        final Double value = handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.getDouble(target);
        });

        return value == null ? 0d : value;
    }

    public static <T> void setDouble(Class<T> targetClass, T target, String fieldName, double newValue) {
        handleErrorsVoid(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.setDouble(target, newValue);
        });
    }

    // For objects and references:
    /** Safely-wrapped field accessor class for reflective gets and sets of the fields of a class. */
    public static class FieldAccessor<T, R> {
        private static final Map<FieldAccessor<?, ?>, WeakReference<FieldAccessor<?, ?>>> ACCESSORS = new WeakHashMap<>();

        private final Field field;

        private FieldAccessor(Field field) {
            this.field = field;
        }

        @SuppressWarnings("unchecked")
        private static <T, R> FieldAccessor<T, R> getAccessor(Field field) {
            final FieldAccessor<T, R> accessor = new FieldAccessor<>(field);
            final WeakReference<FieldAccessor<?, ?>> ref = ACCESSORS.get(accessor);

            if (ref != null) {
                final FieldAccessor<?, ?> cached = ref.get();
                if (cached != null) return (FieldAccessor<T, R>) cached;
            }

            ACCESSORS.put(accessor, new WeakReference<>(accessor));
            return accessor;
        }

        /**
         * Get the current value of a field in a target instance.
         *
         * @param target Target instance (may be {@code null} if method is {@code static})
         * @return Current value of field
         */
        @SuppressWarnings("unchecked")
        public R get(T target) {
            try {
                final Object ret = field.get(target);

                if (ret == null) return null;
                return (R) ret;
            } catch (SecurityException e) {
                log.error("Encountered security exception:");
                log.error(e);
            } catch (ClassCastException | IllegalArgumentException e) {
                log.error("Wrong type:");
                log.error(e);
            } catch (IllegalAccessException e) {
                log.error("Failed to make field accessible, so illegal access performed:");
                log.error(e);
            } catch (NullPointerException e) {
                log.error("Null value caught:");
                log.error(e);
            } catch (Exception e) {
                log.error("Other exception occurred:");
                log.error(e);
            }

            return null;
        }

        /**
         * Set the current value of a field in a target instance.
         *
         * @param target   Target instance (may be {@code null} if method is {@code static})
         * @param newValue New value to put in field
         */
        @SuppressWarnings("unchecked")
        public void set(T target, R newValue) {
            try {
                field.set(target, newValue);
            } catch (SecurityException e) {
                log.error("Encountered security exception:");
                log.error(e);
            } catch (ClassCastException | IllegalArgumentException e) {
                log.error("Wrong type:");
                log.error(e);
            } catch (IllegalAccessException e) {
                log.error("Failed to make field accessible, so illegal access performed:");
                log.error(e);
            } catch (NullPointerException e) {
                log.error("Null value caught:");
                log.error(e);
            } catch (Exception e) {
                log.error("Other exception occurred:");
                log.error(e);
            }
        }

        @Override
        public int hashCode() {
            // Every different method has a unique hash code.
            return field.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FieldAccessor<?, ?>)) return false;
            return field.equals(((FieldAccessor<?, ?>) obj).field);
        }
    }

    /**
     * Get a field accessor for reflective access to a field.
     *
     * @param targetClass    Target class to reflect
     * @param fieldName      Name of field to extract
     * @return               Field accessor wrapper for safely interacting with the field
     */
    @SuppressWarnings("unchecked")
    public static <U, T extends U, F> FieldAccessor<U, F> getFieldAccessor(Class<T> targetClass, String fieldName) {
        return handleErrors(() -> {
            final Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return FieldAccessor.getAccessor(field);
        });
    }

    /** Safely-wrapped method invoker class for reflective invocations of methods of a class. */
    public static class MethodInvoker<T, R> {
        private static final Map<MethodInvoker<?, ?>, WeakReference<MethodInvoker<?, ?>>> INVOKERS = new WeakHashMap<>();

        private final Method method;

        private MethodInvoker(Method method) {
            this.method = method;
        }

        @SuppressWarnings("unchecked")
        private static <T, R> MethodInvoker<T, R> getInvoker(Method method) {
            final MethodInvoker<T, R> invoker = new MethodInvoker<>(method);
            final WeakReference<MethodInvoker<?, ?>> ref = INVOKERS.get(invoker);

            if (ref != null) {
                final MethodInvoker<?, ?> cached = ref.get();
                if (cached != null) return (MethodInvoker<T, R>) cached;
            }

            INVOKERS.put(invoker, new WeakReference<>(invoker));
            return invoker;
        }

        /**
         * Invoke a reflected method on an object instance with some particular arguments.
         *
         * @param target Target instance (may be {@code null} if method is {@code static})
         * @param args   Arguments to invoke with (MUST MATCH METHOD SIGNATURE)
         * @return       Return value of method, if applicable
         */
        @SuppressWarnings("unchecked")
        public R invoke(T target, Object... args) {
            try {
                final Object ret = method.invoke(target, args);

                if (ret == null) return null;
                return (R) ret;
            } catch (SecurityException e) {
                log.error("Encountered security exception:");
                log.error(e);
            } catch (ClassCastException | IllegalArgumentException e) {
                log.error("Wrong type:");
                log.error(e);
            } catch (IllegalAccessException e) {
                log.error("Failed to make field accessible, so illegal access performed:");
                log.error(e);
            } catch (NullPointerException e) {
                log.error("Null value caught:");
                log.error(e);
            } catch (InvocationTargetException e) {
                log.error("Cannot invoke method on target:");
                log.error(e);
            } catch (Exception e) {
                log.error("Other exception occurred:");
                log.error(e);
            }

            return null;
        }

        @Override
        public int hashCode() {
            // Every different method has a unique hash code.
            return method.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodInvoker<?, ?>)) return false;
            return method.equals(((MethodInvoker<?, ?>) obj).method);
        }
    }

    /**
     * Access a method, and create an invoker wrapper for it.
     *
     * @param targetClass Target class to reflect
     * @param methodName  Name of method to get
     * @param argTypes    Argument types of method
     * @return Method invoker wrapper for safely invoking the reflected method.
     * @param <T> Target Type
     * @param <R> Method Return Type
     */
    public static <U, T extends U, R> MethodInvoker<U, R> getMethodInvoker(Class<T> targetClass, String methodName, Class<?>... argTypes) {
        return handleErrors(() -> {
            final Method method = targetClass.getDeclaredMethod(methodName, argTypes);
            method.setAccessible(true);

            return MethodInvoker.getInvoker(method);
        });
    }

    @FunctionalInterface
    private interface ReflectiveRunnable {
        void run() throws NoSuchFieldException, NoSuchMethodException, SecurityException, ClassCastException, IllegalArgumentException, IllegalAccessException, NullPointerException, InvocationTargetException;
    }

    @FunctionalInterface
    private interface ReflectiveCallable<T> {
        T call() throws NoSuchFieldException, NoSuchMethodException, SecurityException, ClassCastException, IllegalArgumentException, IllegalAccessException, NullPointerException, InvocationTargetException;
    }
}
