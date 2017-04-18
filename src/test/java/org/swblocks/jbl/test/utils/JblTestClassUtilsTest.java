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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link JblTestClassUtils}.
 */
public class JblTestClassUtilsTest {
    @Test
    public void assertConstructorIsPrivate() {
        Assert.assertTrue(JblTestClassUtils.assertConstructorIsPrivate(JblTestClassUtils.class));
    }

    @Test
    public void assertBeanGettersCorrect() {
        final ConstructorInjectionBean bean = new ConstructorInjectionBean("Test");

        final Map<String, Object> injectedValues = new HashMap<>();
        injectedValues.put("name", "Test");

        Assert.assertTrue(JblTestClassUtils.assertGetterCorrectForConstructorInjection(injectedValues, bean));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorBeanNotNull() {
        final ConstructorInjectionBean bean = null;

        final Map<String, Object> injectedValues = new HashMap<>();
        injectedValues.put("name", "Test");

        Assert.assertTrue(JblTestClassUtils.assertGetterCorrectForConstructorInjection(injectedValues, bean));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorInjectedValuesEmpty() {
        final ConstructorInjectionBean bean = new ConstructorInjectionBean("Test");

        final Map<String, Object> injectedValues = new HashMap<>();

        Assert.assertTrue(JblTestClassUtils.assertGetterCorrectForConstructorInjection(injectedValues, bean));
    }

    @Test
    public void assertGetterReturnsSetterValuesMatch() {
        final GetterAndSetterBean bean = new GetterAndSetterBean();

        final Map<String, Object> injectedValues = new HashMap<>();
        injectedValues.put("name", "Test");

        Assert.assertTrue(JblTestClassUtils.assertSettersAndGettersCorrect(injectedValues, bean,
                Collections.emptyMap()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertGetterReturnsSetterValueFailsWithEmptyMap() {
        final GetterAndSetterBean bean = new GetterAndSetterBean();

        final Map<String, Object> injectedValues = new HashMap<>();

        Assert.assertTrue(JblTestClassUtils.assertSettersAndGettersCorrect(injectedValues, bean,
                Collections.emptyMap()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertGetterReturnsSetterValueFailsWithNullBean() {
        final GetterAndSetterBean bean = null;

        final Map<String, Object> injectedValues = new HashMap<>();
        injectedValues.put("name", "Test");

        Assert.assertTrue(JblTestClassUtils.assertSettersAndGettersCorrect(injectedValues, bean,
                Collections.emptyMap()));
    }

    @Test
    public void assertTypedGetterReturnsSetterValuesMatch() {
        final GetterAndSetterTypedBean<String> bean = new GetterAndSetterTypedBean<>();

        final Map<String, Object> injectedValues = new HashMap<>();
        injectedValues.put("name", "Test");

        final Map<String, Class> typeOverrides = new HashMap<>();
        typeOverrides.put("name", java.lang.Object.class);

        Assert.assertTrue(JblTestClassUtils.assertSettersAndGettersCorrect(injectedValues, bean, typeOverrides));
    }
}