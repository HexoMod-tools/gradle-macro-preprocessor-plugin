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


import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings({"WeakerAccess","unused"})
public class PreprocessorTask extends DefaultTask {

    // The task ID
    public static final String TASK_ID = "macroPreprocessor";

    // Extension
    private final PreprocessorExtension extension;


    @Inject
    public PreprocessorTask() {
        this.extension = getProject().getExtensions().findByType(PreprocessorExtension.class);
    }

    @TaskAction
    public void process() throws IOException {

        // Instantiate the preprocessor
        Preprocessor preprocessor = new Preprocessor(this.extension.getVars());

        log("Starting macro preprocessor");

        // Data
        Project project = this.extension.getProject();
        Set<String> sources = this.extension.getSources();
        Set<String> resources = this.extension.getResources();
        File target = this.extension.getTarget();
        File srcTarget = new File(target, "java");
        File resTarget = new File(target, "resources");

        log("  Checking sources folders...");

        // Default sources
        if(sources.isEmpty()) {
            project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
                Set<File> srcDirs = sourceSet.getJava().getSrcDirs();
                for(File srcDir: srcDirs) {
                    sources.add(srcDir.getAbsolutePath());
                }
            });
        }
        // Check
        for(String source : sources) {
            if(!Files.isDirectory((new File(source)).toPath())) {
                log("    " + source + " is not a valid folder!");
            }
        }

        log("  Checking resources folders...");

        // Default resources
        if(resources.isEmpty()) {
            project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
                Set<File> resDirs = sourceSet.getResources().getSrcDirs();
                for(File resDir: resDirs) {
                    resources.add(resDir.getAbsolutePath());
                }
            });
        }
        // Check
        for(String resource : resources) {
            if(!Files.isDirectory((new File(resource)).toPath())) {
                log("    " + resource + " is not a valid folder!");
            }
        }

        log("  Creating target folder...");

        // Create target directory
        FileUtils.forceMkdirParent(srcTarget);
        FileUtils.forceMkdirParent(resTarget);

        log("  Processing files...");

        // Loop throw all source files
        for (String source : sources) {
            File srcDir = new File(source);
            for (File file : project.fileTree(srcDir)) {
                log("    Processing " + file.toString());
                File out = srcTarget.toPath().resolve(srcDir.toPath().relativize(file.toPath())).toFile();
                preprocessor.process(file, out);
            }
        }
        // Loop throw all resource files
        for (String resource : resources) {
            File resDir = new File(resource);
            for (File file : project.fileTree(resDir)) {
                log("    Processing " + file.toString());
                File out = resTarget.toPath().resolve(resDir.toPath().relativize(file.toPath())).toFile();
                preprocessor.process(file, out);
            }
        }

        log("  Updating main sourset...");

        SourceSet main = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        main.getJava().setSrcDirs(Collections.singleton(srcTarget));
        main.getResources().setSrcDirs(Collections.singleton(resTarget));
    }

    // Print out a string if verbose is enable
    private void log(String msg) {
        if(this.extension != null && this.extension.isVerbose()) {
            System.out.println(msg);
        }
    }
}
