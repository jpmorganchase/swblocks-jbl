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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * The tests for {@link Result} class.
 */
public class ResultTest {

    @Test
    public void successResult() {
        final Result<String> result = Result.success("succeeded");

        assertEquals(true, result.isSuccess());
        assertEquals("succeeded", result.getData());
        try {
            result.getException();
            fail("A success result will not have an exception");
        } catch (final FatalApplicationError exception) {
            assertEquals("Result.getException is called on successful operation", exception.getCause().getMessage());
        }
    }

    @Test
    public void failureResult() {
        final IllegalStateException illegalStateException = new IllegalStateException("illegal state found");
        final Result result = Result.failure(() -> illegalStateException);

        assertEquals(false, result.isSuccess());
        try {
            result.getData();
            fail("A failure result will not have any data");
        } catch (final FatalApplicationError exception) {
            assertEquals("Result.getData is called on failed operation", exception.getCause().getMessage());
        }
        assertEquals(illegalStateException, result.getException());
    }

    @Test
    public void failureResultWithNullExceptionSupplier() {
        final Result result = Result.failure(null);

        assertEquals(false, result.isSuccess());
        try {
            result.getData();
            fail("A failure result will not have any data");
        } catch (final FatalApplicationError exception) {
            assertEquals("Result.getData is called on failed operation", exception.getCause().getMessage());
        }
        try {
            result.getException();
            fail("A failure result must have an exception");
        } catch (final FatalApplicationError exception) {
            assertEquals("Result.getException - exception not provided", exception.getCause().getMessage());
        }
    }

    @Test
    public void failureResultWithNullException() {
        final Result result = Result.failure(() -> null);

        assertEquals(false, result.isSuccess());
        try {
            result.getData();
            fail("A failure result will not have any data");
        } catch (final FatalApplicationError exception) {
            assertEquals("Result.getData is called on failed operation", exception.getCause().getMessage());
        }
        try {
            result.getException();
            fail("A failure result must have an exception");
        } catch (final FatalApplicationError exception) {
            assertEquals("Result.getException - exception not provided", exception.getCause().getMessage());
        }
    }
}