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

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The base exception class for all exceptions.
 *
 * <p>It provides basic support for capture of basic core properties and subsequent enhancement with other properties,
 * including custom properties
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = -3761053769452690215L;

    private final Throwable referenceException;
    private final Instant timeThrown;
    private final StackTraceElement callerFrame;
    private final TreeMap<String, Object> properties = new TreeMap<>();

    /**
     * A protected constructor (that allows referenceException to be set) to be used only by the derived types (if
     * necessary).
     *
     * <p>See also documentation for {@link RuntimeException#RuntimeException(String, Throwable)}
     *
     * @param referenceExceptionToChain Exception that is chained to this base exception
     * @param message                   See documentation for {@link RuntimeException#RuntimeException(String)}
     * @param cause                     See documentation for {@link RuntimeException#RuntimeException(Throwable)}
     */
    protected BaseException(final Throwable referenceExceptionToChain, final String message, final Throwable cause) {
        super(message, cause, true /* enableSuppression */, true /* writableStackTrace */);

        /*
         * Populate some core properties such as full class name, line number, method name, time thrown, etc
         */

        this.referenceException = referenceExceptionToChain == null ? this : referenceExceptionToChain;

        this.timeThrown = Instant.now();
        this.callerFrame = this.referenceException.getStackTrace()[0];

        final String referenceExceptionMessage = this.referenceException.getMessage();

        if (referenceExceptionMessage != null) {
            putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, this.referenceException.getMessage());
        }

        putProperty(EhCoreProperties.EH_PROPERTY_TIME_THROWN, this.timeThrown);
        putProperty(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME, this.callerFrame.getClassName());
        putProperty(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME, this.callerFrame.getMethodName());
        putProperty(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER, this.callerFrame.getLineNumber());
    }

    /**
     * See documentation for {@link RuntimeException#RuntimeException(String)}.
     *
     * @param message See documentation for {@link RuntimeException#RuntimeException(String)}
     */
    public BaseException(final String message) {
        this(message, null /* cause */);
    }

    /**
     * See documentation for {@link RuntimeException#RuntimeException(String, Throwable)}.
     *
     * @param message See documentation for {@link RuntimeException#RuntimeException(String)}
     * @param cause   See documentation for {@link RuntimeException#RuntimeException(Throwable)}
     */
    public BaseException(final String message, final Throwable cause) {
        this(null /* referenceException */, message, cause);
    }

    /**
     * Gets the time at which the exception was thrown.
     *
     * @return The time at which it was thrown
     */
    public Instant getTimeThrown() {
        return this.timeThrown;
    }

    /**
     * Gets the caller frame.
     *
     * @return The caller frame
     */
    public StackTraceElement getCallerFrame() {
        return this.callerFrame;
    }

    /**
     * Appends or removes property to the exception details. The name parameter is expected to not be null or empty.
     * However the value parameter can be null in which case all properties associated with that property name will be
     * removed from the exception.
     *
     * @param name  The name of the exception property (could be one of the core properties in {@link EhCoreProperties})
     *              or other value. It cannot be null or empty.
     * @param value The value of the property. If value is null all properties associated with that property name will
     *              be removed.
     */
    public void putProperty(final String name, final Object value) {
        EhCoreSupport.ensureOrFatal(name != null && !name.isEmpty(), "The parameter 'name' cannot be null or empty");

        if (value == null) {
            this.properties.remove(name);
            return;
        }

        EhCoreProperties.validateProperty(name, value);

        final Object current = this.properties.get(name);

        /*
         * We only add the property if it wasn't set before or if
         * it is different than the current / last property value
         */

        if (current == null) {
            this.properties.put(name, value);
        } else if (current instanceof List) {
            final List list = (List) current;
            if (list.isEmpty() || !list.get(list.size() - 1).equals(value)) {
                list.add(value);
            }
        } else {
            if (!current.equals(value)) {
                final List list = new ArrayList(4);
                list.add(current);
                list.add(value);
                this.properties.put(name, list);
            }
        }
    }

    /**
     * Gets property from the exception by name. If multiple properties were appended the last one will be returned.
     *
     * @param name The name of the exception property (could be one of the core properties in {@link EhCoreProperties})
     * @param <E>  The expected type
     * @return The property value
     */
    public <E> E tryGetProperty(final String name) {
        final Object value = this.properties.getOrDefault(name, null);

        if (value != null) {
            if (value instanceof List) {
                final List list = (List) value;
                if (list.isEmpty()) {
                    /*
                     * This might be the case where someone set an empty list as a property explicitly
                     * Just return it
                     */
                    return (E) list;
                } else {
                    /*
                     * Return the last element, which is the last property value set
                     */
                    return (E) list.get(list.size() - 1);
                }
            }
            return (E) value;
        }

        return null;
    }

    /**
     * Gets property of the exception as string.
     *
     * @param name The name of the exception property (could be one of the core properties in {@link EhCoreProperties})
     * @return The property value
     */
    public String tryGetPropertyAsString(final String name) {
        final Object value = tryGetProperty(name);

        if (value != null) {
            return value.toString();
        }

        return null;
    }

    /**
     * * Obtains the exception details dump, including all properties, for an exception.
     *
     * @param callback The callback invoked with the string buffer
     * @param <R>      The return type of the callback (if any)
     * @return The same as the return value from the callback
     */
    public <R> R getExceptionDetails(final Function<StringBuffer, R> callback) {
        return EhSupport.propagateFn(() -> {
            try (
                    final StringWriter writer = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(writer)
            ) {
                printExceptionDetails(printWriter);

                return callback.apply(writer.getBuffer());
            }
        });
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception as string.
     *
     * @return The exception details as string
     */
    public String getExceptionDetailsAsString() {
        return getExceptionDetails(StringBuffer::toString);
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception into a print writer.
     *
     * @param printWriter The print writer where to store them
     */
    public void printExceptionDetails(final PrintWriter printWriter) {
        printWriter.print("Exception was thrown of type '");
        printWriter.print(this.referenceException.getClass().getCanonicalName());
        printWriter.print("'; created at the following location: '");
        printWriter.print(this.callerFrame.getClassName());
        printWriter.print(".");
        printWriter.print(this.callerFrame.getMethodName());
        printWriter.print(":");
        printWriter.print(this.callerFrame.getLineNumber());
        printWriter.println("'");

        printWriter.println("Exception properties:");
        for (final Map.Entry<String, Object> entry : this.properties.entrySet()) {
            final Consumer<Object> printValue = value -> {
                printWriter.print("[");
                printWriter.print(entry.getKey());
                printWriter.print("] ");
                printWriter.println(value);
            };
            final Object value = entry.getValue();
            if (value instanceof List) {
                final List list = (List) value;
                for (int i = list.size() - 1; i >= 0; --i) {
                    printValue.accept(list.get(i));
                }
            } else {
                printValue.accept(value);
            }
        }

        printWriter.println("Exception stack trace:");
        this.referenceException.printStackTrace(printWriter);
        printWriter.flush();
    }

    /**
     * Obtains the exception details dump, including all properties, for an exception into a print stream.
     *
     * @param printStream The print stream where to store them
     */
    public void printExceptionDetails(final PrintStream printStream) {
        EhSupport.propagate(() -> {
            try (
                    final Writer writer = new OutputStreamWriter(printStream,
                            Charset.forName("UTF-8"));
                    final PrintWriter printWriter = new PrintWriter(writer)
            ) {
                printExceptionDetails(printWriter);
            }
        });
    }
}
