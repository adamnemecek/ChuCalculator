import java.util.StringTokenizer;

abstract class Expression
{
  static Expression parse(String expText) throws SyntaxException
  {
    StringTokenizer tokenizer = new StringTokenizer(expText," \t");
    int numtokens = tokenizer.countTokens();

    if (numtokens<1||numtokens>3)
      throw new SyntaxException("right hand side \""+expText+
                                "\" has "+numtokens+
                                " tokens (should be 1-3)");

    String tokens[] = new String[numtokens];
    for(int i=0;i<numtokens;i++) tokens[i] = tokenizer.nextToken();

    Expression result = null;
    switch(numtokens) {
    case 1:
      result = new Identifier(tokens[0]);
      break;
    case 2:
      result = new UnaryExpression(tokens[0],
                                   new Identifier(tokens[1]));
      break;
    case 3:
      result = new BinaryExpression(new Identifier(tokens[0]),
                                    tokens[1],
                                    new Identifier(tokens[2]));
      break;
    }
    return result;
  }

  abstract String show();
  abstract Chu eval(Calc c) throws ExecutionException;
}

class Identifier extends Expression
{
  String identifier;

  Identifier(String identifier)
  {
    this.identifier = identifier;
  }

  String show()
  {
    return identifier;
  }

  Chu eval(Calc calc) throws ExecutionException
  {
    return calc.lookupChu(identifier);
  }
}

class UnaryExpression extends Expression
{
  String unaryOp;
  Expression argExp;

  UnaryExpression(String unaryOp, Expression argExp)
  {
    this.unaryOp = unaryOp;
    this.argExp = argExp;
  }

  String show()
  {
    return unaryOp + " " + argExp.show();
  }

  Chu eval(Calc calc) throws ExecutionException
  {
    UnaryOperator operator = calc.lookupUnaryOperator(unaryOp);
    Chu arg = argExp.eval(calc);

    if(arg==null) throw new ExecutionException(argExp.show()+" is undefined");

    return operator.apply(arg, calc.getContext());
  }
}

class BinaryExpression extends Expression
{
  String binaryOp;
  Expression leftExp, rightExp;

  BinaryExpression(Expression leftExp, String binaryOp, Expression rightExp)
  {
    this.leftExp  = leftExp;
    this.binaryOp = binaryOp;
    this.rightExp = rightExp;
  }

  String show()
  {
    return "(" + leftExp.show() +
             " " + binaryOp + " " +
                rightExp.show() + ")";
  }

  Chu eval(Calc calc) throws ExecutionException
  {
    BinaryOperator operator = calc.lookupBinaryOperator(binaryOp);
    Chu left  = leftExp .eval(calc);
    if(left==null) throw new
      ExecutionException(leftExp.show()+" is undefined");

    Chu right = rightExp.eval(calc);
    if(right==null) throw new
      ExecutionException(rightExp.show()+" is undefined");

    return operator.apply(left, right, calc.getContext());
  }
}




