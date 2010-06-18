// The MIT License
// 
// Copyright (c) 2009 University Corporation for Atmospheric
// Research and Massachusetts Institute of Technology Lincoln
// Laboratory.
// 
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.


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

