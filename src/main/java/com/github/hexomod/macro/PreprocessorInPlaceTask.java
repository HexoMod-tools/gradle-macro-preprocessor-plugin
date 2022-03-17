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

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@SuppressWarnings({"WeakerAccess", "unused"})
@CacheableTask
public class PreprocessorInPlaceTask extends DefaultTask {

    public static final String TASK_ID = "macroPreprocessorInPlace";

    private final Project project;
    private final PreprocessorExtension extension;

    @Inject
    public PreprocessorInPlaceTask() {
        this.project = getProject();
        this.extension = project.getExtensions().findByType(PreprocessorExtension.class);
    }

    @TaskAction
    public void process() throws IOException {
        extension.log("Processing files ...");
        // Loop through all SourceSets
        for (SourceSet sourceSet : project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets()) {
            processSourceSet(sourceSet);
        }
    }

    private void processSourceSet(final SourceSet sourceSet) throws IOException {
        extension.log("  Processing sourceSet : " + sourceSet.getName());

        // Java files
        if (extension.getInPlace() || extension.getResources().getInPlace()) {
            processSourceDirectorySet(sourceSet.getJava());
        }

        // Resources files
        if (extension.getInPlace() || extension.getResources().getInPlace()) {
            processSourceDirectorySet(sourceSet.getResources());
        }
    }

    private void processSourceDirectorySet(final SourceDirectorySet sourceDirectorySet) throws IOException {
        extension.log("    Processing directory : " + sourceDirectorySet.getName());

        Preprocessor inPlacePreprocessor = new Preprocessor(extension.getVars(), false);

        for (File sourceDirectory : sourceDirectorySet.getSrcDirs()) {
            for (File sourceFile : project.fileTree(sourceDirectory)) {
                extension.log("    Processing " + sourceFile.toString());
                inPlacePreprocessor.process(sourceFile, sourceFile);
            }
        }
    }
}
