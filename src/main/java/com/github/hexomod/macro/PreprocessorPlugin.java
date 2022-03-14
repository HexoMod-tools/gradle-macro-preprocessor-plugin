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

import com.github.hexomod.macro.tasks.PreprocessorJavaTask;
import com.github.hexomod.macro.tasks.PreprocessorResourcesTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.plugins.ide.eclipse.internal.AfterEvaluateHelper;
import org.gradle.util.GUtil;

import java.io.File;


@SuppressWarnings({"unused"})
public class PreprocessorPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {

        // Preprocessor require either java plugin or java-library plugin
        if(!project.getPluginManager().hasPlugin("java") && !project.getPluginManager().hasPlugin("java-library")) {
            throw new ProjectConfigurationException("The \"java\" or \"java-library\" plugin is required by MacroPreprocessor plugin.",new java.lang.Throwable("void apply(Project project)"));
        }

        // Make sure java plugin is applied
        project.getPluginManager().apply(JavaPlugin.class);

        // Configure extension
        PreprocessorExtension extension = configureExtension(project);

        // Configure preprocessors
        project.afterEvaluate( root -> {
            configurePreprocessor(project, extension);
        });
    }

    private PreprocessorExtension configureExtension(Project project) {
        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        return project.getExtensions().create(
                PreprocessorExtension.NAME
                , PreprocessorExtension.class
                , project
                , sourceSets);
    }

    private void configurePreprocessor(Project project, final PreprocessorExtension extension) {
        for(SourceSet sourceSet : extension.getSourceSets()) {
            final JavaCompile compileTask = (JavaCompile) project.getTasks().findByName(sourceSet.getCompileJavaTaskName());
            final ProcessResources resourceTask = (ProcessResources) project.getTasks().findByName(sourceSet.getProcessResourcesTaskName());
            registerPreprocessorTask(project, extension, sourceSet, compileTask).get();
            registerPreprocessorTask(project, extension, sourceSet, resourceTask).get();
        }
    }

    private TaskProvider<PreprocessorJavaTask> registerPreprocessorTask(Project project, PreprocessorExtension extension, SourceSet sourceSet, JavaCompile compileTask) {
        return project.getTasks().register(PreprocessorJavaTask.TASK_ID + (sourceSet.getName() == "main" ? "" : GUtil.toCamelCase(sourceSet.getName())), PreprocessorJavaTask.class, preprocessor -> {
            preprocessor.setDescription("Apply macro to source code.");
            preprocessor.setGroup(BasePlugin.BUILD_GROUP);

            preprocessor.setSource(compileTask.getSource());
            preprocessor.setSourceSet(sourceSet);
            preprocessor.setDestinationDir(new File(new File(extension.getProcessDir(), sourceSet.getName()), "java"));

            compileTask.setSource(preprocessor.getOutputs());
            compileTask.dependsOn(preprocessor);
        });
    }

    private TaskProvider<PreprocessorResourcesTask> registerPreprocessorTask(Project project, PreprocessorExtension extension, SourceSet sourceSet, ProcessResources resourcesTask) {
        return project.getTasks().register(PreprocessorResourcesTask.TASK_ID + (sourceSet.getName() == "main" ? "" : GUtil.toCamelCase(sourceSet.getName())), PreprocessorResourcesTask.class, preprocessor -> {
            preprocessor.setDescription("Apply macro to resources files.");
            preprocessor.setGroup(BasePlugin.BUILD_GROUP);

            preprocessor.from(resourcesTask.getSource());
            preprocessor.setSourceSet(sourceSet);
            preprocessor.into(new File(new File(extension.getProcessDir(), sourceSet.getName()), "resources"));

            resourcesTask.from(preprocessor.getDestinationDir());
            resourcesTask.dependsOn(preprocessor);
        });
    }

}
