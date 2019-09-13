import java.util.StringTokenizer;

abstract class Statement implements Executable
{
  static Statement parse(String statementText) throws SyntaxException 
  {
    int index = statementText.indexOf('=');

    if(index < 0) {
      int value;
      try {
        value = Integer.parseInt(statementText);
        return new SetKStatement(value);
      } 
      catch(NumberFormatException e) {
        return new InvokeStatement(statementText);
      }
    }
    else {
      String lhs = statementText.substring(0, index).trim();
      String rhs = statementText.substring(index+1).trim();
      return new AssignStatement(lhs,rhs);
    }
  }

  abstract String show();

  public abstract void exec(Calc c) throws ExecutionException;
}

class SetKStatement extends Statement
{
  int value;

  SetKStatement(int _value) { value = _value; }

  String show() {
    return "Change K to " + value;
  }

  public void exec(Calc c) throws ExecutionException {
    c.setK(value);
  }
}

class InvokeStatement extends Statement
{
  String executableName;

  InvokeStatement(String executableName) 
  {
    this.executableName = executableName;
  }

  String show() 
  { 
    return "Invoke program " + executableName; 
  }

  public void exec(Calc calc) throws ExecutionException 
  {
    calc.lookupExecutable(executableName).exec(calc);
  }
}

class AssignStatement extends Statement
{
  String lhs;
  Expression rhs;

  AssignStatement(String lhs, String rhs) throws SyntaxException 
  {
    this.lhs = lhs;
    this.rhs = Expression.parse(rhs);
  }

  String show()
  {
    return lhs + " gets " + rhs.show();
  }

  public void exec(Calc calc) throws ExecutionException
  {
    calc.bindVariable(lhs, rhs.eval(calc));
  }
}
