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
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Class containing immutable dates defining the start and finish times of a date range.
 */
public class DateRange {
    public static final BiPredicate<DateRange, Instant> RANGE_CHECK = (range, time) ->
            time.compareTo(range.getStart()) >= 0 && time.compareTo(range.getFinish()) < 0;

    private final Instant start;
    private final Instant finish;

    public DateRange(final Instant start, final Instant finish) {
        this.start = start;
        this.finish = finish;
    }

    public Instant getStart() {
        return this.start;
    }

    public Instant getFinish() {
        return this.finish;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            final DateRange other = (DateRange) obj;
            return Objects.equals(this.getStart(), other.getStart()) &&
                    Objects.equals(this.getFinish(), other.getFinish());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.start) + Objects.hashCode(this.finish);
    }
}
