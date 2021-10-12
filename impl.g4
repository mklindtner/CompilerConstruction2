grammar impl;

/* A small imperative language */

start: cs += command* EOF;

program:
	c = command					# SingleCommand
	| '{' cs += command* '}'	# MultipleCommands;

command:
	x = ID '=' e = expr ';'						# Assignment
	| 'output' e = expr ';'						# Output
	| 'while' '(' c = condition ')' p = program	# WhileLoop
	| 'if' '(' c = condition ')' p = program (
		'else' p2 = program
	)?																	# IfThen
	| x = ID '[' i = expr ']' '=' v = expr ';'							# AssignArray
	| 'for' '(' x = ID '=' n1 = FLOAT '..' n2 = expr ')' p = program	# ForLoop;

expr:
	expr OP = ('*' | '/') expr		# MultDiv
	| expr OP = ('+' | '-') expr	# AddSub
	| x = ID '[' i = expr ']'		# IDArray
	| c = FLOAT						# Constant
	| x = ID						# Variable
	| '(' e = expr ')'				# Parenthesis;

condition:
	e1 = expr c = COMPARISON e2 = expr	# Compare
	| '!' c = condition					# Not
	| condition 'and' condition			# And
	| condition 'or' condition			# Or;

ID: ALPHA (ALPHA | NUM)*;
FLOAT: '-'? NUM+ ('.' NUM+)?;
COMPARISON: '==' | '>=' | '<=' | '>' | '<' | '!=';

ALPHA: [a-zA-Z_ÆØÅæøå];
NUM: [0-9];

WHITESPACE: [ \n\t\r]+ -> skip;
COMMENT: '//' ~[\n]* -> skip;
COMMENT2: '/*' (~[*] | '*' ~[/])* '*/' -> skip;
