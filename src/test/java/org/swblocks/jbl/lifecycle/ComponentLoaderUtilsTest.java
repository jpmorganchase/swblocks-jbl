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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import org.junit.Test;
import org.swblocks.jbl.test.utils.JblTestClassUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link ComponentLoaderUtils}.
 */
public class ComponentLoaderUtilsTest {
    @Test
    public void hasPrivateConstructor() {
        JblTestClassUtils.assertConstructorIsPrivate(ComponentLoaderUtils.class);
    }

    @Test
    public void testAddingJarToSystemClassPath() throws UnsupportedEncodingException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("TestResourceFile.txt");
        assertNull(is);

        ComponentLoaderUtils.addExternalPathToClasspath("src/test/component-loader-test.resources");
        ComponentLoaderUtils.addExternalPathToClasspath("jbl/src/test/component-loader-test.resources");
        is = getClass().getClassLoader().getResourceAsStream("TestResourceFile.txt");
        assertNotNull(is);
        final String resourceInfo =
                new BufferedReader(new InputStreamReader(is, "UTF-8")).lines().collect(Collectors.joining());
        assertEquals("Test", resourceInfo);
    }

    @Test
    public void testCreatingLifeCycleClass() {
        final ComponentLifecycle lifecycle = ComponentLoaderUtils.instanceOfLifecycleComponent("",
                "org.swblocks.jbl.lifecycle.ComponentLoaderUtilsTest$ComponentLifecycleMock");
        assertNotNull(lifecycle);
        assertTrue(lifecycle instanceof ComponentLifecycleMock);
    }

    /**
     * A dummy test class to be dynamically loaded and created.
     */
    private static class ComponentLifecycleMock implements ComponentLifecycle {
        public ComponentLifecycleMock() {
        }

        @Override
        public boolean isRunning() {
            return false;
        }
    }
}