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

package org.swblocks.jbl.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Performance timer utility class.
 *
 * <p>This is used to monitor and report duration of operations in a consistent and standard format.
 * The string output is scaled to the closest time unit.
 * If the time duration is over 1 second, then it is reported in seconds.
 * If the time duration is over 10 milliseconds, then it is reported in milliseconds.
 * If it is less than 10 milliseconds it is reported in nanoseconds.
 *
 * <p>All values are reported in decimal form, it does not round to the time unit.
 *
 * <p>Example usage
 *
 * <pre><code>
 *
 * Timer timer = Timer.createStarted();
 * do stuff
 * LOGGER.debug("Time taken by operation was {}",timer.stopAndPrintAsReadableString());
 * </code></pre>
 *
 * <p>The class supports lap counts for timing internal looping and still reporting overall time. <p>Example usage
 *
 * <pre> <code>
 *
 * Timer timer = Timer.createStarted();
 * for (int i=0; i&lt;10; i++) {
 *   do stuff
 *   LOGGER.debug("Time taken inside loop was {}",timer.lapAndPrintAsReadableString());
 * }
 * LOGGER.debug("Total time taken by operation was {}",timer.stopAndPrintAsReadableString());
 * </code></pre>
 */
public final class Timer {

    /**
     * Nanos in a second as a double to ensure double arithmetic.
     */
    private static final double NANOS_PER_SECOND = 1E9;
    /**
     * Millis in a second as a double to ensure double arithmetic.
     */
    private static final double MILLIS_PER_SECOND = 1E6;
    private final Supplier<Instant> currentTimeGeneration;
    private Instant startTime;
    private Instant stopTime;
    private Instant lapTime;

    /**
     * Constructor passing the current time Supplier.
     *
     * @param currentTime current time Supplier class
     */
    private Timer(final Supplier<Instant> currentTime) {
        this.currentTimeGeneration = currentTime;
    }

    /**
     * Creates and returns a Timer which is already started and running. Uses the {@link Instant} class to generate the
     * current time.
     *
     * @return running Timer instance
     */
    public static Timer createStarted() {
        return Timer.createStarted(Instant::now);
    }

    /**
     * Creates and returns a Timer which is already started and running. Uses the passed {@code Supplier} class to
     * generate the current time.
     *
     * @param currentTimeGeneration implementation to generate the current time.
     */
    static Timer createStarted(final Supplier<Instant> currentTimeGeneration) {
        final Timer timer = new Timer(currentTimeGeneration);
        timer.start();
        return timer;
    }

    /**
     * Stop the timer and return the duration as a readable string with the units prepended.
     *
     * @return String representation of the duration since the time was started with the units.
     */
    public String stopAndPrintAsReadableString() {
        stop();
        return asReadableString();
    }

    /**
     * Generates a lap time and returns the duration between this and the last lap time as a readable string with the
     * units prepended.
     *
     * @return String representation of the duration since the last lap time or start if this is the first lap.
     */
    public String lapAndPrintAsReadableString() {
        return asReadableString(lap().toNanos());
    }

    /**
     * Gets the <code>Duration</code> between the start and stop times
     *
     * @return duration between start and stop times.
     */
    public Duration getDuration() {
        return Duration.between(this.startTime, this.stopTime);
    }

    /**
     * Returns the <code>Duration</code> between the start time and the latest lap time.
     *
     * @return duration between start time and latest lap time.
     */
    public Duration getLapDuration() {
        return Duration.between(this.startTime, this.lapTime);
    }

    /**
     * Starts the timer, if already started or stopped, will start again from this point in time.
     */
    public void start() {
        this.startTime = this.currentTimeGeneration.get();
        this.stopTime = this.startTime;
        this.lapTime = this.startTime;
    }

    /**
     * Stops the timer, if the timer was already stopped this call makes no difference to the internal state.
     */
    void stop() {
        if (this.stopTime == this.startTime) {
            this.stopTime = this.currentTimeGeneration.get();
        }
    }

    /**
     * Marks a new lap time, it does not stop the timer.
     *
     * @return Duration between last lap and current time
     */
    Duration lap() {
        final Instant currentLap = this.lapTime;
        this.lapTime = this.currentTimeGeneration.get();
        return Duration.between(currentLap, this.lapTime);
    }

    /**
     * Generates a readable string for the time between the start and end. The timer must have been stopped, otherwise
     * this will report 0 nanoseconds.
     *
     * @return String representation of the number of seconds between starting the timer and stopping it.
     */
    private String asReadableString() {
        return asReadableString(getDurationInNanos());
    }

    private String asReadableString(final long duration) {
        if (duration > NANOS_PER_SECOND) {
            return asSecString(duration) + " seconds";
        } else if (duration > 1E5) {
            return asMilliString(duration) + " milliseconds";
        }
        return asNanoString(duration) + " nanoseconds";
    }

    /**
     * Gets the number of nanoseconds between the start and stop as a string.
     *
     * @param duration in nanoseconds
     * @return String representation of the number of nanosecond between the start and stop times.
     */
    private String asNanoString(final long duration) {
        return Long.toString(duration);
    }

    /**
     * Gets the number of milliseconds between the start and stop as a string.
     *
     * @param duration in nanoseconds
     * @return String representation of the number of millisecond between the start and stop times.
     */
    private String asMilliString(final long duration) {
        return Double.toString(duration / MILLIS_PER_SECOND);
    }

    /**
     * Gets the number of seconds between the start and stop as a string.
     *
     * @param duration in nanoseconds
     * @return String representation of the number of second between the start and stop times.
     */
    private String asSecString(final long duration) {
        return Double.toString(duration / NANOS_PER_SECOND);
    }

    private long getDurationInNanos() {
        return getDuration().toNanos();
    }
}
