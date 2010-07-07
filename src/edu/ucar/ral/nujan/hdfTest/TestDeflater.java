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


package edu.ucar.ral.nujan.hdfTest;

import java.util.zip.Deflater;

/**
 * Simple test of java.util.Deflater.  Does not use HDF5.
 */


class TestDeflater {

public static void main( String[] args) {

  StringBuilder sbuf = new StringBuilder();
  for (int ii = 0; ii < 100; ii++) {
    sbuf.append("0123456789");
  }
  byte[] invec = sbuf.toString().getBytes();
  prtf("invec len: %d", invec.length);

  byte[] outvec = new byte[10];

  Deflater deflater = new Deflater(5);
  deflater.setInput( invec);

  int compLen;
  while (true) {
    prtf("\nloop head: needsInput: %s", deflater.needsInput());
    compLen = deflater.deflate( outvec);
    prtf("compLen: %d", compLen);
    prtf("needsInput: %s", deflater.needsInput());
    if (compLen == 0) break;
  }

  deflater.finish();
  while (true) {
    compLen = deflater.deflate( outvec);
    prtf("final compLen: %d", compLen);
    prtf("  needsInput: %s", deflater.needsInput());
    if (compLen == 0) break;
  }
}

static void prtf( String msg, Object ... args) {
  System.out.println( String.format( msg, args));
}

} // end class
