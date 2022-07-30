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

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.hexomod.macro.Preprocessor.SLASH_KEYWORDS;
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
        assertTrue(preprocessor.evaluateExpression("VAR_BOOL==true && VAR_INT==1"));
        assertTrue(preprocessor.evaluateExpression("VAR_STRING==another string || VAR_INT==1"));
    }

    @Test
    public void processLines_simple_if_true() {

        String testLine = "String message = 'test';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_INT==1");
        lines.add("///" + testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(1));
    }

    @Test
    public void processLines_simple_if_false() {

        String testLine = "String message = 'test';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_INT!=1");
        lines.add(testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertNotEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(1));
    }

    @Test
    public void processLines_complex_if_true() {

        String testLine1 = "String message1 = 'test 1';";
        String testLine2 = "String message2 = 'test 2';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_INT==1");
        lines.add(testLine1);
        lines.add("//#if VAR_BOOL==true");
        lines.add(testLine2);
        lines.add("//#endif");
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(1));

        assertEquals(preprocessor.uncommentLine(testLine2, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.commentLine(testLine2, SLASH_KEYWORDS), lines.get(3));
    }

    @Test
    public void processLines_complex_if_false() {

        String testLine1 = "String message1 = 'test 1';";
        String testLine2 = "String message2 = 'test 2';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_INT==1");              // true
        lines.add(testLine1);
            lines.add("//#if VAR_BOOL==false");     // false
            lines.add(testLine2);
            lines.add("//#endif");
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.uncommentLine(testLine2, SLASH_KEYWORDS), lines.get(3));

        assertNotEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.commentLine(testLine2, SLASH_KEYWORDS), lines.get(3));
    }

    @Test
    public void processLines_simple_elseif_t_f_f() {

        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_DOUBLE>=1");           // true
        lines.add(testLine0);
        lines.add("//#elseif VAR_INT>=0");          // false
        lines.add(testLine1);
        lines.add("//#else");                       // false
        lines.add(testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(5));

        assertNotEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(5));
    }

    @Test
    public void processLines_simple_elseif_f_t_f() {

        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_INT==0");          // false
        lines.add(testLine0);
        lines.add("//#elseif VAR_INT==1");      // true
        lines.add(testLine1);
        lines.add("//#else");                   // false
        lines.add(testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertNotEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(5));

        assertEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(5));
    }

    @Test
    public void processLines_simple_elseif_f_f_t() {

        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_DOUBLE>=2");           // false
        lines.add(testLine0);
        lines.add("//#elseif VAR_INT>1");           // false
        lines.add(testLine1);
        lines.add("//#else");                       // true
        lines.add(testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertNotEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(5));

        assertEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(5));
    }

	@Test
	public void processLines_simple_elseif_f_t_f_f() {
		String testLine0 = "String message = '0';";
		String testLine1 = "String message = '1';";
		String testLine2 = "String message = '2';";
		String testLine = "String message = '';";

		List<String> lines = new ArrayList<>();
		lines.add("//#if VAR_INT>100");            // false
		lines.add(testLine0);
		lines.add("//#elseif VAR_INT<10");         // true
		lines.add(testLine1);
		lines.add("//#elseif VAR_BOOL");           // false (but condition true)
		lines.add(testLine2);
		lines.add("//#else");
		lines.add(testLine);
		lines.add("//#endif");

		Preprocessor preprocessor = new Preprocessor(vars);
		lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

		assertEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
		assertEquals(testLine1, lines.get(3));
		assertEquals(preprocessor.commentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
		assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(7));
	}

	@Test
	public void processLines_complex_elseif_t_f_f() {

        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine2 = "String message = '2';";
        String testLine3 = "String message = '3';";
        String testLine4 = "String message = '4';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_DOUBLE>=1");           // true
        lines.add(testLine0);
            lines.add("//#if VAR_DOUBLE>=1");           // true
            lines.add(testLine1);
            lines.add("//#elseif VAR_INT>=0");          // false
            lines.add(testLine2);
            lines.add("//#else");                       // false
            lines.add(testLine3);
            lines.add("//#endif");
        lines.add("//#elseif VAR_INT>=0");          // false
        lines.add(testLine4);
        lines.add("//#else");                       // false
        lines.add(testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.uncommentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
        assertNotEquals(preprocessor.uncommentLine(testLine3, SLASH_KEYWORDS), lines.get(7));
        assertNotEquals(preprocessor.uncommentLine(testLine4, SLASH_KEYWORDS), lines.get(10));
        assertNotEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(12));

        assertNotEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertEquals(preprocessor.commentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
        assertEquals(preprocessor.commentLine(testLine3, SLASH_KEYWORDS), lines.get(7));
        assertEquals(preprocessor.commentLine(testLine4, SLASH_KEYWORDS), lines.get(10));
        assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(12));
    }

    @Test
    public void processLines_complex_elseif_f_t_f() {

        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine2 = "String message = '2';";
        String testLine3 = "String message = '3';";
        String testLine4 = "String message = '4';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_INT==0");          // false
        lines.add(testLine0);
            lines.add("//#if VAR_INT==0");          // false
            lines.add(testLine1);
            lines.add("//#elseif VAR_INT==1");      // false
            lines.add(testLine2);
            lines.add("//#else");                   // false
            lines.add(testLine3);
            lines.add("//#endif");
        lines.add("//#elseif VAR_INT==1");      // true
        lines.add(testLine4);
        lines.add("//#else");                   // false
        lines.add(testLine);
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertNotEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.uncommentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
        assertNotEquals(preprocessor.uncommentLine(testLine3, SLASH_KEYWORDS), lines.get(7));
        assertEquals(preprocessor.uncommentLine(testLine4, SLASH_KEYWORDS), lines.get(10));
        assertNotEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(12));

        assertEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertEquals(preprocessor.commentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
        assertEquals(preprocessor.commentLine(testLine3, SLASH_KEYWORDS), lines.get(7));
        assertNotEquals(preprocessor.commentLine(testLine4, SLASH_KEYWORDS), lines.get(10));
        assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(12));
    }

    @Test
    public void processLines_complex_elseif_f_f_t() {

        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine2 = "String message = '2';";
        String testLine3 = "String message = '3';";
        String testLine4 = "String message = '4';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#if VAR_DOUBLE>=2");           // false
        lines.add(testLine0);
        lines.add("//#elseif VAR_INT>1");           // false
        lines.add(testLine1);
        lines.add("//#else");                       // true
        lines.add(testLine2);
            lines.add("//#if VAR_DOUBLE>=2");           // false
            lines.add(testLine3);
            lines.add("//#elseif VAR_INT>1");           // false
            lines.add(testLine4);
            lines.add("//#else");                       // true
            lines.add(testLine);
            lines.add("//#endif");
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertNotEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertNotEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertEquals(preprocessor.uncommentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
        assertNotEquals(preprocessor.uncommentLine(testLine3, SLASH_KEYWORDS), lines.get(7));
        assertNotEquals(preprocessor.uncommentLine(testLine4, SLASH_KEYWORDS), lines.get(9));
        assertEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(11));

        assertEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(1));
        assertEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(3));
        assertNotEquals(preprocessor.commentLine(testLine2, SLASH_KEYWORDS), lines.get(5));
        assertEquals(preprocessor.commentLine(testLine3, SLASH_KEYWORDS), lines.get(7));
        assertEquals(preprocessor.commentLine(testLine4, SLASH_KEYWORDS), lines.get(9));
        assertNotEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(11));
    }

    @Test
    public void processLines_simple_ifdef_elseif_t_f_f() {

        String debug = "String message = '0';";
        String testLine0 = "String message = '0';";
        String testLine1 = "String message = '1';";
        String testLine = "String message = '';";

        List<String> lines = new ArrayList<>();
        lines.add("//#ifdef VAR_BOOL");                 // true
            lines.add("//#if VAR_DOUBLE>=1");           // true
            lines.add(testLine0);
            lines.add("//#elseif VAR_INT>=0");          // false
            lines.add(testLine1);
            lines.add("//#else");                       // false
            lines.add(testLine);
            lines.add("//#endif");
        lines.add("//#endif");

        Preprocessor preprocessor = new Preprocessor(vars);
        lines = preprocessor.processLines(lines, SLASH_KEYWORDS);

        assertEquals(preprocessor.uncommentLine(testLine0, SLASH_KEYWORDS), lines.get(2));
        assertNotEquals(preprocessor.uncommentLine(testLine1, SLASH_KEYWORDS), lines.get(4));
        assertNotEquals(preprocessor.uncommentLine(testLine, SLASH_KEYWORDS), lines.get(6));

        assertNotEquals(preprocessor.commentLine(testLine0, SLASH_KEYWORDS), lines.get(2));
        assertEquals(preprocessor.commentLine(testLine1, SLASH_KEYWORDS), lines.get(4));
        assertEquals(preprocessor.commentLine(testLine, SLASH_KEYWORDS), lines.get(6));
    }
}
