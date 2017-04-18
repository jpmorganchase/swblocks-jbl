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

package org.swblocks.jbl.util.retry;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Test;
import org.swblocks.jbl.eh.Result;
import org.swblocks.jbl.test.utils.JblTestClassUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test class for the {@link Retrier} class.
 */
public class RetrierTest {
    public static final Result<String> FAILURE_RESULT =
            Result.failure(() -> new IllegalArgumentException("failure result"));
    public static final Result<String> SUCCESS_RESULT = Result.success("success");

    @Test
    public void testPrivateConstructor() {
        JblTestClassUtils.assertConstructorIsPrivate(Retrier.class);
    }

    @Test
    public void defaultConstantTimeGeneratorSleepTime() {
        final Supplier<Iterator<Long>> constantTimeGenerator =
                Retrier.DEFAULT_CONSTANT_TIME_GENERATOR;

        final Iterator<Long> iterator = constantTimeGenerator.get();

        assertTrue(iterator.hasNext());

        for (int i = 0; i < 10; i++) {
            assertEquals(100L, iterator.next().longValue());
        }
    }

    @Test
    public void defaultCappedIncrementingTimeGeneratorSleepTime() {
        final Supplier<Iterator<Long>> cappedIncrementingTimeGenerator =
                Retrier.DEFAULT_CAPPED_INCREMENTING_TIME_GENERATOR;
        final Iterator<Long> iterator = cappedIncrementingTimeGenerator.get();

        assertTrue(iterator.hasNext());

        assertGeneratedTime(5, 100L, iterator);
        assertGeneratedTime(5, 1000L, iterator);
        assertGeneratedTime(5, 10000L, iterator);
        assertGeneratedTime(5, 100000L, iterator);
        assertGeneratedTime(50, 300000L, iterator);
    }

    @Test
    public void defaultCappedIncrementingTimeGeneratorSleepTimeResets() {
        final Supplier<Iterator<Long>> cappedIncrementingTimeGenerator =
                Retrier.DEFAULT_CAPPED_INCREMENTING_TIME_GENERATOR;

        Iterator<Long> iterator = cappedIncrementingTimeGenerator.get();
        assertGeneratedTime(5, 100L, iterator);
        assertGeneratedTime(5, 1000L, iterator);

        iterator = cappedIncrementingTimeGenerator.get();
        assertGeneratedTime(5, 100L, iterator);
        assertGeneratedTime(5, 1000L, iterator);
        assertGeneratedTime(5, 10000L, iterator);
        assertGeneratedTime(5, 100000L, iterator);

        iterator = cappedIncrementingTimeGenerator.get();
        assertGeneratedTime(5, 100L, iterator);
        assertGeneratedTime(5, 1000L, iterator);
        assertGeneratedTime(5, 10000L, iterator);
        assertGeneratedTime(5, 100000L, iterator);
        assertGeneratedTime(50, 300000L, iterator);
    }

    @Test
    public void retriesPredicateFilterAll() {
        final Predicate<Result<Boolean>> predicate = Retrier.createDefaultExceptionPredicate();
        assertTrue(predicate.test(Result.failure(() -> new Exception(""))));
    }

    @Test
    public void retriesTopLevelException() {
        final Predicate<Result<Boolean>> predicate =
                Retrier.exceptionsFilterPredicate(RuntimeException.class, IllegalArgumentException.class);

        assertTrue(predicate.test(Result.failure(() -> new RuntimeException(""))));
        assertTrue(predicate.test(Result.failure(() -> new NumberFormatException(""))));

        assertFalse(predicate.test(Result.failure(() -> new Exception(""))));
    }

    @Test(expected = Error.class)
    public void predicateFailsWithSuccess() {
        final Predicate<Result<String>> predicate =
                Retrier.exceptionsFilterPredicate(RuntimeException.class, IllegalArgumentException.class);
        predicate.test(SUCCESS_RESULT);
    }

    @Test
    public void retriesNestedLevelExceptions() {
        final Predicate<Result<Boolean>> predicate =
                Retrier.exceptionsFilterPredicate(RuntimeException.class, IllegalArgumentException.class);

        assertTrue(predicate.test(Result.failure(() -> new Exception(new Exception(new NumberFormatException())))));

        assertFalse(predicate.test(Result.failure(() -> new Exception(new Exception(new Exception())))));
    }

    @Test
    public void neverRetryActionWithSuccess() {
        final Predicate<Result<String>> predicate = mock(Predicate.class);

        when(predicate.test(any(Result.class))).thenReturn(true);

        final ActionRetrier<String> retrier = Retrier.createNonRetrier();
        final Result<String> result = retrier.run(() -> SUCCESS_RESULT, Retrier.createDefaultExceptionPredicate());

        assertEquals(true, result.isSuccess());
        verifyZeroInteractions(predicate);
    }

    @Test
    public void neverRetryActionWithFailure() {
        final Predicate<Result<String>> predicate = mock(Predicate.class);

        when(predicate.test(any(Result.class))).thenReturn(true);

        final ActionRetrier<String> retrier = Retrier.createNonRetrier();
        final Result<String> result = retrier.run(() -> FAILURE_RESULT, Retrier.createDefaultExceptionPredicate());

        assertEquals(false, result.isSuccess());
        verifyZeroInteractions(predicate);
    }

    @Test
    public void retryOnceActionRetrierWithFailure() {
        final Predicate<Result<String>> predicate = mock(Predicate.class);
        when(predicate.test(any(Result.class))).thenReturn(true);

        final ActionRetrier<String> retrier = Retrier.createDefaultSingleSleepRetrier();
        final Result<String> result = retrier.run(() -> FAILURE_RESULT, predicate);

        assertEquals(false, result.isSuccess());
        verify(predicate, times(1)).test(any(Result.class));
    }

    @Test
    public void retryOnceActionRetrierWithSuccess() {
        final Predicate<Result<String>> predicate = mock(Predicate.class);
        when(predicate.test(any(Result.class))).thenReturn(true);

        final ActionRetrier<String> retrier = Retrier.createDefaultSingleSleepRetrier();
        final Result<String> result = retrier.run(() -> SUCCESS_RESULT, predicate);

        assertEquals(true, result.isSuccess());
        verifyZeroInteractions(predicate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void actionRetrierMustHaveRetryAttempts() {
        final ActionRetrier<String> retrier =
                Retrier.createSleepRetrier(Retrier.createConstantTimeGenerator(1000), 0);
        final Predicate<Result<String>> predicate = Retrier.exceptionsFilterPredicate(IllegalArgumentException.class);

        retrier.run(() -> Result.failure(() -> new Exception()), predicate);
    }

    @Test(expected = NoSuchElementException.class)
    public void constantTimeGeneratorHasNoPauseTime() {
        final ActionRetrier<String> retrier =
                Retrier.createSleepRetrier(Retrier.createConstantTimeGenerator(0), 1);
        final Predicate<Result<String>> predicate = mock(Predicate.class);
        when(predicate.test(any(Result.class))).thenReturn(true);

        retrier.run(() -> FAILURE_RESULT, predicate);
    }

    @Test(expected = NoSuchElementException.class)
    public void incrementingTimeGeneratorHasNoPauseTime() {
        final ActionRetrier<String> retrier =
                Retrier.createSleepRetrier(Retrier.createCappedIncrementingTimeGenerator(0, 0, 0, 0), 1);
        final Predicate<Result<String>> predicate = mock(Predicate.class);
        when(predicate.test(any(Result.class))).thenReturn(true);

        retrier.run(() -> FAILURE_RESULT, predicate);
    }

    @Test
    public void testDefaultCappedIncrementorActionRetrier() {
        final Predicate<Result<String>> predicate = mock(Predicate.class);
        when(predicate.test(any(Result.class))).thenReturn(true);

        final ActionRetrier<String> retrier = Retrier.createDefaultCappedIncrementingRetrier();
        final Result<String> result = retrier.run(() -> FAILURE_RESULT, predicate);

        assertEquals(false, result.isSuccess());
        verify(predicate, times(5)).test(any(Result.class)); // default tries only 5 times
    }

    @Test
    public void continuousConstantSleepRetrierRetriesUntilSuccess() {
        final ActionRetrier<String> retrier = Retrier.createDefaultContinuousConstantSleepRetrier();
        final Predicate<Result<String>> predicate = Retrier.createDefaultExceptionPredicate();

        final Supplier<Result<String>> action = mock(Supplier.class);
        when(action.get()).thenReturn(FAILURE_RESULT).thenReturn(FAILURE_RESULT).thenReturn(SUCCESS_RESULT);

        final Result<String> result = retrier.run(action, predicate);
        assertEquals(true, result.isSuccess());

        verify(action, times(3)).get();
    }

    @Test
    public void continuousCappedIncrementingSleepRetrierRetriesUntilSuccess() {
        final ActionRetrier<String> retrier = Retrier.createDefaultContinuousCappedIncrementingRetrier();
        final Predicate<Result<String>> predicate = Retrier.createDefaultExceptionPredicate();
        final Supplier<Result<String>> action = mock(Supplier.class);
        when(action.get()).thenReturn(FAILURE_RESULT).thenReturn(FAILURE_RESULT).thenReturn(SUCCESS_RESULT);

        final Result<String> result = retrier.run(action, predicate);
        assertEquals(true, result.isSuccess());

        verify(action, times(3)).get();
    }


    private void assertGeneratedTime(final int numberOfTimes,
                                     final long expectedValue,
                                     final Iterator<Long> timeSequence) {
        for (int i = 0; i < numberOfTimes; i++) {
            assertThat(timeSequence.next(), equalTo(expectedValue));
        }
    }
}
