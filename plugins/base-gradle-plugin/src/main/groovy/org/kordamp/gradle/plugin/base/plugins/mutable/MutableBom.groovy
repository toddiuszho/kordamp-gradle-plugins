/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 Andres Almiray.
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
package org.kordamp.gradle.plugin.base.plugins.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.api.Project
import org.kordamp.gradle.plugin.base.model.mutable.MutableInformation
import org.kordamp.gradle.plugin.base.plugins.Bom

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
class MutableBom extends AbstractFeature implements Bom {
    Set<String> compile = new LinkedHashSet<>()
    Set<String> runtime = new LinkedHashSet<>()
    Set<String> test = new LinkedHashSet<>()
    Set<String> excludes = new LinkedHashSet<>()

    MutableBom(Project project) {
        super(project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @Override
    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        if (enabled) {
            map.compile = compile
            map.runtime = runtime
            map.test = test
            map.excludes = excludes
        }

        ['bom': map]
    }

    void compile(String str) {
        compile << str
    }

    void runtime(String str) {
        runtime << str
    }

    void test(String str) {
        test << str
    }

    void exclude(String str) {
        excludes << str
    }

    void copyInto(MutableBom copy) {
        super.copyInto(copy)
        copy.compile.addAll(compile)
        copy.runtime.addAll(runtime)
        copy.test.addAll(test)
        copy.excludes.addAll(excludes)
    }

    @CompileDynamic
    static void merge(MutableBom o1, MutableBom o2) {
        AbstractFeature.merge(o1, o2)
        o1.compile.addAll(((o1.compile ?: []) + (o2.compile ?: [])).unique())
        o1.runtime.addAll(((o1.runtime ?: []) + (o2.runtime ?: [])).unique())
        o1.test.addAll(((o1.test ?: []) + (o2.test ?: [])).unique())
        o1.excludes.addAll(((o1.excludes ?: []) + (o2.excludes ?: [])).unique())
    }

    List<String> validate(MutableInformation info) {
        List<String> errors = []

        if (!enabled) return errors

        errors
    }
}
