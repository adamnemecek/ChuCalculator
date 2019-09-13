import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

class Program implements Executable {
  Vector statements;

  Program(String programText) throws SyntaxException
  {
    statements = new Vector();

    StringTokenizer tokenizer = new StringTokenizer(programText, ",\n");
    while(tokenizer.hasMoreTokens())
    {
      String statementText = tokenizer.nextToken().trim();
      if(statementText.length() != 0)
        statements.addElement(Statement.parse(statementText));
    }
  }

  void show()
  {
    System.out.println("Program:");
    Enumeration enum = statements.elements();
    while(enum.hasMoreElements()) {
      Statement s = (Statement)enum.nextElement();
      System.out.println(s.show());
    }
  }

  public void exec(Calc c) throws ExecutionException
  {
    Enumeration enum = statements.elements();
    while(enum.hasMoreElements()) {
      Statement s = (Statement)enum.nextElement();
      s.exec(c);
    }
  }
}







