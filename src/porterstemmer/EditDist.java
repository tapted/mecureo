package porterstemmer;

/**
 * class EditDistance => Levenshtein distance
 * Java implementation by 
 * @Author Michael Gilleland (Merriam Park Software)
 *
 * from
 *
 * http://www.merriampark.com/ld.htm
 *
 */
public class EditDist {
	
  /*
   * Get minimum of three values
   */
  private static final int Minimum (int a, int b, int c) {
    if (a < b) {
    	if (a < c)
    	    return a;
    	else
    		return c;
	} else if (b < c) {
		return b;
	} else {
		return c;
	}
  }

  //*****************************
  // Compute Levenshtein distance
  //*****************************

  public static final byte dist (String s, String t) {
   int d[][]; // matrix
   int n; // length of s
   int m; // length of t
   int i; // iterates through s
   int j; // iterates through t
   char s_i; // ith character of s
   char t_j; // jth character of t
   int cost; // cost

    // Step 1

    n = s.length ();
    m = t.length ();
    if (n == 0) {
      return toByte(m);
    }
    if (m == 0) {
      return toByte(n);
    }
    d = new int[n+1][m+1];

    // Step 2

    for (i = 0; i <= n; i++) {
      d[i][0] = i;
    }

    for (j = 0; j <= m; j++) {
      d[0][j] = j;
    }

    // Step 3

    for (i = 1; i <= n; i++) {

      s_i = s.charAt (i - 1);

      // Step 4

      for (j = 1; j <= m; j++) {

        t_j = t.charAt (j - 1);

        // Step 5

        if (s_i == t_j) {
          cost = 0;
        }
        else {
          cost = 1;
        }

        // Step 6

        d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

      }

    }

    // Step 7

    return toByte(d[n][m]);

  }
  
  private static final byte toByte(int i) {
  	if (i > 127)
  	    return (byte)127;
  	else
  		return (byte)i;
  }

}
