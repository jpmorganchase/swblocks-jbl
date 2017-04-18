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

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The core properties of all exceptions.
 */
public final class EhCoreProperties {
    public static final String EH_PROPERTY_MESSAGE = "eh:/properties/core/message";
    public static final String EH_PROPERTY_FULL_CLASS_NAME = "eh:/properties/core/full-class-name";
    public static final String EH_PROPERTY_LOCATION_METHOD_NAME = "eh:/properties/core/method-name";
    public static final String EH_PROPERTY_LOCATION_LINE_NUMBER = "eh:/properties/core/line-number";
    public static final String EH_PROPERTY_TIME_THROWN = "eh:/properties/core/time-thrown";
    public static final String EH_PROPERTY_IS_EXPECTED = "eh:/properties/core/is-expected";
    public static final String EH_PROPERTY_IS_USER_FRIENDLY = "eh:/properties/core/is-user-friendly";
    public static final String EH_PROPERTY_ENDPOINT = "eh:/properties/core/endpoint";
    public static final String EH_PROPERTY_HOST = "eh:/properties/core/host";
    public static final String EH_PROPERTY_PORT = "eh:/properties/core/port";
    public static final String EH_PROPERTY_IO_FILE_PATH = "eh:/properties/core/io-file-path";
    public static final String EH_PROPERTY_HTTP_URL = "eh:/properties/core/http-url";
    public static final String EH_PROPERTY_HTTP_REDIRECT_URL = "eh:/properties/core/http-redirect-url";
    public static final String EH_PROPERTY_HTTP_STATUS_CODE = "eh:/properties/core/http-status-code";
    public static final String EH_PROPERTY_HTTP_RESPONSE_HEADERS = "eh:/properties/core/http-response-headers";
    public static final String EH_PROPERTY_HTTP_REQUEST_DETAILS = "eh:/properties/core/http-request-details";
    public static final String EH_PROPERTY_COMMAND_OUTPUT = "eh:/properties/core/command-output";
    public static final String EH_PROPERTY_COMMAND_EXIT_CODE = "eh:/properties/core/command-exit-code";
    public static final String EH_PROPERTY_PARSER_FILE_PATH = "eh:/properties/core/parser-file-path";
    public static final String EH_PROPERTY_PARSER_LINE_NUMBER = "eh:/properties/core/parser-line-number";
    public static final String EH_PROPERTY_PARSER_COLUMN = "eh:/properties/core/parser-column";
    public static final String EH_PROPERTY_PARSER_REASON = "eh:/properties/core/parser-reason";
    public static final String EH_PROPERTY_CUSTOM_PREFIX = "eh:/properties/custom/";

    private static final Map<String, Class<?>> builtInPropertiesTypes;

    static {
        builtInPropertiesTypes = new HashMap<>();

        builtInPropertiesTypes.put(EH_PROPERTY_MESSAGE, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_FULL_CLASS_NAME, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_LOCATION_METHOD_NAME, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_LOCATION_LINE_NUMBER, Integer.class);
        builtInPropertiesTypes.put(EH_PROPERTY_TIME_THROWN, Instant.class);
        builtInPropertiesTypes.put(EH_PROPERTY_IS_EXPECTED, Boolean.class);
        builtInPropertiesTypes.put(EH_PROPERTY_IS_USER_FRIENDLY, Boolean.class);
        builtInPropertiesTypes.put(EH_PROPERTY_ENDPOINT, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_HOST, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_PORT, Integer.class);
        builtInPropertiesTypes.put(EH_PROPERTY_IO_FILE_PATH, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_HTTP_URL, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_HTTP_REDIRECT_URL, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_HTTP_STATUS_CODE, Integer.class);
        builtInPropertiesTypes.put(EH_PROPERTY_HTTP_RESPONSE_HEADERS, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_HTTP_REQUEST_DETAILS, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_COMMAND_OUTPUT, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_COMMAND_EXIT_CODE, Integer.class);
        builtInPropertiesTypes.put(EH_PROPERTY_PARSER_FILE_PATH, String.class);
        builtInPropertiesTypes.put(EH_PROPERTY_PARSER_LINE_NUMBER, Integer.class);
        builtInPropertiesTypes.put(EH_PROPERTY_PARSER_COLUMN, Integer.class);
        builtInPropertiesTypes.put(EH_PROPERTY_PARSER_REASON, String.class);
    }

    /**
     * Declare the constructor private to enforce the class is static.
     */
    private EhCoreProperties() {
    }

    /**
     * Validates the type of the value of a property and returns the value. If the name of the property matches with
     * some of the built in properties the expected type should also match.
     *
     * @param name  The name of the property
     * @param value The value of the property
     * @return Same as the value if the type is correct
     * @throws IllegalArgumentException if the type of the property is incorrect
     */
    public static Object validateProperty(final String name, final Object value) {
        final Class<?> expectedType = builtInPropertiesTypes.getOrDefault(name, null);
        final Class<?> type = value.getClass();

        if (expectedType != null && !expectedType.equals(type)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Exception property type '%s' is not valid for property name '%s'",
                            type.getCanonicalName(),
                            name));
        }

        return value;
    }

    /**
     * Obtains read-only view of the built in properties and their expected types.
     *
     * @return The build-in properties map (name, type)
     */
    public static Map<String, Class<?>> getBuiltInProperties() {
        return Collections.unmodifiableMap(builtInPropertiesTypes);
    }
}
