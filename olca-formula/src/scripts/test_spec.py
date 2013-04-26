from string import Template

a_t = Template("""assertEquals(${result}, interpreter.eval("${formula}"), 1e-15);""")

spec = """
    pi      #> Math.PI
    e       #> Math.E
    -1.0    #> -1
    -(-1)   #> 1
    -2^-3   #> -0.125
    2 *-2   #> -4
    6/2/2   #> 1.5

    12 div 5    #> 2
    13 mod 5    #> 3
    4 + -2      #> 2
    3 - 6       #> -3

    if(3 = 3;2;0)       #> 2
    if(3 == 4;2;0)      #> 0
    if(3 <> 4; 8; 3)    #> 8
    if(5 != 5; 8; 3)    #> 3
    if(-4 < -5; 4; 2)   #> 2
    if(3 <= 3.0; 4;2)   #> 4
    if(3 > -3; 4; 2)    #> 4
    if(3 >=-3; 4; 2)    #> 4

    if(3 > 2 & 4 > 3; 4; 2)     #> 4
    if(3 > 2 && 4 < 3; 4; 2)    #> 2
    if(3 < 2 | 4 < 3; 4; 2)     #> 2
    if(3 > 2 || 4 < 3; 4; 2)    #> 4

    abs(-(-(-2)))   #> 2
    acos(-1)        #> Math.PI
    arccos(-1)      #> Math.PI

    if(and(3>2;4>3;5>4);4;2)    #> 4

    asin(-1)    #> -Math.PI/2
    arcsin(-1)  #> -Math.PI/2
    atan(-1)    #> -Math.PI/4
    arctan(-1)  #> -Math.PI/4

    avg(1;2;3;4;5)  #> 3
    mean(1;2;3;4;5) #> 3
    ceil(2 + 0.2)   #> 3
    cos(3-3)        #> 1
    cosh(0^200)     #> 1
    cotan(pi/2)     #> 0
    cot(pi/2)       #> 0
    exp(2)          #> Math.E * Math.E

    floor(3.0 - 0.3)    #> 2
    frac(3-0.3)         #> 0.7

    if(1>2;1;2)     #> 2
    iff(1>2;1;2)    #> 2
    iif(1>2;1;2)    #> 2

    ipower(4;2)     #> 16
    ln(e*e)         #> 2
    lg(10*10*10)    #> 3
    log(100 * 10)   #> 3

    max(8;5;1;8;12) #> 12
    min(8;5;1;8;12) #> 1

    if(not(4>3);1;0)            #> 0
    if(or(1>2;3>4;5==5);1;0)    #> 1

    power(4;0.5)    #> 2
    pow(9;0.5)      #> 3
    round(0.5)      #> 1
    sin(2*PI)       #> 0
    sinh(4-4)       #> 0
    sqr(3)          #> 9
    sqrt(9)         #> 3
    tan(pi/4)       #> 1
    tanh(0)         #> 0

    trunc(2.7)      #> 2
    int(2.7)        #> 2

"""

def a(formula, result):
    print a_t.substitute(formula=formula, result=result)

def op(method, formula, result):
    print op_t.substitute(method=method, formula=formula, result=result);

def main():
    for line in spec.splitlines():
        l = line.strip()
        if l != "":
            (formula, sep,  result) = l.partition(" #> ")
            a(formula.strip(), result.strip())

if __name__ == '__main__':
    main()
