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

package org.swblocks.jbl.test.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.swblocks.jbl.collections.CollectionUtils;
import org.swblocks.jbl.eh.EhSupport;
import org.swblocks.jbl.eh.Result;

import static java.util.Locale.ENGLISH;

/**
 * Test utility class for testing properties about a class.
 */
public class JblTestClassUtils {
    /**
     * Private constructor to ensure static use.
     */
    private JblTestClassUtils() {
    }

    /**
     * Asserts that the constructor of a Class is private.
     *
     * <p>To be used with unit testing.
     *
     * @param clazz Class to test.
     * @return true or false if the class has a private no args constructor
     */
    public static boolean assertConstructorIsPrivate(final Class clazz) {
        return EhSupport.propagateFn(() -> {
            final Constructor constructor = clazz.getDeclaredConstructor();
            final boolean isPrivate = Modifier.isPrivate(constructor.getModifiers());
            constructor.setAccessible(true);
            constructor.newInstance();
            constructor.setAccessible(false);
            return isPrivate;
        });
    }

    /**
     * Asserts that getter methods return the correct values when a class in initialised by a constructor.
     *
     * @param injectedValues the map containing the property name and value pairs to be checked
     * @param bean           the class that is initialised
     * @param <T>            the bean object type
     * @return true if the bean getters return the expected values
     */
    public static <T> boolean assertGetterCorrectForConstructorInjection(final Map<String, Object> injectedValues,
                                                                         final T bean) {
        EhSupport.ensureArg(CollectionUtils.isNotEmpty(injectedValues), "Injected property values are not present");
        EhSupport.ensureArg(bean != null, "Test bean is null");

        final Set<String> properties = injectedValues.keySet();
        final Result<T> result = EhSupport.propagateFn(() -> {
            for (final String property : properties) {
                final Object exampleValue = injectedValues.get(property);

                final String capitalisedProperty =
                        property.substring(0, 1).toUpperCase(ENGLISH) + property.substring(1);
                final String getterName = "get" + capitalisedProperty;

                final Object value = bean.getClass().getMethod(getterName).invoke(bean);
                EhSupport.ensure(exampleValue.equals(value), "Constructor broken for property %s", property);
            }
            return Result.success(bean);
        });

        return result.isSuccess();
    }

    /**
     * Asserts that getter methods return the same values applied by the setter method in a class.
     *
     * @param injectedValues the map containing the property name and value pairs to be checked
     * @param bean           the class that is under test
     * @param typeOverrides  the map containing type overrides for the property
     * @param <T>            the bean object type
     * @return true if the getters return the same value as the setters
     */
    public static <T> boolean assertSettersAndGettersCorrect(final Map<String, Object> injectedValues,
                                                             final T bean,
                                                             final Map<String, Class> typeOverrides) {
        EhSupport.ensureArg(CollectionUtils.isNotEmpty(injectedValues), "Injected property values are not present");
        EhSupport.ensureArg(bean != null, "Test bean is null");

        final Set<String> properties = injectedValues.keySet();
        final Result<T> result = EhSupport.propagateFn(() -> {
            for (final String property : properties) {
                final Object exampleValue = injectedValues.get(property);
                final Class classType =
                        typeOverrides.containsKey(property) ? typeOverrides.get(property) : exampleValue.getClass();

                final String capitalisedProperty =
                        property.substring(0, 1).toUpperCase(ENGLISH) + property.substring(1);

                final String setterName = "set" + capitalisedProperty;
                bean.getClass().getMethod(setterName, classType).invoke(bean, exampleValue);

                final String getterName = "get" + capitalisedProperty;
                final Object value = bean.getClass().getMethod(getterName).invoke(bean);
                EhSupport.ensure(exampleValue.equals(value), "Constructor broken for property %s", property);
            }
            return Result.success(bean);
        });

        return result.isSuccess();
    }
}
