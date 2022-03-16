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
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.*;
import org.gradle.language.base.internal.tasks.SimpleStaleClassCleaner;
import org.gradle.language.base.internal.tasks.StaleClassCleaner;

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

    @OutputDirectory
    File destinationDir;

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

    protected void copy() {
        StaleClassCleaner cleaner = new SimpleStaleClassCleaner(this.getOutputs());
        cleaner.addDirToClean(this.getDestinationDir());
        cleaner.execute();
        super.copy();
    }

    @TaskAction
    public void process() throws IOException {
        extension.log("Processing resources files ...");
        processSourceSet();
    }

    private void processSourceSet() throws IOException {
        extension.log("  Processing sourceSet : " + sourceSet.getName());

        SourceDirectorySet resourceDirectorySet = sourceSet.getResources();
        /*Set<File> resDirs =*/ processSourceDirectorySet(resourceDirectorySet, sourceSet.getName());
    }

    private Set<File> processSourceDirectorySet(final SourceDirectorySet sourceDirectorySet, String sourceSetName) throws IOException {
        extension.log("    Processing directory : " + sourceDirectorySet.getName());

        Set<File> dirs = new LinkedHashSet<>();
        Preprocessor preprocessor = new Preprocessor(extension.getVars(), extension.getRemove() || extension.getResources().getRemove());
        Preprocessor inPlacePreprocessor = new Preprocessor(extension.getVars(), false);

        for (File sourceDirectory : sourceDirectorySet.getSrcDirs()) {
            String resourceDirName = sourceDirectory.getName();

            File destination = getDestinationDir();
            dirs.add(destination);

            for (File sourceFile : project.fileTree(sourceDirectory)) {
                extension.log("    Processing " + sourceFile.toString());
                File processFile = destination.toPath().resolve(sourceDirectory.toPath().relativize(sourceFile.toPath())).toFile();
                preprocessor.process(sourceFile, processFile);

                if(extension.getInPlace() || extension.getResources().getInPlace()) {
                    inPlacePreprocessor.process(sourceFile, sourceFile);
                }
            }
        }

        return dirs;
    }
}
