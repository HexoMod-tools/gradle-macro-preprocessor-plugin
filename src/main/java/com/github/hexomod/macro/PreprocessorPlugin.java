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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

@SuppressWarnings({"unused"})
public class PreprocessorPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {

        // Preprocessor require either java plugin or java-library plugin
        if(!project.getPluginManager().hasPlugin("java") && !project.getPluginManager().hasPlugin("java-library")) {
            throw new IllegalStateException("The \"java\" or \"java-library\" plugin is required by MacroPreprocessor plugin.");
        }

        // Make sure java plugin is applied
        project.getPluginManager().apply(JavaPlugin.class);

        //
        PreprocessorExtension extension = configureExtension(project);
        configurePreprocessor(project, extension);
    }

    private PreprocessorExtension configureExtension(Project project) {
        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        return project.getExtensions().create(
                PreprocessorExtension.EXTENSION_NAME
                , PreprocessorExtension.class
                , project
                , sourceSets);
    }

    private void configurePreprocessor(Project project, final PreprocessorExtension extension) {
        PreprocessorTask preprocessorTask = registerPreprocessorTask(project, extension).get();

        // Get compile task from SourceSet
        for(SourceSet sourceSet : extension.getSourceSets()) {
            final Task compileTask = project.getTasks().findByName( sourceSet.getCompileJavaTaskName() );
            compileTask.dependsOn(preprocessorTask);
        }

        // Make replacePreprocessor task depends on macroPreprocessor (if exist)
        try {
            Task replacePreprocessor = project.getTasks().getByName("replacePreprocessor");
            preprocessorTask.dependsOn(replacePreprocessor);
        } catch (UnknownTaskException ignored) {}
    }

    private TaskProvider<PreprocessorTask> registerPreprocessorTask(Project project, PreprocessorExtension extension) {
        return project.getTasks().register(PreprocessorTask.TASK_ID, PreprocessorTask.class, preprocessor -> {
            preprocessor.setDescription("Apply macro to source code.");
            preprocessor.setGroup(BasePlugin.BUILD_GROUP);
        });
    }

}
