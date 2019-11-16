/*
 * This file is part of gradle.macro.preprocessor.plugin, licensed under the MIT License (MIT).
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


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;

@SuppressWarnings({"unused"})
public class PreprocessorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        // Register macro preprocessor extension
        PreprocessorExtension extension = project.getExtensions().create(
                PreprocessorExtension.EXTENSION_NAME
                , PreprocessorExtension.class
                , project);

        // Register macro preprocessor task
        TaskProvider<PreprocessorTask> macroPreprocessorTask = project.getTasks().register(
                PreprocessorTask.TASK_ID
                , PreprocessorTask.class);

        // Make sure java plugin is applied
        if (!project.getPlugins().hasPlugin(JavaPlugin.class)) {
            project.getPlugins().apply(JavaPlugin.class);
        }

        // Make compileJava task depends on macro preprocessor
        Task javaCompile = project.getTasks().getByName("compileJava");
        javaCompile.dependsOn(macroPreprocessorTask);

        // Make sourceJava task depends on macro preprocessor (if exist)
        try {
            Task sourceJava = project.getTasks().getByName("sourceJava");
            sourceJava.dependsOn(macroPreprocessorTask);
        } catch (UnknownTaskException ignored) {}
    }
}
