grammar Eqn;

eqn:
       '(' eqn ')'                                        # Parens
    |   ID '[' subscript (',' subscript)* ']'             # ArrayAccess
    |   ID ('(' ')' | '(' eqn (',' eqn)* ')')             # FunCall
    |   <assoc=right> eqn POW eqn                         # Power
    |   <assoc=right> op=('+' | '-') eqn                  # UnarySign
    |   <assoc=right> NOT eqn                             # Not
    |   eqn op=( '*' | '/' | MOD ) eqn                    # MulDiv
    |   eqn op=('+'|'-') eqn                              # AddSub
    |   eqn op=( EQ | NEQ | GT | GE | LT | LE ) eqn       # Comp
    |   eqn op=( AND | OR ) eqn                           # Logic
    |   IF eqn THEN eqn ELSE eqn                          # IfThenElse
    |   NUMBER                                            # number
    |   ID                                                # var
    ;

subscript:
       ID                                                 # IdSubscript
    |  NUMBER                                             # IntSubscript
    ;


POW :   '^' | '**' ;
MUL :   '*' ;
DIV :   '/' ;
ADD :   '+' ;
SUB :   '-' ;
MOD :   [mM][oO][dD] | '%' ;
NOT :   [nN][oO][tT] | '!' ;
AND :   [aA][nN][dD] | '&' ;
OR  :   [oO][rR] | '|' ;
EQ  :   '=' | '==' ;
NEQ :   '!=' | '<>' ;
GT  :   '>' ;
GE  :   '>=' ;
LT  :   '<' ;
LE  :   '<=' ;
IF  :   [iI][fF] ;
THEN:   [tT][hH][eE][nN] ;
ELSE:   [eE][lL][sS][eE] ;

ID : STR | KEYWORD;
KEYWORD : KEYWORD_START (KEYWORD_START | [0-9])* ;
KEYWORD_START :  ([a-z] | [A-Z] | '_' | '$') ;
STR : '"' .+? '"';
NUMBER:  [0-9]+ ('.' [0-9]+)? NUM_EXP? | '.' [0-9]+ NUM_EXP? ;
NUM_EXP: ('e' | 'E') [+-]? [0-9]+ ;

WS  :   [ \t\r\n]+ -> skip ; // skip all whitespaces
COMMENT : '{' .*? '}' -> skip ; // skip comments in curly braces
