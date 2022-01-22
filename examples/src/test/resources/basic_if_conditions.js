// basic if conditions

test_variable = 14;
test_variable_copy_expression = 28;

if(test_variable_copy_expression != null && 
    test_variable != 2) {
    log("test variable");
    log(test_variable);
}

incremental_variable = 0;
if((test_variable_copy_expression >= test_variable && test_variable == 14) && test_variable_copy_expression >= test_variable) {
    incremental_variable = incremental_variable + 1;
}

if(test_variable_copy_expression < test_variable) {
    incremental_variable = incremental_variable - 1;
}

log("incremental variable");
log(incremental_variable);