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


class TestAlpha<TP> {
  TP value;
  TestAlpha( TP value) {
    this.value = value;
  }
}




class TestJava {

public static void main( String[] args) {
  Object obj;

  prtln("test String");
  obj = new String("hi");
  if (obj instanceof Object) prtln("  instanceof Object");
  if (obj instanceof Object[]) prtln("  instanceof Object[]");
  if (obj instanceof Object[][]) prtln("  instanceof Object[][]");
  if (obj instanceof Object[][][]) prtln("  instanceof Object[][][]");

  prtln("test String[]");
  obj = new String[3];
  if (obj instanceof Object) prtln("  instanceof Object");
  if (obj instanceof Object[]) prtln("  instanceof Object[]");
  if (obj instanceof Object[][]) prtln("  instanceof Object[][]");
  if (obj instanceof Object[][][]) prtln("  instanceof Object[][][]");

  prtln("test String[][]");
  obj = new String[3][3];
  if (obj instanceof Object) prtln("  instanceof Object");
  if (obj instanceof Object[]) prtln("  instanceof Object[]");
  if (obj instanceof Object[][]) prtln("  instanceof Object[][]");
  if (obj instanceof Object[][][]) prtln("  instanceof Object[][][]");


  prtln("test int[]");
  obj = new int[3];
  if (obj instanceof Object) prtln("  instanceof Object");
  if (obj instanceof Object[]) prtln("  instanceof Object[]");
  if (obj instanceof Object[][]) prtln("  instanceof Object[][]");
  if (obj instanceof Object[][][]) prtln("  instanceof Object[][][]");
  if (obj instanceof int[]) prtln("  instanceof int[]");
  if (obj instanceof int[][]) prtln("  instanceof int[][]");

  prtln("test int[][]");
  obj = new int[3][3];
  if (obj instanceof Object) prtln("  instanceof Object");
  if (obj instanceof Object[]) prtln("  instanceof Object[]");
  if (obj instanceof Object[][]) prtln("  instanceof Object[][]");
  if (obj instanceof Object[][][]) prtln("  instanceof Object[][][]");
  if (obj instanceof int[]) prtln("  instanceof int[]");
  if (obj instanceof int[][]) prtln("  instanceof int[][]");

  new TestAlpha<String>("hi");
  // illegal: requires reference, not primitive: new TestAlpha<int>( 3);

  SortedMap<String,Charset>charsetMap = Charset.availableCharsets();
  for (String key : charsetMap.keySet()) {
    prtln("key: \"" + key + "\"  charset: " + charsetMap.get( key));
  }


  prtln("start array test");
  int nx = 1000 * 100;
  int ny = 1000 * 100;
  byte[][] amat = new byte[nx][ny];
  amat[nx-1][ny-1] = 3;
  prtln("end array test.  last ele: " + amat[nx-1][ny-1]);

}

static void prtln( String msg) {
  System.out.println( msg);
}

} // end class

