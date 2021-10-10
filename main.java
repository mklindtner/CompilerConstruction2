import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class main {
	public static void main(String[] args) throws IOException {

		// we expect exactly one argument: the name of the input file
		if (args.length != 1) {
			System.err.println("\n");
			System.err.println("Simple interpreter\n");
			System.err.println("==================\n\n");
			System.err.println("Please give as input argument a filename\n");
			System.exit(-1);
		}
		String filename = args[0];

		// open the input file
		CharStream input = CharStreams.fromFileName(filename);
		// new ANTLRFileStream (filename); // depricated

		// create a lexer/scanner
		implLexer lex = new implLexer(input);

		// get the stream of tokens from the scanner
		CommonTokenStream tokens = new CommonTokenStream(lex);

		// create a parser
		implParser parser = new implParser(tokens);

		// and parse anything from the grammar for "start"
		ParseTree parseTree = parser.start();

		// Construct an interpreter and run it on the parse tree
		// Interpreter interpreter = new Interpreter();
		Command p = (Command) new AstMaker().visit(parseTree);
		p.eval(new Environment());
	}
}

// We write an interpreter that implements interface
// "implVisitor<T>" that is automatically generated by ANTLR
// This is parameterized over a return type "<T>" which is in our case
// simply a Double.

class AstMaker extends AbstractParseTreeVisitor<AST> implements implVisitor<AST> {

	public AST visitStart(implParser.StartContext ctx) {
		Command program = new NOP();
		for (implParser.CommandContext c : ctx.cs)
			program = new Sequence(program, (Command) visit(c));
		return program;
	};

	public AST visitSingleCommand(implParser.SingleCommandContext ctx) {
		return visit(ctx.c);
	}

	public AST visitMultipleCommands(implParser.MultipleCommandsContext ctx) {
		Command program = new NOP();
		for (implParser.CommandContext c : ctx.cs)
			program = new Sequence(program, (Command) visit(c));
		return program;
	}

	public AST visitParenthesis(implParser.ParenthesisContext ctx) {
		return visit(ctx.e);
	};

	public AST visitVariable(implParser.VariableContext ctx) {
		return new Variable(ctx.x.getText());
	};

	public AST visitMultDiv(implParser.MultDivContext ctx) {
		String op = ctx.OP.getText();
		System.out.println("op:" + op);
		if (op.equals("*")) {
			return new MultDiv((Expr) visit(ctx.expr(0)), (Expr) visit(ctx.expr(1)), op);
		}
		return new MultDiv((Expr) visit(ctx.expr(0)), (Expr) visit(ctx.expr(1)), "/");
	}

	// husk mellemrum trololo
	public AST visitAddSub(implParser.AddSubContext ctx) {
		String op = ctx.OP.getText();
		if (op.equals("+")) {
			return new AddSub((Expr) visit(ctx.expr(0)), (Expr) visit(ctx.expr(1)), op);
		}
		return new AddSub((Expr) visit(ctx.expr(0)), (Expr) visit(ctx.expr(1)), op);
	}

	public AST visitConstant(implParser.ConstantContext ctx) {
		return new Constant(Double.parseDouble(ctx.c.getText()));
	};

	public AST visitUnequal(implParser.UnequalContext ctx) {
		Expr v1 = (Expr) visit(ctx.e1);
		Expr v2 = (Expr) visit(ctx.e2);
		return new Unequal(v1, v2);
	}

	public AST visitAssignment(implParser.AssignmentContext ctx) {
		String v = ctx.x.getText();
		Expr e = (Expr) visit(ctx.e);
		return new Assignment(v, e);
	}

	public AST visitAssignArray(implParser.AssignArrayContext ctx) {
		String id = ctx.x.getText();
		Expr accessor = (Expr) visit(ctx.i);
		Expr action = (Expr) visit(ctx.v);
		// Double value = Double.parseDouble(ctx.v.getText());
		System.out.println("ID: " + id);
		// System.out.println("accessor: " + accessor);
		System.out.println("value:" + action);

		return new AssignArray(id, accessor, action);
	}

	public AST visitIDArray(implParser.IDArrayContext ctx) {
		System.out.println("-----Called visitArray-----");
		String id = ctx.x.getText();
		Expr accessor = (Expr) visit(ctx.i);
		// String accessor = ctx.i.getText();
		System.out.println("ID: " + id);
		System.out.println("accessor: " + accessor);
		return new IDArray(id, accessor);
	}

	public AST visitOutput(implParser.OutputContext ctx) {
		Expr e = (Expr) visit(ctx.e);
		return new Output(e);
	}

	public AST visitWhileLoop(implParser.WhileLoopContext ctx) {
		Condition c = (Condition) visit(ctx.c);
		Command body = (Command) visit(ctx.p);
		return new While(c, body);
	}

	public AST visitIfThen(implParser.IfThenContext ctx) {
		System.out.println("----inside IfThen----");
		Condition c = (Condition) visit(ctx.c);
		Command body = (Command) visit(ctx.p);
		System.out.println("compare :" + c);
		System.out.println("Command :" + body);
		return new IfThen(c, body);
	}

	public AST visitCompare(implParser.CompareContext ctx) {
		return new Compare((Expr) visit(ctx.expr(0)), (Expr) visit(ctx.expr(1)));
	}

	public AST visitForLoop(implParser.ForLoopContext ctx) {
		String id = ctx.x.getText();

		Double start = Double.parseDouble(ctx.n1.getText());
		Expr end = (Expr) visit(ctx.n2);
		Command body = (Command) visit(ctx.p);

		System.out.println("----visitForLoop:-----");
		System.out.println("id: " + id);
		System.out.println("text from n1: " + start);
		System.out.println("text from n2: " + end);
		System.out.println("text from body: " + body);

		return new ForLoop(id, start, end, body);
		// return null;
	}

	public AST visitAndCondition(implParser.AndConditionContext ctx) {
		Condition e1 = (Condition) visit(ctx.condition(0));
		Condition e2 = (Condition) visit(ctx.condition(1));

		return new AndCondition(e1, e2);
	}

	public AST visitOrCondition(implParser.OrConditionContext ctx) {
		Condition c1 = (Condition) visit(ctx.condition(0));
		Condition c2 = (Condition) visit(ctx.condition(1));
		return new OrCondition(c1, c2);
	}

	public AST visitNotCondition(implParser.NotConditionContext ctx) {
		System.out.print("---visiting NotCondition----");
		Condition c1 = (Condition) visit(ctx.c);
		return new NotCondition(c1);
	}
}
