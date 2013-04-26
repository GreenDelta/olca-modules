from string import Template
import functions

# A script for generating the functions with two 
# numeric argument.

func_templ_str = """
// generated with func2_templ.py
package org.openlca.expressions.functions;

class $className extends Function2 {

    @Override
    protected double eval(double arg1, double arg2) {
        return $formula;
    }

    @Override
    public String getName() {
        return "$funcName";
    }
}
"""
template = Template(func_templ_str)

for function in functions.functions2:
    f = open(function[0]+".java", "wb")
    f.write(template.substitute(
                              className=function[0], 
                              formula=function[1], 
                              funcName=function[2]))
    f.close