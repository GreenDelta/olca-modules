
# numeric functions with one argument
functions1 = [
("Abs", "Math.abs(arg)", "abs"),
("Acos", "Math.acos(arg)", "acos"),
("Asin", "Math.asin(arg)", "asin"),
("Atan", "Math.atan(arg)", "atan"),
("Cos", "Math.cos(arg)", "cos"),
("Cosh", "Math.cosh(arg)", "cosh"),
("Cotan", "1 / Math.tan(arg)", "cotan"),
("Ceil", "Math.ceil(arg)", "ceil"),
("Exp", "Math.exp(arg)", "exp"),
("Floor", "Math.floor(arg)", "floor"),
("Frac", "arg - ((int)arg)", "frac"), 
("Int", "(int)arg", "int"),    
("Ln", "Math.log(arg)", "ln"),      
("Log", "Math.log10(arg)", "log"),     
("Round", "(double)Math.round(arg)", "round"),        
("Sin", "Math.sin(arg)", "sin"),       
("Sinh", "Math.sinh(arg)", "sinh"),     
("Sqr", "arg * arg", "sqr"), 
("Sqrt", "Math.sqrt(arg)", "sqrt"), 
("Tan", "Math.tan(arg)", "tan"), 
("Tanh", "Math.tanh(arg)", "tanh"), 
]

# numeric functions with two arguments
functions2 = [
("Pow", "Math.pow(arg1, arg2)", "pow"),
("IPow", "Math.pow(arg1, (double) ((int)arg2))", "ipow")               
]

# numeric functions with n arguments
functionsN = [
("Sum", """
    double sum = 0;
    for(double arg : args) 
        sum += arg;
    return sum;
""", 0, "sum"),

("Avg", """
    double sum = 0;
    for(double arg : args) 
        sum += arg;
    return sum / args.length;    
""", 0, "avg"),

("Max", """
    double max = -Double.MAX_VALUE;
    for(double arg : args)
        if(arg > max)
            max = arg;
    return max;
""", 0, "max"),

("Min", """
    double min = Double.MAX_VALUE;
    for(double arg : args)
        if(arg < min)
            min = arg;
    return min;
""", 0, "min")              
]