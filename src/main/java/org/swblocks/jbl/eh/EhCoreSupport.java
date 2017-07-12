/*
 * This file is part of the swblocks-jbl library.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.swblocks.jbl.eh;

import java.io.Closeable;
import java.util.function.Consumer;

import org.swblocks.jbl.functional.ThrowsConsumer;
import org.swblocks.jbl.functional.ThrowsRunnable;
import org.swblocks.jbl.functional.ThrowsSupplier;

/**
 * A static class for basic exception core support helpers.
 */
class EhCoreSupport {
    public static final Consumer<Throwable> defaultAbortHandler = throwable -> {
        throw new FatalApplicationError("A fatal application error has occurred", throwable);
    };
    /**
     * This is the rethrow handler to be used by the catch blocks below.
     *
     * <p>The derived class {@link EhSupport} can change it to something different which can allow more enhanced
     * behavior to be provided there (e.g. invoking throwEnhanced)
     */
    protected static Consumer<Throwable> throwHandler = EhCoreSupport::<RuntimeException>rethrowInternal;

    protected static final Thread.UncaughtExceptionHandler defaultUncaughtHandler = (thread, exception) -> {
        System.err.print("Uncaught exception in thread \"" + thread.getName() + "\" ");
        exception.printStackTrace(System.err);
        Runtime.getRuntime().halt(1);
    };

    protected static Thread.UncaughtExceptionHandler uncaughtHandler = defaultUncaughtHandler;

    private static final Thread.UncaughtExceptionHandler uncaughtHandlerLocalCallback =
            (thread, exception) -> uncaughtHandler.uncaughtException(thread, exception);

    static {
        Thread.setDefaultUncaughtExceptionHandler(uncaughtHandlerLocalCallback);
        Thread.currentThread().setUncaughtExceptionHandler(uncaughtHandlerLocalCallback);
    }

    /*
     * Consumers of JBL can provide an implementation of this handler
     * that logs the exception details and then terminates the application
     *
     * The default handler (above) simply throws an Error type exception
     * that should (hopefully) result in termination; an alternative and
     * more robust default implementation would be to simply call
     * System.exit(-1)
     */
    private static Consumer<Throwable> abortHandler = defaultAbortHandler;

    /**
     * Declare the constructor protected to enforce the class is static, but allow for derived extensions.
     */
    protected EhCoreSupport() {
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Throwable> void rethrowInternal(final Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Rethrow an {@link java.lang.Throwable} as is, but making it unchecked.
     *
     * <p>Implements the 'sneaky throw' pattern to allow us to deal sensibly with checked exceptions.
     *
     * <p>For more details see here: https://www.google.com/#q=java+sneaky+throw
     *
     * @param throwable to be rethrown and unchecked.
     */
    public static void rethrow(final Throwable throwable) {
        throwHandler.accept(throwable);
    }

    /**
     * Executes a callback that throws exceptions and if exceptions are raised they get propagated as is, but all
     * exceptions are propagated as unchecked exceptions.
     *
     * @param callback The callback to execute
     */
    public static void propagate(final ThrowsRunnable callback) {
        try {
            callback.run();
        } catch (final Throwable throwable) {
            rethrow(throwable);
        }
    }

    /**
     * Executes a supplier callback that throws exceptions and if exceptions are raised they get propagated as is, but
     * all exceptions are propagated as unchecked exceptions.
     *
     * @param <R>      The return type
     * @param callback The supplier callback to execute
     * @return The value returned by the callback
     */
    public static <R> R propagateFn(final ThrowsSupplier<R> callback) {
        try {
            return callback.get();
        } catch (final Throwable throwable) {
            rethrow(throwable);
        }

        throw new IllegalStateException("EhCoreSupport.rethrow must always return or throw an exception");
    }

    /**
     * Executes a callback that is expected to not throw an exception and if exception is thrown a fatal handler is
     * invoked (which by default will throw a non-recoverable exception).
     *
     * @param callback The callback to execute
     */
    public static void fatalOnException(final ThrowsRunnable callback) {
        try {
            callback.run();
        } catch (final Throwable throwable) {
            abortHandler.accept(throwable);
        }
    }

    /**
     * Executes a supplier callback that is expected to not throw an exception and if exception is thrown a fatal
     * handler is invoked (which by default will throw a non-recoverable exception).
     *
     * @param <R>      The return type
     * @param callback The supplier callback to execute
     * @return The value returned by the callback
     */
    public static <R> R fatalOnExceptionFn(final ThrowsSupplier<R> callback) {
        try {
            return callback.get();
        } catch (final Throwable throwable) {
            abortHandler.accept(throwable);
        }

        throw new IllegalStateException("EhCoreSupport.fatalOnExceptionFn must always return or throw an exception");
    }

    /**
     * Checks that the expected condition is satisfied otherwise will throw {@link IllegalStateException}.
     *
     * @param expectedCondition the provided parameter condition
     * @param message           the error message to display id the  parameter condition is not satisfied
     * @param args              list of args provided for formatting the message
     */
    public static void ensure(final boolean expectedCondition, final String message, final Object... args) {
        if (!expectedCondition) {
            rethrow(new IllegalStateException(String.format(message, args)));
        }
    }

    /**
     * Checks that the expected parameter condition is satisfied or will throw {@link IllegalArgumentException}.
     *
     * @param expectedCondition the provided parameter condition
     * @param message           the error message to display id the  parameter condition is not satisfied
     * @param args              list of args provided for formatting the message
     */
    public static void ensureArg(final boolean expectedCondition, final String message, final Object... args) {
        if (!expectedCondition) {
            rethrow(new IllegalArgumentException(String.format(message, args)));
        }
    }

    /**
     * Checks that the expected condition is satisfied otherwise will ultimately execute a fatal exception handler.
     *
     * @param expectedCondition the condition that must be satisfied
     * @param message           the message that will be passed to the exception handler
     * @param args              list of args that are being checked
     */
    public static void ensureOrFatal(final boolean expectedCondition, final String message, final Object... args) {
        if (!expectedCondition) {
            abort(String.format(message, args));
        }
    }

    /**
     * Checks that the expected condition is satisfied otherwise will ultimately execute a fatal exception handler.
     *
     * @param expectedCondition the condition that must be satisfied
     * @param cause             the exception to add as the cause.
     * @param message           the message that will be passed to the exception handler
     * @param args              list of args that are being checked
     */
    public static void ensureOrFatal(final boolean expectedCondition, final Exception cause,
                                     final String message, final Object... args) {
        if (!expectedCondition) {
            abort(String.format(message, args), cause);
        }
    }

    /**
     * Method that takes a message that will be used by the fatal exception handler wrapped in a {@link
     * java.lang.IllegalStateException} and an {@link Exception} to be added as the Cause.
     *
     * @param message the message that will be used in the fatal exception
     * @param cause   the exception to add as the cause.
     */
    public static void abort(final String message, final Exception cause) {
        abortHandler.accept(new IllegalStateException(message, cause));
    }

    /**
     * Method that takes a message that will be used by the fatal exception handler wrapped in a {@link
     * java.lang.IllegalStateException}.
     *
     * @param message the message that will be used in the fatal exception
     */
    public static void abort(final String message) {
        abortHandler.accept(new IllegalStateException(message));
    }

    /**
     * Returns the current fatal no-throw exception handler for the EH library.
     *
     * @return The current fatal no-throw exception handler for the EH library
     */

    public static Consumer<Throwable> getAbortHandler() {
        return abortHandler;
    }

    /**
     * Sets a new fatal no-throw exception handler for the EH library code
     *
     * @param abortHandler The new fatal no-throw exception handler for the EH library code or null. If null is provided
     *                     then the default handler is used.
     */
    public static void setAbortHandler(final Consumer<Throwable> abortHandler) {
        EhCoreSupport.abortHandler = abortHandler == null ? defaultAbortHandler : abortHandler;
    }

    /**
     * Same as a normal finally block except that the handler will receive the current exception info if one has been
     * thrown. The throwable parameter in the handler is non-null if an exception was thrown and its value will be is
     * null if this was normal / non-exceptional finally execution.
     *
     * @param callback The callback to execute
     * @param handler  The finally handler to execute (with extra exception info provided)
     */
    public static void enhancedFinally(final ThrowsRunnable callback, final Consumer<Throwable> handler) {
        Throwable savedThrowable = null;
        try {
            callback.run();
        } catch (final Throwable throwable) {
            savedThrowable = throwable;
            rethrow(throwable);
        } finally {
            handler.accept(savedThrowable);
        }
    }

    /**
     * Same functionality as {@link #enhancedFinally(ThrowsRunnable, Consumer)} except that it takes a supplier callback
     * which returns value and then this helper returns the same value.
     *
     * @param callback The supplier callback to execute
     * @param handler  The try/finally handler to execute
     * @param <R>      The return type of the supplier callback
     * @return The value returned by the callback
     */
    public static <R> R enhancedFinallyFn(final ThrowsSupplier<R> callback, final Consumer<Throwable> handler) {
        Throwable savedThrowable = null;
        try {
            return callback.get();
        } catch (final Throwable throwable) {
            savedThrowable = throwable;
            rethrow(throwable);
        } finally {
            handler.accept(savedThrowable);
        }

        throw new IllegalStateException("EhCoreSupport.catchOrFinallyFn must always return or throw an exception");
    }

    /**
     * Executes a callback that throws and suppresses all exceptions  if possible (i.e. throwable parameter has
     * exception suppression enabled)
     *
     * @param throwable The throwable to capture the suppressed exception. It can be null (see above).
     * @param callback  The callback to execute
     */
    public static void trySuppressExceptions(final Throwable throwable, final ThrowsRunnable callback) {
        try {
            callback.run();
        } catch (final Throwable suppressed) {
            if (throwable != null) {
                throwable.addSuppressed(suppressed);
            } else {
                rethrow(suppressed);
            }
        }
    }

    /**
     * Convenience wrapper for {@link #trySuppressExceptions(Throwable, ThrowsRunnable)}
     *
     * @param throwable The throwable to capture the suppressed exception. It can be null (see above).
     * @param callback  The callback to execute
     */
    public static void suppressExceptionsOrFatal(final Throwable throwable, final ThrowsRunnable callback) {
        fatalOnException(() -> trySuppressExceptions(throwable, callback));
    }

    /**
     * Tries to close quietly a closable object of type T and throws if the exception thrown by close cannot be
     * suppressed (i.e. throwable is null or it does not support suppression of exceptions)
     *
     * @param throwable     The throwable to capture the suppressed exception. It can be null (see above).
     * @param closeable     The closeable object to be closed
     * @param closeCallback The callback that is responsible for closing the object
     * @param <T>           The type of the closeable object (typically object that implements {@link Closeable})
     */
    public static <T> void tryCloseQuietly(
            final Throwable throwable,
            final T closeable,
            final ThrowsConsumer<T> closeCallback) {
        trySuppressExceptions(throwable, () -> {
            if (closeable != null) {
                closeCallback.accept(closeable);
            }
        });
    }

    /**
     * Convenience wrapper for {@link #tryCloseQuietly(Throwable, Object, ThrowsConsumer)} for objects which implement
     * {@link Closeable} interface
     *
     * @param throwable The throwable to capture the suppressed exception. It can be null (see above).
     * @param closeable The closeable object to be closed
     */
    public static void tryCloseQuietly(final Throwable throwable, final Closeable closeable) {
        tryCloseQuietly(throwable, closeable, Closeable::close);
    }

    /**
     * Convenience wrapper for {@link #tryCloseQuietly(Throwable, Object, ThrowsConsumer)}
     *
     * <p>Tries to close quietly a closable object of type T and aborts if the exception thrown by close cannot be
     * suppressed (i.e. throwable is null or it does not support suppression of exceptions)
     *
     * @param throwable     The throwable to capture the suppressed exception. It can be null (see above).
     * @param closeable     The closeable object to be closed
     * @param closeCallback The callback that is responsible for closing the object
     * @param <T>           The type of the closeable object (typically object that implements {@link Closeable})
     */
    public static <T> void closeQuietlyOrFatal(
            final Throwable throwable,
            final T closeable,
            final ThrowsConsumer<T> closeCallback) {
        fatalOnException(() -> tryCloseQuietly(throwable, closeable, closeCallback));
    }

    /**
     * Convenience wrapper for {@link #closeQuietlyOrFatal(Throwable, Object, ThrowsConsumer)} for objects which
     * implement {@link Closeable} interface
     *
     * @param throwable The throwable to capture the suppressed exception. It can be null (see above).
     * @param closeable The closeable object to be closed
     */
    public static void closeQuietlyOrException(final Throwable throwable, final Closeable closeable) {
        closeQuietlyOrFatal(throwable, closeable, Closeable::close);
    }

    /**
     * Obtains the default uncaught exception handler, which usually prints the stacktrace and terminates the VM
     * with {@link Runtime#halt} - by calling Runtime.getRuntime().halt(1)
     *
     * @return The default uncaught exception handler
     */
    public static Thread.UncaughtExceptionHandler getDefaultUncaughtHandler() {
        return defaultUncaughtHandler;
    }

    /**
     * Obtains the currently installed uncaught exception handler, which if
     * {@link #setUncaughtHandler(Thread.UncaughtExceptionHandler)} is never called is typically the default handler
     * {@link #getDefaultUncaughtHandler()}
     *
     * @return The currently installed uncaught exception handler
     */
    public static Thread.UncaughtExceptionHandler getUncaughtHandler() {
        return uncaughtHandler;
    }

    /**
     * Sets new uncaught exception handler. Typically it is expected to execute only very simple and safe code and
     * then terminate the VM and if null it provided it installs the default such {@link #getDefaultUncaughtHandler()}
     *
     * @param uncaughtHandler The new Handler to be used as the default UncaughtExceptionHandler.
     */
    public static void setUncaughtHandler(final Thread.UncaughtExceptionHandler uncaughtHandler) {
        final Thread.UncaughtExceptionHandler newHandler = (null == uncaughtHandler ? defaultUncaughtHandler : uncaughtHandler);
        EhCoreSupport.uncaughtHandler = newHandler;
        Thread.setDefaultUncaughtExceptionHandler(newHandler);
        Thread.currentThread().setUncaughtExceptionHandler(newHandler);
    }
}
