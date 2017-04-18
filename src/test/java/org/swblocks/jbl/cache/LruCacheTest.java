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

import java.time.Instant;
import java.time.Period;
import java.util.LinkedHashMap;

import org.junit.Test;
import org.swblocks.jbl.util.DateRange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link LruCache}.
 */
public class LruCacheTest {
    private final LruCache<DateRange, DateRange> cache = getLruCache(1, 2, 0.75f);

    @Test
    public void removesOldestEntry() {
        final Instant now = Instant.now();

        final DateRange rangeOne = new DateRange(now, now.plus(Period.ofWeeks(1)));
        this.cache.putIfAbsent(rangeOne, rangeOne);
        assertTrue(this.cache.containsKey(rangeOne));

        final DateRange rangeTwo = new DateRange(now.plus(Period.ofWeeks(1)), now.plus(Period.ofWeeks(2)));
        this.cache.putIfAbsent(rangeTwo, rangeTwo);
        assertTrue(this.cache.containsKey(rangeOne));
        assertTrue(this.cache.containsKey(rangeTwo));

        this.cache.get(rangeTwo);

        final DateRange rangeThree = new DateRange(now.plus(Period.ofWeeks(2)), now.plus(Period.ofWeeks(3)));
        this.cache.putIfAbsent(rangeThree, rangeThree);

        assertFalse(this.cache.containsKey(rangeOne));
        assertTrue(this.cache.containsKey(rangeTwo));
        assertTrue(this.cache.containsKey(rangeThree));
    }

    @Test
    public void equalsWorks() {
        final LruCache<DateRange, DateRange> thisCache = getLruCache(1, 2, 0.75f);
        LruCache<DateRange, DateRange> otherCache = getLruCache(1, 2, 0.75f);
        assertTrue(thisCache.equals(otherCache));

        otherCache = getLruCache(1, 5, 0.75f);
        assertFalse(thisCache.equals(otherCache));

        final LinkedHashMap otherMap = new LinkedHashMap(1, 0.75f);
        assertFalse(thisCache.equals(otherMap));
    }

    @Test
    public void hashCodeWorks() {
        final LruCache<DateRange, DateRange> thisCache = getLruCache(1, 2, 0.75f);
        LruCache<DateRange, DateRange> other = getLruCache(1, 2, 0.75f);
        assertEquals(thisCache.hashCode(), other.hashCode());

        other = getLruCache(1, 5, 0.75f);
        assertFalse(thisCache.hashCode() == other.hashCode());
    }

    private LruCache getLruCache(final int initialCapacity, final int maximumCapacity, final float loadFactor) {
        return LruCache.getCache(initialCapacity, maximumCapacity, loadFactor);
    }
}