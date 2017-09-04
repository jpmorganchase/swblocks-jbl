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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.swblocks.jbl.eh.EhSupport;
import org.swblocks.jbl.eh.Result;

/**
 * The action retry logic encapsulation.
 *
 * <p>The Retrier executes an action and can be passed a predicate to determine if a retry should occur on failure.
 *
 * <p>Example usage to create an ActionRetrier with a default time generator to pause for 5 seconds and repeat 10 times
 * (retries). The retrier will run an action and the result will be tested to see if it should be retried.
 * <blockquote><pre>
 *      final ActionRetrier retrier =
 *              Retrier.createSleepRetrier(Retrier.createConstantTimeGenerator(5000), 10);
 *
 *      final Predicate&lt;Result&gt; predicate =
 *              Retrier.exceptionsFilterPredicate(IllegalArgumentException.class);
 *
 *      // always fails and will retry...
 *      final Result result =
 *              retrier.run(() -&gt; Result.failure(() -&gt; new Exception()), predicate);
 *
 *      // more code
 * </pre></blockquote>
 *
 * <p>The class is configured with a number of static members and methods to create default time generators and action
 * retriers.
 *
 * <p>Example to create an ActionRetrier using the default capped incrementing time generator with 5 retry attempts:
 * <blockquote><pre>
 *      final ActionRetrier retrier =
 *              Retrier.createSleepRetrier(Retrier.DEFAULT_CAPPED_INCREMENTING_TIME_GENERATOR, 5);
 *
 *      // default - filter all
 *      final Predicate&lt;Result&gt; predicate = Retrier.PREDICATE_EXCEPTIONS_FILTER_ALL;
 *
 *      // will not retry as predicate test fails...
 *      final Result result = retrier.run(() -&gt; Result.failure(() -&gt; new Exception()), predicate);
 *
 *     // more code
 * </pre></blockquote>
 */
public final class Retrier {
    public static final long TIME_GENERATOR_DEFAULT_DELAY_IN_MILLISECONDS = 100L;
    public static final long TIME_GENERATOR_DEFAULT_CAPPED_DELAY_IN_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);
    public static final long TIME_GENERATOR_DEFAULT_STEP_MULTIPLIER = 10L;
    public static final int TIME_GENERATOR_DEFAULT_RETRIES_BETWEEN_STEPS = 5;

    public static final Supplier<Iterator<Long>> DEFAULT_CAPPED_INCREMENTING_TIME_GENERATOR =
            createCappedIncrementingTimeGenerator(
                    TIME_GENERATOR_DEFAULT_DELAY_IN_MILLISECONDS,
                    TIME_GENERATOR_DEFAULT_CAPPED_DELAY_IN_MILLISECONDS,
                    TIME_GENERATOR_DEFAULT_STEP_MULTIPLIER,
                    TIME_GENERATOR_DEFAULT_RETRIES_BETWEEN_STEPS
            );

    public static final Supplier<Iterator<Long>> DEFAULT_CONSTANT_TIME_GENERATOR =
            createConstantTimeGenerator(TIME_GENERATOR_DEFAULT_DELAY_IN_MILLISECONDS);

    /**
     * Private constructor to enforce static use.
     */
    private Retrier() {
    }

    /**
     * A predicate that returns true for any exceptions derived from {@link Exception}.
     *
     * @param <T> the type of object
     * @return the predicate to perform the check
     */
    public static <T> Predicate<Result<T>> createDefaultExceptionPredicate() {
        return exceptionsFilterPredicate(Exception.class);
    }

    /**
     * A predicate that matches against a set of exception types.
     *
     * @param recoverableTypes The recoverable exception types
     * @param <T>              the object type
     * @return The predicate to perform the test
     */
    public static <T> Predicate<Result<T>> exceptionsFilterPredicate(final Class... recoverableTypes) {
        return result -> {
            EhSupport.ensureOrFatal(!result.isSuccess(), "Successful operations should not be retried");

            Throwable exception = result.getException();

            while (exception != null) {
                if (matchAssignableFromClass(exception.getClass(), recoverableTypes)) {
                    return true;
                }
                exception = exception.getCause();
            }

            return false;
        };
    }

    /**
     * Create a retrier that executes the action once only and will never retry.
     *
     * @param <T> the object type
     * @return the {@code ActionRetrier}
     */
    public static <T> ActionRetrier<T> createNonRetrier() {
        return (action, shouldRetry) -> action.get();
    }

    /**
     * Create a retrier that will retry an action once if the action fails.
     *
     * @param <T> the object type
     * @return the {@code ActionRetrier}
     */
    public static <T> ActionRetrier<T> createDefaultSingleSleepRetrier() {
        return createSleepRetrier(DEFAULT_CONSTANT_TIME_GENERATOR, 1 /* numberOfRetries */);
    }

    /**
     * Create a retrier that will continually retry with a constant time between retry attempts.
     *
     * @param <T> the object type
     * @return the {@code ActionRetrier}
     */
    public static <T> ActionRetrier<T> createDefaultContinuousConstantSleepRetrier() {
        return createContinuousSleepRetrier(DEFAULT_CONSTANT_TIME_GENERATOR);
    }

    /**
     * Create a retrier that that will increment the time between retry attempts if a failure occurs.
     *
     * @param <T> the object type
     * @return the {@code ActionRetrier}
     */
    public static <T> ActionRetrier<T> createDefaultCappedIncrementingRetrier() {
        return createSleepRetrier(DEFAULT_CAPPED_INCREMENTING_TIME_GENERATOR,
                TIME_GENERATOR_DEFAULT_RETRIES_BETWEEN_STEPS);
    }

    /**
     * Create a retrier that will keep retrying the action until success and incrementing the time between attempts.
     *
     * @param <T> the object type
     * @return the {@code ActionRetrier}
     */
    public static <T> ActionRetrier<T> createDefaultContinuousCappedIncrementingRetrier() {
        return createContinuousSleepRetrier(DEFAULT_CAPPED_INCREMENTING_TIME_GENERATOR);
    }

    /**
     * Method to return a ActionRetrier to execute an action.
     *
     * @param timeGenerator   the time generator used to derive sleep times
     * @param numberOfRetries the number of retry attempts
     * @param <T>             the object type
     * @return the ActionRetrier
     */
    public static <T> ActionRetrier<T> createSleepRetrier(final Supplier<Iterator<Long>> timeGenerator,
                                                          final int numberOfRetries) {
        return (action, shouldRetry) -> {
            EhSupport.ensureArg(numberOfRetries > 0, "numberOfRetries %s should be a positive number",
                    numberOfRetries);

            final Iterator<Long> sleepTimes = timeGenerator.get();
            Result<T> result = Result.failure(() -> new IllegalStateException("Result not expected to be used"));

            for (int i = 0; i < numberOfRetries; i++) {
                result = action.get();
                if (result.isSuccess()) {
                    return result;
                } else if (shouldRetry.test(result)) {
                    EhSupport.propagate(() -> Thread.sleep(sleepTimes.next()));
                }
            }

            return result;
        };
    }

    /**
     * Method to create an ActionRetrier that has no limit on the the number of retry attempts.
     *
     * @param timeGenerator the time generator used to derive sleep times
     * @param <T>           the object type
     * @return the ActionRetrier
     */
    public static <T> ActionRetrier<T> createContinuousSleepRetrier(final Supplier<Iterator<Long>> timeGenerator) {
        return (action, shouldRetry) -> {
            final Iterator<Long> sleepTimes = timeGenerator.get();

            while (true) {
                final Result<T> result = action.get();
                if (!result.isSuccess() && shouldRetry.test(result)) {
                    EhSupport.propagate(() -> Thread.sleep(sleepTimes.next()));
                } else {
                    return result;
                }
            }
        };
    }

    /**
     * Create a constant time generator.
     *
     * @param timeInMiliseconds the time that the generator will always return
     * @return the iterator that always returns the defined time
     */
    public static Supplier<Iterator<Long>> createConstantTimeGenerator(final long timeInMiliseconds) {
        return () -> new Iterator<Long>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Long next() {
                if (timeInMiliseconds == 0L) {
                    throw new NoSuchElementException("No pause time specified");
                }
                return timeInMiliseconds;
            }
        };
    }

    /**
     * Method to create a capped time incrementing time generator (iterator) from the specified arguments.
     *
     * <p>Once the number of retries has been exceeded, the delay is incremented by the step until the maximum delay has
     * been reached. The delay will never exceed the capped delay.
     *
     * @param initialDelayInMillis the initial delay in milliseconds that should be used for the sleep time
     * @param cappedDelayInMillis  the maximum delay in milliseconds that the time generator will return
     * @param stepMultiplier       the step multiplier that is applied to the current time delay
     * @param retriesBetweenSteps  the number of retries that are attempted before the step multiplier is applied
     * @return the iterator that returns the calulated sleep times
     */
    public static Supplier<Iterator<Long>> createCappedIncrementingTimeGenerator(
            final long initialDelayInMillis,
            final long cappedDelayInMillis,
            final long stepMultiplier,
            final int retriesBetweenSteps) {
        return () -> new CappedIncrementingTimeGeneratorIterator(initialDelayInMillis, cappedDelayInMillis,
                stepMultiplier, retriesBetweenSteps);
    }

    /**
     * Matches if a type is assignable to one of the types in a collection of types. TODO: this is a generic helper and
     * probably best to move in another location and made public
     *
     * @param type  The type to match
     * @param types The collection of types to match against
     * @return true if the input type can be matched to a type it is assignable from in the collection
     */
    private static boolean matchAssignableFromClass(final Class type, final Class... types) {
        for (final Class<?> matchedType : types) {
            if (matchedType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    private static class CappedIncrementingTimeGeneratorIterator implements Iterator<Long> {
        private final long cappedDelayInMillis;
        private final long stepMultiplier;
        private final int retriesBetweenSteps;

        private int retries = 0;
        private long currentDelay;

        public CappedIncrementingTimeGeneratorIterator(final long initialDelayInMillis,
                                                       final long cappedDelayInMillis,
                                                       final long stepMultiplier,
                                                       final int retriesBetweenSteps) {
            this.cappedDelayInMillis = cappedDelayInMillis;
            this.stepMultiplier = stepMultiplier;
            this.retriesBetweenSteps = retriesBetweenSteps;
            this.currentDelay = initialDelayInMillis;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Long next() {
            if (this.currentDelay < this.cappedDelayInMillis && this.retries >= this.retriesBetweenSteps) {
                this.retries = 0;
                this.currentDelay = Math.min(this.currentDelay * this.stepMultiplier, this.cappedDelayInMillis);
            }
            this.retries++;

            if (this.currentDelay == 0L) {
                throw new NoSuchElementException("No pause time specified");
            }

            return this.currentDelay;
        }
    }
}