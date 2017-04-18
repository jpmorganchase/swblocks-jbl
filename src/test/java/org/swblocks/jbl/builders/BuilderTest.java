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

package org.swblocks.jbl.builders;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link Builder}.
 */
@SuppressWarnings("unchecked")
public class BuilderTest {
    @Test
    public void testBuilderCreatorNoVerifier() {
        final Supplier mockCreator = mock(Supplier.class);
        final Function mockBuilder = mock(Function.class);

        when(mockCreator.get()).thenReturn(1);

        final Builder testBuilder = Builder.instanceOf(mockCreator, mockBuilder);
        assertNotNull(testBuilder);

        testBuilder.build();
        verify(mockBuilder, times(1)).apply(1);
    }

    @Test
    public void testBuilderCreatorVerifier() {
        final Supplier mockCreator = mock(Supplier.class);
        final Predicate mockPredicate = mock(Predicate.class);
        final Function mockBuilder = mock(Function.class);
        final ArgumentCaptor intCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor buildCaptor = ArgumentCaptor.forClass(Integer.class);

        when(mockCreator.get()).thenReturn(1);
        when(mockPredicate.test(intCaptor.capture())).thenReturn(true);
        when(mockBuilder.apply(buildCaptor.capture())).thenReturn(3);

        final Builder testBuilder = Builder.instanceOf(mockCreator, mockPredicate, mockBuilder);
        assertNotNull(testBuilder);

        final ArgumentCaptor consumerCaptor1 = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor consumerCaptor2 = ArgumentCaptor.forClass(Integer.class);

        final BiConsumer mockConsumer = mock(BiConsumer.class);
        doNothing().when(mockConsumer).accept(consumerCaptor1.capture(), consumerCaptor2.capture());

        testBuilder.with(mockConsumer, 2);

        final Object result = testBuilder.build();
        assertNotNull(result);
        assertEquals(Integer.valueOf(3), result);
        assertEquals(1, intCaptor.getValue());
        verify(mockBuilder, times(1)).apply(1);
        assertEquals(1, consumerCaptor1.getValue());
        assertEquals(2, consumerCaptor2.getValue());
    }

    @Test
    public void testBuilderCreatorVerifierFails() {
        final Supplier mockCreator = mock(Supplier.class);
        final Predicate mockPredicate = mock(Predicate.class);
        final Function mockBuilder = mock(Function.class);

        when(mockCreator.get()).thenReturn(1);
        when(mockPredicate.test(any())).thenReturn(false);

        final Builder testBuilder = Builder.instanceOf(mockCreator, mockPredicate, mockBuilder);

        final Object result = testBuilder.build();
        assertNull(result);
    }

}