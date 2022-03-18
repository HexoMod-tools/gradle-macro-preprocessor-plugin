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

import org.gradle.api.*;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.stream.Collectors;


@SuppressWarnings({"unused"})
@CacheableTask
public class PreprocessorPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {

        // Preprocessor require either java plugin or java-library plugin
        if (!project.getPluginManager().hasPlugin("java") && !project.getPluginManager().hasPlugin("java-library")) {
            throw new ProjectConfigurationException("The \"java\" or \"java-library\" plugin is required by MacroPreprocessor plugin.", new java.lang.Throwable("void apply(Project project)"));
        }

        // Make sure java plugin is applied
        project.getPluginManager().apply(JavaPlugin.class);

        // Configure extension
        PreprocessorExtension extension = configureExtension(project);

        //
        PreprocessorInPlaceTask inPlaceTask = RegisterInPlaceTask(project, extension);

        // Register and configure preprocessors task
        project.afterEvaluate(root -> {
            configureInPlacePreprocessor(project, extension, inPlaceTask);
            RegisterPreprocessors(project, extension, inPlaceTask);
        });
    }


    private PreprocessorExtension configureExtension(final Project project) {
        return project.getExtensions().create(
                PreprocessorExtension.NAME
                , PreprocessorExtension.class
                , project);
    }


    private PreprocessorInPlaceTask RegisterInPlaceTask(final Project project, final PreprocessorExtension extension) {
        PreprocessorInPlaceTask inPlaceTask = project.getTasks().register(PreprocessorInPlaceTask.TASK_ID, PreprocessorInPlaceTask.class, preprocessor -> {
            preprocessor.setDescription("Apply macro to source code.");
            preprocessor.setGroup("preprocessor");
        }).get();

        try {
            // Make macroPreprocessor task depends on replacePreprocessor (if exist)
            Task replacePreprocessor = project.getTasks().getByName("replacePreprocessor");
            makeDependsOn(inPlaceTask, replacePreprocessor);
        } catch (UnknownTaskException ignored) {
        }

        return inPlaceTask;
    }

    private void configureInPlacePreprocessor(final Project project, final PreprocessorExtension extension, final PreprocessorInPlaceTask inPlaceTask) {
        // Get all sourceSet to create one preprocessor per sourceSet
        final SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();

        // Get compile task from SourceSet
        for (SourceSet sourceSet : sourceSets) {
            final JavaCompile compileTask = (JavaCompile) project.getTasks().findByName(sourceSet.getCompileJavaTaskName());
            makeDependsOn(compileTask, inPlaceTask);
            final ProcessResources resourceTask = (ProcessResources) project.getTasks().findByName(sourceSet.getProcessResourcesTaskName());
            makeDependsOn(resourceTask, inPlaceTask);
        }
    }


    private void RegisterPreprocessors(final Project project, final PreprocessorExtension extension, final PreprocessorInPlaceTask inPlaceTask) {
        // Get all sourceSet to create one preprocessor per sourceSet
        final SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();

        // Register each preprocessor
        for (SourceSet sourceSet : sourceSets) {
            // Java files
            if (extension.getEnable() && extension.getJava().getEnable()) {
                final JavaCompile compileTask = (JavaCompile) project.getTasks().findByName(sourceSet.getCompileJavaTaskName());
                PreprocessorTask preprocessor = RegisterJavaPreprocessor(project, extension, sourceSet, compileTask).get();
                makeDependsOn(preprocessor, inPlaceTask);
                makeDependsOn(project, preprocessor, "replacePreprocessor" + (sourceSet.getName() == "main" ? "" : GUtil.toCamelCase(sourceSet.getName())) + "Java");
                makeDependsOn(compileTask, preprocessor);
            }
            // Resources files
            if (extension.getEnable() && extension.getResources().getEnable()) {
                final ProcessResources resourceTask = (ProcessResources) project.getTasks().findByName(sourceSet.getProcessResourcesTaskName());
                PreprocessorTask preprocessor = RegisterResourcesPreprocessor(project, extension, sourceSet, resourceTask).get();
                makeDependsOn(preprocessor, inPlaceTask);
                makeDependsOn(project, preprocessor, "replacePreprocessor" + (sourceSet.getName() == "main" ? "" : GUtil.toCamelCase(sourceSet.getName())) + "Resource");
                makeDependsOn(resourceTask, preprocessor);
            }
        }
    }

    private TaskProvider<PreprocessorTask> RegisterJavaPreprocessor(final Project project, final PreprocessorExtension extension, SourceSet sourceSet, JavaCompile compileTask) {
        return project.getTasks().register(PreprocessorTask.getJavaTaskName(sourceSet), PreprocessorTask.class, preprocessor -> {
            preprocessor.setDescription("Apply macro to source code.");
            preprocessor.setGroup("preprocessor");
            preprocessor.setSourceSet(sourceSet);
            preprocessor.from(sourceSet.getJava().getSrcDirs());
            preprocessor.exclude(sourceSet.getResources().getSrcDirs().stream().map(File::getPath).collect(Collectors.toList()));
            preprocessor.setDestinationDir(new File(new File(extension.getProcessDir(), sourceSet.getName()), "java"));
        });
    }

    private TaskProvider<PreprocessorTask> RegisterResourcesPreprocessor(final Project project, final PreprocessorExtension extension, SourceSet sourceSet, ProcessResources resourcesTask) {
        return project.getTasks().register(PreprocessorTask.getResourceTaskName(sourceSet), PreprocessorTask.class, preprocessor -> {
            preprocessor.setDescription("Apply macro to source code.");
            preprocessor.setGroup("preprocessor");
            preprocessor.setSourceSet(sourceSet);
            preprocessor.from(sourceSet.getResources().getSrcDirs());
            preprocessor.setDestinationDir(new File(new File(extension.getProcessDir(), sourceSet.getName()), "resources"));
        });
    }


    private void makeDependsOn(Task instance, Task task) {
        instance.dependsOn(task);
    }

    private void makeDependsOn(final Project project, Task instance, String taskName) {
        try {
            Task task = project.getTasks().getByName(taskName);
            makeDependsOn(instance, task);
        } catch (UnknownTaskException ignored) {
        }
    }

    private void makeDependsOn(final Project project, String instanceName, Task task) {
        try {
            Task instance = project.getTasks().getByName(instanceName);
            makeDependsOn(instance, task);
        } catch (UnknownTaskException ignored) {
        }
    }
}
