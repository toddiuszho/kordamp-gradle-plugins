/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
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
package org.kordamp.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.kordamp.gradle.model.Information

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectConfigurationExtension {
    /**
     * Configures a {@code sourceJar} task per {@code SourceSet} if enabled.
     */
    boolean sources = true

    /**
     * Configures {@code javadoc} and {@code javadocJAr} tasks per {@code SourceSet} if enabled.
     */
    boolean apidocs = true

    /**
     * Configures a {@code minPom} task per {@code SourceSet} if enabled.
     */
    boolean minpom = true

    /**
     * Customizes Manifest and MetaInf entries of {@code jar} tasks if enabled.
     */
    boolean release = false

    final Information info

    ProjectConfigurationExtension(Project project) {
        info = new Information(project)
    }

    void info(Action<? super Information> action) {
        action.execute(info)
    }
}
