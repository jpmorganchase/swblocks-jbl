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

package org.swblocks.jbl.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class providing a simple LRU cache implementation using {@link LinkedHashMap}.
 *
 * <p>The implementation is not thread safe and access must be synchronised in a multi threaded application. For
 * example:
 * <blockquote><pre>
 *     final int initialCapacity = 10;
 *     final int maximumEntries = 20;
 *
 *     final Map&lt;Range, Object&gt; cache =
 *          Collections.synchronizedMap(new LruCache(initialCapacity, maximumCapacity, 0.75F));
 * </pre></blockquote>
 *
 * <p>The {@code LinkedHashMap} orders its entries by access order so that once the size of the map reaches the
 * maximumEntries value, the least read entry is removed from the map when a new entry is added.
 *
 * @param <K> type of key stored in the {@link Map}.
 * @param <V> type of values stored in the {@link Map}.
 */
public final class LruCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -1096634636585306482L;

    private final int maximumEntries;

    private LruCache(final int initialCapacity, final int maximumEntries, final float loadFactor) {
        super(initialCapacity, loadFactor, true);
        this.maximumEntries = maximumEntries;
    }

    /**
     * Static method to create and return an new instance of a simple LRU cache.
     *
     * @param initialCapacity the initial capacity of the cache
     * @param maximumEntries  the maximum number of entries allowed in this cache
     * @param loadFactor      the map load factor
     * @param <K>             the key object type
     * @param <V>             the value object type
     * @return the new instance of the {@code LruCache}
     */
    public static <K, V> LruCache<K, V> getCache(final int initialCapacity,
                                                 final int maximumEntries,
                                                 final float loadFactor) {
        return new LruCache<>(initialCapacity, maximumEntries, loadFactor);
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > this.maximumEntries;
    }

    @Override
    public boolean equals(final Object object) {
        return super.equals(object) && this.getClass() == object.getClass() &&
                this.maximumEntries == ((LruCache) object).maximumEntries;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.maximumEntries;
    }
}