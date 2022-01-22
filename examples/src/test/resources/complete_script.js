// variables

test_variable = 14;
test_variable_copy = test_variable;
test_variable_expression = test_variable * 2;
test_variable_copy_expression = test_variable_copy * 2;

log("test variable copy expression");
log(test_variable_copy_expression);

//if(undeclared_variable == null) {
//    log(undeclared variable called)
//}

if(test_variable_copy_expression != null && 
    test_variable != 2) {
    log("test variable");
    log(test_variable);
}

test_boolean_expression = test_variable_copy_expression >= test_variable && test_variable == 14;

incremental_variable = 0;
if(test_variable_copy_expression >= test_variable) {
    incremental_variable = incremental_variable + 1;
}
log("incremental variable");
log(incremental_variable);

for_loop_value = 0;
for(i=0, i<test_variable_expression, i=i+1) {
    if(i % 2 == 0) {
        log("incremental value");
        log(i);
        log("for loop value");
        log(for_loop_value);
        for_loop_value = for_loop_value + 1;
    }
}
log("final for loop value");
log(for_loop_value);

test_diff = 0.0;
if(for_loop_value != 0) {
    test_diff_numerator = for_loop_value * 1.0;
    test_variable_expression_double = test_variable_expression * 1.0;
    test_diff_fraction = test_diff_numerator / test_variable_expression_double;
    test_diff = test_diff_fraction * 100.0;
    test_diff_combined = ((for_loop_value * 1.0) / (test_variable_expression * 1.0)) * 100.0;
    log("test diff");
    log(test_diff);
    log(test_diff_combined);
    if(test_diff == test_diff_combined) {
        log("value matches. updating original value");
        test_diff = test_diff + test_diff_combined;
    }
}

log("test diff");
log(test_diff);

log("exit");