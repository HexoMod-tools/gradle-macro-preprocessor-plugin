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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"WeakerAccess","unused"})
public class Preprocessor {

    static final Map<String, String> SLASH_KEYWORDS = new HashMap<String, String>() {{
        put("ifdef",   "//#ifdef");
        put("if",      "//#if");
        put("else",    "//#else");
        put("elseif",  "//#elseif");
        put("endif",   "//#endif");
        put("comment", "///");
    }};

    static final Map<String, String> HASH_KEYWORDS = new HashMap<String, String>() {{
        put("ifdef",   "##ifdef");
        put("if",      "##if");
        put("else",    "##else");
        put("elseif",  "##elseif");
        put("endif",   "##endif");
        put("comment", "###");
    }};

    static Map<String, Map<String, String>> EXTENSION_KEYWORDS = new HashMap<String, Map<String, String>>() {{
        put("java",   SLASH_KEYWORDS);
        put("gradle", SLASH_KEYWORDS);
        put("yaml",   HASH_KEYWORDS);
        put("yml",    HASH_KEYWORDS);
    }};

    private Map<String, Object> vars;
    private boolean remove;

    public Preprocessor(Map<String, Object> vars) {
        this(vars, false);
    }

    public Preprocessor(Map<String, Object> vars, boolean remove) {
        this.vars = vars;
        this.remove = remove;
    }

    public void process(File inFile, File outFile) throws IOException {
        String fileExtension = FilenameUtils.getExtension(inFile.getName());
        // First check if the file need to be processed
        // If not, the file is just copied to its destination
        if(!EXTENSION_KEYWORDS.containsKey(fileExtension)) {
            FileUtils.copyFile(inFile, outFile);
        }
        // If yes, the file is processed
        else {
            //
            try {
                // Convert input file to list of lines
                List<String> lines = FileUtils.readLines(inFile, StandardCharsets.UTF_8);
                // Process lines
                lines = processLines(lines, EXTENSION_KEYWORDS.get(fileExtension));
                // Create parent folder if needed
                FileUtils.forceMkdirParent(outFile);
                // Write output file
                FileUtils.writeLines(outFile, StandardCharsets.UTF_8.toString(), lines, "\n", false );
            }
            catch (Exception e) {
                if(e instanceof ParserException) { throw e; }
                else {  throw new RuntimeException("Failed to convert file " + inFile, e); }
            }
        }
    }

    List<String> processLines(List<String> lines, Map<String, String> keywords) throws ParserException {
        LinkedList<Boolean> state = new LinkedList<>();
        LinkedList<Boolean> skips = new LinkedList<>();
        List<String> newLines = new ArrayList<>();
        // By default the line is considered as active
        state.push(true);
        skips.push(false);

        // Loop through all lines
        for(String line : lines) {
            String trimLine = line.trim();

            // ifdef
            if(trimLine.startsWith(keywords.get("ifdef"))) {
                // Check condition
                boolean active = this.vars.get(trimLine.substring(keywords.get("ifdef").length()).trim()) != null;
                // Store the last active state
                state.push(active & state.getFirst());
                //
                skips.push(active);
                // Keep macro line
                if(!remove) newLines.add(line);
            }
            // if
            else if(trimLine.startsWith(keywords.get("if"))) {
                // Evaluate if condition
                boolean active = evaluateExpression(trimLine.substring(keywords.get("if").length()));
                // Store the last active state
                state.push(active & state.getFirst());
                //
                skips.push(active);
                // Keep macro line
                if(!remove) newLines.add(line);
            }
            // elseif
            else if(trimLine.startsWith(keywords.get("elseif"))) {
                // get last skip
                boolean skip = skips.getFirst();
                //
                if(!skip) {
                    // get last active state
                    boolean active = state.getFirst();
                    // Evaluate elseif condition
                    active = (!active) & evaluateExpression(trimLine.substring(keywords.get("elseif").length()));
                    // Revert the last state
                    state.pop();
                    // Store the last active state
                    state.push((active) & state.getFirst());
                    //
                    skips.pop();
                    skips.push(skip & skips.getFirst());
                }
                else {
                    state.pop();
                    state.push(false);
                }
                // Keep macro line
                if(!remove) newLines.add(line);
            }
            // else
            else if(trimLine.startsWith(keywords.get("else"))) {
                // get last skip
                boolean skip = skips.getFirst();
                //
                if(!skip) {
                    // get last active state
                    boolean active = state.getFirst();
                    // Revert the last state
                    state.pop();
                    state.push((!active) & state.getFirst());
                    //
                    skips.pop();
                    skips.push((!skip) & skips.getFirst());
                }
                else {
                    state.pop();
                    state.push(false);
                }
                // Keep macro line
                if(!remove) newLines.add(line);
            }
            // endif
            else if(trimLine.startsWith(keywords.get("endif"))) {
                // Enable
                state.pop();
                //
                skips.pop();
                // Keep macro line
                if(!remove) newLines.add(line);
            }
            else {
                // get last active state
                boolean active = state.getFirst();
                //
                if(active)
                    newLines.add(uncommentLine(line, keywords));
                else {
                    if(!remove) newLines.add(commentLine(line, keywords));
                }
            }
        }
        return newLines;
    }

    String commentLine(String line, Map<String, String> keywords) {
        if(line.isEmpty()) {
            return line;
        }
        int indent = getIndentSize(line);
        String trimLine = line.trim();
        if(trimLine.startsWith(keywords.get("comment"))) {
            return line;
        }
        else {
            return StringUtils.repeat(" ", indent) +  keywords.get("comment") + " " + trimLine;
        }
    }

    String uncommentLine(String line, Map<String, String> keywords) {
        int indent = getIndentSize(line);
        String trimLine = line.trim();
        if(trimLine.startsWith(keywords.get("comment"))) {
            return StringUtils.repeat(" ", indent) + trimLine.substring(keywords.get("comment").length()).trim();
        }
        else {
            return line;
        }
    }

    int getIndentSize(String str) {
        if(str.isEmpty()) {
            return 0;
        }
        int count = 0;
        while (str.length() > count && Character.isWhitespace(str.charAt(count))) {
            ++count;
        }
        return count;
    }

    Object evaluateVariable(String var) {
        // Test if var can be converted to number
        if (NumberUtils.isCreatable(var)) {
            Object number = NumberUtils.createNumber(var);
            if(number instanceof Float){
                return Double.parseDouble(number.toString());
            } else {
                return number;
            }
        }
        // Test if var can be converted to boolean
        else if((var != null) && (var.equalsIgnoreCase("true") || var.equalsIgnoreCase("false"))) {
            return Boolean.parseBoolean(var);
        }
        //
        else {
            return this.vars.getOrDefault(var, var);
        }
    }

    boolean evaluateExpression(String expr) {
        // Clean the string
        expr = expr.trim();
        // Logical OR
        String[] parts = expr.split("\\|\\|");
        if (parts.length > 1) {
            return Arrays.stream(parts).anyMatch(this::evaluateExpression);
        }
        // Logical AND
        parts = expr.split("&&");
        if (parts.length > 1) {
            return Arrays.stream(parts).allMatch(this::evaluateExpression);
        }
        // Find expression
        Matcher matcher = Pattern.compile("(.+)(<=|>=|==|!=|<|>)(.+)").matcher(expr);
        if (matcher.matches()) {
            Object left = evaluateVariable(matcher.group(1).trim());
            Object right = evaluateVariable(matcher.group(3).trim());
            // Compare booleans
            if(left instanceof Boolean && right instanceof Boolean) {
                boolean nLeft = (Boolean) left;
                boolean nRight = (Boolean) right;
                switch (matcher.group(2)) {
                    case "==": return nLeft == nRight;
                    case "!=": return !(nLeft == nRight);
                }
                return false;
            }
            // Compare numbers
            else if(left instanceof Number && right instanceof Number) {
                double nLeft = Double.parseDouble(left.toString());
                double nRight = Double.parseDouble(right.toString());
                switch (matcher.group(2)) {
                    case "<=": return nLeft <= nRight;
                    case ">=": return nLeft >= nRight;
                    case "==": return nLeft == nRight;
                    case "!=": return !(nLeft == nRight);
                    case "<": return nLeft < nRight;
                    case ">": return nLeft > nRight;
                }
                return false;
            }
            // Compare strings
            else if(left instanceof String && right instanceof String) {
                String sLeft = (String) left;
                String sRight = (String) right;
                switch (matcher.group(2)) {
                    case "<=": return StringUtils.compare(sLeft, sRight) <= 0;
                    case ">=": return StringUtils.compare(sLeft, sRight) >= 0;
                    case "==": return StringUtils.equalsIgnoreCase(sLeft, sRight);
                    case "!=": return !(StringUtils.equalsIgnoreCase(sLeft, sRight));
                    case "<": return StringUtils.compare(sLeft, sRight) < 0;
                    case ">": return StringUtils.compare(sLeft, sRight) > 0;
                }
                return false;
            }
            else {
                return false;
            }
        }
        return false;
    }


    static class ParserException extends RuntimeException {
        ParserException(String e) {
            super(e);
        }
    }
}
