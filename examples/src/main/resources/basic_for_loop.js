// basic for loop

test_variable = 14;
test_variable_expression = test_variable * 2;

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