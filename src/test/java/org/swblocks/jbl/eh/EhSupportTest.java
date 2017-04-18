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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.swblocks.jbl.test.utils.JblTestClassUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Basic exception support helpers tests.
 */
public class EhSupportTest {
    private static void verifyExceptionHasEndpoint(
            final IllegalArgumentException exception,
            final IllegalArgumentException exceptionThrown) {
        assertSame(exception, exceptionThrown);

        final Throwable clause = exception.getCause();

        assertNotNull(clause);
        assertTrue(clause instanceof PropertiesHolderException);
        assertSame(clause, EhSupport.getPropertiesException(exception));

        final Object propertyValue = EhSupport.tryGetProperty(exception, EhCoreProperties.EH_PROPERTY_ENDPOINT);

        assertNotNull(propertyValue);
        assertTrue(propertyValue instanceof String);
        assertEquals(propertyValue, "localhost:3333");
    }

    /**
     * Invokes the private default instructor to remove code coverage noise.
     */
    @Test
    public void hasPrivateConstructor() {
        JblTestClassUtils.assertConstructorIsPrivate(EhSupport.class);
    }

    @Test
    public void basicTests() {
        final String message = "Test exception";
        final String messageCause = "Test cause exception";
        final String baseMessage = "Test base exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final RuntimeException runtimeException = new RuntimeException(message, exceptionCause);
        final BaseException baseException = new BaseException(baseMessage);

        assertSame(baseException, EhSupport.getPropertiesException(baseException));
        assertNull(baseException.getCause());

        assertNull(exceptionCause.getCause());
        assertSame(EhSupport.enhance(runtimeException), runtimeException);

        final Throwable propertiesException = exceptionCause.getCause();

        assertNotNull(propertiesException);
        assertTrue(propertiesException instanceof BaseException);
        assertSame(propertiesException, EhSupport.getPropertiesException(runtimeException));

        assertSame(EhSupport.enhance(runtimeException), runtimeException);
        assertSame(propertiesException, exceptionCause.getCause());
        assertSame(propertiesException, EhSupport.getPropertiesException(runtimeException));

        assertNotNull(EhSupport.getTimeThrown(runtimeException));

        assertNotNull(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_MESSAGE));
        assertNotNull(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_TIME_THROWN));
        assertNotNull(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME));
        assertNotNull(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME));
        assertNotNull(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER));

        assertEquals(EhSupport.tryGetProperty(runtimeException,
                EhCoreProperties.EH_PROPERTY_MESSAGE), runtimeException.getMessage());
        assertEquals(EhSupport.tryGetProperty(baseException,
                EhCoreProperties.EH_PROPERTY_MESSAGE), baseException.getMessage());

        final StackTraceElement callerFrame = runtimeException.getStackTrace()[0];

        assertEquals(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME),
                callerFrame.getClassName());
        assertEquals(EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME),
                callerFrame.getMethodName());
        assertEquals(
                EhSupport.tryGetProperty(runtimeException, EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER),
                (Integer) callerFrame.getLineNumber());

        final String customPropertyName = EhCoreProperties.EH_PROPERTY_CUSTOM_PREFIX + "my_custom_property_name";

        assertNull(EhSupport.tryGetProperty(runtimeException, customPropertyName));

        final UnsupportedOperationException customPropertyValue = new UnsupportedOperationException("Test message");
        EhSupport.putProperty(runtimeException, customPropertyName, customPropertyValue);
        assertNotNull(EhSupport.tryGetProperty(runtimeException, customPropertyName));
        assertEquals(EhSupport.tryGetProperty(runtimeException, customPropertyName), customPropertyValue);

        assertEquals("java.lang.UnsupportedOperationException: Test message",
                EhSupport.tryGetPropertyAsString(runtimeException, customPropertyName));

        final String exceptionDetails = EhSupport.getExceptionDetailsAsString(runtimeException);
        assertEquals(exceptionDetails,
                EhSupport.getPropertiesException(runtimeException).getExceptionDetailsAsString());

        EhSupport.getExceptionDetails(runtimeException, stringBuffer -> {
            assertEquals(stringBuffer.toString(), exceptionDetails);
            return null;
        });

        EhSupport.propagate(() -> {
            try (
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    final PrintStream printStream =
                            new PrintStream(stream, true /* autoFlush */, "UTF-8" /* encoding */)
            ) {
                EhSupport.printExceptionDetails(runtimeException, printStream);
                assertEquals(stream.toString("UTF-8"), exceptionDetails);
            }
        });

        EhSupport.propagate(() -> {
            try (
                    final StringWriter writer = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(writer)
            ) {
                EhSupport.printExceptionDetails(runtimeException, printWriter);
                assertEquals(writer.toString(), exceptionDetails);
            }
        });

    }

    @Test
    public void throwEnhancedTests() {
        {
            final IllegalArgumentException exceptionThrown = new IllegalArgumentException("Test exception");

            try {
                EhSupport.throwEnhanced(exceptionThrown);
            } catch (final IllegalArgumentException exception) {
                assertSame(exception, exceptionThrown);

                final Throwable clause = exception.getCause();

                assertNotNull(clause);
                assertTrue(clause instanceof PropertiesHolderException);
                assertSame(clause, EhSupport.getPropertiesException(exception));

                assertNull(EhSupport.tryGetProperty(exception, EhCoreProperties.EH_PROPERTY_ENDPOINT));
            }
        }

        {
            final IllegalArgumentException exceptionThrown = new IllegalArgumentException("Test exception");

            try {
                EhSupport.throwEnhanced(exceptionThrown, EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333");
            } catch (final IllegalArgumentException exception) {
                verifyExceptionHasEndpoint(exception, exceptionThrown);
            }
        }
    }

    @Test
    public void enhanceAndRethrowTests() {
        {
            final IllegalArgumentException exceptionThrown = new IllegalArgumentException("Test exception");

            try {
                EhSupport.enhanceAndRethrow(() -> {
                            throw exceptionThrown;
                        },
                        EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333"
                );

                fail("enhanceAndRethrow must throw");
            } catch (final IllegalArgumentException exception) {
                verifyExceptionHasEndpoint(exception, exceptionThrown);
            }
        }

        {
            final IllegalArgumentException exceptionThrown = new IllegalArgumentException("Test exception");

            try {
                EhSupport.enhanceAndRethrow(() -> {
                            throw exceptionThrown;
                        },
                        propertiesException -> propertiesException.putProperty(
                                EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333")
                );

                fail("enhanceAndRethrow must throw");
            } catch (final IllegalArgumentException exception) {
                verifyExceptionHasEndpoint(exception, exceptionThrown);
            }
        }

        EhSupport.enhanceAndRethrow(() -> {
        }, EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333");

        EhSupport.enhanceAndRethrow(() -> {
                },
                propertiesException -> propertiesException.putProperty(
                        EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333")
        );
    }

    @Test
    public void enhanceAndRethrowFnTests() {
        {
            final IllegalArgumentException exceptionThrown = new IllegalArgumentException("Test exception");

            try {
                EhSupport.enhanceAndRethrowFn(() -> {
                            throw exceptionThrown;
                        },
                        EhCoreProperties.EH_PROPERTY_ENDPOINT,
                        "localhost:3333"
                );

                fail("enhanceAndRethrowFn must throw");
            } catch (final IllegalArgumentException exception) {
                verifyExceptionHasEndpoint(exception, exceptionThrown);
            }
        }

        {
            final IllegalArgumentException exceptionThrown = new IllegalArgumentException("Test exception");

            try {
                EhSupport.enhanceAndRethrowFn(() -> {
                            throw exceptionThrown;
                        },
                        propertiesException -> propertiesException.putProperty(
                                EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333")
                );

                fail("enhanceAndRethrowFn must throw");
            } catch (final IllegalArgumentException exception) {
                verifyExceptionHasEndpoint(exception, exceptionThrown);
            }
        }

        {
            final String value = EhSupport.enhanceAndRethrowFn(() -> "Test value",
                    EhCoreProperties.EH_PROPERTY_ENDPOINT,
                    "localhost:3333"
            );

            assertEquals("Test value", value);
        }
        {
            final String value = EhSupport.enhanceAndRethrowFn(() -> "Test value", propertiesException ->
                    propertiesException.putProperty(EhCoreProperties.EH_PROPERTY_ENDPOINT, "localhost:3333")
            );

            assertEquals("Test value", value);
        }
    }

    @Test
    public void chkResultSuccess() {
        assertEquals(EhSupport.checkResult(Result.success(42L)), Long.valueOf(42));
    }

    @Test(expected = IllegalStateException.class)
    public void chkResultFails() {
        EhSupport.checkResult(Result.failure(IllegalStateException::new));
    }
}
