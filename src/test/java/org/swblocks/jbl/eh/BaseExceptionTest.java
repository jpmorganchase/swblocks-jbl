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
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * The tests for {@link BaseException} class.
 */
public class BaseExceptionTest {
    @Test
    public void emptyListProperty() {
        final List<String> emptyListProperty = Collections.emptyList();

        final String message = "Test exception";
        final String messageCause = "Test cause exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final BaseException exception = new BaseException(message, exceptionCause);

        final String myCustomPropertyValueStr = "My custom property value";
        exception.putProperty(myCustomPropertyValueStr, emptyListProperty);

        final List<String> savedProperty = exception.tryGetProperty(myCustomPropertyValueStr);
        assertEquals(emptyListProperty, savedProperty);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionDetailsThrowsException() {
        final String message = "Test exception";
        final String messageCause = "Test cause exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final BaseException exception = new BaseException(message, exceptionCause);

        exception.getExceptionDetails(stringBuffer -> {
            throw new IllegalStateException();
        });
    }

    @Test(expected = NullPointerException.class)
    public void printExceptionWithNullStream() {
        final String message = "Test exception";
        final String messageCause = "Test cause exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final BaseException exception = new BaseException(message, exceptionCause);

        final PrintStream stream = null;
        exception.printExceptionDetails(stream);
    }

    @Test(expected = IllegalStateException.class)
    public void printExceptionWithStreamFails() {
        final String message = "Test exception";
        final String messageCause = "Test cause exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final BaseException exception = new BaseException(message, exceptionCause);

        final PrintStream stream = Mockito.mock(PrintStream.class);

        Mockito.doThrow(new IllegalStateException()).when(stream).close();

        exception.printExceptionDetails(stream);

        Mockito.verifyZeroInteractions(stream);
    }

    @Test
    public void basicTests() {
        final String message = "Test exception";
        final String messageCause = "Test cause exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final BaseException exception = new BaseException(message, exceptionCause);

        assertDefaultProperties(message, exceptionCause, exception);

        final String myCustomPropertyName = EhCoreProperties.EH_PROPERTY_CUSTOM_PREFIX + "my-custom-property";
        final String myCustomPropertyValueStr = "My custom property value";
        final Long myCustomPropertyValueLong = 42L;

        assertCustomProperties(exception, myCustomPropertyName, myCustomPropertyValueStr, myCustomPropertyValueLong);

        assertExceptionChaining(message, exception, myCustomPropertyValueStr, myCustomPropertyValueLong);
    }

    private void assertExceptionChaining(final String message,
                                         final BaseException exception,
                                         final String myCustomPropertyValueStr,
                                         final Long myCustomPropertyValueLong) {
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, exception.getMessage());
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, message + " #2");
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, message + " #3");
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, message + " #3");

        final String exceptionDetails = exception.getExceptionDetailsAsString();

        exception.getExceptionDetails(stringBuffer -> {
            assertEquals(stringBuffer.toString(), exceptionDetails);
            return null;
        });

        final int lineNumber = exception.getCallerFrame().getLineNumber();
        final int causeLineNumber = lineNumber - 1;
        final String[] lines = exception.getExceptionDetailsAsString().split(System.lineSeparator());

        assertTrue(lines.length > 10);

        final String line0Expected =
                "Exception was thrown of type 'org.swblocks.jbl.eh.BaseException'; " +
                        "created at the following location: " +
                        "'org.swblocks.jbl.eh.BaseExceptionTest.basicTests:" + lineNumber + "'";

        int index = 0;

        assertEquals(line0Expected, lines[index++]);
        assertEquals("Exception properties:", lines[index++]);
        assertEquals("[eh:/properties/core/full-class-name] org.swblocks.jbl.eh.BaseExceptionTest",
                lines[index++]);
        assertEquals("[eh:/properties/core/line-number] " + lineNumber, lines[index++]);
        assertEquals("[eh:/properties/core/message] Test exception #3", lines[index++]);
        assertEquals("[eh:/properties/core/message] Test exception #2", lines[index++]);
        assertEquals("[eh:/properties/core/message] Test exception", lines[index++]);
        assertEquals("[eh:/properties/core/method-name] basicTests", lines[index++]);
        assertTrue(lines[index++].startsWith("[eh:/properties/core/time-thrown] "));
        assertEquals("[eh:/properties/custom/my-custom-property] " +
                myCustomPropertyValueLong, lines[index++]);
        assertEquals("[eh:/properties/custom/my-custom-property] " +
                myCustomPropertyValueStr, lines[index++]);
        assertEquals("Exception stack trace:", lines[index++]);
        assertEquals("org.swblocks.jbl.eh.BaseException: Test exception", lines[index++]);
        assertEquals("\tat org.swblocks.jbl.eh.BaseExceptionTest.basicTests(BaseExceptionTest.java:" +
                lineNumber + ")", lines[index++]);

        boolean causeFramesFound = false;

        for (int i = index; i < lines.length; ++i) {
            if (lines[i].equals("Caused by: java.lang.Exception: Test cause exception") &&
                    (i + 1) < lines.length &&
                    lines[i + 1].equals(
                            "\tat org.swblocks.jbl.eh.BaseExceptionTest.basicTests(BaseExceptionTest.java:" +
                                    causeLineNumber + ")")) {
                causeFramesFound = true;
                break;
            }
        }

        assertTrue(causeFramesFound);

        EhSupport.propagate(() -> {
            try (
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    final PrintStream printStream =
                            new PrintStream(stream, true /* autoFlush */, "UTF-8" /* encoding */)
            ) {
                exception.printExceptionDetails(printStream);
                assertEquals(stream.toString("UTF-8"), exception.getExceptionDetailsAsString());
            }
        });

        try {
            exception.putProperty(null, "value");
        } catch (final FatalApplicationError fatalException) {
            assertEquals("A fatal application error has occurred", fatalException.getMessage());
            assertNotNull(fatalException.getCause());
            assertEquals("The parameter 'name' cannot be null or empty",
                    fatalException.getCause().getMessage());
        }

        try {
            exception.putProperty("", "value");
        } catch (final FatalApplicationError fatalException) {
            assertEquals("A fatal application error has occurred", fatalException.getMessage());
            assertNotNull(fatalException.getCause());
            assertEquals("The parameter 'name' cannot be null or empty",
                    fatalException.getCause().getMessage());
        }

        assertEquals(message + " #3", exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_MESSAGE));
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, null);
        assertEquals(null, exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_MESSAGE));
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, null);
        assertEquals(null, exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_MESSAGE));
        exception.putProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, message + " #3");
        assertEquals(message + " #3", exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_MESSAGE));
    }

    private void assertCustomProperties(final BaseException exception,
                                        final String myCustomPropertyName,
                                        final String myCustomPropertyValueStr,
                                        final Long myCustomPropertyValueLong) {
        assertNull(exception.tryGetProperty(myCustomPropertyName));
        assertNull(exception.tryGetPropertyAsString(myCustomPropertyName));

        exception.putProperty(myCustomPropertyName, myCustomPropertyValueStr);
        assertNotNull(exception.tryGetProperty(myCustomPropertyName));
        assertNotNull(exception.tryGetPropertyAsString(myCustomPropertyName));
        assertEquals(myCustomPropertyValueStr, exception.tryGetProperty(myCustomPropertyName));
        assertEquals(myCustomPropertyValueStr, exception.tryGetPropertyAsString(myCustomPropertyName));

        exception.putProperty(myCustomPropertyName, myCustomPropertyValueLong);
        assertNotNull(exception.tryGetProperty(myCustomPropertyName));
        assertNotNull(exception.tryGetPropertyAsString(myCustomPropertyName));
        assertEquals(myCustomPropertyValueLong, exception.tryGetProperty(myCustomPropertyName));
        assertEquals(myCustomPropertyValueLong.toString(), exception.tryGetPropertyAsString(myCustomPropertyName));
    }

    private void assertDefaultProperties(final String message,
                                         final Throwable exceptionCause,
                                         final BaseException exception) {
        assertNotNull(exception.getMessage());
        assertNotNull(exception.getCause());
        assertNotNull(exception.getCallerFrame());
        assertNotNull(exception.getTimeThrown());

        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_MESSAGE));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_TIME_THROWN));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER));

        assertSame(exception.getCause(), exceptionCause);
        assertEquals(exception.getMessage(), message);
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_MESSAGE), exception.getMessage());
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_TIME_THROWN), exception.getTimeThrown());
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME),
                exception.getCallerFrame().getClassName());
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME),
                exception.getCallerFrame().getMethodName());
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER),
                (Integer) exception.getCallerFrame().getLineNumber());

        assertEquals(exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_MESSAGE), exception.getMessage());
        assertEquals(exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_TIME_THROWN),
                exception.getTimeThrown().toString());
        assertEquals(exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME),
                exception.getCallerFrame().getClassName());
        assertEquals(exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME),
                exception.getCallerFrame().getMethodName());
        assertEquals(exception.tryGetPropertyAsString(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER),
                ((Integer) exception.getCallerFrame().getLineNumber()).toString());
    }
}
