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

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


@SuppressWarnings({"WeakerAccess","unused"})
@CacheableTask
public class PreprocessorJavaTask extends SourceTask {

    public static final String TASK_ID = "macroPreprocessorJava";

    private final Project project;
    private final PreprocessorExtension extension;
    private SourceSet sourceSet;

    @OutputDirectory
    private File destinationDir;

    @Inject
    public PreprocessorJavaTask() {
        this.project = getProject();
        this.extension = project.getExtensions().findByType(PreprocessorExtension.class);
    }

    public SourceSet getSourceSet() {
        return sourceSet;
    }

    public void setSourceSet(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
    }

    public File getDestinationDir() {
        return project.file(destinationDir);
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    @TaskAction
    public void process() throws IOException {
        extension.log("Processing java files ...");
        processSourceSet();
    }

    private void processSourceSet() throws IOException {
        extension.log("  Processing sourceSet : " + sourceSet.getName());

        SourceDirectorySet javaDirectorySet = sourceSet.getJava();
        /*Set<File> srcDirs =*/ processSourceDirectorySet(javaDirectorySet, sourceSet.getName());
    }

    private Set<File> processSourceDirectorySet(final SourceDirectorySet sourceDirectorySet, String sourceSetName) throws IOException {
        extension.log("    Processing directory : " + sourceDirectorySet.getName());

        Set<File> dirs = new LinkedHashSet<>();
        Preprocessor preprocessor = new Preprocessor(extension.getVars(), extension.getRemove() || extension.getJava().getRemove());
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
