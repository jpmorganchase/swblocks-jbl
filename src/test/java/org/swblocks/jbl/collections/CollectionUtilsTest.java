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

package org.swblocks.jbl.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.swblocks.jbl.test.utils.JblTestClassUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test cases for {@link CollectionUtils}.
 */
public class CollectionUtilsTest {
    @Test
    public void testCollectionIsNotEmpty() {
        assertFalse(CollectionUtils.isNotEmpty((Collection) null));
        assertFalse(CollectionUtils.isNotEmpty(Collections.emptyList()));
        assertTrue(CollectionUtils.isNotEmpty(Collections.singletonList("item1")));
    }

    @Test
    public void testMapIsNotEmpty() {
        assertFalse(CollectionUtils.isNotEmpty((Map) null));
        assertFalse(CollectionUtils.isNotEmpty(new HashMap<>()));
        assertTrue(CollectionUtils.isNotEmpty(Collections.singletonMap("key1", "value1")));
    }

    @Test
    public void testUnmodifiableMapOnlyCreatesOneWrapper() {
        final Map<String, String> testMap = Collections.singletonMap("key1", "value1");
        final Map<String, String> unmodifiableTestMap = CollectionUtils.unmodifiableMap(testMap);
        assertNotNull(unmodifiableTestMap);

        final Map<String, String> unmodifiableTestMapClone = CollectionUtils.unmodifiableMap(unmodifiableTestMap);
        // Test for exact object reference equality.
        assertTrue(unmodifiableTestMapClone == unmodifiableTestMap);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableMapIsUnmodifiable() {
        final Map<String, String> testMap = Collections.singletonMap("key1", "value1");
        final Map<String, String> unmodifiableTestMap = CollectionUtils.unmodifiableMap(testMap);
        unmodifiableTestMap.put("key2", "value2");
    }

    @Test
    public void testUnmodifiableMapFromEmpty() {
        final Map<String, String> unmodifiableEmptyMap = CollectionUtils.unmodifiableMap(new HashMap<>());
        assertEquals(Collections.EMPTY_MAP.getClass(), unmodifiableEmptyMap.getClass());
    }

    @Test
    public void testUnmodifiableMapFromNull() {
        final Map<String, String> unmodifiableNullMap = CollectionUtils.unmodifiableMap(null);
        assertNotNull(unmodifiableNullMap);
        assertEquals(Collections.EMPTY_MAP.getClass(), unmodifiableNullMap.getClass());
    }

    @Test
    public void testUnmodifiableListOnlyCreatesOneWrapper() {
        final List<String> testList = Collections.singletonList("value1");
        final List<String> unmodifiableTestList = CollectionUtils.unmodifiableList(testList);
        assertNotNull(unmodifiableTestList);

        final List<String> unmodifiableTestListClone = CollectionUtils.unmodifiableList(unmodifiableTestList);
        // Test for exact object reference equality.
        assertTrue(unmodifiableTestListClone == unmodifiableTestList);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableListIsUnmodifiable() {
        final List<String> testList = Collections.singletonList("value1");
        final List<String> unmodifiableTestList = CollectionUtils.unmodifiableList(testList);
        unmodifiableTestList.add("value2");
    }

    @Test
    public void testUnmodifiableListFromEmpty() {
        final List<String> unmodifiableEmptyList = CollectionUtils.unmodifiableList(new ArrayList<String>());
        assertEquals(Collections.EMPTY_LIST.getClass(), unmodifiableEmptyList.getClass());
    }

    @Test
    public void testUnmodifiableListFromNull() {
        final List<String> unmodifiableNullList = CollectionUtils.unmodifiableList(null);
        assertNotNull(unmodifiableNullList);
        assertEquals(Collections.EMPTY_LIST.getClass(), unmodifiableNullList.getClass());
    }

    @Test
    public void testPrivateConstructor() {
        assertTrue(JblTestClassUtils.assertConstructorIsPrivate(CollectionUtils.class));
    }
}