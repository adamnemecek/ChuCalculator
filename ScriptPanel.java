import java.awt.*;
import java.awt.event.*;
import java.util.Observer;
import java.util.Observable;

class ScriptPanel extends Panel
{
  TextArea scriptArea;
  Button execButton;
  Calc calc;

  ScriptPanel(Calc _calc)
  {
    super();
    calc = _calc;

    setLayout(new GridBagLayout());

    scriptArea = new TextArea(null, 10, 50,
                              TextArea.SCROLLBARS_VERTICAL_ONLY);
    Layout.addComponent(this, scriptArea,
                        0, 0, 10, 4);

    execButton = new Button("Execute selected text");
    execButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        try {
          calc.calculate(scriptArea.getSelectedText(),false);
        }
        catch(ExecutionException x) {
          calc.broadcast(Calc.FAIL, x.getMessage());
        }
      }
    });
    Layout.addComponent(this, execButton,
                        0, 4, 10, 1);

    calc.addObserver(new Observer() {
      public void update(Observable o, Object arg)
      {
        switch(calc.eventCode) {
        case Calc.BEGIN:
        case Calc.FAIL:
          break;

        case Calc.END:
          if(arg != null)
            scriptArea.setText(scriptArea.getText() + (String)arg + ", ");
          break;
        }
      }
    });
  }
}

