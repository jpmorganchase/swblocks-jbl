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

package org.swblocks.jbl.eh;

/**
 * Indicates a fatal application error that should result in the app termination.
 *
 * <p>It is defined as package private as it is not intended to be used outside of the EH package
 */
class FatalApplicationError extends Error {
    FatalApplicationError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
