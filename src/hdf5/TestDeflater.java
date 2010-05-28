
package hdfnetTest;

import java.util.zip.Deflater;


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
