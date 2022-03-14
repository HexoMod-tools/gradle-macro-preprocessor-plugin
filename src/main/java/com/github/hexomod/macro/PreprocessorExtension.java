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

import groovy.lang.Closure;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess","unused"})
public class PreprocessorExtension {

    /**
     * Name of the extension to use in build.gradle
     */
    public static final String NAME = "macroPreprocessorSettings";

    /**
     * The current project
     */
    private final ProjectInternal project;

    /**
     * Project SourceSet
     */
    private final SourceSetContainer sourceSets;

    /**
     * Enable logging to console while preprocessing files
     */
    private boolean verbose;

    /**
     * Remove commented lines
     */
    private boolean remove;

    /**
     * Directory where files will be processed
     */
    private File processDir;

    /**
     * Map of variables
     */
    private final Map<String, Object> vars;


    public PreprocessorExtension(ProjectInternal project, SourceSetContainer sourceSets) {
        this.project = project;
        this.sourceSets = sourceSets;
        this.verbose = false;
        this.processDir = new File(project.getBuildDir(), "preprocessor/macro");
        this.vars = new LinkedHashMap<>();
    }

    public Object sourceSets(Closure closure) {
        return sourceSets.configure(closure);
    }

    public SourceSetContainer getSourceSets() {
        return sourceSets;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean getRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public File getProcessDir() {
        return processDir;
    }

    public void setProcessDir(File processDir) {
        this.processDir = processDir;
    }

    public void setProcessDir(String processDir) {
        String buildName = this.project.getBuildDir().getName();
        if(processDir.startsWith(buildName)) {
            setProcessDir(new File(this.project.getBuildDir().getParentFile(), processDir));
        }
        else {
            setProcessDir(new File(this.project.getBuildDir(), processDir));
        }
    }

    public Map<String, Object> getVars() {
        return this.vars;
    }

    public void setVars(Map<String, Object> vars) {
        this.vars.putAll(vars);
    }

    // Print out a string if verbose is enabled
    public void log(String msg) {
        if(getVerbose()) {
            System.out.println(msg);
        }
    }
}
