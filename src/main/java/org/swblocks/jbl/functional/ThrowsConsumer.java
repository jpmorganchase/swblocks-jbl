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

package org.swblocks.jbl.functional;

/**
 * See docs for {@link java.util.function.Consumer}
 *
 * <p>The main difference between {@link java.util.function.Consumer} and {@link ThrowsConsumer} is that this one can
 * throw an exception
 */
@FunctionalInterface
public interface ThrowsConsumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param input the input argument
     * @throws Throwable Any checked or unchecked exception or throwable
     */
    void accept(T input) throws Throwable;
}
