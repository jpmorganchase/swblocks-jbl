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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the PropertiesHolderException class.
 */
public class PropertiesHolderExceptionTest {
    @Test
    public void basicTests() {
        final String message = "Test exception";
        final String messageCause = "Test cause exception";

        final Throwable exceptionCause = new Exception(messageCause);
        final RuntimeException referenceException = new RuntimeException(message, exceptionCause);
        final PropertiesHolderException exception = new PropertiesHolderException(referenceException);

        assertNotNull(exception.getMessage());
        assertNotNull(exception.getCallerFrame());
        assertNotNull(exception.getTimeThrown());

        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_MESSAGE));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_TIME_THROWN));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME));
        assertNotNull(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER));

        assertEquals(exception.getMessage(), message);
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_MESSAGE), referenceException.getMessage());
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_TIME_THROWN), exception.getTimeThrown());

        final StackTraceElement callerFrame = referenceException.getStackTrace()[0];

        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME),
                callerFrame.getClassName());
        assertEquals(exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME),
                callerFrame.getMethodName());
        assertEquals(
                exception.tryGetProperty(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER),
                (Integer) callerFrame.getLineNumber());

        assertEquals("org.swblocks.jbl.eh.PropertiesHolderExceptionTest",
                exception.getCallerFrame().getClassName());
        assertEquals("basicTests", exception.getCallerFrame().getMethodName());
        assertEquals(35, exception.getCallerFrame().getLineNumber());

        final int lineNumber = exception.getCallerFrame().getLineNumber();
        final int causeLineNumber = lineNumber - 1;

        final String[] lines = exception.getExceptionDetailsAsString().split(System.lineSeparator());

        assertTrue(lines.length > 9);

        final String line0Expected = "Exception was thrown of type 'java.lang.RuntimeException'; " +
                "created at the following location: " +
                "'org.swblocks.jbl.eh.PropertiesHolderExceptionTest.basicTests:" +
                lineNumber + "'";

        final String fullClassName =
                "[eh:/properties/core/full-class-name] org.swblocks.jbl.eh.PropertiesHolderExceptionTest";

        assertEquals(line0Expected, lines[0]);
        assertEquals(lines[1], "Exception properties:");
        assertEquals(lines[2], fullClassName);
        assertEquals(lines[3], "[eh:/properties/core/line-number] " + lineNumber);
        assertEquals(lines[4], "[eh:/properties/core/message] Test exception");
        assertEquals(lines[5], "[eh:/properties/core/method-name] basicTests");
        assertTrue(lines[6].startsWith("[eh:/properties/core/time-thrown] "));
        assertEquals(lines[7], "Exception stack trace:");
        assertEquals("java.lang.RuntimeException: Test exception", lines[8]);
        assertEquals(
                "\tat org.swblocks.jbl.eh.PropertiesHolderExceptionTest.basicTests(PropertiesHolderExceptionTest" +
                        ".java:" + lineNumber + ")", lines[9]);

        boolean causeFramesFound = false;

        for (int i = 11; i < lines.length; ++i) {
            if (lines[i].equals("Caused by: java.lang.Exception: Test cause exception") &&
                    (i + 1) < lines.length &&
                    lines[i + 1].equals("\tat org.swblocks.jbl.eh.PropertiesHolderExceptionTest.basicTests" +
                            "(PropertiesHolderExceptionTest.java:" + causeLineNumber + ")")) {
                causeFramesFound = true;
                break;
            }
        }

        assertTrue(causeFramesFound);
    }
}
