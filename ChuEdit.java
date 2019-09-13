import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Observer;
import java.util.Observable;

class ChuEdit extends Panel {
  // Data
  Calc calc;

  // GUI Elements
  Choice choice;
  ChuView leftView, rightView;
  Button parseButton, storeButton;
  TextArea messageArea;

  ChuEdit(Calc _calc) 
  {
    calc = _calc;

    setLayout(new GridBagLayout());

    // choice: menu of space names
    choice = new Choice();
    loadChoice();

    // pick initial setting
    choice.select("p");

    choice.addItemListener( new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
        DisplayIdentifier();
      }
    });
    Layout.addComponent(this, choice,
                        0, 0, 3, 1);

    // leftView: displays space for editing
    leftView = new ChuView();

    leftView.rowField.setEditable(true);
    leftView.rowField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {
        Parse();        
      }
    });

    leftView.colField.setEditable(true);
    leftView.colField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {
        Parse();        
      }
    });

    leftView.kField.setEditable(true);
    leftView.kField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {
        Parse();        
      }
    });

    leftView.textArea.setEditable(true);

    Layout.addComponent(this, leftView,
                        0, 1, 3, 5);

    // parseButton: parse leftView and display result in rightView
    parseButton = new Button("Parse>>");
    parseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {
        Parse();
      }
    });
    Layout.addComponent(this, parseButton,
                        3, 1, 1, 1);

    // storeButton: save rightView into location selected by choice
    storeButton = new Button("<<Store");
    storeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {
        Store();
      }
    });
    Layout.addComponent(this, storeButton,
                        4, 1, 1, 1);

    // messageArea: Display error messages and other feedback;
    messageArea = new TextArea(null, 10, 20,
                               TextArea.SCROLLBARS_NONE);
    messageArea.setEditable(false);
    Layout.addComponent(this, messageArea,
                        3, 2, 2, 4);


    // rightView: displays result of parsing
    rightView = new ChuView();
    
    Layout.addComponent(this, rightView,
                        5, 1, 3, 5);

   
    // register observer to handle updates after calculation 
    calc.addObserver( new Observer() {
      public void update(Observable o, Object arg)
      {
        switch(calc.eventCode) {
        case Calc.BEGIN:
          break;
        case Calc.END: // Fall through
        case Calc.FAIL:
          String identifier = choice.getSelectedItem();

          choice.removeAll();
          loadChoice();

          if(identifier != null) choice.select(identifier);    

          DisplayIdentifier();
          break;
        }
      }
    });

    DisplayIdentifier();
  }

  /* Internal Utilities */

  void loadChoice()
  {
    Enumeration e = calc.lvalueIdentifiers();
    while(e.hasMoreElements()) 
    {
      choice.add((String)e.nextElement());
    }
  }

  void DisplayIdentifier()  
  { 
    try {
      String identifier = choice.getSelectedItem();
      leftView.DisplaySpace(calc.lookupVariable(identifier), true);
    }
    catch (ExecutionException x) {
      messageArea.setText(x.getMessage());
    }
  }

  void Parse() 
  {
    if(leftView.textArea.getText().equals("Undefined")) {
      messageArea.setText("Please enter a grid of digits.  "+
                          "\"Undefined\" doesn't parse to a chu space");
      return;
    }
    try {
      rightView.DisplaySpace( new Chu(leftView.kField.  getText(),             
                                      leftView.rowField.getText(),
                                      leftView.colField.getText(),
                                      leftView.textArea.getText()));
      messageArea.setText("Parse finished OK.\n"+
                          "Hit STORE to save result in a variable.");
    }
    catch (Chu.ParseException x) {
      messageArea.setText(x.getMessage());
    }    
  }

  void Store()
  {
    try {
      String identifier = choice.getSelectedItem();
      calc.bindVariable(identifier, rightView.currentChu);
      calc.broadcast(Calc.END, null);
      messageArea.setText("Changed value "+
                          "of "+identifier);
    }
    catch(ExecutionException x) {
      messageArea.setText(x.getMessage());
    }
  }
}
