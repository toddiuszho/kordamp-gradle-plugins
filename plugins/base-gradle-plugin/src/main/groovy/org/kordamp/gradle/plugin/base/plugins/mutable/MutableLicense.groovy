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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.model.mutable.MutableInformation
import org.kordamp.gradle.plugin.base.model.mutable.MutableLicenseSet
import org.kordamp.gradle.plugin.base.plugins.License

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project'])
class MutableLicense extends AbstractFeature implements License {
    final MutableLicenseSet licenses = new MutableLicenseSet()

    MutableLicense(Project project) {
        super(project)
    }

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Map<String, Object>> toMap() {
        Map map = [enabled: enabled]

        map.licenses = licenses.licenses.collectEntries { org.kordamp.gradle.plugin.base.model.mutable.MutableLicense license ->
            [(license.id?.name() ?: license.name): license.toMap()]
        }

        ['license': map]
    }

    void licenses(Action<? super MutableLicenseSet> action) {
        action.execute(licenses)
    }

    void licenses(@DelegatesTo(MutableLicenseSet) Closure action) {
        ConfigureUtil.configure(action, licenses)
    }

    void copyInto(MutableLicense copy) {
        super.copyInto(copy)
        licenses.copyInto(copy.licenses)
    }

    static void merge(MutableLicense o1, MutableLicense o2) {
        AbstractFeature.merge(o1, o2)
        MutableLicenseSet.merge(o1.licenses, o2.licenses)
    }

    List<String> validate(MutableInformation info) {
        List<String> errors = []

        if (!enabled) return errors

        errors = licenses.validate(project)

        errors
    }

    List<org.kordamp.gradle.plugin.base.model.mutable.MutableLicense> allLicenses() {
        licenses.licenses
    }

    @CompileDynamic
    List<String> resolveBintrayLicenseIds() {
        List<String> ids = allLicenses().collect { it.id?.bintray() ?: '' }.unique()
        ids.remove('')
        ids
    }
}
