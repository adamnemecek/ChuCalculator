import java.awt.*;

// CHUVIEW: For displaying and editing Chu spaces
class ChuView extends Panel
{
  // Data
  Chu currentChu;
 
  // GUI elements
  TextField rowField,colField,kField;
  TextArea textArea;

  ChuView() {
    super();    
    this.currentChu = null;    

    // Build and layout ChuView
    setLayout(new GridBagLayout());

    rowField = new TextField(4); 
    rowField.setEditable(false);
    Layout.addComponent(this, new Label("Points"), 
                        0, 0, 1, 1);
    Layout.addComponent(this, rowField, 
                        0, 1, 1, 1);

    colField = new TextField(4); 
    colField.setEditable(false);
    Layout.addComponent(this, new Label("States"), 
                        1, 0, 1, 1);
    Layout.addComponent(this, colField, 
                        1, 1, 1, 1);

    kField   = new TextField(4);   
    kField.setEditable(false); 
    Layout.addComponent(this, new Label("k"), 
                        2, 0, 1, 1);
    Layout.addComponent(this, kField, 
                        2, 1, 1, 1);

    textArea = new TextArea(10,20); 
    textArea.setEditable(false);
    Layout.addComponent(this, textArea, 
                        0, 2, 3, 3);
  }

  /* Convenience methods */

  void DisplayMessage(String message)
  {
    currentChu = null;

    rowField.setText("");
    colField.setText("");
    kField.setText("");
    textArea.setText(message);
  }

  // By default, display matrix, nrows, ncols, and k.
  // call DisplaySpace(foo, true) to display only the matrix.
  void DisplaySpace(Chu newChu) { 
    DisplaySpace(newChu, false); 
  } 
  void DisplaySpace(Chu newChu, boolean matrixOnly) 
  {
    if(newChu == null) {
      currentChu = null;
      DisplayMessage("Undefined");
    }
    else if(newChu != currentChu) {
      currentChu = newChu;
      
      if(!matrixOnly) {
        kField.setText(String.valueOf(currentChu.K()));
        rowField.setText(String.valueOf(currentChu.nrows()));
        colField.setText(String.valueOf(currentChu.ncols()));
      }
      try {
        textArea.setText(currentChu.unparse());
      }
      catch(Chu.ParseException x) {
        DisplayMessage(x.getMessage());
      }
    }
  }

  
}






