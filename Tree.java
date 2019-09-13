// A tree is used to store a collection of equal-length "lines"
// The lines are sequences of integers in the range 0..arity-1

class Tree
{
  private int arity;
  private int length;
  private Node top;

  /* constructor */
  Tree(int arity,int length)
  {
    this.arity = arity;
    this.length = length;
    this.top = new Node(null,0);
  }

  /* inspectors */
  final int arity() { return arity;}
  final int length() { return length;}
  final Node top() { return top;}

  // findLine: Returns a linked list of the
  // indexes of all lines matching the given line.
  Link findLine(int line[])
  {
    if(line.length != length) return null;

    Node current=top;

    for(int loc=0;loc<line.length;loc++) {
      current = current.child(line[loc]);
      if (current==null) return null;
    }
    return current.data();
  }

  /* mutator */

  // addLine: Inserts the given line at the given index.
  // Returns a linked list of the indexes of all other
  // lines which match the new line.
  Link addLine(int line[], int index)
  {
    if(line.length != length) return null;

    Node current=top;

    for(int loc=0; loc<line.length; loc++)
      current = current.grow(line[loc], arity);

    Link result = current.data();
    current.addDatum(index);
    return result;
  }

  /* for debugging purposes only */

  void show()
  {
    System.out.println("ARITY "+arity);
    System.out.println("LENGTH "+length);
    top.show("");
  }
}









