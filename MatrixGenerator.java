/* Generates all matrixes whose rows and columns are taken
 * from the prefix trees passed at construction time.
 *
 * After construction, each successful call to next
 * produces a representation of a new matrix.
 */
class MatrixGenerator
{
  private Tree rowTree; // prefix tree of rows
  private Tree colTree; // prefix tree of columns

  private int nrows,ncols; // Shape of matrix
  private int K; // Entries of matrix are in 0...K-1

  // The search algorithm works by trial extension of a region
  // of overlapping partial rows and columns.  The two arrays
  // below represent that region by locating the Node for
  // each partial row and column.
  private Node rowNodes[];
  private Node colNodes[];

  // more search variables:  current(Row/Col/Branch)
  // these variables give the cell we are trying to fill,
  // and the value we are trying to fill it with.
  private int currentRow, currentCol, currentBranch;

  // if done is true then there are no more matricies
  private boolean done;

  // These arrays represent a matrix.  (The arrays point
  // to lists of indexes of lines that form the matrix.)
  // After a successful call to next(), the caller
  // can examine these arrays to extract the matrix.
  Link rowLinks[];
  Link colLinks[];

  /* Constructor */

  MatrixGenerator(Tree rowTree,Tree colTree)
  {
    ncols = rowTree.length();
    currentCol=0;

    nrows = colTree.length();
    currentRow=0;

    K = rowTree.arity();
    currentBranch=0;

    done = false;

    // initialize row search arrays
    rowNodes = new Node[nrows];
    rowLinks = new Link[nrows];
    for(int r=0;r<nrows;r++) {
      rowNodes[r] = rowTree.top();
      rowLinks[r] = null;
    }

    // initialize column search arrays
    colNodes = new Node[ncols];
    colLinks = new Link[ncols];
    for(int c=0;c<ncols;c++) {
      colNodes[c] = colTree.top();
      colLinks[c] = null;
    }
  }

  /* Inspectors */

  int nrows() { return nrows;}
  int ncols() { return ncols;}
  int K() { return K;}

  // next: Try to find the next morphism
  // If there is no such morphism, return false
  // If there is such a morphism, put lists of the
  // possible rows and columns into rowLinks, colLinks,
  // then return true.
  boolean next()
  {
    boolean success;

    // Loop Invarients:
    // The prefixes represented by rowNodes and colNodes
    //   cover the same set of cells and match in all values.
    // This set of cells is always the interval before some cell
    //   in the following "herringbone" order:
    //      1  2  3  4
    //     5   9 10 11
    //     6 12  15 16
    //     7 13 17  19
    //     8 14 18 20

    if (done) return false;

    // Outer loop: drive search forward, extending matrix,
    // check for when we go out of bounds.
    outer: while(currentRow<nrows && currentCol<ncols)
    {
      // Inner loop: go forward one step.
      // Back up as many cells as needed before taking a forward step.
      while(true)
      {
        // If all possibilities for this cell are exhausted,
        // then back up until it is possible to go forward.
        // If we have to back up and fail, then return false.
        while(currentBranch==K) {
          success = backward();
          if (!success) return false;
        }

        // If we succeed in going forward, then we re-test bounds.
        // Otherwise we try another value for currentBranch
        success = forward();
        if(success) continue outer;
        else currentBranch++;
      }
    }

    // If we get here, the search went out of bounds.
    // Thus we have a matrix to record.
    for(int r=0;r<nrows;r++)
      rowLinks[r] = rowNodes[r].data();
    for(int c=0;c<ncols;c++)
      colLinks[c] = colNodes[c].data();

    // move search one step beyond this morphism
    // then return true to indicate we have a morphism
    backward();
    return true;
  }

  private boolean backward()
  {
    // Can't back up from 0,0
    if(currentRow==0 && currentCol==0) {
      done = true;
      return false;
    }

    // First step currentRow, currentCol backward
    if(currentRow <= currentCol) {
      currentCol--;  // Shrink a row leftwards

      // If the row is entirely empty,
      //  then go to the end of the previous column.
      if(currentRow == currentCol+1)
         currentRow = nrows-1;
    }
    else {
      currentRow--;  // Shrink a column upwards

      // If the column is entirely empty,
      //  then go to the end of the previous row.
      if (currentRow == currentCol)
          currentCol = ncols-1;
    }

    // Second, restore currentBranch and the prefix trees
    currentBranch = rowNodes[currentRow].branch();
    currentBranch++;
    rowNodes[currentRow] = rowNodes[currentRow].parent();
    colNodes[currentCol] = colNodes[currentCol].parent();

    return true;  // Report Success
  }

  private boolean forward()
  {
    // Try the current value of branch in the current cell
    Node rn = rowNodes[currentRow].child(currentBranch);
    Node cn = colNodes[currentCol].child(currentBranch);

    // If it doesn't work, then report failure
    if (rn == null || cn == null) return false;

    // First update currentBranch and the prefix trees
    rowNodes[currentRow] = rn;
    colNodes[currentCol] = cn;
    currentBranch = 0;

    // Second, step currentRow, currentCol forward
    if (currentRow <= currentCol) {
      currentCol++;  // Grow a row rightward

      // If the row is entirely full,
      //  then go to the start of the next column.
      if (currentCol == ncols) {
        currentCol = currentRow;
        currentRow = currentCol+1;
      }
    }
    else {
      currentRow++;  // Grow a column downward

      // If the column is entirely full,
      //  then go to the start of the next row.
      if (currentRow == nrows) {
        currentRow = currentCol+1;
        currentCol = currentRow;
      }
    }

    return true; // Report Success
  }

  public static void main(String args[]) throws Chu.ParseException
  {
    Chu source,target;
    MatrixGenerator G;

    source = new Chu("2", "4", "4",
                     "0000\n0101\n0011\n0110\n");
    target = new Chu("2", "4", "4",
                     "1100\n0110\n0011\n1001\n");

    Tree rt = target.rowTree();
    rt.show();
    Tree ct = source.colTree();
    ct.show();

    G = new MatrixGenerator(rt,ct);
    System.out.println("made generator");

    int i=0;
    while(G.next()) {
      i++;
      System.out.println("Morphism "+Integer.toString(i));
      System.out.println("Row Map");
      for(int r=0;r<G.rowLinks.length;r++) {
        G.rowLinks[r].show("From " + Integer.toString(r)+" To ");
      }
      System.out.println("Column Map");
      for(int c=0;c<G.colLinks.length;c++) {
        G.colLinks[c].show("From " + Integer.toString(c)+" To ");
      }
      System.out.println("");
    }
  }
}






















