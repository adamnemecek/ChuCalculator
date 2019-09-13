import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;
import java.util.StringTokenizer;

class Chu implements Conformable
{
  /* A Chu space is a matrix with entries drawn from
   * a set of some finite size K.  The members of the
   * set are represented by numbers in [0,K-1].
   */
  private int K;
  private int nrows;
  private int ncols;
  private int matrix[][];
  private Chu standard;  // Pointer to standardized version of this space

  /* Trusting constructor: performs no consistency checks
   */
  private Chu(int K, int nrows, int ncols, int matrix[][],
              boolean standardized)
  {
    this.K = K;
    this.nrows = nrows;
    this.ncols = ncols;
    this.matrix = matrix;
    standard = (standardized ? this : null);
  }

  /* Special constructor: builds tensor unit
   */
  Chu(int size)
  {
    this(size, 1, size, new int[1][size], true);
    for(int i=0;i<size;i++) matrix[0][i] = i;
  }

  /* Parse constructor: builds a Chu space from the given Strings.
   * Rows are newline terminated, other whitespace is ignored.
   * Entries are represented by digits 0..K-1 ( K<=10 ).
   * A ParseException is thrown to report trouble.
   */

  class ParseException extends Exception
  {
    ParseException(String message) { super(message); }
  }

  Chu(String kText, String rowText, String colText, String text)
    throws ParseException
  {
    this(0, 0, 0, null, false);

    // Initialize K, nrows, ncols

    boolean kInit=false;
    K = 0;
    if (kText!=null) {
      kText = kText.trim();
      try {
        K = Integer.parseInt(kText);
        if(K<0 || K>10) throw new
          ParseException("K="+K+" is out of bounds.  Use 0<=K<=10");
        else
          kInit = true;
      }
      catch(NumberFormatException x) {
        if(kText.length() != 0) throw new
          ParseException("can't parse K='"+kText+"'");
      }
    }

    boolean nrowsInit=false;
    nrows = 0;
    if (rowText!=null) {
      rowText = rowText.trim();
      try {
        nrows = Integer.parseInt(rowText);
        if(nrows<0) throw new
          ParseException("#rows="+nrows+" is out of bounds.  "+
                              " Use nrows>=0");
        else
          nrowsInit = true;
      }
      catch(NumberFormatException x) {
        if(rowText.length() != 0) throw new
          ParseException("can't parse #rows='"+rowText+"'");
      }
    }

    boolean ncolsInit=false;
    ncols = 0;
    if (colText!=null) {
      colText = colText.trim();
      try {
        ncols = Integer.parseInt(colText);
        if(ncols<0) throw new
          ParseException("#cols="+ncols+" is out of bounds.  "+
                              " Use ncols>=0");
        else
          ncolsInit = true;
      }
      catch(NumberFormatException x) {
        if(colText.length() != 0) throw new
          ParseException("can't parse #cols='"+colText+"'");
      }
    }

    // Set up rowTokenizer
    // Infer nrows if not already initialized
    // Set up matrix

    if (text==null) text = "";
    text = text.trim(); // cut trailing whitespace, esp. newlines

    StringTokenizer rowTokenizer = new StringTokenizer(text,"\n");
    if(!nrowsInit) {
      nrows = rowTokenizer.countTokens();
      nrowsInit = true;
    }
    matrix = new int[nrows][];

    // Build rows from rowTokenizer: store results in matrix

    int r;
    for(r=0; r<nrows && rowTokenizer.hasMoreTokens() ;r++)
    {

      String rowString = rowTokenizer.nextToken();
      Vector entries = new Vector();

      // put all the entries in this row into the Vector entries

      for(int j=0;j<rowString.length();j++)
      {
        char ch = rowString.charAt(j);
        if((ch == ' ' || ch == '\t')) continue; // skip white space

        if(Character.isDigit(ch)) {
          int d = Character.digit(ch,10);
          if(kInit) {
            if (d < K)
              entries.addElement(new Integer(d));
            else { // Out of bounds
              throw new ParseException("Entry ("+(r+1)+","+
                                       (entries.size()+1)+")"+
                                       " out of bounds: "+
                                       d+" > K-1="+(K-1));
            }
          }
          else { // !kInit
            entries.addElement(new Integer(d));
            if(d>=K) K=d+1;
          }
        }
        else { // !isDigit
          int c = entries.size();
          throw new ParseException("Entry ("+(r+1)+","+(c+1)+")"+
                                   "=`"+rowString.substring(c,c+1)+
                                   "' is not a digit");
        }
      }

      // Infer ncols if not already initialized
      // Set up matrix[r]

      if(!ncolsInit) {
        ncols = entries.size();
        ncolsInit = true;
      }
      matrix[r] = new int[ncols];

      // copy ncols elements into matrix[r]: pad as needed

      int c;
      for(c=0 ; c<ncols && c<entries.size() ; c++)
        matrix[r][c] = ((Integer)entries.elementAt(c)).intValue();
      for(; c<ncols ; c++)
        matrix[r][c] = 0;
    }

    // Pad matrix with rows of zeros as needed

    for( ; r<nrows ; r++) {
      matrix[r] = new int[ncols];
      for(int c=0; c<ncols; c++) {
        matrix[r][c] = 0;
      }
    }
  }

  /* unparse: Returns a string representing this space.
   * Errors are handled by throwing a ParseException/
   */
  String unparse() throws ParseException
  {
    if(K>10) throw new ParseException("K="+K+" is out of bounds");
    StringBuffer out = new StringBuffer(100 + nrows*(ncols+1));

    for(int r=0;r<nrows;r++) {
      for(int c=0;c<ncols;c++) {
        out.append(Character.forDigit(matrix[r][c],10));
      }
      out.append('\n');
    }
    return out.toString();
  }

  /* Inspectors */

  int K() { return K;}
  int nrows() { return nrows;}
  int ncols() { return ncols;}

  Tree rowTree()
  {
    Tree result = new Tree(K,ncols);
    int row[];
    for(int r=0;r<nrows;r++) {    // Loop over rows
      row = matrix[r];            // Get next row
      result.addLine(row,r);      // add row to tree
    }
    return result;
  }

  Tree colTree()
  {
    Tree result = new Tree(K,nrows);
    int col[] = new int[nrows];
    for(int c=0;c<ncols;c++) {    // Loop over columns
      for(int r=0;r<nrows;r++)    // Get next column
        col[r]=matrix[r][c];
      result.addLine(col,c);      // add column to tree
    }
    return result;
  }

  /* Unary Operations */

  Chu dual()
  {
    int new_matrix[][] = new int[ncols][nrows];

    for(int c=0;c<nrows;c++) for(int r=0;r<ncols;r++)
      new_matrix[r][c] = matrix[c][r];

    return new Chu(K, ncols, nrows, new_matrix,
                   (standard==this)); // dual is standard iff original is
  }

  // query: The rows of ?A are closed under the following operation:
  // Form a square matrix whose rows and columns are rows of A, and
  // build a new row from the diagonal.  The implementation below
  // simply performs this operation repeatedly until there is nothing
  // new generated.
  Chu query()
  {
    if(K==2) return query2();

    // The final number of rows is unknown,
    // so for now hold them in a Vector.
    Vector result_rows = new Vector();
    for(int r=0; r<nrows; r++)
      result_rows.addElement(matrix[r]);

    // row_tree holds the same rows as result_rows.
    //  (the Tree form is useful for feeding the MatrixGenerator)
    Tree row_tree = rowTree();

    // ?A must contain all constant rows
    for(int k=0; k<K; k++) {
      int[] const_row = new int[ncols];
      for(int i=0; i<ncols; i++) const_row[i] = k;
      if(row_tree.findLine(const_row) == null)
      {
        // This constant row is new!
        row_tree.addLine(const_row, result_rows.size());
        result_rows.addElement(const_row);
      }
    }

    while(true)
    {
      // Build all diagonals and put them in future_rows
      Vector future_rows = new Vector();
      MatrixGenerator MG = new MatrixGenerator(row_tree, row_tree);
      while(MG.next())
      {
        int[] diagonal = new int[ncols];
        for(int i=0; i<ncols; i++) {
          int row_index = MG.rowLinks[i].datum();
          int[] row = (int[])result_rows.elementAt(row_index);
          diagonal[i] = row[i];
        }
        future_rows.addElement(diagonal);
      }

      // Search future_rows for new rows.
      // Add new rows to row_tree, result_rows.
      // If none of the rows are new, break the loop.
      Enumeration e = future_rows.elements();
      boolean done = true;
      while(e.hasMoreElements())
      {
        int[] row = (int[])e.nextElement();
        if(row_tree.findLine(row) == null)
        {
          // This row is new!
          done = false;
          row_tree.addLine(row, result_rows.size());
          result_rows.addElement(row);
        }
      }
      if(done) break;
    }

    // All the rows have been generated: now build the result
    int[][] new_matrix = new int[result_rows.size()][];
    result_rows.copyInto(new_matrix);
    return new Chu(K, result_rows.size(), ncols, new_matrix, false);
  }

  // query2: Closes the rows of A under union and instersection.
  Chu query2()
  {
    // The final number of rows is unknown,
    // so for now hold them in a Vector.
    Vector result_rows = new Vector();

    // row_tree holds the same rows as result_rows.
    // (The purpose of the Tree is simply to make
    // checking for duplicates faster)
    Tree row_tree = new Tree(2,ncols);

    // Put all the rows of original space on the stack
    Stack future_rows = new Stack();
    for(int r=0;r<nrows;r++)
      future_rows.push(matrix[r]);

    // Don't forget the union and intersection of the empty set of rows:

    int[] zero_row = new int[ncols];
    for(int c=0;c<ncols;c++) zero_row[c]=0;
    future_rows.push(zero_row);

    int[] one_row = new int[ncols];
    for(int c=0;c<ncols;c++) one_row[c]=1;
    future_rows.push(one_row);

    // Loop until no rows remain to insert
    while(!future_rows.empty())
    {
      // Is the row on the top of the Stack new?
      int[] row = (int[])future_rows.pop();
      if(row_tree.findLine(row) == null)
      {
        // The row is new: put all unions and intersections on the Stack
        Enumeration e = result_rows.elements();
        while(e.hasMoreElements())
        {
          int[] old_row = (int[])e.nextElement();

          // Calculate union and put it on the Stack
          int[] union = new int[ncols];
          for(int c=0;c<ncols;c++)
            union[c] = (((row[c]==1)||(old_row[c]==1))?1:0);
          future_rows.push(union);

          // Calculate intersection and put it on the Stack
          int[] intersection = new int[ncols];
          for(int c=0;c<ncols;c++)
            intersection[c] = (((row[c]==1)&&(old_row[c]==1))?1:0);
          future_rows.push(intersection);
        }

        // Add row to the result
        row_tree.addLine(row, result_rows.size());
        result_rows.addElement(row);
      }
    }

    // All the rows have been generated: now build the result
    int[][] new_matrix = new int[result_rows.size()][ncols];
    result_rows.copyInto(new_matrix);
    return new Chu(2, result_rows.size(), ncols, new_matrix, false);
  }

  /* Binary operations */

  static Chu choice(Chu A, Chu B)
  {
    int K = A.K;
    if (B.K > K) K = B.K;
    int nrows = A.nrows + B.nrows;
    int ncols = A.ncols + B.ncols;

    int matrix[][] = new int[nrows][ncols];
    for(int r=0;r<nrows;r++)
    {
      for(int c=0;c<ncols;c++)
      {
        if (r<A.nrows) {
          if (c<A.ncols)
            matrix[r][c] = A.matrix[r][c];
          else
            matrix[r][c] = 0;
        }
        else {
          if (c<A.ncols)
            matrix[r][c] = 0;
          else
            matrix[r][c] = B.matrix[r-A.nrows][c-A.ncols];
        }
      }
    }
    return new Chu(K, nrows, ncols, matrix, false);
  }

  static Chu product(Chu A,Chu B)
  {
    if (A == null || B == null) return null;
    int K = A.K;  if (B.K > K) K=B.K;

    int nrows = A.nrows * B.nrows;
    int ncols = A.ncols + B.ncols;

    int matrix[][] = new int[nrows][ncols];
    int r=0; int c=0;
    for(int ar=0;ar<A.nrows;ar++) // Loop over rows of A
    {
      for(int br=0;br<B.nrows;br++) // Loop over rows of B
      {
        // Create concatination of A.matrix[ar] and B.matrix[br]

        for(int ac=0;ac<A.ncols;ac++)
          matrix[r][c++] = A.matrix[ar][ac];

        for(int bc=0;bc<B.ncols;bc++)
          matrix[r][c++] = B.matrix[br][bc];

        r++; c=0;
      }
    }
    return new Chu(K, nrows, ncols, matrix, false);
  }

  static Chu sequence(Chu A, Chu B)
  {
    if (A == null || B == null) return null;
    int K = A.K;  if (B.K > K) K=B.K;

    // Classify columns of A and B

    int[] classificationA = A.classifyCols();
    int[] classificationB = B.classifyCols();

    // Count rows and columns of answer.
    //   A column of the answer consists of the concatination of
    // a column of A and a column of B.  Duplicates are not allowed.
    // The column (state) of A must be final   (= FINAL   || UNKNOWN).
    // The column (state) of B must be initial (= INITIAL || UNKNOWN).

    int nrows = A.nrows + B.nrows;
    int ncols = 0;

    for(int ac=0; ac<A.ncols; ac++) // Loop over cols of A
    {
      if(classificationA[ac] == DUPLICATE) continue;

      for(int bc=0; bc<B.ncols; bc++) // Loop over cols of B
      {
        if(classificationB[bc] == DUPLICATE) continue;

        if( (classificationA[ac] == UNKNOWN) ||
            (classificationA[ac] == FINAL)   ||
            (classificationB[bc] == UNKNOWN) ||
            (classificationB[bc] == INITIAL) )
        {
          ncols++;
        }
      }
    }

    // Form answer, column by column

    int matrix[][] = new int[nrows][ncols];
    int r=0; int c=0;

    for(int ac=0; ac<A.ncols; ac++) // Loop over cols of A
    {
      if(classificationA[ac] == DUPLICATE) continue;

      for(int bc=0; bc<B.ncols; bc++) // Loop over cols of B
      {
        if(classificationB[bc] == DUPLICATE) continue;

        if( (classificationA[ac] == UNKNOWN) ||
            (classificationA[ac] == FINAL)   ||
            (classificationB[bc] == UNKNOWN) ||
            (classificationB[bc] == INITIAL) )
        {
          // Create concatination of A.matrix[*][ac] and B.matrix[*][bc]

          for(int ar=0; ar<A.nrows; ar++)
            matrix[r++][c] = A.matrix[ar][ac];

          for(int br=0; br<B.nrows; br++)
            matrix[r++][c] = B.matrix[br][bc];

          r=0; c++;
        }
      }
    }

    // Build and return result
    return new Chu(K, nrows, ncols, matrix, false);
  }

  private static final int UNKNOWN = 0;   // < nothing,   > nothing
  private static final int INITIAL = 1;   // < something, > nothing
  private static final int FINAL = 2;     // < nothing,   > something
  private static final int MIDDLE = 3;    // < something, > something
  private static final int DUPLICATE = 4; // == previous something

  // classifyCols: Returns an array of integers which classify
  // the columns of a Chu space into the five catagories above.

  private int[] classifyCols()
  {
    int classification[] = new int[ncols];

OUTER: for(int c=0; c<ncols; c++)
    {
      classification[c] = UNKNOWN;

INNER: for(int d=0; d<c; d++)
      {
        // skip comparisons against duplicates or middle elements.
        switch(classification[d]) {
        case DUPLICATE:
        case MIDDLE:
          continue INNER;
        }

        switch(compareCols(c, d))
        {
        // col c <> col d, so nothing can be infered
        case IC:
          continue INNER;

        // col c == col d, throw out c by classifying it as DUPLICATE.
        case EQ:
          classification[c] = DUPLICATE;
          continue OUTER;

        // col c < col d.
        case LT:
          switch(classification[c]) {
          case UNKNOWN: classification[c] = INITIAL; break;
          case FINAL:   classification[c] = MIDDLE;  break;
          }
          switch(classification[d]) {
          case UNKNOWN: classification[d] = FINAL;   break;
          case INITIAL: classification[d] = MIDDLE;  break;
          }
          break;

        // col c > col d.
        case GT:
          switch(classification[c]) {
          case UNKNOWN: classification[c] = FINAL;   break;
          case INITIAL: classification[c] = MIDDLE;  break;
          }
          switch(classification[d]) {
          case UNKNOWN: classification[d] = INITIAL; break;
          case FINAL:   classification[d] = MIDDLE;  break;
          }
          break;
        }
      } // INNER
    } // OUTER
    return classification;
  }

  private static final int EQ = 0; // ==
  private static final int LT = 1; // <
  private static final int GT = 2; // >
  private static final int IC = 3; // <> aka incomparable

  // compareCols: Compares two columns componentwise
  // and returns one of the four code values above.

  private int compareCols(int col1, int col2)
  {
    int result = EQ;

    for(int r=0; r<nrows; r++)
    {
      if(matrix[r][col1] == matrix[r][col2]) {
        continue;
      }
      else if(matrix[r][col1] < matrix[r][col2]) {
        switch(result) {
        case EQ: result = LT; break;
        case GT: return IC;
        }
      }
      else {
        switch(result) {
        case EQ: result = GT; break;
        case LT: return IC;
        }
      }
    }
    return result;
  }

  static Chu implication(Chu A, Chu B)
  {
    int K = A.K;
    if (K > B.K) K = B.K;

    // The "rows" of implication are Chu transforms from A to B
    // These transforms consist of matrices that are ambigiously
    // composed of columns of A or rows of B.  Thus the size of
    // these rows/transforms/matrices is:
    int size = A.nrows*B.ncols;

    // The number of transforms is not known in advance, so
    // for now they will go in a variable-length Vector:
    Vector transforms = new Vector();

    // Build the MatrixGenerator, using prefix trees
    // of the possible rows and columns of the matrix:
    MatrixGenerator MG = new MatrixGenerator(B.rowTree(), A.colTree());

    while (MG.next())
    {
      // Count instances of this matrix.
      // Whenever there are multiple choices for a row or column,
      // the number of instances is multiplied.
      int num_instances = 1;
      for(int r=0;r<MG.nrows();r++) {
        Link l = MG.rowLinks[r];
        int length=0;
        while(l!=null) {
          l=l.next();
          length++;
        }
        num_instances *= length;
      }
      for(int c=0;c<MG.ncols();c++) {
        Link l = MG.colLinks[c];
        int length=0;
        while(l!=null) {
          l=l.next();
          length++;
        }
        num_instances *= length;
      }

      // Build the current transform
      int[] transform = new int[size];
      for(int r=0;r<MG.nrows();r++)
        for(int c=0;c<MG.ncols();c++)
	  {
	    int row_index = MG.rowLinks[r].datum();
	    int row[] = B.matrix[row_index];
	    int entry = row[c];
	    transform[r*MG.ncols() + c] = entry;
	  }

      // Record the transform
      for(int i=0;i<num_instances;i++) {
        transforms.addElement(transform);
      }
    }

    // We now have all the transforms, so we can package up the result:
    int new_nrows = transforms.size();
    int matrix[][] = new int[new_nrows][];
    transforms.copyInto(matrix);
    return new Chu(K, new_nrows, size, matrix, false);
  }

  /* Conformable interface */

  public Chu conform(Context context)
  {
    if(context.standardization) return standardize();
    else return this;
  }

  private Chu standardize()
  {
    // If the standard version of this space is not known,
    // compute it and keep a pointer to it.
    if(standard == null)
    {
      // new_nrows counts non-repeat rows
      // unique_rows[] contains indexes of non-repeat rows;
      // (Similarly for cols)
      int[] unique_rows = new int[nrows];
      int[] unique_cols = new int[ncols];
      int new_nrows = row_sort(unique_rows);
      int new_ncols = col_sort(unique_cols);

      if((nrows==new_nrows) && (ncols==new_ncols))
      { // Already standardized!
        standard = this;
      }
      else
      { // Build the standardized version
        int new_matrix[][] = new int[new_nrows][new_ncols];
        for(int r=0; r<new_nrows; r++)
          for(int c=0; c<new_ncols; c++)
            new_matrix[r][c] = matrix[unique_rows[r]][unique_cols[c]];

        standard = new Chu(K,new_nrows,new_ncols,new_matrix,true);
      }
    }

    return standard;
  }

  private int row_sort(int[] unique_rows)
  {
    /* Record(unique_rows) and count(num_unique) all unique rows.
     * Throw out all copies.
     */
    int num_unique = 0;
    sort: for(int r=0;r<nrows;r++) {

      /* Look for row r in the current set of unique rows.
       * If row r is not a copy, insert it into the set.
       * l,h mark bounds of possible insertion locations
       */
      int l=0,h=num_unique;
      search: while(l<h) {

        /* Does row unique_rows[m] match row r?
         */
        int m = (l+h)/2;
        compare: for(int c=0;c<ncols;c++) {

          /* scan quickly for differences
           */
          if (matrix[unique_rows[m]][c] == matrix[r][c])
            continue compare;

          /* row unique_rows[m] does not match row r.
           * narrow range and continue search.
           */
          if (matrix[unique_rows[m]][c] > matrix[r][c])
            h=m;
          else
            l=m+1;
          continue search;

        } // end compare

        /* If we get here, we have a match.
         * Throw out row r
         */
        continue sort;

      } // end search

      /* We have a new row.  Insert it!
       */
      for(int i=num_unique;i>l;i--)
        unique_rows[i] = unique_rows[i-1];
      unique_rows[l] = r;
      num_unique++;

    } // end sort

    return num_unique;
  }

  private int col_sort(int[] unique_cols)
  {
    /* Record (in unique_cols) and count (in num_unique) all unique cols.
     * Throw out all copies.
     */
    int num_unique = 0;
    sort: for(int c=0;c<ncols;c++) {

      /* Look for col c in the current set of unique cols.
       * If col c is not a copy, insert it into the set.
       * l,h mark bounds of possible insertion locations
       */
      int l=0,h=num_unique;
      search: while(l<h) {

        /* Does col unique_cols[m] match col c?
         */
        int m = (l+h)/2;
        compare: for(int r=0;r<nrows;r++) {

          /* scan quickly for differences
           */
          if (matrix[r][unique_cols[m]] == matrix[r][c])
            continue compare;

          /* col unique_rcols[m] does not match col c.
           * narrow range and continue search.
           */
          if (matrix[r][unique_cols[m]] > matrix[r][c])
            h=m;
          else
            l=m+1;
          continue search;

        } // end compare

        /* If we get here, we have a match.
         * Throw out col c
         */
        continue sort;

      } // end search

      /* We have a new col.  Insert it!
       */
      for(int i=num_unique;i>l;i--)
        unique_cols[i] = unique_cols[i-1];
      unique_cols[l] = c;
      num_unique++;

    } // end sort

    return num_unique;
  }

  public static void main(String args[])
  {
    try {
      Chu[] chus = new Chu[2];
      chus[0] = new Chu(null, null, null, "1100\n1010");
      chus[1] = new Chu(null, null, null, "000111222\n012012012\n");

      for(int i=0; i<chus.length; i++) {

        System.out.println(chus[i].unparse());
/*
        int[] classification = chus[i].classifyCols();

        for(int c=0; c<classification.length; c++)
          System.out.print(classification[c]+" ");

        for(int j=0; j<=i; j++) {
          System.out.println(sequence(chus[i], chus[j]).unparse());
        }
*/

        Chu q = chus[i].query();
        System.out.println(q.unparse());

        for(int j=0; j<q.ncols*q.ncols; j++)
          System.out.print( (j%(q.ncols+1)==0) ? "*" : " " );
        System.out.println("");
        System.out.println(implication(q.dual(), q).unparse());
        System.out.print("\n------------------------\n");
      }
    }
    catch(ParseException x) {
      System.out.println(x.getMessage());
    }
  }
}










