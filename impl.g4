grammar impl;

/* A small imperative language */

start   :  cs+=command* EOF ;

program : c=command                      # SingleCommand
	| '{' cs+=command* '}'           # MultipleCommands
	;
	
command : x=ID '=' e=expr ';'	           # Assignment
	| 'output' e=expr ';'            	   # Output
    | 'while' '('c=condition')' p=program  # WhileLoop
	| 'if' c=condition 'then' p=program    #IfThen
	| x=ID'['i=(ID|FLOAT)']''='v=expr ';'   #AssignArray		
	| 'for' '('x=ID'='n1=FLOAT'..'n2=expr')' p=program  #ForLoop
	; //a[1] = 2;



expr	: 
	expr OP=(MULT|DIV) expr 	# MultDiv
	| expr OP=(ADD|MINUS) expr 	# AddSub
	| x=ID'['i=(ID|FLOAT)']'	# IDArray
	| c=FLOAT     	      		# Constant
	| x=ID		      			# Variable
	| '(' e=expr ')'      		# Parenthesis	
	;

condition : 
	  e1=expr '!=' e2=expr # Unequal	
	  | expr '==' expr # Compare
	  | '!' c=condition #NotCondition
	  | condition 'and' condition #AndCondition
	  | condition 'or' condition #OrCondition
	  ;  

ID    : ALPHA (ALPHA|NUM)* ;
FLOAT : '-'? NUM+ ('.' NUM+)? ;

ALPHA : [a-zA-Z_ÆØÅæøå] ;
NUM   : [0-9] ;
ADD   : '+';
MINUS : '-';
MULT  : '*';
DIV   : '/';


WHITESPACE : [ \n\t\r]+ -> skip;
COMMENT    : '//'~[\n]*  -> skip;
COMMENT2   : '/*' (~[*] | '*'~[/]  )*   '*/'  -> skip;
