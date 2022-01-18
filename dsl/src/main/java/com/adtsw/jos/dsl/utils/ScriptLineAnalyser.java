package com.adtsw.jos.dsl.utils;

public class ScriptLineAnalyser {

    private static String regex = "\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
    
    public static String getCleanLine(String inputLine) {

        // StringBuffer b = new StringBuffer();
        // Pattern p = Pattern.compile("(\\s+)?([^ \"]+|\"[^\"]*\")*");
        // Matcher m = p.matcher(inputLine);
        // while (m.find()) {
        //     if (m.group(2) != null)
        //         b.append(m.group(2));
        // }
        String replacedString = inputLine.replaceAll(regex, "");
        return replacedString;
    }
}
