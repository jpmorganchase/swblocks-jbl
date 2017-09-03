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

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Generic class for supporting a range of {@link Comparable} objects.
 */
public class Range<T extends Comparable<? super T>> implements Predicate<T> {
    @SuppressWarnings("unchecked")
    public static final BiPredicate<Range, Comparable> RANGE_CHECK = (range, test) ->
            test.compareTo(range.getStart()) >= 0 && test.compareTo(range.getFinish()) < 0;

    private final T start;
    private final T finish;

    public Range(final T start, final T finish) {
        this.start = start;
        this.finish = finish;
    }

    @Override
    public boolean test(final T value) {
        return value.compareTo(getStart()) >= 0 && value.compareTo(getFinish()) < 0;
    }

    public T getStart() {
        return start;
    }

    public T getFinish() {
        return finish;
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
            final Range other = (Range) obj;
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
