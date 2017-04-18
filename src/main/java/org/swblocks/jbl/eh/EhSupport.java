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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

import org.swblocks.jbl.functional.ThrowsRunnable;
import org.swblocks.jbl.functional.ThrowsSupplier;

/**
 * A static class for EH support helpers.
 */
public final class EhSupport extends EhCoreSupport {
    static {
        /*
         * Configure a more advanced throw handler in EhCoreSupport to ensure
         * that any exception that is thrown or rethrown via EhSupport will
         * be enhanced immediately (e.g. to capture time thrown, etc)
         */
        throwHandler = throwable -> EhCoreSupport.<RuntimeException>rethrowInternal(enhance(throwable));
    }

    /**
     * Declare the constructor private to enforce the class is static and final.
     */
    private EhSupport() {
        super();
    }

    /**
     * Obtains a properties exception associated with this exception.
     *
     * @param exception The exception to get the properties exception for
     * @return The properties exception
     */
    public static BaseException getPropertiesException(final Throwable exception) {
        if (exception instanceof BaseException) {
            return (BaseException) exception;
        }

        Throwable search = exception;
        Throwable cause = search.getCause();

        while (cause != null && !(cause instanceof BaseException)) {
            search = cause;
            cause = search.getCause();
        }

        if (cause != null) {
            /*
             * A properties capable exception already exist in the chain
             */

            return (BaseException) cause;
        }

        /**
         * If we are here that means we have traversed the entire chain
         * and did not find properties exception.
         *
         * <p>Let's bootstrap {@link PropertiesHolderException} at the end of the chain
         */

        final PropertiesHolderException result = new PropertiesHolderException(exception);
        search.initCause(result);
        return result;
    }

    /**
     * Enhance an exception if it is not already enhanced and return it.
     *
     * @param exception The exception to be enhanced
     * @return Same as the input exception
     */
    public static Throwable enhance(final Throwable exception) {
        getPropertiesException(exception);
        return exception;
    }

    /**
     * Enhance an exception if it is not already enhanced, add a new property and then return it.
     *
     * @param exception The exception to be enhanced
     * @param name      The name of the property
     * @param value     The value of the property
     * @return Same as the input exception
     */
    public static Throwable enhance(final Throwable exception, final String name, final Object value) {
        getPropertiesException(exception).putProperty(name, value);
        return exception;
    }

    /**
     * Enhance an exception if it is not already enhanced and rethrow it.
     *
     * @param exception The exception to be enhanced
     */
    public static void throwEnhanced(final Throwable exception) {
        rethrow(enhance(exception));
    }

    /**
     * Enhance an exception if it is not already enhanced, add a new property and then rethrow it.
     *
     * @param exception The exception to be enhanced
     * @param name      The name of the property
     * @param value     The value of the property
     */
    public static void throwEnhanced(final Throwable exception, final String name, final Object value) {
        rethrow(enhance(exception, name, value));
    }

    /**
     * If an exception if thrown from a simple callback it gets enhanced by the specified callback and then rethrown.
     *
     * @param callback        The simple callback to guard
     * @param enhanceCallback The callback to be called to enhance the exception (if thrown)
     */
    public static void enhanceAndRethrow(
            final ThrowsRunnable callback,
            final Consumer<BaseException> enhanceCallback
    ) {
        try {
            callback.run();
        } catch (final Throwable exception) {
            enhanceCallback.accept(getPropertiesException(exception));
            rethrow(exception);
        }
    }

    /**
     * If an exception if thrown from a simple callback it gets enhanced with the specified property and rethrown.
     *
     * @param callback The simple callback to guard
     * @param name     The name of the property
     * @param value    The value of the property
     */
    public static void enhanceAndRethrow(
            final ThrowsRunnable callback,
            final String name,
            final Object value
    ) {
        enhanceAndRethrow(callback, baseException -> baseException.putProperty(name, value));
    }

    /**
     * If an exception if thrown from a functional callback it gets enhanced by the specified callback and then
     * rethrown.
     *
     * @param callback        The functional callback to guard
     * @param enhanceCallback The callback to be called to enhance the exception (if thrown)
     * @param <R>             The return type of the functional callback
     * @return The value returned from the functional callback
     */
    public static <R> R enhanceAndRethrowFn(
            final ThrowsSupplier<R> callback,
            final Consumer<BaseException> enhanceCallback
    ) {
        try {
            return callback.get();
        } catch (final Throwable exception) {
            enhanceCallback.accept(getPropertiesException(exception));
            rethrow(exception);
        }

        throw new IllegalStateException("EhCoreSupport.rethrow must always throw an exception");
    }

    /**
     * If an exception if thrown from a functional callback it gets enhanced with the specified property and rethrown.
     *
     * @param callback The functional callback to guard
     * @param name     The name of the property
     * @param value    The value of the property
     * @param <R>      The return type of the functional callback
     * @return The value returned from the functional callback
     */
    public static <R> R enhanceAndRethrowFn(
            final ThrowsSupplier<R> callback,
            final String name,
            final Object value
    ) {
        return enhanceAndRethrowFn(callback, baseException -> baseException.putProperty(name, value));
    }

    /**
     * Obtains the time thrown / enhanced property of an exception.
     *
     * @param exception The exception to obtain the time thrown / enhanced property
     * @return The time thrown / enhanced
     */
    public static Instant getTimeThrown(final Throwable exception) {
        return getPropertiesException(exception).getTimeThrown();
    }

    /**
     * Associate a property to an exception.
     *
     * @param exception The exception to associate a property with
     * @param name      The name of the property
     * @param value     The value of the property
     */
    public static void putProperty(final Throwable exception, final String name, final Object value) {
        getPropertiesException(exception).putProperty(name, value);
    }

    /**
     * Tries to obtain the value of a property from an exception (if already associated).
     *
     * @param exception The exception to obtain the property value from
     * @param name      The name of the property
     * @param <E>       The expected type of the value of the property
     * @return The value of the property or null if property with such name was not found
     */
    public static <E> E tryGetProperty(final Throwable exception, final String name) {
        return getPropertiesException(exception).tryGetProperty(name);
    }

    /**
     * Tries to obtain the value of a property as string from an exception (if already associated).
     *
     * @param exception The exception to obtain the property value from
     * @param name      The name of the property
     * @return The value of the property as string or null if property with such name was not found
     */
    public static String tryGetPropertyAsString(final Throwable exception, final String name) {
        return getPropertiesException(exception).tryGetPropertyAsString(name);
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception.
     *
     * @param exception The exception to obtain the exception details dump for
     * @param callback  The callback invoked with the string buffer
     * @param <R>       The return type of the callback (if any)
     * @return The same as the return value from the callback
     */
    public static <R> R getExceptionDetails(final Throwable exception, final Function<StringBuffer, R> callback) {
        return getPropertiesException(exception).getExceptionDetails(callback);
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception as string.
     *
     * @param exception The exception to obtain the exception details dump for
     * @return The exception details as string
     */
    public static String getExceptionDetailsAsString(final Throwable exception) {
        return getPropertiesException(exception).getExceptionDetailsAsString();
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception into a print writer.
     *
     * @param exception   The exception to obtain the exception details dump for
     * @param printWriter The print writer where to store them
     */
    public static void printExceptionDetails(final Throwable exception, final PrintWriter printWriter) {
        getPropertiesException(exception).printExceptionDetails(printWriter);
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception into a print stream.
     *
     * @param exception   The exception to obtain the exception details dump for
     * @param printStream The print stream where to store them
     */
    public static void printExceptionDetails(final Throwable exception, final PrintStream printStream) {
        getPropertiesException(exception).printExceptionDetails(printStream);
    }

    /**
     * Checks  if Result object is successful and returns the actual result value. Otherwise it throws the exception
     * held in the Result object.
     *
     * @param result The Result object to be checked
     * @param <E>    The type of the actual return value
     * @return The actual value from the Result object if the result parameter is successful
     */
    public static <E> E checkResult(final Result<E> result) {
        if (!result.isSuccess()) {
            throwEnhanced(result.getException());
        }
        return result.getData();
    }
}
