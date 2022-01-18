package com.adtsw.jos.dsl.utils;

import java.util.regex.Pattern;

public class PatternFinder {

    protected final static String negativeValuePrefixRegex = "-";
    protected final static String variableNameStartRegex = "a-zA-Z";
    protected final static String variableNameRegex = "a-zA-Z0-9\\_\\.";
    protected final static String variableNameOrValueRegex = "a-zA-Z0-9\\_\\.\\\"\\ ";
    protected final static String operatorRegex = "\\!\\*\\+\\-\\/\\<\\>\\=\\%";
    protected final static String bracketsRegex = "\\(\\)";
    protected final static String separatorsRegex = "\\,\\:\\|\\&";
    protected final static String operatorOrSymbolOrBracketsRegex = operatorRegex + separatorsRegex + bracketsRegex;
    protected final static String variableNameOrValueOrSymbolRegex = variableNameOrValueRegex + operatorRegex + separatorsRegex + bracketsRegex;
    
    protected final static String validVariableNameRegex = "[" + variableNameStartRegex + "]([" + variableNameRegex + "]+)?[=]";
    protected final static String optionalValidVariableNameRegex = "(" + validVariableNameRegex + ")?";
    
    protected final static Pattern alphaNumericPattern = Pattern.compile(
        "^" + validVariableNameRegex +
            "([" + negativeValuePrefixRegex + "])?" + "[" + variableNameOrValueRegex + "]+$"
    );
    protected final static Pattern expressionPattern = Pattern.compile(
        "^" + validVariableNameRegex +
        "([" + negativeValuePrefixRegex + "])?" + "[" + variableNameOrValueOrSymbolRegex + "]+$"
    );
    protected final static Pattern assignmentFunctionCallPattern = Pattern.compile(
        "^" + validVariableNameRegex +
            "[" + variableNameOrValueRegex + "]+[\\(][" + variableNameOrValueOrSymbolRegex + "]+[\\)]$"
    );
    protected final static Pattern functionCallPattern = Pattern.compile(
        "^" + optionalValidVariableNameRegex +
            "[" + variableNameOrValueRegex + "]+[\\(][" + variableNameOrValueOrSymbolRegex + "]+[\\)]$"
    );
    protected final static Pattern tokenMatcherPattern = Pattern.compile("[" + operatorOrSymbolOrBracketsRegex + "]+");

    public static boolean isAssignmentPattern(String line) {
        return assignmentFunctionCallPattern.matcher(line).matches() || expressionPattern.matcher(line).matches() ||
            alphaNumericPattern.matcher(line).matches();
    }

    public static boolean isFunctionPattern(String objectValue) {
        boolean matches = functionCallPattern.matcher(objectValue).matches();
        boolean firstBracketEncountered = false;
        if(matches && objectValue.contains("(")) {
            for (int i = 0; i < objectValue.length() && matches; i++) {
                if(i > 0 && objectValue.charAt(i) == '(') {
                    if(!firstBracketEncountered) {
                        if(!Character.isLetterOrDigit(objectValue.charAt(i - 1))) {
                            matches = false;
                        }
                        firstBracketEncountered = true;
                    } else {
                        if(Character.isLetterOrDigit(objectValue.charAt(i - 1))) {
                            matches = false;
                        }
                    }
                }
            }
        }
        return matches;
    }

    public static boolean isExpressionPattern(String objectValue) {
        boolean matches = expressionPattern.matcher(objectValue).matches();
        if(matches && objectValue.contains("(")) {
            for (int i = 0; i < objectValue.length() && matches; i++) {
                if(i > 0 && objectValue.charAt(i) == '(') {
                    char previousChar = objectValue.charAt(i - 1);
                    if(Character.isLetterOrDigit(previousChar)) {
                        matches = false;
                    }
                }
            }
        }
        return matches;
    }

    public static boolean isValuePattern(String objectValue) {
        return alphaNumericPattern.matcher(objectValue).matches();
    }
}
