from string import Template
import functions

# A script for generating the functions with n 
# numeric argument.

func_templ_str = """
// generated with funcN_templ.py
package org.openlca.expressions.functions;

class $className extends FunctionN {

    @Override
    protected double getDefault() {
        return $default;
    }

    @Override
    protected double eval(double[] args) {
       $formula
    }

    @Override
    public String getName() {
        return "$funcName";
    }
}
"""
template = Template(func_templ_str)

for function in functions.functionsN:
    f = open(function[0]+".java", "wb")
    f.write(template.substitute(
                              className=function[0], 
                              formula=function[1], 
                              default=function[2],
                              funcName=function[3]))
    f.close