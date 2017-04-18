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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.swblocks.jbl.eh.EhSupport;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * The tests for {@link Timer} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class TimerTest {
    @Mock
    private Supplier<Instant> mockTimer;

    private Timer timerUnderTest;

    /**
     * Setup...
     */
    @Before
    public void testSetUp() {
        when(this.mockTimer.get()).thenReturn(Instant.ofEpochSecond(500000, 0),
                Instant.ofEpochSecond(500001, 230000000), Instant.ofEpochSecond(550001, 230000000),
                Instant.ofEpochSecond(550001, 240000000), Instant.ofEpochSecond(550001, 245000000),
                Instant.ofEpochSecond(600001, 245000000), Instant.ofEpochSecond(600001, 245100000),
                Instant.ofEpochSecond(600001, 245200000));
        this.timerUnderTest = Timer.createStarted(this.mockTimer);
    }

    @Test
    public void createStartedAndThenStopsWithDuration() {
        final Timer timer = Timer.createStarted();
        assertNotNull(timer);
        assertNotNull(timer.getDuration());
        assertEquals(0, timer.getDuration().getNano());
        EhSupport.propagate(() ->
                await().atLeast(1, TimeUnit.MILLISECONDS).until(() -> timer.stopAndPrintAsReadableString()));
        assertNotEquals(0, timer.getDuration().getNano());
    }

    @Test
    public void testStopAndStopAgain() {
        this.timerUnderTest.stop();
        assertEquals("PT1.23S", this.timerUnderTest.getDuration().toString());
        // Stop again does nothing.
        this.timerUnderTest.stop();
        assertEquals("PT1.23S", this.timerUnderTest.getDuration().toString());
    }

    @Test
    public void stopAndPrintAsHumanReadableString() {
        assertEquals("1.23 seconds", this.timerUnderTest.stopAndPrintAsReadableString());
        this.timerUnderTest.start();
        assertEquals("10.0 milliseconds", this.timerUnderTest.stopAndPrintAsReadableString());
        this.timerUnderTest.start();
        assertEquals("50000.0 seconds", this.timerUnderTest.stopAndPrintAsReadableString());
        this.timerUnderTest.start();
        assertEquals("100000 nanoseconds", this.timerUnderTest.stopAndPrintAsReadableString());
    }

    @Test
    public void lapAndPrintAsHumanReadableString() {
        assertEquals("1.23 seconds", this.timerUnderTest.lapAndPrintAsReadableString());
        // Difference between first lap and second lap
        assertEquals("50000.0 seconds", this.timerUnderTest.lapAndPrintAsReadableString());
        // Difference between start and finish
        assertEquals("50001.24 seconds", this.timerUnderTest.stopAndPrintAsReadableString());
    }

    @Test
    public void getDuration() {
        this.timerUnderTest.stop();
        assertEquals(Duration.ofSeconds(1, 230000000), this.timerUnderTest.getDuration());
    }

    @Test
    public void getLapDuration() {
        this.timerUnderTest.lap();
        assertEquals(Duration.ofSeconds(1, 230000000), this.timerUnderTest.getLapDuration());
    }
}