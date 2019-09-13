import java.awt.*;

class Layout
{
  public static void
  addComponent(Container container, Component component,
               int gridx, int gridy,
               int gridwidth, int gridheight)
  {
    addComponent(container, component,
                 gridx, gridy, gridwidth, gridheight,
                 GridBagConstraints.NONE, GridBagConstraints.CENTER);
  }

  public static void
  addComponent(Container container, Component component,
               int gridx, int gridy,
               int gridwidth, int gridheight,
               int fill, int anchor)
  {
    LayoutManager lm = container.getLayout();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gridx;
    gbc.gridy = gridy;
    gbc.gridwidth = gridwidth;
    gbc.gridheight = gridheight;
    gbc.fill = fill;
    gbc.anchor = anchor;
    container.add(component, gbc);
  }
}




