class Link
{
  private Link next;
  private int datum;

  /* constructor */

  Link(Link _next,int _datum)
  {
    next = _next;
    datum = _datum;
  }

  /* inspectors */

  final Link next() { return next; }
  final int datum() { return datum; }

  /* for debugging purposes only */

  void show(String pad)
  {
    System.out.println(pad + Integer.toString(datum));
    if(next != null) next.show(pad);
  }
}







