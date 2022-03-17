/*
 * This file is part of MacroPreprocessor, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2019 Hexosse <https://github.com/hexomod-tools/gradle.macro.preprocessor.plugin>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.hexomod.macro;


import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.*;
import org.gradle.language.base.internal.tasks.SimpleStaleClassCleaner;
import org.gradle.language.base.internal.tasks.StaleClassCleaner;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;


@SuppressWarnings({"WeakerAccess", "unused"})
@CacheableTask
public class PreprocessorTask extends Copy {

    public static final String TASK_ID = "macroPreprocessor";
    public static final String TASK_RESOURCE_SUFFIX = "Resource";
    public static final String TASK_JAVA_SUFFIX = "Java";

    public static String getResourceTaskName(SourceSet sourceSet) {
        return TASK_ID + (sourceSet.getName() == "main" ? "" : GUtil.toCamelCase(sourceSet.getName())) + TASK_RESOURCE_SUFFIX;
    }

    public static String getJavaTaskName(SourceSet sourceSet) {
        return TASK_ID + (sourceSet.getName() == "main" ? "" : GUtil.toCamelCase(sourceSet.getName())) + TASK_JAVA_SUFFIX;
    }

    private final Project project;
    private final PreprocessorExtension extension;

    @Nested
    private SourceSet sourceSet;

    @Inject
    public PreprocessorTask() {
        this.project = getProject();
        this.extension = project.getExtensions().findByType(PreprocessorExtension.class);
    }

    public void setSourceSet(SourceSet sourceSets) {
        this.sourceSet = sourceSets;
    }

    protected void copy() {
        if (sourceSet != null) {
            extension.log("Copying files ...");
            StaleClassCleaner cleaner = new SimpleStaleClassCleaner(this.getOutputs());
            cleaner.addDirToClean(this.getDestinationDir());
            cleaner.execute();
            super.copy();
        }
    }

    @TaskAction
    public void process() throws IOException {
        if (sourceSet != null) {
            extension.log("Processing files ...");
            processSourceSet();
        }
    }

    private void processSourceSet() throws IOException {
        extension.log("  Processing sourceSet : " + sourceSet.getName());

        if (getName().equals(getJavaTaskName(sourceSet))) {
            processSourceDirectorySet(sourceSet.getJava(), extension.getRemove() || extension.getJava().getRemove());
            sourceSet.getJava().setSrcDirs(Collections.singletonList(getDestinationDir()));
        }

        if (getName().equals(getResourceTaskName(sourceSet))) {
            processSourceDirectorySet(sourceSet.getResources(), extension.getRemove() || extension.getResources().getRemove());
            sourceSet.getResources().setSrcDirs(Collections.singletonList(getDestinationDir()));
        }
    }

    private void processSourceDirectorySet(final SourceDirectorySet sourceDirectorySet, boolean remove) throws IOException {
        extension.log("    Processing directory : " + sourceDirectorySet.getName());

        Preprocessor preprocessor = new Preprocessor(extension.getVars(), remove);

        for (File sourceDirectory : sourceDirectorySet.getSrcDirs()) {
            for (File sourceFile : project.fileTree(sourceDirectory)) {
                extension.log("    Processing " + sourceFile.toString());
                File processFile = getDestinationDir().toPath().resolve(sourceDirectory.toPath().relativize(sourceFile.toPath())).toFile();
                preprocessor.process(sourceFile, processFile);
            }
        }
    }
}
