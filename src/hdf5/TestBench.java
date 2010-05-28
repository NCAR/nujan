
package hdfnet;

import java.nio.charset.Charset;
import java.util.SortedMap;


// Conclusions:
// If total array size in bytes > 0.65 * Xmx parameter, get OutOfMemoryError
// Must use an integer to specify array dimensions.
// Size limit for any one dimension is 2 GB.
// The total number of elements can exceed 2GB.


class TestA<TP> {
  TP value;
  TestA( TP value) {
    this.value = value;
  }
}




class TestBench {

public static void main( String[] args) {

  for (int ii = 0; ; ii++) {
    int size = ii * 100 * 1024 * 1024;
    byte[] bbuf = new byte[size];
    prtln("ii: " + ii + "  1.e-6*size: " + 1.e-6*size + "  bbuf: " + bbuf);
  }
}




static void prtln( String msg) {
  System.out.println( msg);
}

} // end class

