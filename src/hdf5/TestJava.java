
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

