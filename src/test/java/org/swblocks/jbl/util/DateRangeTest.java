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

package org.swblocks.jbl.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link DateRange}.
 */
public class DateRangeTest {
    @Test
    public void testEquals() {
        final Instant start = Instant.now().minus(5, ChronoUnit.DAYS);
        final Instant end = Instant.now().plus(5, ChronoUnit.DAYS);

        final DateRange dateRange = new DateRange(start, end);
        final DateRange other = new DateRange(start, end);

        assertTrue(dateRange.equals(dateRange));
        assertFalse(dateRange.equals(null));
        assertFalse(dateRange.equals(new Integer(5)));
        assertTrue(dateRange.equals(other));

        assertFalse(dateRange.equals(new DateRange(Instant.now(), end)));
        assertFalse(dateRange.equals(new DateRange(start, Instant.now())));
    }

    @Test
    public void testHashcode() {
        final Instant start = Instant.MIN;
        final Instant end = Instant.MAX;

        final DateRange dateRange = new DateRange(start, end);
        assertEquals(-962094838, dateRange.hashCode());
    }

    @Test
    public void testRangeCheck() {
        final Instant now = Instant.now();
        final Instant start = now.minus(5, ChronoUnit.DAYS);
        final Instant end = now.plus(5, ChronoUnit.DAYS);

        final DateRange dateRange = new DateRange(start, end);
        assertTrue(DateRange.RANGE_CHECK.test(dateRange, now));
        assertTrue(DateRange.RANGE_CHECK.test(dateRange, start));

        // End date is not considered part of the range.
        assertFalse(DateRange.RANGE_CHECK.test(dateRange, end));
    }
}
