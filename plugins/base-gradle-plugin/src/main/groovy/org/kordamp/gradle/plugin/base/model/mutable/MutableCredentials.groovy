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
package org.kordamp.gradle.plugin.base.model.mutable

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.kordamp.gradle.plugin.base.model.Credentials

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@ToString(includeNames = true)
class MutableCredentials implements Credentials {
    String name
    String username
    String password

    @Override
    String toString() {
        toMap().toString()
    }

    @CompileDynamic
    Map<String, Object> toMap() {
        [
            name    : name,
            username: username,
            password: ('*' * 12)
        ]
    }

    MutableCredentials copyOf() {
        MutableCredentials copy = new MutableCredentials()
        copyInto(copy)
        copy
    }

    void copyInto(MutableCredentials copy) {
        copy.name = name
        copy.username = username
        copy.password = password
    }

    static MutableCredentials merge(MutableCredentials o1, MutableCredentials o2) {
        if (o1) {
            o1.name = o1.name ?: o2?.name
            o1.username = o1.username ?: o2?.username
            o1.password = o1.password ?: o2?.password
            return o1
        } else if (o2) {
            return o2.copyOf()
        } else {
            return null
        }
    }

    @Override
    boolean isEmpty() {
        isBlank(username) && isBlank(password)
    }
}
