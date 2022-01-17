package com.adtsw.jos.dsl.utils;

import java.util.regex.Pattern;

public class PatternFinder {

    protected final static String negativeValuePrefixRegex = "-";
    protected final static String variableNameOrValueRegex = "a-zA-Z0-9\\_\\.";
    protected final static String operatorRegex = "\\!\\*\\+\\-\\/\\<\\>\\=\\%";
    protected final static String bracketsRegex = "\\(\\)";
    protected final static String separatorsRegex = "\\,\\:\\|\\&";
    protected final static String operatorOrSymbolOrBracketsRegex = operatorRegex + separatorsRegex + bracketsRegex;
    protected final static String variableNameOrValueOrSymbolRegex = variableNameOrValueRegex + operatorRegex + separatorsRegex;

    protected final static Pattern alphaNumericPattern = Pattern.compile(
        "^[" + variableNameOrValueRegex + "]+[=]" +
            "([" + negativeValuePrefixRegex + "])?" + "[" + variableNameOrValueRegex + "]+$"
    );
    protected final static Pattern expressionPattern = Pattern.compile(
        "^[" + variableNameOrValueRegex + "]+[=]" +
            "[" + variableNameOrValueRegex + "]+[" + variableNameOrValueOrSymbolRegex + "]+$"
    );
    protected final static Pattern assignmentFunctionCallPattern = Pattern.compile(
        "^[" + variableNameOrValueRegex + "]+[=]" +
            "[" + variableNameOrValueRegex + "]+[\\(][" + variableNameOrValueOrSymbolRegex + "]++[\\)]$"
    );
    protected final static Pattern functionCallPattern = Pattern.compile(
        "^([" + variableNameOrValueRegex + "]+[=])?" +
            "[" + variableNameOrValueRegex + "]+[\\(][" + variableNameOrValueOrSymbolRegex + "]++[\\)]$"
    );
    protected final static Pattern tokenMatcherPattern = Pattern.compile("[" + operatorOrSymbolOrBracketsRegex + "]+");

    public static boolean isAssignmentPattern(String line) {
        return assignmentFunctionCallPattern.matcher(line).matches() || expressionPattern.matcher(line).matches() ||
            alphaNumericPattern.matcher(line).matches();
    }

    public static boolean isFunctionPattern(String objectValue) {
        return functionCallPattern.matcher(objectValue).matches();
    }

    public static boolean isExpressionPattern(String objectValue) {
        return expressionPattern.matcher(objectValue).matches();
    }

    public static boolean isValuePattern(String objectValue) {
        return alphaNumericPattern.matcher(objectValue).matches();
    }
}
