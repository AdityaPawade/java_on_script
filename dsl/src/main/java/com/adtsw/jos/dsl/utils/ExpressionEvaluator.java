package com.adtsw.jos.dsl.utils;

import com.adtsw.jcommons.utils.ArithmeticUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

    private final Pattern tokenPattern = Pattern.compile("((([0-9]*[.])?[0-9]+)|([\\+\\-\\*\\/\\(\\)\\^\\%]))");

    private final Map<String, Integer> operatorPriority = new HashMap<>() {{
        put("-", 0);
        put("+", 0);
        put("/", 1);
        put("*", 1);
        put("^", 2);
        put("%", 2);
        put(">", 1);
        put("<", 1);
        put(">=", 1);
        put("<=", 1);
        put("==", 1);
        put("!=", 1);
        put("!", 1);
        put("&&", 0);
        put("||", 0);
    }};

    private final Set<String> binaryOperators = new HashSet<>() {{
        add("-");add("+");add("/");add("*");add("^");add("%");
        add(">");add("<");add(">=");add("<=");add("==");add("!=");add("&&");add("||");
    }};

    private final Set<String> unaryOperators = new HashSet<>() {{
        add("!");
    }};

    /**
     * Performs the Shunting Yard algorithm
     * @param /expression the mathematical expression
     * @return the result, if found
     * @throws IllegalArgumentException invalid expression exception
     * @throws NumberFormatException error while parsing a number, probably due to an invalid expression
     * @throws ArithmeticException division by 0
     */
    public Object evaluate(String expression) throws IllegalArgumentException, NumberFormatException, ArithmeticException{
        
        // throw an exception if the string is null or empty
        if(expression == null || expression.trim().length() == 0)
            throw new IllegalArgumentException("Empty expression or null");

        //remove all blank spaces
        expression = expression.replaceAll("\\s+","");
        //add a 0 before the "-" in order to consider the "-" as a standalone operator
        expression = expression.replace("(-", "(0-");
        //same thing here
        if (expression.startsWith("-")){
            expression = "0" + expression;
        }

        //read the expression and check if it contains only allowed token
        Matcher matcher = tokenPattern.matcher(expression);

        int counter = 0; //must be equal to the index of the end of the last matching group
        List<Object> tokens = new ArrayList<>();
        while(matcher.find()){
            if(matcher.start() != counter){
                //at this point counter indicates the end of the last matching group. If the next matching group
                //doesn't start at this index, it means that some characters were skipped
                throw new IllegalArgumentException("Invalid Expression:" + expression + ". Error between " + counter+ " end " + matcher.start());
            }
            String token = matcher.group().trim();
            tokens.add(ObjectLoader.getObject(token));//add the token if it's okay
            counter += token.length();//update the counter
        }
        if(counter != expression.length()){
            //if the matcher reaches the end of the string, we want to check if the last matching group ends at the end of the expression
            throw new IllegalArgumentException("Invalid end of expression");
        }

        return evaluate(tokens.toArray());
    }

    public Object evaluate(Object[] tokens) {

        List<Object> operationQueue = getOperationQueue(tokens);
        // Then, the output queue represents normally the RPN
        return operate(operationQueue);
    }

    private Object operate(List<Object> operationQueue) {
        Stack<Object> executionStack = new Stack<>();
        for(Object token : operationQueue) {
            //for each token (normally there are only numbers OR operators)
            //if the token is not an operator and is a number, try to parse it and push it onto the stack
            if(token == null) {
                executionStack.push(null);
            } else if(token instanceof Double){
                executionStack.push(token);
            } else if(token instanceof Float) {
                executionStack.push(token);
            } else if(token instanceof Integer) {
                executionStack.push(token);
            } else if(token instanceof Boolean) {
                executionStack.push(token);
            } else {
                // if it's an operator, get the 2 elements at the top and perform the right operation.
                // you should remind that is always op2 (operator) op1
                String operator = (String) token;
                Object result;
                if(binaryOperators.contains(operator) && executionStack.size() > 1){
                    Object operand1 = executionStack.pop();
                    Object operand2 = executionStack.pop();
                    //System.out.println("executing " + operand2 + " " + operator + " " + operand1);
                    result = executeBinaryOperation(token, operator, operand1, operand2);
                    //System.out.println(operand2 + " " + operator + " " + operand1 + " = " + result);
                } else if(unaryOperators.contains(operator) && executionStack.size() > 0) {
                    Object operand1 = executionStack.pop();
                    //System.out.println("executing " + operator + " " + operand1);
                    result = executeUnaryOperation(token, operator, (Boolean) operand1);
                    //System.out.println(operator + " " + operand1 + " = " + result);
                } else {
                    throw new IllegalArgumentException(token + " is not an operator or is not handled");
                }
                executionStack.push(result);
            }
        }
        //normally, it remains only one number in the stack
        if(executionStack.empty() || executionStack.size() > 1){
            throw new IllegalArgumentException("Invalid expression, could not find a result. An operator seems to be absent");
        }
        return executionStack.peek();
    }

    private List<Object> getOperationQueue(Object[] tokens) {
        List<Object> outputQueue = new ArrayList<>(); //output queue
        Stack<String> operatorStack = new Stack<>(); //operators stack

        //the main algorithm
        for (int i = 0; i < tokens.length; i++) {
            Object token = tokens[i];
            //read the token. We have 4 options:
            // - it's an operator
            // - it's a (
            // - it's a )
            // - it's a number
            if (token instanceof String) {
                if (operatorPriority.containsKey(token)) {
                    //it's an operator.
                    //We have to check:
                    // - if the stack is not empty
                    // - if the element on the top of the stack is an operator
                    // - if the operator represented by the token has a priority less or equal than the operator
                    //   represented by the element on the top of the stack, and if is left-associated
                    //   OR
                    //   if the operator represented by the token has a priority less than the operator represented
                    //   by the element on the top of the stacke, and is right associated
                    while (!operatorStack.empty() &&
                        operatorPriority.containsKey(operatorStack.peek()) &&
                        ((operatorPriority.get(token) <= operatorPriority.get(operatorStack.peek()) && !token.equals("^")) ||
                            (operatorPriority.get(token) < operatorPriority.get(operatorStack.peek()) && token.equals("^")))) {
                        outputQueue.add(operatorStack.pop()); //pop the element on the top of the stack and add it to the output
                    }
                    operatorStack.push((String) token); // finally, push the token on the top of the stack

                } else if (token.equals("(")) {
                    //if it's a left parenthesis, push it onto the stack. It will be removed as the associted right parenthesis will be found
                    operatorStack.push((String) token);
                } else if (token.equals(")")) {
                    //if it's a right parenthesis, pop the stack until a left parenthesis is found, or the the stack is empty 
                    while (!operatorStack.empty()) {
                        if (!operatorStack.peek().equals("(")) {
                            outputQueue.add(operatorStack.pop());
                        } else {
                            break;
                        }
                    }
                    if (!operatorStack.empty()) {
                        operatorStack.pop();// finally, remove the left parenthesis
                    }
                }
            } else {
                outputQueue.add(token); //numbers are immediately added to the output queue
            }
        }

        while(!operatorStack.empty()){
            outputQueue.add(operatorStack.pop());
            // while the stack is not empty, pop elements (normally there are only operators in the stack 
            // at this point) and add it to the ouput queue
        }
        return outputQueue;
    }

    private Object executeBinaryOperation(Object token, String operator, Object operand1, Object operand2) {
        Object result;
        switch (operator) {
            case "+":
                result = ArithmeticUtil.add((Number) operand2, (Number) operand1);
                break;
            case "-":
                result = ArithmeticUtil.subtract((Number) operand2, (Number) operand1);
                break;
            case "*":
                result = ArithmeticUtil.multiply((Number) operand2, (Number) operand1);
                break;
            case "/":
                result = ArithmeticUtil.divide((Number) operand2, (Number) operand1);
                break;
            case "^":
                result = ArithmeticUtil.pow((Number) operand2, (Number) operand1);
                break;
            case "%":
                result = ArithmeticUtil.mod((Number) operand2, (Number) operand1);
                break;
            case ">":
                result = ArithmeticUtil.moreThan((Number) operand2, (Number) operand1);
                break;
            case "<":
                result = ArithmeticUtil.lessThan((Number) operand2, (Number) operand1);
                break;
            case ">=":
                result = ArithmeticUtil.moreThanOrEqualTo((Number) operand2, (Number) operand1);
                break;
            case "<=":
                result = ArithmeticUtil.lessThanOrEqualTo((Number) operand2, (Number) operand1);
                break;
            case "==":
                result = (operand2.equals(operand1));
                break;
            case "!=":
                result = (!operand2.equals(operand1));
                break;
            case "||":
                result = ((Boolean) operand2 || (Boolean) operand1);
                break;
            case "&&":
                result = ((Boolean) operand2 && (Boolean) operand1);
                break;
            default:
                throw new IllegalArgumentException(token + " is not an operator or is not handled");
        }
        return result;
    }

    private Object executeUnaryOperation(Object token, String operator, Boolean operand1) {
        Object result;
        switch (operator) {
            case "!":
                result = !operand1;
                break;
            default:
                throw new IllegalArgumentException(token + " is not an operator or is not handled");
        }
        return result;
    }
}