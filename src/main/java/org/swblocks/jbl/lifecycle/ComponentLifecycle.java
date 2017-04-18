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

/**
 * A common interface defining methods for start/stop lifecycle control for components to implement and can be
 * integrated into the JBL lifecycle process. The startup process begins with the lowest phase value and ends with the
 * highest phase value (Integer.MIN_VALUE is the lowest possible, and Integer.MAX_VALUE is the highest possible). The
 * shutdown process will apply the reverse order. Any components with the same value will be arbitrarily ordered within
 * the same phase.
 */
public interface ComponentLifecycle {
    int DEFAULT_PHASE = 5;

    /**
     * Check whether this component is currently running.
     *
     * @return true if component thinks it is running, false otherwise.
     */
    boolean isRunning();

    /**
     * Start this component. Does nothing if component is already running.
     */
    default void start() {
    }

    /**
     * Stops this component in a synchronous call, such that the component is fully stopped upon return of this method.
     */
    default void stop() {
    }

    /**
     * Return the phase value of this object.
     *
     * <p>The lower the value, the earlier in the startup sequence the process will start.
     *
     * @return The phase value of this object
     */
    default int getPhase() {
        return DEFAULT_PHASE;
    }
}
