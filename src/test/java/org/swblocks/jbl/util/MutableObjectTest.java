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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link MutableObject}.
 */
public class MutableObjectTest {
    @Test
    public void mutableObjectTests() {
        final MutableObject<Integer> mutable1 = new MutableObject<>(null);
        final MutableObject<Integer> mutable2 = new MutableObject<>(42);

        assertEquals(0, mutable1.hashCode());

        assertEquals(mutable1.getValue(), null);
        assertEquals(mutable2.getValue(), (Integer) 42);

        mutable1.setValue(55);
        assertEquals(mutable1.getValue(), (Integer) 55);

        assertEquals(55, mutable1.hashCode());
        assertEquals(42, mutable2.hashCode());

        assertNotEquals(mutable1, mutable2);
        mutable1.setValue(42);
        assertEquals(mutable1, mutable2);
        assertFalse(mutable1.equals(null));
        assertFalse(mutable1.equals(42));
        assertTrue(mutable1.equals(mutable2));
        assertTrue(mutable1.equals(mutable1));
    }
}
