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
import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import org.kordamp.gradle.plugin.base.plugins.Kotlindoc

import static org.kordamp.gradle.StringUtils.isBlank

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
@Canonical
@EqualsAndHashCode(excludes = ['project', 'projects', 'kotlindocTasks', 'kotlindocJarTasks'])
class MutableKotlindoc extends AbstractFeature implements Kotlindoc {
    private final Set<String> PLATFORMS = ['Common', 'JVM', 'JS', 'Native'] as Set
    private final Set<String> FORMATS = ['html', 'javadoc', 'html-as-java', 'markdown', 'gfm', 'jekyll'] as Set

    String moduleName = ""
    List<String> outputFormats = ['html']
    File outputDirectory
    List<Object> includes = []
    List<Object> samples = []
    int jdkVersion
    String cacheRoot
    String languageVersion
    String apiVersion
    boolean includeNonPublic = false
    boolean skipDeprecated = false
    boolean reportUndocumented = true
    boolean skipEmptyPackages = true
    boolean noStdlibLink = false
    Set<String> impliedPlatforms = [] as Set
    final MutableLinkMappingSet linkMappings = new MutableLinkMappingSet()
    final MutableExternalDocumentationLinkSet externalDocumentationLinks = new MutableExternalDocumentationLinkSet()
    final MutablePackageOptionSet packageOptions = new MutablePackageOptionSet()

    boolean replaceJavadoc = false

    private final Set<Project> projects = new LinkedHashSet<>()
    private final Set<Task> kotlindocTasks = new LinkedHashSet<>()
    private final Set<Jar> kotlindocJarTasks = new LinkedHashSet<>()

    private boolean replaceJavadocSet
    private boolean includeNonPublicSet
    private boolean skipDeprecatedSet
    private boolean reportUndocumentedSet
    private boolean skipEmptyPackagesSet
    private boolean noStdlibLinkSet

    MutableKotlindoc(Project project) {
        super(project)
        doSetEnabled(project.plugins.findPlugin('kotlin') != null)
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
            List<Map<String, Map<String, String>>> lms = []
            linkMappings.linkMappings.each { lm ->
                if (!lm.empty) lms << [(lm.dir): [
                    url   : lm.url,
                    path  : lm.path,
                    suffix: lm.suffix
                ]]
            }

            List<Map<String, Map<String, String>>> edls = []
            externalDocumentationLinks.externalDocumentationLinks.each { el ->
                if (!el.empty) edls << [(el.url): [
                    packageListUrl: el.packageListUrl
                ]]
            }

            List<Map<String, Map<String, String>>> pos = []
            packageOptions.packageOptions.each { po ->
                if (!po.empty) pos << [(po.prefix): [
                    includeNonPublic  : po.includeNonPublic,
                    reportUndocumented: po.reportUndocumented,
                    skipDeprecated    : po.skipDeprecated,
                    suppress          : po.suppress
                ]]
            }

            map.replaceJavadoc = replaceJavadoc
            map.moduleName = moduleName
            map.outputDirectory = outputDirectory
            map.outputFormats = outputFormats
            map.includes = includes
            map.samples = samples
            map.jdkVersion = jdkVersion
            map.cacheRoot = cacheRoot
            map.languageVersion = languageVersion
            map.samapiVersionples = apiVersion
            map.includeNonPublic = includeNonPublic
            map.skipDeprecated = skipDeprecated
            map.reportUndocumented = reportUndocumented
            map.skipEmptyPackages = skipEmptyPackages
            map.noStdlibLink = noStdlibLink
            map.impliedPlatforms = impliedPlatforms
            map.linkMappings = lms
            map.externalDocumentationLinks = edls
            map.packageOptions = pos
        }

        ['kotlindoc': map]
    }

    void normalize() {
        if (!enabledSet) {
            setEnabled(project.plugins.findPlugin('kotlin') != null)
        }

        if (!impliedPlatforms) {
            impliedPlatforms << 'JVM'
        }

        this
    }

    void setReplaceJavadoc(boolean replaceJavadoc) {
        this.replaceJavadoc = replaceJavadoc
        this.replaceJavadocSet = true
    }

    boolean isReplaceJavadocSet() {
        this.replaceJavadocSet
    }

    void setIncludeNonPublic(boolean includeNonPublic) {
        this.includeNonPublic = includeNonPublic
        this.includeNonPublicSet = true
    }

    boolean isIncludeNonPublicSet() {
        this.includeNonPublicSet
    }

    void setSkipDeprecated(boolean skipDeprecated) {
        this.skipDeprecated = skipDeprecated
        this.skipDeprecatedSet = true
    }

    boolean isSkipDeprecatedSet() {
        this.skipDeprecatedSet
    }

    void setReportUndocumented(boolean reportUndocumented) {
        this.reportUndocumented = reportUndocumented
        this.reportUndocumentedSet = true
    }

    boolean isReportUndocumentedSet() {
        this.reportUndocumentedSet
    }

    void setSkipEmptyPackages(boolean skipEmptyPackages) {
        this.skipEmptyPackages = skipEmptyPackages
        this.skipEmptyPackagesSet = true
    }

    boolean isSkipEmptyPackagesSet() {
        this.skipEmptyPackagesSet
    }

    void setNoStdlibLink(boolean noStdlibLink) {
        this.noStdlibLink = noStdlibLink
        this.noStdlibLinkSet = true
    }

    boolean isNoStdlibLinkSet() {
        this.noStdlibLinkSet
    }

    void linkMappings(Action<? super MutableLinkMappingSet> action) {
        action.execute(linkMappings)
    }

    void linkMappings(@DelegatesTo(MutableLinkMappingSet) Closure action) {
        ConfigureUtil.configure(action, linkMappings)
    }

    void externalDocumentationLinks(Action<? super MutableExternalDocumentationLinkSet> action) {
        action.execute(externalDocumentationLinks)
    }

    void externalDocumentationLinks(@DelegatesTo(MutableExternalDocumentationLinkSet) Closure action) {
        ConfigureUtil.configure(action, externalDocumentationLinks)
    }

    void packageOptions(Action<? super MutablePackageOptionSet> action) {
        action.execute(packageOptions)
    }

    void packageOptions(@DelegatesTo(MutablePackageOptionSet) Closure action) {
        ConfigureUtil.configure(action, packageOptions)
    }

    void copyInto(MutableKotlindoc copy) {
        super.copyInto(copy)
        copy.@replaceJavadoc = replaceJavadoc
        copy.@replaceJavadocSet = replaceJavadocSet
        copy.@includeNonPublic = includeNonPublic
        copy.@includeNonPublicSet = includeNonPublicSet
        copy.@skipDeprecated = skipDeprecated
        copy.@skipDeprecatedSet = skipDeprecatedSet
        copy.@reportUndocumented = reportUndocumented
        copy.@reportUndocumentedSet = reportUndocumentedSet
        copy.@skipEmptyPackages = skipEmptyPackages
        copy.@skipEmptyPackagesSet = skipEmptyPackagesSet
        copy.@noStdlibLink = noStdlibLink
        copy.@noStdlibLinkSet = noStdlibLinkSet
        copy.moduleName = moduleName
        copy.outputFormats = new ArrayList<>(outputFormats)
        copy.outputDirectory = outputDirectory
        copy.jdkVersion = jdkVersion
        copy.cacheRoot = cacheRoot
        copy.languageVersion = languageVersion
        copy.apiVersion = apiVersion
        copy.impliedPlatforms.addAll(impliedPlatforms)
        copy.includes = new ArrayList<>(includes)
        copy.samples = new ArrayList<>(samples)
        linkMappings.copyInto(copy.linkMappings)
        externalDocumentationLinks.copyInto(copy.externalDocumentationLinks)
        packageOptions.copyInto(copy.packageOptions)
    }

    @CompileDynamic
    static void merge(MutableKotlindoc o1, MutableKotlindoc o2) {
        o2.normalize()
        AbstractFeature.merge(o1, o2)
        o1.setReplaceJavadoc((boolean) (o1.replaceJavadocSet ? o1.replaceJavadoc : o2.replaceJavadoc))
        o1.moduleName = o1.moduleName ?: o2.moduleName
        o1.outputFormats = new ArrayList<>(((o1.outputFormats ?: []) + (o2?.outputFormats ?: [])).unique())
        o1.outputDirectory = (o1.outputDirectory ?: o2.outputDirectory) ?: o1.project.file("${o1.project.buildDir}/docs/kotlindoc")
        o1.jdkVersion = (o1.jdkVersion ?: o2.jdkVersion) ?: 6
        o1.cacheRoot = o1.cacheRoot ?: o2.cacheRoot
        o1.languageVersion = o1.languageVersion ?: o2.languageVersion
        o1.apiVersion = o1.apiVersion ?: o2.apiVersion
        o1.setIncludeNonPublic((boolean) (o1.includeNonPublicSet ? o1.includeNonPublic : o2.includeNonPublic))
        o1.setSkipDeprecated((boolean) (o1.skipDeprecatedSet ? o1.skipDeprecated : o2.skipDeprecated))
        o1.setReportUndocumented((boolean) (o1.reportUndocumentedSet ? o1.reportUndocumented : o2.reportUndocumented))
        o1.setSkipEmptyPackages((boolean) (o1.skipEmptyPackagesSet ? o1.skipEmptyPackages : o2.skipEmptyPackages))
        o1.setNoStdlibLink((boolean) (o1.noStdlibLinkSet ? o1.noStdlibLink : o2.noStdlibLink))
        o1.impliedPlatforms = new ArrayList<>(((o1.impliedPlatforms ?: []) + (o2?.impliedPlatforms ?: [])).unique())
        o1.includes = new ArrayList<>(((o1.includes ?: []) + (o2?.includes ?: [])).unique())
        o1.samples = new ArrayList<>(((o1.samples ?: []) + (o2?.samples ?: [])).unique())
        o1.projects().addAll(o2.projects())
        o1.kotlindocTasks().addAll(o2.kotlindocTasks())
        o1.kotlindocJarTasks().addAll(o2.kotlindocJarTasks())
        MutableLinkMappingSet.merge(o1.linkMappings, o2.linkMappings)
        MutableExternalDocumentationLinkSet.merge(o1.externalDocumentationLinks, o2.externalDocumentationLinks)
        MutablePackageOptionSet.merge(o1.packageOptions, o2.packageOptions)
        o1.normalize()
    }

    List<String> validate() {
        List<String> errors = []

        if (!enabled) return errors

        impliedPlatforms.each { platform ->
            if (!PLATFORMS.contains(platform)) {
                errors << "Platform '$platform' is not supported".toString()
            }
        }

        outputFormats.each { format ->
            if (!FORMATS.contains(format)) {
                errors << "Output format '$format' is not supported".toString()
            }
        }

        errors
    }

    Set<Project> projects() {
        projects
    }

    Set<Task> kotlindocTasks() {
        kotlindocTasks
    }

    Set<Jar> kotlindocJarTasks() {
        kotlindocJarTasks
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class MutableLinkMappingSet implements LinkMappingSet {
        final List<MutableLinkMapping> linkMappings = []

        void linkMapping(Action<? super MutableLinkMapping> action) {
            MutableLinkMapping linkMapping = new MutableLinkMapping()
            action.execute(linkMapping)
            linkMappings << linkMapping
        }

        void linkMapping(@DelegatesTo(MutableLinkMapping) Closure action) {
            MutableLinkMapping linkMapping = new MutableLinkMapping()
            ConfigureUtil.configure(action, linkMapping)
            linkMappings << linkMapping
        }

        void copyInto(MutableLinkMappingSet linkMappingSet) {
            linkMappingSet.linkMappings.addAll(linkMappings.collect { it.copyOf() })
        }

        static void merge(MutableLinkMappingSet o1, MutableLinkMappingSet o2) {
            Map<String, MutableLinkMapping> a = o1.linkMappings.collectEntries { [(it.dir): it] }
            Map<String, MutableLinkMapping> b = o2.linkMappings.collectEntries { [(it.dir): it] }

            a.each { k, linkMapping ->
                MutableLinkMapping.merge(linkMapping, b.remove(k))
            }
            a.putAll(b)
            o1.linkMappings.clear()
            o1.linkMappings.addAll(a.values())
        }

        List<MutableLinkMapping> resolveLinkMappings() {
            linkMappings.findAll { !it.isEmpty() }
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class MutableLinkMapping implements LinkMapping {
        String dir
        String path
        String url
        String suffix

        MutableLinkMapping copyOf() {
            MutableLinkMapping copy = new MutableLinkMapping()
            copyInto(copy)
            copy
        }

        void copyInto(MutableLinkMapping copy) {
            copy.dir = dir
            copy.path = path
            copy.url = url
            copy.suffix = suffix
        }

        static void merge(MutableLinkMapping o1, MutableLinkMapping o2) {
            o1.dir = o1.dir ?: o2?.dir
            o1.path = o1.path ?: o2?.path
            o1.url = o1.url ?: o2?.url
            o1.suffix = o1.suffix ?: o2?.suffix
        }

        boolean isEmpty() {
            isBlank(dir) || isBlank(path) || isBlank(url)
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class MutableExternalDocumentationLinkSet implements ExternalDocumentationLinkSet {
        final List<MutableExternalDocumentationLink> externalDocumentationLinks = []

        void externalDocumentationLink(Action<? super MutableExternalDocumentationLink> action) {
            MutableExternalDocumentationLink externalDocumentationLink = new MutableExternalDocumentationLink()
            action.execute(externalDocumentationLink)
            externalDocumentationLinks << externalDocumentationLink
        }

        void externalDocumentationLink(@DelegatesTo(MutableExternalDocumentationLink) Closure action) {
            MutableExternalDocumentationLink externalDocumentationLink = new MutableExternalDocumentationLink()
            ConfigureUtil.configure(action, externalDocumentationLink)
            externalDocumentationLinks << externalDocumentationLink
        }

        void copyInto(MutableExternalDocumentationLinkSet externalDocumentationLinkSet) {
            externalDocumentationLinkSet.externalDocumentationLinks.addAll(externalDocumentationLinks.collect {
                it.copyOf()
            })
        }

        static void merge(MutableExternalDocumentationLinkSet o1, MutableExternalDocumentationLinkSet o2) {
            Map<String, MutableExternalDocumentationLink> a = o1.externalDocumentationLinks.collectEntries {
                [(it.url): it]
            }
            Map<String, MutableExternalDocumentationLink> b = o2.externalDocumentationLinks.collectEntries {
                [(it.url): it]
            }

            a.each { k, externalDocumentationLink ->
                MutableExternalDocumentationLink.merge(externalDocumentationLink, b.remove(k))
            }
            a.putAll(b)
            o1.externalDocumentationLinks.clear()
            o1.externalDocumentationLinks.addAll(a.values())
        }

        List<MutableExternalDocumentationLink> resolveExternalDocumentationLinks() {
            externalDocumentationLinks.findAll { !it.isEmpty() }
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class MutableExternalDocumentationLink implements ExternalDocumentationLink {
        String url
        String packageListUrl

        MutableExternalDocumentationLink copyOf() {
            MutableExternalDocumentationLink copy = new MutableExternalDocumentationLink()
            copyInto(copy)
            copy
        }

        void copyInto(MutableExternalDocumentationLink copy) {
            copy.url = url
            copy.packageListUrl = packageListUrl
        }

        static void merge(MutableExternalDocumentationLink o1, MutableExternalDocumentationLink o2) {
            o1.url = o1.url ?: o2?.url
            o1.packageListUrl = o1.packageListUrl ?: o2?.packageListUrl
        }

        boolean isEmpty() {
            isBlank(url)
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class MutablePackageOptionSet implements PackageOptionSet {
        final List<MutablePackageOption> packageOptions = []

        void packageOption(Action<? super MutablePackageOption> action) {
            MutablePackageOption packageOption = new MutablePackageOption()
            action.execute(packageOption)
            packageOptions << packageOption
        }

        void packageOption(@DelegatesTo(MutablePackageOption) Closure action) {
            MutablePackageOption packageOption = new MutablePackageOption()
            ConfigureUtil.configure(action, packageOption)
            packageOptions << packageOption
        }

        void copyInto(MutablePackageOptionSet packageOptionSet) {
            packageOptionSet.packageOptions.addAll(packageOptions.collect { it.copyOf() })
        }

        static void merge(MutablePackageOptionSet o1, MutablePackageOptionSet o2) {
            Map<String, MutablePackageOption> a = o1.packageOptions.collectEntries { [(it.prefix): it] }
            Map<String, MutablePackageOption> b = o2.packageOptions.collectEntries { [(it.prefix): it] }

            a.each { k, packageOption ->
                MutablePackageOption.merge(packageOption, b.remove(k))
            }
            a.putAll(b)
            o1.packageOptions.clear()
            o1.packageOptions.addAll(a.values())
        }

        List<MutablePackageOption> resolvePackageOptions() {
            packageOptions.findAll { !it.isEmpty() }
        }
    }

    @CompileStatic
    @Canonical
    @ToString(includeNames = true)
    static class MutablePackageOption implements PackageOption {
        String prefix
        boolean includeNonPublic = false
        boolean reportUndocumented = true
        boolean skipDeprecated = false
        boolean suppress = false

        private boolean includeNonPublicSet
        private boolean reportUndocumentedSet
        private boolean skipDeprecatedSet
        private boolean suppressSet

        void setIncludeNonPublic(boolean includeNonPublic) {
            this.includeNonPublic = includeNonPublic
            this.includeNonPublicSet = true
        }

        boolean isIncludeNonPublicSet() {
            this.includeNonPublicSet
        }

        void setReportUndocumented(boolean reportUndocumented) {
            this.reportUndocumented = reportUndocumented
            this.reportUndocumentedSet = true
        }

        boolean isReportUndocumentedSet() {
            this.reportUndocumentedSet
        }

        void setSkipDeprecated(boolean skipDeprecated) {
            this.skipDeprecated = skipDeprecated
            this.skipDeprecatedSet = true
        }

        boolean isSkipDeprecatedSet() {
            this.skipDeprecatedSet
        }

        void setSuppress(boolean suppress) {
            this.suppress = suppress
            this.suppressSet = true
        }

        boolean isSuppressSet() {
            this.suppressSet
        }

        MutablePackageOption copyOf() {
            MutablePackageOption copy = new MutablePackageOption()
            copyInto(copy)
            copy
        }

        void copyInto(MutablePackageOption copy) {
            copy.prefix = prefix
            copy.@includeNonPublic = includeNonPublic
            copy.@includeNonPublicSet = includeNonPublicSet
            copy.@reportUndocumented = reportUndocumented
            copy.@reportUndocumentedSet = reportUndocumentedSet
            copy.@skipDeprecated = skipDeprecated
            copy.@skipDeprecatedSet = skipDeprecatedSet
            copy.@suppress = suppress
            copy.@suppressSet = suppressSet
        }

        static void merge(MutablePackageOption o1, MutablePackageOption o2) {
            o1.prefix = o1.prefix ?: o2?.prefix
            o1.setIncludeNonPublic((boolean) (o1.includeNonPublicSet ? o1.includeNonPublic : o2.includeNonPublic))
            o1.setReportUndocumented((boolean) (o1.reportUndocumentedSet ? o1.reportUndocumented : o2.reportUndocumented))
            o1.setSkipDeprecated((boolean) (o1.skipDeprecatedSet ? o1.skipDeprecated : o2.skipDeprecated))
            o1.setSuppress((boolean) (o1.suppressSet ? o1.suppress : o2.suppress))
        }

        boolean isEmpty() {
            isBlank(prefix)
        }
    }
}
