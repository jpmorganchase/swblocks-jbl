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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import org.swblocks.jbl.eh.EhSupport;

/**
 * Class to load additional {@link ComponentLifecycle} components at runtime as defined by an external file mapping of
 * resources to load.
 */
public final class ComponentLoaderUtils {
    /**
     * Private constructor to enforce static use.
     */
    private ComponentLoaderUtils() {
    }

    static void addExternalPathToClasspath(final String newUrl) {
        EhSupport.propagate(() -> {
            final File file = new File(newUrl);
            final URI uri = file.toURI();
            if (file.exists()) {
                final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                final Class<URLClassLoader> urlClass = URLClassLoader.class;
                final Method method = urlClass.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(urlClassLoader, uri.toURL());
            }
        });
    }

    /**
     * Creates an instance of a {@link ComponentLifecycle} as requested in the parameters.
     *
     * <p>This allows the creation of components defined outside the default system classpath.
     *
     * @param resource  Name of resource which contains the class to load, blank if class is already in the
     *                  classloader.
     * @param className Name of {@link ComponentLifecycle} class to load.
     * @return Instance of class to use.
     */
    public static ComponentLifecycle instanceOfLifecycleComponent(final String resource, final String className) {
        return EhSupport.propagateFn(() -> {
            final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            if (resource != null && !resource.isEmpty() && urlClassLoader.findResource(resource) == null) {
                addExternalPathToClasspath(resource);
            }

            final Class clazz = urlClassLoader.loadClass(className);
            @SuppressWarnings("unchecked")
            final Object object = clazz.getConstructor().newInstance();
            if (object instanceof ComponentLifecycle) {
                return (ComponentLifecycle) object;
            }
            return null;
        });
    }
}
