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


import java.util.function.Predicate;
import java.util.function.Supplier;

import org.swblocks.jbl.eh.Result;

/**
 * Interface that must be implemented by action retrier classes.
 */
@FunctionalInterface
public interface ActionRetrier<T> {
    /**
     * Uses an acceptance condition to determine whether to proceed with the action result.
     *
     * @param action      the action to execute
     * @param shouldRetry predicate to determine if the action should be retried on failure
     * @return the result from the action - success or failure
     */
    Result<T> run(final Supplier<Result<T>> action, final Predicate<Result<T>> shouldRetry);
}