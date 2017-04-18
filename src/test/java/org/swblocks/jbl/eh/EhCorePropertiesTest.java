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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the EhCoreProperties class.
 */
public class EhCorePropertiesTest {
    private void assertThrown(final Runnable callback, final String expectedMessage) {
        try {
            callback.run();
            fail("This method must throw an exception");
        } catch (final IllegalArgumentException exception) {
            assertEquals(expectedMessage, exception.getMessage());
        }
    }

    /**
     * Invokes the private default instructor to remove code coverage noise.
     */
    @Test
    public void invokePrivateDefaultConstructor() {
        EhSupport.propagate(() -> {
            final Constructor<EhCoreProperties> constructor = EhCoreProperties.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    public void basicTests() {
        final String stringValue = "test42";
        final Integer intValue = 42;
        final Instant instantValue = Instant.now();

        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_MESSAGE, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_FULL_CLASS_NAME, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME, stringValue));
        assertEquals(intValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER, intValue));
        assertEquals(instantValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_TIME_THROWN, instantValue));
        assertEquals(Boolean.TRUE,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_IS_EXPECTED, Boolean.TRUE));
        assertEquals(Boolean.TRUE,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_IS_USER_FRIENDLY, Boolean.TRUE));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_ENDPOINT, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_HOST, stringValue));
        assertEquals(intValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_PORT, intValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_IO_FILE_PATH, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_HTTP_URL, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_HTTP_REDIRECT_URL, stringValue));
        assertEquals(intValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_HTTP_STATUS_CODE, intValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_HTTP_RESPONSE_HEADERS, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_HTTP_REQUEST_DETAILS, stringValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_COMMAND_OUTPUT, stringValue));
        assertEquals(intValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_COMMAND_EXIT_CODE, intValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_PARSER_FILE_PATH, stringValue));
        assertEquals(intValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_PARSER_LINE_NUMBER, intValue));
        assertEquals(intValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_PARSER_COLUMN, intValue));
        assertEquals(stringValue,
                EhCoreProperties.validateProperty(EhCoreProperties.EH_PROPERTY_PARSER_REASON, stringValue));

        Assert.assertEquals("eh:/properties/custom/", EhCoreProperties.EH_PROPERTY_CUSTOM_PREFIX);

        final String prefix = "Exception property type ";

        assertThrown(() -> EhCoreProperties.validateProperty(
                EhCoreProperties.EH_PROPERTY_LOCATION_METHOD_NAME, intValue),
                prefix + "'java.lang.Integer' is not valid for property name 'eh:/properties/core/method-name'");
        assertThrown(() -> EhCoreProperties.validateProperty(
                EhCoreProperties.EH_PROPERTY_LOCATION_LINE_NUMBER, stringValue),
                prefix + "'java.lang.String' is not valid for property name 'eh:/properties/core/line-number'");
        assertThrown(() -> EhCoreProperties.validateProperty(
                EhCoreProperties.EH_PROPERTY_IS_EXPECTED, intValue),
                prefix + "'java.lang.Integer' is not valid for property name 'eh:/properties/core/is-expected'");

        final String customPropertyName = EhCoreProperties.EH_PROPERTY_CUSTOM_PREFIX + "my_custom_property_name";

        final UnsupportedOperationException customPropertyValue = new UnsupportedOperationException("Test message");

        assertEquals(customPropertyValue, EhCoreProperties.validateProperty(customPropertyName,
                customPropertyValue));

        final Map<String, Class<?>> builtInProperties = EhCoreProperties.getBuiltInProperties();

        assertTrue(builtInProperties.size() > 0);

        try {
            builtInProperties.put(customPropertyName, customPropertyValue.getClass());
            fail("builtInProperties is expected to be unmodifiable and builtInProperties.put(...) must throw");
        } catch (final UnsupportedOperationException exception) {
            assertNotNull(exception);
        }

        EhSupport.propagate(() -> {
            for (final Map.Entry<String, Class<?>> entry : builtInProperties.entrySet()) {
                try {
                    final Object valueInstance = entry.getValue().newInstance();

                    assertEquals(EhCoreProperties.validateProperty(entry.getKey(), valueInstance),
                            valueInstance);
                } catch (final Exception exception) {
                    /*
                     * The boxed equivalents of primitive types will throw InstantiationException.
                     */
                    assertTrue(exception instanceof InstantiationException);
                }
            }
        });
    }
}
