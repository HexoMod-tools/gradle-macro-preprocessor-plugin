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
package com.github.hexomod.macro.extensions;

public abstract class SourceType {

    private boolean enable;
    private boolean inPlace;
    private boolean remove;


    public SourceType() {
        enable = true;
        inPlace = false;
        remove = false;
    }

    /**
     * Returns whether this source type is enabled.
     * @return whether this source type is enabled.
     */
    public boolean getEnable() {
        return enable;
    }

    /**
     * Sets whether the preprocessor is enabled for this source type.
     * @param enable Whether this source type is enabled.
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * Returns whether changes are made in-place.
     * @return Whether changes are made in-place.
     */
    public boolean getInPlace() {
        return inPlace;
    }

    /**
     * Sets whether the preprocessor changes should be made in-place.
     * That means that the changes will be applied directly to the source files.
     * The latter is especially useful for development on Java source files.
     * @param inPlace Whether the preprocessor should be applied in-place.
     */
    public void setInPlace(boolean inPlace) {
        this.inPlace = inPlace;
    }

    /**
     * Returns whether preprocessor statements shall be removed.
     * @return Whether preprocessor statements shall be removed.
     */
    public boolean getRemove() {
        return remove;
    }

    /**
     * Sets whether preprocessor statements shall be removed.
     * @param remove Whether preprocessor statements shall be removed.
     */
    public void setRemove(boolean remove) {
        this.remove = remove;
    }
}
