import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Observer;
import java.util.Observable;

public class GUI extends Applet
{
  // Calculation engine
  Calc calc;

  // Status Panel components
  TextField statusField;
  Button cancelButton;

  // Context Panel components
  boolean ignore;
  Checkbox standardizationBox;
  TextField kField;

  // Main panel components
  ChuSource inputLeft, inputRight;
  ChuTarget output;

  // Operation Button Layout
  static String[] unaryButtonNames = {
    "ID", "_|_", "!", "?"
  };

  static String[] binaryButtonNames = {
      "+", "*", null, ";", "U", null, "&", "#", "-o", "=>"
  };
  
  // Bottom Panel modes
  static String[] modes = {"Script", "Edit"};
  static final int SCRIPT = 0;
  static final int EDIT   = 1;

  // INIT:  Builds all GUI components, and
  // sets up their layout and event handling 
  public void init() 
  {
    // First Build the calculator
    calc = new Calc();
    
    // Use bigger font for readability
    Font ourfont = new Font("Helvetica",Font.BOLD,14);
    setFont(ourfont);

    // Status Panel
    Panel statusPanel = new Panel();

    statusField = new TextField(50);
    statusField.setEditable(false);

    statusPanel.add(new Label("Status"));
    statusPanel.add(statusField);

    cancelButton = new Button("CANCEL");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        try {
          calc.cancel();
        }
        catch(ExecutionException x) {
          statusField.setText(x.getMessage());
        }
      }
    });
    statusPanel.add(cancelButton);

    calc.addObserver(new Observer() {
      public void update(Observable o, Object arg) 
      {
        switch(calc.eventCode) {
        case Calc.BEGIN:
          if(arg==null)
            statusField.setText("Executing script");
          else
            statusField.setText("Executing " + (String)arg);
          break;

        case Calc.END:
          statusField.setText("Done");
          break;

        case Calc.FAIL:
          statusField.setText((String)arg);
          break;
        }
      }
    });

    // Context panel
    Panel contextPanel = new Panel();

    ignore = true;

    standardizationBox = new Checkbox("Unique");
    standardizationBox.setState(true);

    standardizationBox.addItemListener( new ItemListener() {
      public void itemStateChanged(ItemEvent e)
      {
        if(ignore) return;
        try {
          calc.calculate((standardizationBox.getState() ? "Unique" : "Multi"), 
                         true);
        }
        catch(ExecutionException x) {
          calc.broadcast(Calc.FAIL, x.getMessage());
        }
      }
    });
    contextPanel.add(standardizationBox);
	
    kField = new TextField("2");
    kField.setEditable(true);

    kField.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        if(ignore) return;

        int newK;
        String kText = kField.getText();
        try {
          newK = Integer.parseInt(kText);
          calc.calculate(Integer.toString(newK), true);  
        }
        catch(NumberFormatException x) {
          calc.broadcast(Calc.FAIL, "can't parse K='"+kText+"'");
        }
        catch(ExecutionException x) {
          calc.broadcast(Calc.FAIL, x.getMessage());
        }
      }
    });
    contextPanel.add(new Label("K"));
    contextPanel.add(kField);

    ignore = false;

    calc.addObserver(new Observer() {
      public void update(Observable o, Object arg) 
      {
        switch(calc.eventCode) {
        case Calc.BEGIN:
          break;
        case Calc.END: // Fall through
        case Calc.FAIL: 
          ignore = true;
  
          Context context = calc.getContext();
  
          boolean std = context.standardization;
          standardizationBox.setState(std);
          standardizationBox.setLabel(std ? "Unique" : "Multi");
    
          kField.setText(Integer.toString(context.k));

          ignore = false;
        }
      }
    });
   
    // Operation button panels

    Panel unaryOperButtonPanel = new Panel(new GridBagLayout());

    for(int i=0; i<unaryButtonNames.length; ++i)
    {
      String name = unaryButtonNames[i];
      Component c;

      if(name == null) {
        c = new Label("");
      }
      else {
        Button b = new Button(name);

        b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) 
          {
            try {
              String statementText = output.getIdentifier()+
                                     "="+ 
                                     e.getActionCommand()+
                                     " "+
                                     inputLeft.getIdentifier();
              calc.calculate(statementText,true);
            }
            catch(ExecutionException x) {
              statusField.setText(x.getMessage());
            }
          }
        });

        c = b;
      }
      Layout.addComponent(unaryOperButtonPanel, c,
                          0, i, 1, 1);
    }

    Panel binaryOperButtonPanel = new Panel(new GridBagLayout());

    for(int i=0; i<binaryButtonNames.length; ++i)
    { 
      String name = binaryButtonNames[i];
      Component c;

      if(name==null) {
        c = new Label("");
      }
      else {
        Button b = new Button(name);

        b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) 
          {
            try {
              String statementText = output.getIdentifier()+
                                     "="+ 
                                     inputLeft.getIdentifier()+
                                     " "+
                                     e.getActionCommand()+
                                     " "+
                                     inputRight.getIdentifier();
              calc.calculate(statementText,true);
            }
            catch(ExecutionException x) {
              statusField.setText(x.getMessage());
            }
          }
        });

        c = b;
      }

      Layout.addComponent(binaryOperButtonPanel, c,
                          0, i, 1, 1);
    }

    // Main panel 
    Panel mainPanel = new Panel(new GridBagLayout());

    Layout.addComponent(mainPanel, unaryOperButtonPanel,
                        0, 0, 1, 8);

    inputLeft = new ChuSource(calc);
    Layout.addComponent(mainPanel, inputLeft,
                        1, 0, 2, 8);

    Layout.addComponent(mainPanel, binaryOperButtonPanel,
                        3, 1, 1, 8);

    inputRight = new ChuSource(calc);
    Layout.addComponent(mainPanel, inputRight,
                        4, 0, 2, 8);
 
    output = new ChuTarget(calc);
    Layout.addComponent(mainPanel, output,
                        6, 0, 2, 8);

    // Card Panel
    final CardLayout cardLayout = new CardLayout();
    final Panel cardPanel = new Panel(cardLayout);

    cardPanel.add(new ScriptPanel(calc), modes[SCRIPT]);
    cardPanel.add(new ChuEdit(calc),     modes[EDIT]);
   
    // CheckboxPanel
    Panel checkboxPanel = new Panel(new GridLayout(modes.length, 1));

    final CheckboxGroup cbg = new CheckboxGroup();
    Checkbox[] cba = new Checkbox[modes.length]; 
    for(int i=0;i<modes.length;i++) {
      cba[i]  = new Checkbox(modes[i],false,cbg);
      checkboxPanel.add(cba[i]);

      cba[i].addItemListener( new ItemListener() {
        public void itemStateChanged(ItemEvent e)
        {
          cardLayout.show(cardPanel, cbg.getSelectedCheckbox().getLabel());
        }
      });
    }
    cbg.setSelectedCheckbox(cba[SCRIPT]);

    // BottomPanel
    Panel bottomPanel = new Panel();
    bottomPanel.add(checkboxPanel);  
    bottomPanel.add(cardPanel);

    // Whole Applet

    this.setLayout(new GridBagLayout());

    Layout.addComponent(this, statusPanel,
                        0, 0, 1, 1);

    Layout.addComponent(this, contextPanel,
                        0, 1, 1, 1);

    Layout.addComponent(this, mainPanel,
                        0, 2, 1, 1);

    Layout.addComponent(this, bottomPanel,
                        0, 3, 1, 1);
  }
}




