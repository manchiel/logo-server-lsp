grammar Logo;

// PARSER RULES

program
    : NEWLINE* (line (NEWLINE+ line)*)? NEWLINE* EOF
    ;

line
    : statement+
    ;

statement
    : procedureDefinition
    | command
    ;

procedureDefinition
    : TO name=procedureName parameterList? NEWLINE procedureBody END
    ;

parameterList
    : parameterDeclaration+
    ;

parameterDeclaration
    : COLON NAME
    ;

procedureBody
    : (line NEWLINE+)*
    ;

command
    : turtleCommand
    | controlCommand
    | procedureCall
    | printCommand
    | makeCommand
    | localCommand
    ;

turtleCommand
    : FORWARD expression
    | BACK expression
    | LEFT expression
    | RIGHT expression
    | PENUP
    | PENDOWN
    | HIDETURTLE
    | SHOWTURTLE
    | HOME
    | CLEARSCREEN
    | SETXY expression expression
    ;

controlCommand
    : REPEAT expression block
    | IF expression block
    | IFELSE expression trueBlock=block falseBlock=block
    | STOP
    | OUTPUT expression
    ;

block
    : LBRACKET command* RBRACKET
    ;

printCommand
    : PRINT (expression | QUOTED_WORD)
    ;

makeCommand
    : MAKE QUOTED_WORD expression
    ;

localCommand
    : LOCAL QUOTED_WORD
    ;

procedureCall
    : name=procedureName expression*
    ;

procedureName
    : NAME
    ;

expression
    : LPAREN expression RPAREN
    | MINUS expression
    | left=expression op=(MULTIPLY | DIVIDE) right=expression
    | left=expression op=(PLUS | MINUS) right=expression
    | left=expression compOp right=expression
    | variableReference
    | NUMBER
    | procedureCall
    ;

variableReference
    : COLON NAME
    ;

compOp
    : EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN
    ;

// LEXER RULES

TO          : T O ;
END         : E N D ;

FORWARD     : F O R W A R D | F D ;
BACK        : B A C K       | B K ;
LEFT        : L E F T       | L T ;
RIGHT       : R I G H T     | R T ;
PENUP       : P E N U P     | P U ;
PENDOWN     : P E N D O W N | P D ;
HIDETURTLE  : H I D E T U R T L E | H T ;
SHOWTURTLE  : S H O W T U R T L E | S T ;
HOME        : H O M E ;
CLEARSCREEN : C L E A R S C R E E N | C S ;
SETXY       : S E T X Y ;

REPEAT      : R E P E A T ;
IF          : I F ;
IFELSE      : I F E L S E ;
STOP        : S T O P ;
OUTPUT      : O U T P U T   | O P ;

PRINT       : P R I N T ;
MAKE        : M A K E ;
LOCAL       : L O C A L ;

COLON       : ':' ;
LBRACKET    : '[' ;
RBRACKET    : ']' ;
LPAREN      : '(' ;
RPAREN      : ')' ;
PLUS        : '+' ;
MINUS       : '-' ;
MULTIPLY    : '*' ;
DIVIDE      : '/' ;
EQUALS      : '=' ;
NOTEQUALS   : '<>' ;
LESSTHAN    : '<' ;
GREATERTHAN : '>' ;

NUMBER      : [0-9]+ ('.' [0-9]+)? ;
QUOTED_WORD : '"' [a-zA-Z_][a-zA-Z0-9_]* ;
NAME        : [a-zA-Z_][a-zA-Z0-9_]* ;

NEWLINE     : '\r'? '\n' ;
WS          : [ \t]+ -> skip ;
COMMENT     : ';' ~[\r\n]* -> skip ;

fragment A: [aA]; fragment B: [bB]; fragment C: [cC]; fragment D: [dD];
fragment E: [eE]; fragment F: [fF]; fragment G: [gG]; fragment H: [hH];
fragment I: [iI]; fragment J: [jJ]; fragment K: [kK]; fragment L: [lL];
fragment M: [mM]; fragment N: [nN]; fragment O: [oO]; fragment P: [pP];
fragment Q: [qQ]; fragment R: [rR]; fragment S: [sS]; fragment T: [tT];
fragment U: [uU]; fragment V: [vV]; fragment W: [wW]; fragment X: [xX];
fragment Y: [yY]; fragment Z: [zZ];