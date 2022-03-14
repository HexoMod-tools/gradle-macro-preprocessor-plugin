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
package com.github.hexomod.macro.tasks;


import com.github.hexomod.macro.Preprocessor;
import com.github.hexomod.macro.PreprocessorExtension;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.*;
import org.gradle.plugins.ide.eclipse.internal.AfterEvaluateHelper;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;


@SuppressWarnings({"WeakerAccess","unused"})
@CacheableTask
public class PreprocessorResourcesTask extends Copy {

    public static final String TASK_ID = "macroPreprocessorResources";

    private final Project project;
    private final PreprocessorExtension extension;
    private SourceSet sourceSet;

    @Inject
    public PreprocessorResourcesTask() {
        this.project = getProject();
        this.extension = project.getExtensions().findByType(PreprocessorExtension.class);
    }

    public SourceSet getSourceSet() {
        return sourceSet;
    }

    public void setSourceSet(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
    }

    @TaskAction
    public void process() throws IOException {
        extension.log("Processing resources files ...");
        // Process sourceSet
        processSourceSet(sourceSet);
    }

    private void processSourceSet(final SourceSet sourceSet) throws IOException {
        extension.log("  Processing sourceSet : " + sourceSet.getName());

        SourceDirectorySet resourceDirectorySet = sourceSet.getResources();
        Set<File> resDirs = processSourceDirectorySet(resourceDirectorySet, sourceSet.getName());
    }

    private Set<File> processSourceDirectorySet(final SourceDirectorySet sourceDirectorySet, String sourceSetName) throws IOException {
        extension.log("    Processing directory : " + sourceDirectorySet.getName());

        Set<File> dirs = new LinkedHashSet<>();
        Preprocessor preprocessor = new Preprocessor(extension.getVars(), extension.getRemove());

        for (File sourceDirectory : sourceDirectorySet.getSrcDirs()) {
            String resourceDirName = sourceDirectory.getName();

            File processDir = new File(extension.getProcessDir(), sourceSetName);
            processDir = new File(processDir, resourceDirName);
            FileUtils.forceMkdir(processDir);

            dirs.add(processDir);

            for (File sourceFile : project.fileTree(sourceDirectory)) {
                extension.log("    Processing " + sourceFile.toString());
                File processFile = processDir.toPath().resolve(sourceDirectory.toPath().relativize(sourceFile.toPath())).toFile();
                preprocessor.process(sourceFile, processFile);
            }
        }

        return dirs;
    }
}
