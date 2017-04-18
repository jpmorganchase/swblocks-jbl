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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Collection Utilities.
 *
 * <p>Lightweight utility class for Collections.
 */
public class CollectionUtils {
    private static final Map exampleUnmodifiableMap = Collections.unmodifiableMap(Collections.emptyMap());
    private static final List exampleUnmodifiableList = Collections.unmodifiableList(Collections.emptyList());

    // Enforce static use of class.
    private CollectionUtils() {
    }

    /**
     * Utility method to check for a not empty {@link Collection} including a null check.
     *
     * @param collection {@link Collection} to test for non empty
     * @return true if collection is not null and has at least one entry.
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Utility method to check for a not empty {@link Map} including a null check.
     *
     * @param map {@link Map} to test for non empty
     * @param <K> Type of Key in Map
     * @param <V> Type of Value in Map
     * @return true if map is not null and has at least one entry.
     */
    public static <K, V> boolean isNotEmpty(final Map<K, V> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Wraps a Map with the {@code Collections.unmodifiableMap}, but only once.
     *
     * <p>Checks the {@link Map} passed to ensure that it is not already a {@code Collections.unmodifiableMap}.
     * If the parameter is a null or empty, then it returns {@code Collections.emptyMap}.
     *
     * @param map {@link Map} to wrap with {@code Collections.unmodifiableMap}
     * @param <K> Key type
     * @param <V> Value type
     * @return An unmodifiable Map
     */
    public static <K, V> Map<K, V> unmodifiableMap(final Map<K, V> map) {
        if (isNotEmpty(map)) {
            if (!(exampleUnmodifiableMap.getClass().equals(map.getClass()))) {
                return Collections.unmodifiableMap(map);
            }
            return map;
        }
        return Collections.emptyMap();
    }

    /**
     * Wraps a List with the {@code Collections.unmodifiableList}, but only once.
     *
     * <p>Checks the {@link List} passed to ensure that it is not already a {@code Collections.unmodifiableLisy}.
     * If the parameter is a null or empty, then it returns {@code Collections.emptyList}.
     *
     * @param list {@link List} to wrap with {@code Collections.unmodifiableList}
     * @param <V>  Value type
     * @return An unmodifiable List.
     */
    public static <V> List<V> unmodifiableList(final List<V> list) {
        if (isNotEmpty(list)) {
            if (!(exampleUnmodifiableList.getClass().equals(list.getClass()))) {
                return Collections.unmodifiableList(list);
            }
            return list;
        }
        return Collections.emptyList();
    }

}
