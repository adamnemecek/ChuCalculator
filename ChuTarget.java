import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Observer;
import java.util.Observable;

class ChuTarget extends Panel
{
  // Data
  Calc calc;
  boolean newVariableMode;

  // GUI Elements
  ChuView view;
  Choice choice;
  TextField newVariableField;
  
  ChuTarget(Calc _calc) 
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
        if (choice.getSelectedItem().equals("NEW")) {
          newVariableModeOn();
        }
        else {
          if(newVariableMode) {
            newVariableModeOff();
          }
          else {
            DisplayIdentifier();
          }
        }
      }
    });
    Layout.addComponent(this, choice,
                        0, 0, 2, 1);

    newVariableField = new TextField(8); 
    newVariableField.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {
        if(e.getSource() == newVariableField) {
          try {
            String newVariableName = getIdentifier();
            calc.calculate(newVariableName+"=Undef", true);
          }
          catch(ExecutionException x) {
            view.DisplayMessage(x.getMessage());
          }
        }
      }
    });
    Layout.addComponent(this, newVariableField, 
                        2, 0, 1, 1);

    // view: displays selected space
    view = new ChuView();
    Layout.addComponent(this, view,
                        0, 1, 3, 5);    
   
    // register observer to handle updates after calculation 
    calc.addObserver( new Observer() {
      public void update(Observable o, Object arg)
      {
        switch(calc.eventCode) {
        case Calc.BEGIN:
          break;
        case Calc.END: // Fall through
        case Calc.FAIL:
          String identifier;
          if(newVariableMode)
            identifier = newVariableField.getText().trim();
          else
            identifier = choice.getSelectedItem();

          choice.removeAll();
          loadChoice();

          if(identifier != null) choice.select(identifier);    

          newVariableModeOff();
        }
      }
    });

    newVariableModeOff();
  }

  /* Internal Utilities */

  void newVariableModeOn() {
    newVariableMode = true;
    newVariableField.setEditable(true);
    newVariableField.requestFocus();
    view.DisplayMessage("Enter name\nof new variable");
  }

  void newVariableModeOff() {
    newVariableMode = false;
    newVariableField.setEditable(false);
    newVariableField.setText("");
    DisplayIdentifier();
  }

  void loadChoice()
  {
    Enumeration e = calc.lvalueIdentifiers();
    while(e.hasMoreElements()) 
    { 
      choice.add((String)e.nextElement());
    }
    choice.add("NEW");
  }

  void DisplayIdentifier()  
  { 
    try {
      view.DisplaySpace(calc.lookupChu(getIdentifier()));   
    }
    catch (ExecutionException x) {
      view.DisplayMessage(x.getMessage());
    }
  }

  /* Interface used by GUI.java */

  String getIdentifier() throws ExecutionException 
  {
    if(newVariableMode) {
      String newVariableName = newVariableField.getText().trim();
      calc.newVariableNameOK(newVariableName);
      return newVariableName;
    }
    else 
      return choice.getSelectedItem();
  }
}




