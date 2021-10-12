public abstract class AST {
    abstract public JavaType typecheck(Environment env);
};

abstract class Expr extends AST {
    abstract public Value eval(Environment env);
    // abstract public JavaType typecheck(Environment env);
}

abstract class Conditions extends AST {
    abstract public Boolean eval(Environment env);
    // abstract public JavaType typecheck(Environment env);
}

abstract class Command extends AST {
    abstract public void eval(Environment env);
    // abstract public JavaType typecheck(Environment env);
}

enum JavaType {
    DOUBLETYPE, BOOLTYPE
}

class faux { // collection of non-OO auxiliary functions (currently just error)
    public static void error(String msg) {
        System.err.println("Interpreter error: " + msg);
        System.exit(-1);
    }
}

class Value {
    public JavaType javaType;
    public Double d;
    public Boolean b;

    public Value(Double d) {
        this.javaType = JavaType.DOUBLETYPE;
        this.d = d;
    }

    public Value(Boolean b) {
        this.javaType = JavaType.BOOLTYPE;
        this.b = b;
    }

    public String toString() {
        if (this.javaType == JavaType.BOOLTYPE) {
            return "" + b;
        }
        return "" + d;
    }
}

class MultDiv extends Expr {
    Expr e1, e2;
    String op;

    MultDiv(Expr e1, Expr e2, String op) {
        this.e1 = e1;
        this.e2 = e2;
        this.op = op;
    }

    public Value eval(Environment env) {
        Value v1 = e1.eval(env);
        Value v2 = e2.eval(env);
        if (op.equals("*")) {
            // System.out.println("inside multiply");

            return new Value(v1.d * v2.d);
        }
        return new Value(v1.d / v2.d);
    }

    public JavaType typecheck(Environment env) {
        JavaType t1 = e1.typecheck(env);
        JavaType t2 = e2.typecheck(env);
        if (t1 != JavaType.DOUBLETYPE || t2 != JavaType.DOUBLETYPE) {
            faux.error("Multiply must have DoubleType");
            return null;
        }
        return JavaType.DOUBLETYPE;
    }
}

class AddSub extends Expr {
    Expr e1, e2;
    String op;

    AddSub(Expr e1, Expr e2, String op) {
        this.e1 = e1;
        this.e2 = e2;
        this.op = op;
    }

    public Value eval(Environment env) {
        Value v1 = e1.eval(env);
        Value v2 = e2.eval(env);
        if (op.equals("+")) {
            return new Value(v1.d + v2.d);
        }
        return new Value(v1.d - v2.d);
    }

    public JavaType typecheck(Environment env) {
        JavaType t1 = e1.typecheck(env);
        JavaType t2 = e2.typecheck(env);
        if (t1 != JavaType.DOUBLETYPE || t2 != JavaType.DOUBLETYPE) {
            faux.error("Multiply must have DoubleType");
            return null;
        }
        return JavaType.DOUBLETYPE;
    }
}

class Constant extends Expr {
    Double d;

    Constant(Double d) {
        this.d = d;
    }

    public Value eval(Environment env) {
        return new Value(d);
    }

    public JavaType typecheck(Environment env) {
        Value v = this.eval(env);
        return v.javaType;
    }
}

// Do nothing command
class NOP extends Command {
    public void eval(Environment env) {
    };

    public JavaType typecheck(Environment env) {
        return null;
    }
}

class Sequence extends Command {
    Command c1, c2;

    Sequence(Command c1, Command c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public void eval(Environment env) {
        c1.eval(env);
        c2.eval(env);
    }

    public JavaType typecheck(Environment env) {
        c1.typecheck(env);
        c2.typecheck(env);
        return JavaType.BOOLTYPE; // this is fucked
    }
}

class Assignment extends Command {
    String v;
    Expr e;

    Assignment(String v, Expr e) {
        this.v = v;
        this.e = e;
    }

    public void eval(Environment env) {
        Value d = e.eval(env);
        env.setVariable(v, d);
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck assignment---");
        return null;
    }
}

class AssignArray extends Command {
    String id;
    Expr accessor;
    Expr action;

    AssignArray(String id, Expr accessor, Expr action) {
        this.id = id;
        this.accessor = accessor;
        this.action = action;
    }

    public void eval(Environment env) {
        Value v2 = accessor.eval(env);
        Double idx = v2.d;

        int foo = idx.intValue();
        String arrayIndex = id + Integer.toString(foo);

        Value v = action.eval(env);
        env.setVariable(arrayIndex, v);
    }

    public JavaType typecheck(Environment env) {

        Value accesor_value = accessor.eval(env);
        Value action_value = action.eval(env);
        Double idx = accesor_value.d;

        int foo = idx.intValue();
        String arrayIndex = id + Integer.toString(foo);
        JavaType arrayType = env.getVariable(arrayIndex).javaType;

        if (arrayType != action_value.javaType) {
            faux.error("ArrayType is not equal to assignment Type");
            return null;
        }

        return action_value.javaType;
    }
}

class IDArray extends Expr {
    String id;
    Expr accessor;

    IDArray(String id, Expr accessor) {
        this.id = id;
        this.accessor = accessor;
    }

    public Value eval(Environment env) {
        Double idx = accessor.eval(env).d;
        int foo = idx.intValue();
        String arrayIndex = id + Integer.toString(foo);
        return env.getVariable(arrayIndex);
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck IDArray---");
        Value v = accessor.eval(env);
        if (v.javaType.equals(JavaType.DOUBLETYPE)) {
            faux.error("access type must be double");
            return null;
        }
        return v.javaType;
    }
}

class Output extends Command {
    Expr e;

    Output(Expr e) {
        this.e = e;
    }

    public void eval(Environment env) {
        Value v = e.eval(env);
        System.out.println(v);
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck Output---");
        return null;
    }
}

class While extends Command {
    Condition c;
    Command body;

    While(Condition c, Command body) {
        this.c = c;
        this.body = body;
    }

    public void eval(Environment env) {
        while (c.eval(env))
            body.eval(env);
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck while---");
        return null;
    }
}

class Unequal extends Condition {
    Expr e1, e2;

    Unequal(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return !e1.eval(env).equals(e2.eval(env));
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck Unequal---");
        JavaType t1 = e1.eval(env).javaType;
        JavaType t2 = e2.eval(env).javaType;
        if (t1 != t2) {
            faux.error("Expressions must be of same type");
        }
        return t1;
    }
}

class IfThen extends Command {
    Command e1;
    Condition cond;

    IfThen(Condition cond, Command e1) {
        this.cond = cond;
        this.e1 = e1;
    }

    public void eval(Environment env) {
        Boolean val = cond.eval(env);
        if (val) {
            e1.eval(env);
        }
    }

    public JavaType typecheck(Environment env) {
        return null;
    }
}

// this is '=='
class Compare extends Condition {
    Expr e1, e2;

    Compare(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        Value v1 = e1.eval(env);
        Value v2 = e2.eval(env);
        System.out.println("v1: " + v1 + "\tv2:" + v2);
        System.out.println("v1 type:" + v1.javaType + "\tv2 type:" + v2.javaType);

        if (v1.javaType == JavaType.DOUBLETYPE && v2.javaType == JavaType.DOUBLETYPE) {
            if (v1.d.equals(v2.d)) {
                return true;
            }
            return false;
        }
        if (v1.javaType == JavaType.BOOLTYPE && v2.javaType == JavaType.BOOLTYPE) {
            if (v1.b.equals(v2.b)) {
                return true;
            }
            return false;
        }

        return null;
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck compare---");
        JavaType t1 = e1.eval(env).javaType;
        JavaType t2 = e2.eval(env).javaType;
        if (t1 != t2) {
            faux.error("Expressions must be of same type");
        }
        return t1;
    }
}

class Variable extends Expr {
    String varname;

    Variable(String varname) {
        this.varname = varname;
    }

    public Value eval(Environment env) {
        return env.getVariable(varname);
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck variable---");
        return this.eval(env).javaType;
    }

}

class ForLoop extends Command {
    String id;
    Double idx;
    Expr end;
    Command body;

    ForLoop(String id, Double idx, Expr end, Command body) {
        this.id = id;
        this.idx = idx;
        this.end = end;
        this.body = body;
    }

    public void eval(Environment env) {
        Value v = end.eval(env);
        for (double i = idx; i < v.d; i++) {
            env.setVariable(id, new Value(i));
            body.eval(env);
        }
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck forLoop---");
        return null;
    }
}

class AndCondition extends Condition {
    Condition e1, e2;

    AndCondition(Condition e1, Condition e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return e1.eval(env).equals(e2.eval(env));
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck andCondition---");
        Value v1 = new Value(e1.eval(env));
        Value v2 = new Value(e2.eval(env));
        if (v1.javaType != v2.javaType) {
            faux.error("AndCondition must have similar types");
        }
        return v1.javaType;
    }
}

class OrCondition extends Condition {
    Condition c1, c2;

    OrCondition(Condition c1, Condition c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        return c1.eval(env) || (c2.eval(env));
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck orCondition---");
        Value v1 = new Value(c1.eval(env));
        Value v2 = new Value(c2.eval(env));
        if (v1.javaType != v2.javaType) {
            faux.error("AndCondition must have similar types");
        }
        return v1.javaType;
    }
}

class NotCondition extends Condition {
    Condition c1;

    NotCondition(Condition c1) {
        this.c1 = c1;
    }

    public Boolean eval(Environment env) {
        return !c1.eval(env);
    }

    public JavaType typecheck(Environment env) {
        System.out.println("---Typecheck NotCondition---");
        return new Value(c1.eval(env)).javaType;
    }
}

class UnaryMinus extends Expr {
    Expr e;

    UnaryMinus(Expr e) {
        this.e = e;
    }

    public Value eval(Environment env) {
        System.out.println("evaluating unuary minus");
        Value v = e.eval(env);
        return new Value(-1 * v.d);
    }

    public JavaType typecheck(Environment env) {
        Value v = e.eval(env);
        if (v.javaType != JavaType.DOUBLETYPE) {
            faux.error("Unary expr must have minus");
        }
        return v.javaType;
    }
}

class Comparison extends Conditions {
    Expr e1, e2;
    String comparison;

    Comparison(Expr e1, Expr e2, String comparison) {
        this.e1 = e1;
        this.e2 = e2;
        this.comparison = comparison;
    }

    public Boolean eval(Environment env) {
        var v1 = e1.eval(env).d;
        var v2 = e2.eval(env).d;

        switch (comparison) {
            case "==":
                return v1.equals(v2);
            case "!=":
                return !v1.equals(v2);
            case ">=":
                return v1 >= v2;
            case "<=":
                return v1 <= v2;
            case ">":
                return v1 > v2;
            case "<":
                return v1 < v2;
            default:
                return false;
        }
    }


    public JavaType typecheck(Environment env) {
        Value v1 = e1.eval(env);
        Value v2 = e2.eval(env);
        if (v1.javaType != v2.javaType) {
            faux.error("GreaterThen must have same types");
            return null;
        }
        return v1.javaType;
    }

}