public abstract class AST {
};

abstract class Expr extends AST {
    abstract public Value eval(Environment env);
}

abstract class Condition extends AST {
    abstract public Boolean eval(Environment env);
}

abstract class Command extends AST {
    abstract public void eval(Environment env);
}

enum JavaType {
    DOUBLETYPE, BOOLTYPE
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
}

class Constant extends Expr {
    Double d;

    Constant(Double d) {
        this.d = d;
    }

    public Value eval(Environment env) {
        return new Value(d);
    }
}


// Do nothing command
class NOP extends Command {
    public void eval(Environment env) {
    };
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
}

class AssignArray extends Command {
    String id;
    Expr accessor ;
    Expr action;

    AssignArray(String id, Expr accessor, Expr action)
    {
        this.id = id;
        this.accessor = accessor;
        this.action = action;
    }

    /*
        d is an array
        envTable:

            d0 | 1 
            d1 | 9
    */
    public void eval(Environment env) {
        Value v2 = accessor.eval(env);
        Double idx = v2.d;
      

        int foo = idx.intValue();
        String arrayIndex = id+Integer.toString(foo);
        System.out.println("assignArray: "+arrayIndex);

        Value v = action.eval(env);
        env.setVariable(arrayIndex, v);
    }

}

class IDArray extends Expr {
    String id;
    Expr accessor;

    IDArray(String id, Expr accessor)
    {
        this.id = id;
        this.accessor = accessor;
    }

    public Value eval(Environment env) {
        Double idx = accessor.eval(env).d;        
        int foo = idx.intValue();
        String arrayIndex = id+Integer.toString(foo);        
        return env.getVariable(arrayIndex);
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

}

class IfThen extends Command {
    Command e1; 
    Condition cond;

    IfThen(Condition cond, Command e1) {
        this.cond = cond;
        this.e1 = e1;
    }

    public void eval(Environment env) {
        System.out.println("inside IfThen");
        Boolean val = cond.eval(env);
        System.out.println("val:"+val);
        if(val)
        {
            e1.eval(env);
        } 
    }
}

//this is '=='
class Compare extends Condition {
    Expr e1, e2;

    Compare(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        Value v1 = e1.eval(env);
        Value v2 = e2.eval(env); 
        System.out.println("v1: "+v1+"\tv2:"+v2);
        System.out.println("v1 type:"+v1.javaType+"\tv2 type:"+v2.javaType);

        if(v1.javaType == JavaType.DOUBLETYPE && v2.javaType == JavaType.DOUBLETYPE)
        {
            if(v1.d.equals(v2.d)) 
            {
                return true;
            }
            return false;
        }
        if(v1.javaType == JavaType.BOOLTYPE && v2.javaType == JavaType.BOOLTYPE)
        {
            if(v1.b.equals(v2.b)) 
            {
                return true;
            }
            return false;
        }

        return null;
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
}


class ForLoop extends Command {
    String id;
    Double idx;
    Expr end;    
    Command body;
    ForLoop(String id, Double idx, Expr end,Command body) {
        this.id = id;
        this.idx = idx;
        this.end = end;
        this.body = body;
    }

    public void eval(Environment env) {        
        Value v = end.eval(env);
        for(double i = idx; i < v.d; i++)
        {
            env.setVariable(id, new Value(i));
            body.eval(env);
        }
    }
}


class AndCondition extends Condition {
    Condition e1, e2;
    AndCondition(Condition e1, Condition e2)
    {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Boolean eval(Environment env)
    {
        return e1.eval(env).equals(e2.eval(env));
    }
}

class OrCondition extends Condition {
    Condition c1, c2;

    OrCondition(Condition c1, Condition c2)
    {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env)
    {
        return c1.eval(env) || (c2.eval(env));
    }
}

class NotCondition extends Condition {
    Condition c1;

    NotCondition(Condition c1)
    {
        this.c1 = c1;
    }

    public Boolean eval(Environment env)
    {
        return !c1.eval(env);
    }
}