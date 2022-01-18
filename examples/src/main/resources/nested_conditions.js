// variables

test_variable = 14;
test_variable_expression = test_variable * 2;

test_diff = 0.0;
if(test_variable != 0) {
    
    test_diff_numerator = test_variable * 1.0;
    test_variable_expression_double = test_variable_expression * 1.0;
    test_diff_fraction = test_diff_numerator / test_variable_expression_double;
    test_diff = test_diff_fraction * 100.0;
    test_diff_combined = ((test_variable * 1.0) / (test_variable_expression * 1.0)) * 100.0;
    
    log("test diff");
    log(test_diff);
    log("test diff in combined equation");
    log(test_diff_combined);
    if(test_diff == test_diff_combined) {
        log("value matches. updating original value");
        test_diff = test_diff + test_diff_combined;
    }
}

log("test diff");
log(test_diff);