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
import java.util.function.BiPredicate;

/**
 * Class containing immutable dates defining the start and finish times of a date range.
 *
 * @deprecated use {@link Range}.
 */
@Deprecated
public class DateRange extends Range<Instant> {
    public static final BiPredicate<DateRange, Instant> RANGE_CHECK = (range, time) ->
            time.compareTo(range.getStart()) >= 0 && time.compareTo(range.getFinish()) < 0;

    public DateRange(final Instant start, final Instant finish) {
        super(start, finish);
    }
}
