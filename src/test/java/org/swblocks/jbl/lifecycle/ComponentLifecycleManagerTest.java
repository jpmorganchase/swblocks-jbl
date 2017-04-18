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

package org.swblocks.jbl.lifecycle;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link ComponentLifecycleManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentLifecycleManagerTest {
    @Mock
    private ComponentLifecycle componentNotStarted1;
    @Mock
    private ComponentLifecycle componentNotStarted2;
    @Mock
    private ComponentLifecycle componentStarted;
    private ComponentLifecycleManager componentLifecycleManager;

    @Before
    public void setup() {
        when(this.componentNotStarted1.isRunning()).thenReturn(false);
        when(this.componentNotStarted1.getPhase()).thenReturn(1);

        when(this.componentNotStarted2.isRunning()).thenReturn(false);
        when(this.componentNotStarted2.getPhase()).thenReturn(2);
        when(this.componentStarted.isRunning()).thenReturn(true);
        when(this.componentStarted.getPhase()).thenReturn(5);
        this.componentLifecycleManager = new ComponentLifecycleManager(Arrays.asList(
                this.componentNotStarted2, this.componentNotStarted1, this.componentStarted));
    }

    @Test
    public void testStartComponents() {
        this.componentLifecycleManager.start();
        final InOrder order = inOrder(this.componentNotStarted1, this.componentNotStarted2, this.componentStarted);
        order.verify(this.componentNotStarted1, times(1)).isRunning();
        order.verify(this.componentNotStarted1, times(1)).start();
        order.verify(this.componentNotStarted2, times(1)).isRunning();
        order.verify(this.componentNotStarted2, times(1)).start();
        order.verify(this.componentStarted, times(1)).isRunning();
    }

    @Test
    public void testStopComponents() {
        // Have to start before we can stop to ensure the running flag is set.
        this.componentLifecycleManager.start();
        this.componentLifecycleManager.stop();
        final InOrder order = inOrder(this.componentStarted, this.componentNotStarted2, this.componentNotStarted1);
        order.verify(this.componentStarted, times(1)).isRunning();
        order.verify(this.componentStarted, times(1)).stop();
        order.verify(this.componentNotStarted2, times(1)).isRunning();
        order.verify(this.componentNotStarted1, times(1)).isRunning();
    }
}