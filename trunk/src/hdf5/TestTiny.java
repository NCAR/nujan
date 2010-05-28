
package hdfnetTest;

import hdfnet.HdfFile;
import hdfnet.HdfGroup;
import hdfnet.HdfException;



// Very small test, with hardcoded dtype and dims.



public class TestTiny {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs     <int>");
  prtf("  -outFile  <fname>");
  System.exit(1);
}



public static void main( String[] args) {
  try { runit( args); }
  catch( Exception exc) {
    exc.printStackTrace();
    prtf("main: caught: %s", exc);
    System.exit(1);
  }
}


static void runit( String[] args)
throws HdfException
{
  int bugs = -1;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }

  if (bugs < 0) badparms("missing parm: -bugs");
  if (outFile == null) badparms("missing parm: -outFile");

  prtf("TestTiny: bugs: %d", bugs);
  prtf("TestTiny: outFile: \"%s\"", outFile);

  int fileVersion = 1;
  HdfFile hfile = new HdfFile(
    outFile, fileVersion, HdfFile.OPT_ALLOW_OVERWRITE);
  HdfGroup rootGroup = hfile.getRootGroup();


  HdfGroup varAlpha = rootGroup.addVariable(
    "alpha",
    ///HdfGroup.DTYPE_FLOAT64,    // dtype
    HdfGroup.DTYPE_STRING_FIX,    // dtype
    ///0,                         // string length, incl null termination
    7,                         // string length, incl null termination
    ///new int[] {3},             // varDims
    new int[] {2,3},             // varDims
    null,                      // fillValue
    false,                     // isChunked
    0);                        // compressionLevel

  hfile.endDefine();

  ///varAlpha.writeData( new double[] { 1.1, 2.2, 3.3});
  ///varAlpha.writeData( new String[] { "aa", "bb", "cc"});
  varAlpha.writeData( new String[][] {
    {"aa", "bb", "cc"},
    {"dd", "ee", "ff"}
  });

  hfile.close();
}















static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class
