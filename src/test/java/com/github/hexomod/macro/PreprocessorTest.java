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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PreprocessorTest {

    private Map<String, Object> vars = new HashMap<String, Object>() {{
        put("VAR_STRING", "value_string");
        put("VAR_BOOL", true);
        put("VAR_INT", 1);
        put("VAR_DOUBLE", 1.32);
    }};

    @Test
    public void getIndentSize() {
        Preprocessor preprocessor = new Preprocessor(vars);
        assertEquals(preprocessor.getIndentSize(" X"), 1);
        assertEquals(preprocessor.getIndentSize(" X "), 1);
        assertEquals(preprocessor.getIndentSize("\tX"), 1);
        assertEquals(preprocessor.getIndentSize("\tX\t"), 1);
        assertEquals(preprocessor.getIndentSize(" \tX"), 2);
        assertEquals(preprocessor.getIndentSize(" \tX \t"), 2);
        assertEquals(preprocessor.getIndentSize(""), 0);
    }

    @Test
    public void evaluateVariable() {
        Preprocessor preprocessor = new Preprocessor(vars);
        // Equals
        assertEquals(preprocessor.evaluateVariable("VAR_STRING"), "value_string");
        assertEquals(preprocessor.evaluateVariable("VAR_BOOL"), true);
        assertSame(preprocessor.evaluateVariable("VAR_BOOL"), preprocessor.evaluateVariable("true"));
        assertEquals(preprocessor.evaluateVariable("VAR_INT"), 1);
        assertSame(preprocessor.evaluateVariable("VAR_INT"), preprocessor.evaluateVariable("1"));
        assertEquals(preprocessor.evaluateVariable("VAR_DOUBLE"), 1.32);
        assertEquals(((Number) preprocessor.evaluateVariable("VAR_DOUBLE")).doubleValue(), ((Number) preprocessor.evaluateVariable("1.32")).doubleValue(), 0.0);
        // Not equals
        assertNotEquals(preprocessor.evaluateVariable("VAR_STRING"), "another string");
        assertNotEquals(preprocessor.evaluateVariable("VAR_BOOL"), false);
        assertNotEquals(preprocessor.evaluateVariable("VAR_INT"), 2);
        assertNotEquals(preprocessor.evaluateVariable("VAR_DOUBLE"), 2.0);
        //
        assertEquals(preprocessor.evaluateVariable("another string"), "another string");
        assertNotEquals(preprocessor.evaluateVariable("another string"), 2);
        // null
        assertNull(preprocessor.evaluateVariable(null));
    }

    @Test
    public void evaluateExpression() {
        Preprocessor preprocessor = new Preprocessor(vars);
        // String
        assertTrue(preprocessor.evaluateExpression("VAR_STRING==value_string"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING == value_string"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING!=another string"));
        assertFalse(preprocessor.evaluateExpression("VAR_STRING==another string"));
        assertFalse(preprocessor.evaluateExpression("VAR_STRING!=value_string"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING<=value_string"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING<value_string_1"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING>=value_strin"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING>value_strin"));
        // bool
        assertTrue(preprocessor.evaluateExpression("VAR_BOOL==true"));
        assertTrue(preprocessor.evaluateExpression(" VAR_BOOL  == true "));
        assertFalse(preprocessor.evaluateExpression("VAR_BOOL==false"));
        assertTrue(preprocessor.evaluateExpression("VAR_BOOL!=false"));
        // Integer
        assertTrue(preprocessor.evaluateExpression("VAR_INT<=1"));
        assertTrue(preprocessor.evaluateExpression("VAR_INT>=1"));
        assertTrue(preprocessor.evaluateExpression("VAR_INT==1"));
        assertTrue(preprocessor.evaluateExpression("VAR_INT<2"));
        assertTrue(preprocessor.evaluateExpression("VAR_INT>0"));
        assertTrue(preprocessor.evaluateExpression("VAR_INT!=0"));
        assertFalse(preprocessor.evaluateExpression("VAR_INT==2"));
        // Double
        assertTrue(preprocessor.evaluateExpression("VAR_DOUBLE==1.32"));
        assertTrue(preprocessor.evaluateExpression("VAR_DOUBLE!=0"));
        // Complex
        assertTrue(preprocessor.evaluateExpression("VAR_STRING==value_string && VAR_INT==1"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING==another string || VAR_INT==1"));
    }
}