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

/**
 * A simple mutable object holder. Can be used to express out/inout parameters and in other
 * scenarios where mutation needs to happen on a value that is also pinned to a final object
 *
 * <p>Such helper type(s) exists in Apache Commons and many other libraries, but definitely not
 * worth taking library dependency for such small functionality
 *
 * @param <T> Type to wrap in {@link MutableObject}
 */
public final class MutableObject<T> {
    private T value;

    public MutableObject(final T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof MutableObject)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final MutableObject<T> other = (MutableObject<T>) obj;
        return Objects.equals(this.getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
