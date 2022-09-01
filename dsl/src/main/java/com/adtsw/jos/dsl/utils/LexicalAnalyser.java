package com.adtsw.jos.dsl.utils;

import com.adtsw.jcommons.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyser {

    private static final String variableNameRegex = "[a-zA-Z0-9\\_\\.\\\"\\ ]+";
    private static final String numericRegex = "(-)?([0-9]*[.])?[0-9]+";
    
    private static final String variableNameOrNumericValue = "(" + variableNameRegex + ")|(" + numericRegex + ")";
    private static final Pattern variableNameOrNumericValuePattern = Pattern.compile(variableNameOrNumericValue);
                
    private static final String arithmeticOperatorRegex = "(\\+)|(\\-)|(\\*)|(\\/)|(\\^)|(\\%)";
    private static final String booleanOperatorRegex = "(\\=\\=)|(\\!\\=)|(\\<\\=)|(\\>\\=)|(\\<)|(\\>)|(\\&\\&)|(\\|\\|)|(\\!)|(\\=)";
    private static final String bracketsRegex = "(\\()|(\\))";
    private static final String separatorsRegex = "(\\,)|(\\:)";
    private static final String tokenMatcherRegex = "(" +
        variableNameOrNumericValue + "|" +
        arithmeticOperatorRegex + "|" + bracketsRegex + "|" + booleanOperatorRegex + "|" + separatorsRegex +
        ")";
    private static final Pattern tokenPattern = Pattern.compile(tokenMatcherRegex);
    
    public static Object[] getLexemes(String objectValue) {
        List<String> lexemes = new ArrayList<>();
        Matcher tokenMatcher = tokenPattern.matcher(objectValue);
        int lexemeCount = 0;
        int lexemeCharCount = 0;
        while(tokenMatcher.find()) {
            String lexeme = tokenMatcher.group();
            lexemeCharCount = lexemeCharCount + lexeme.length();
            if(lexeme.startsWith("-") && lexeme.length() > 1 && lexemes.size() > 0) {
                String lastLexeme = lexemes.get(lexemes.size() - 1);
                if(variableNameOrNumericValuePattern.matcher(lastLexeme).matches()) {
                    lexemes.add("-");
                    lexemes.add(lexeme.substring(1));
                } else {
                    lexemes.add(lexeme);        
                }
            } else {
                lexemes.add(lexeme);
            }
            lexemeCount ++;
        }
        if(lexemeCharCount != objectValue.length()) {
            throw new RuntimeException("Lexeme count mismatch : " + objectValue + " -> " + JsonUtil.write(lexemes));
        }
        Object[] lexemeObjects = new Object[lexemes.size()];
        for (int i = 0; i < lexemes.size(); i++) {
            Object lexemeObject = ObjectLoader.getObject(lexemes.get(i), (Map<String, Object>) null);
            lexemeObjects[i] = lexemeObject;
        }
        return lexemeObjects;
    }
}
