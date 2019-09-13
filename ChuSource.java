import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Observer;
import java.util.Observable;

class ChuSource extends Panel {
  // Data
  Calc calc;

  // GUI Elements
  ChuView view;
  Choice choice;

  ChuSource(Calc _calc)
  {
    calc = _calc;

    setLayout(new GridBagLayout());

    // choice: menu of space names
    choice = new Choice();
    loadChoice();

    // Pick initial setting
    choice.select("1");

    choice.addItemListener( new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
        DisplayIdentifier();
      }
    });
    Layout.addComponent(this, choice,
                        0, 0, 3, 1);

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
          String identifier = getIdentifier();

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
    Enumeration e = calc.rvalueIdentifiers();
    while(e.hasMoreElements())
    {
      choice.add((String)e.nextElement());
    }
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

  String getIdentifier()
  {
    return choice.getSelectedItem();
  }
}
