import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Observable;

class Calc extends Observable
{
  /* Bindings and State */
  private Hashtable constants, variables, unops, binops, executables;
  private Conformable undef;
  private Context context;
  
  /* Calculation management */
  static final int BEGIN = 0;
  static final int END   = 1;
  static final int FAIL  = 2;
  int eventCode;   // BEGIN, END, or FAIL
  Thread calcThread;

  /* Construct a calculator */

  Calc()
  {
    calcThread = null;
    context = new Context(2,true);

    undef = new Conformable() {
      public Chu conform(Context context)
      {
        return null;
      }
    };
      
    /* Create and register all constants */

    constants = new Hashtable(10);

    constants.put("Undef", undef);

    constants.put("1",       new Conformable() {
      public Chu conform(Context context)
      {
        return new Chu(context.k);
      }
    });

    constants.put("_|_",     new Conformable() { 
      public Chu conform(Context context)
      {
        return (new Chu(context.k)).dual();
      }
    });

    try {
      constants.put("0",       new Chu("2", "0", "1", ""));
      constants.put("T",       new Chu("2", "1", "0", ""));

      constants.put("Pt2",     new Chu("2", "2", "2", "00\n01\n"));
      constants.put("Sp2",     new Chu("2", "2", "2", "01\n10\n"));
      constants.put("GF2^2",   new Chu("2", "4", "4",
                                       "0000\n0101\n0011\n0110\n"));
    }
    catch(Chu.ParseException x) {}

    /* Create and register all variables */

    variables = new Hashtable(20);

    try {
      bindVariable("p", null);
      bindVariable("q", null);
      bindVariable("u", null);
      bindVariable("v", null);
    }
    catch(ExecutionException x) {}

    // Create and register all unary operators

    unops = new Hashtable(5);

    unops.put("ID", new UnaryOperator() {
      Chu apply(Chu arg) throws ExecutionException
      {
        return arg;
      }
    });


    unops.put("_|_", new UnaryOperator() {
      Chu apply(Chu arg) throws ExecutionException
      {
        return arg.dual();
      }
    });

    unops.put("?", new UnaryOperator() {
      Chu apply(Chu arg) throws ExecutionException
      {
        return arg.query();
      }
    });

    unops.put("!", new UnaryOperator() {
      Chu apply(Chu arg) throws ExecutionException
      {
        return arg.dual().query().dual();
      }
    });

    // Create and register all binary operators

    binops = new Hashtable(10);

    binops.put("*", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.implication(leftArg, rightArg.dual()).dual();
      }
    });

    binops.put("#", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.implication(leftArg.dual(), rightArg);
      }
    });

    binops.put("-o", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.implication(leftArg, rightArg);
      }
    });

    binops.put("+", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.product(leftArg.dual(), rightArg.dual()).dual();
      }
    });

    binops.put("&", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.product(leftArg, rightArg);
      }
    });

    binops.put("=>", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.implication(leftArg.dual().query().dual(), rightArg);
      }
    });

    binops.put("U", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.choice(leftArg, rightArg);
      }
    });

    binops.put(";", new BinaryOperator() {
      Chu apply(Chu leftArg, Chu rightArg) throws ExecutionException
      {
        return Chu.sequence(leftArg, rightArg);
      }
    });

    /* Create and register all executables */

    executables = new Hashtable(5);

    Executable e = new Executable () {
      public void exec(Calc c)
      {
        c.setStandardization(true);
      }
    };
    executables.put("Unique", e);
    executables.put("on", e);
    e =	new Executable () {
      public void exec(Calc c)
      {
        c.setStandardization(false);
      }
    }; 
    executables.put("Multi", e);
    executables.put("multi", e);
    executables.put("off", e);
  }

  /* Manage Context */

  void setK(int value) throws ExecutionException 
  {
    if(value >= 0) {
      context.k = value;
    }
    else {
      throw new ExecutionException("Cannot use negative values for K");
    }
  }

  void setStandardization(boolean value) 
  {
    context.standardization = value;
  }

  Context getContext()
  {
    // Return a copy of the context.
    // Doen't expose original to unauthorized modification
    return new Context(context.k, context.standardization);
  }

  /* Manage variables and constants */

  void newVariableNameOK(String newVariableName) 
    throws ExecutionException
  {
    if(newVariableName.length() == 0)
      throw new ExecutionException("The empty string\n"+
                                   "is not a legal\n"+
                                   "variable name");

    if(constants.containsKey(newVariableName) ||
       variables.containsKey(newVariableName) )
    {
      throw new ExecutionException("Identifier "+newVariableName+
                                   "\nis in use");
    }
  }

  void bindVariable(String identifier, Chu value) throws ExecutionException
  {
    if (constants.containsKey(identifier)) 
      throw new ExecutionException("Can't change binding of constant "
                                   +identifier);
    variables.put(identifier, (value==null ? undef : value));
  }

  Enumeration lvalueIdentifiers()
  {
    // Values can be stored in any variable
    return variables.keys();
  }

  Enumeration rvalueIdentifiers() 
  {
    // Any non-undef variable or constant has a value

    Vector v = new Vector();

    for(Enumeration e=constants.keys() ; e.hasMoreElements() ; )
    {
      Object key = e.nextElement();
      if(constants.get(key) != undef) v.addElement(key);
    }
    for(Enumeration e=variables.keys() ; e.hasMoreElements() ; )
    {
      Object key = e.nextElement();
      if(variables.get(key) != undef) v.addElement(key);
    }

    return v.elements();
  }


  // Returns the conformed value of a variable or constant
  Chu lookupChu(String identifier) throws ExecutionException
  {
    Conformable result;

    if (constants.containsKey(identifier)) {
      result = (Conformable)constants.get(identifier);
    }
    else if(variables.containsKey(identifier)) { 
      result = (Conformable)variables.get(identifier);
    }
    else {
      throw new ExecutionException("Unknown Chu Space "+identifier);
    }
    return result.conform(context);
  }

  // Returns the unconformed value of a variable
  Chu lookupVariable(String identifier) throws ExecutionException
  {
    if(variables.containsKey(identifier)) {
      Object v = variables.get(identifier);
      if(v==undef) return null;
      else return (Chu)v;
    }
    else {
      throw new ExecutionException("Unknown Variable "+identifier);
    }
  }

  /* Manage UnaryOperators, BinaryOperators, Executables */

  UnaryOperator lookupUnaryOperator(String identifier) 
    throws ExecutionException
  {
    if(unops.containsKey(identifier)) {
      return (UnaryOperator)unops.get(identifier);
    }
    else {
      throw new ExecutionException("Unknown Unary Operation "+identifier);
    }
  }

  BinaryOperator lookupBinaryOperator(String identifier) 
    throws ExecutionException
  {
    if(binops.containsKey(identifier)) {
      return (BinaryOperator)binops.get(identifier);
    }
    else {
      throw new ExecutionException("Unknown Binary Operation "+identifier);
    }
  }

  Executable lookupExecutable(String identifier)
    throws ExecutionException
  {
    if(executables.containsKey(identifier)) {
      return (Executable)executables.get(identifier);
    }
    else {
      throw new ExecutionException("Unknown Executable "+identifier);
    }
  }

  /* Calculation management */

  void broadcast(int newEventCode, String message) 
  {
    eventCode = newEventCode;
    setChanged();
    notifyObservers(message);
  }

  void calculate(String programText, boolean showText)
    throws ExecutionException
  {
    if (calcThread != null) 
      throw new ExecutionException("Must CANCEL current calculation"+
                                   " before starting another");

    // Set up a separate thread to run the calculation.
    calcThread = new Thread(new Calculation(programText, showText));

    // Start the thread: it will take care of the rest.
    calcThread.start();    
  }

  void cancel() throws ExecutionException
  {
    if(calcThread == null)
      throw new ExecutionException("Nothing to CANCEL");

    // Stop and forget the current calculation.
    calcThread.stop();
    calcThread = null;

    // Tell everyone about the cancelation
    broadcast(FAIL, "Canceled");
  }

  // Calculation is a member class:  That is, every Calculation
  // object is attached to some Calc object, called Calc.this.
  // Calculation provides a run method, so a Calculation object
  // can be used to construct a Thread.
  class Calculation implements Runnable 
  {
    String programText;
    boolean showText;
 
    Calculation(String programText, boolean showText)
    {
      this.programText = programText;
      this.showText = showText;
    }

    public void run() 
    {
      // Tell everyone we're going to start,
      // Do the calculation
      // Tell everyone what happened.
      try {
        broadcast(BEGIN, showText ? programText : null);

        // This line literally does everything!
        // It creates a program from the given text,
        // and executes it against this calculator.
        (new Program(programText)).exec(Calc.this); 

        broadcast(END, showText ? programText : null);
      }
      catch (SyntaxException x) {
        broadcast(FAIL, x.getMessage());
      }      
      catch (ExecutionException x) {
        broadcast(FAIL, x.getMessage());
      }
      finally {
        // The calculatation is done, one way or another.
        // Forget about it!
        calcThread = null;
      }
    }
  }

  /* Main: for testing purposes only */

  public static void main(String args[]) throws IOException
  {
    Calc c = new Calc();

    byte[] buffer = new byte[1000];    
    while(true) {
      System.out.print("> ");
      int length = System.in.read(buffer);
      String programText = new String(buffer, 0, length);
      try {
        (new Program(programText)).exec(c);
      }
      catch (SyntaxException e) {
        System.out.println(e.getMessage());
      }
      catch (ExecutionException e) {
        System.out.println(e.getMessage());
      } 
    }
  }
}


