package com.adtsw.jos.dsl.utils;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ObjectLoader {

    private static final Logger logger = LogManager.getLogger(ObjectLoader.class);
    
    private static final String numericRegex = "(-)?([0-9]*[.])?[0-9]+";
    private static final Pattern numericValuePattern = Pattern.compile(numericRegex);

    public static Object getObject(String stringElement) {
        return getObject(stringElement, (Map<String, Object>) null);
    }
    
    public static Object getObject(String stringElement, Map<String, Object> runTimeVariables) {

        boolean converted = false;
        Object runtimeObject = null;

        boolean isNumeric = numericValuePattern.matcher(stringElement).matches();

        if (Objects.equals(stringElement, "null")) {
            converted = true;
        }
        if(isNumeric && !converted) {
            try{
                runtimeObject = Integer.parseInt(stringElement);
                converted = true;
            } catch(NumberFormatException ignored){}
        }
        if(isNumeric && !converted) {
            try {
                runtimeObject = Long.parseLong(stringElement);
                converted = true;
            } catch (NumberFormatException ignored) {
            }
        }
        if(isNumeric && !converted) {
            try{
                runtimeObject = Double.parseDouble(stringElement);
                converted = true;
            } catch(NumberFormatException ignored){}
        }
        if(!converted) {
            try{
                if("true".equals(stringElement) || "false".equals(stringElement)) {
                    runtimeObject = Boolean.parseBoolean(stringElement);
                    converted = true;
                }
            } catch(NumberFormatException ignored){}
        }
        if(!converted && runTimeVariables != null) {
            Object runtimeVariable = runTimeVariables.get(stringElement);
            if(runtimeVariable != null) {
                runtimeObject = runtimeVariable;
                converted = true;
            }
        }
        if(!converted) {
            runtimeObject = stringElement;
        }
        logger.trace(stringElement + " converted to " + runtimeObject);
        return runtimeObject;
    }

    public static <E extends Enum<E>> E enumLookup(Class<E> enumClass, String id) {
        E result;
        try {
            result = Enum.valueOf(enumClass, id);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                "Invalid value for enum " + enumClass.getSimpleName() + ": " + id);
        }
        return result;
    }
}
