// A Node (of a Tree) represents a given prefix in a set of lines.
// The children of a node are the possible extensions of that prefix.
// A Node consists of:
//  an array of child Nodes (for non-leaves)
//  a List of line indexes (for leaves)
//  an pointer to the parent Node
//   (representing this prefix sans last element)
//  a branch number (the value of the last element)
class Node
{
  private Node children[];
  private Node parent;
  private int branch;
  private Link data;

  /* constructors */
  Node(Node parent, int branch)
  {
    this.children = null; // Create array of children only if needed
    this.parent = parent;
    this.branch = branch;
    this.data = null;
  }

  /* inspectors */

  Node child(int branch)
  {
    if (children == null) return null;
    else return children[branch];
  }

  final Node parent() { return parent;}
  final int branch() { return branch;}
  final Link data() { return data;}

  /* Mutators */

  // addDatum: append an index to the list.
  void addDatum(int datum)
  {
    data = new Link(data,datum);
  }

  // grow: Extend the current prefix using the given branch.
  // Return the Node representing the extended prefix.
  Node grow(int branch, int arity)
  {
    if (children == null)
      children = new Node[arity];
    if (children[branch] == null)
      children[branch] = new Node(this, branch);
    return children[branch];
  }

  /* for debugging purposes only */

  void show(String pad)
  {
    if(data != null) data.show(pad+"LIST ");

    if(children != null) {
      for(int i=0;i<children.length;i++) {
        if(children[i] != null) {
          System.out.println(pad + "Child " + Integer.toString(i));
          children[i].show(pad + " ");
          System.out.println(pad + "EndChild " + Integer.toString(i));
        }
      }
    }
  }
}



