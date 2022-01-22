// basic variable declaration

test_variable = 14;
test_variable_expression = test_variable * 2;

udf_value = custom_udf(var_1: test_variable, var_2: test_variable_expression);

log("udf value");
log(udf_value);