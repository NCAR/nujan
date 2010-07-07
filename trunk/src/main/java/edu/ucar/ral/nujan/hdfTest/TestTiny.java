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

import edu.ucar.ral.nujan.hdf.HdfFileWriter;
import edu.ucar.ral.nujan.hdf.HdfGroup;
import edu.ucar.ral.nujan.hdf.HdfException;



/**
 * Very small test, with hardcoded dtype and dims.
 */



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
  HdfFileWriter hfile = new HdfFileWriter(
    outFile, fileVersion, HdfFileWriter.OPT_ALLOW_OVERWRITE, bugs);
  HdfGroup rootGroup = hfile.getRootGroup();


  HdfGroup varAlpha = rootGroup.addVariable(
    "alpha",
    ///HdfGroup.DTYPE_FLOAT64,    // dtype
    HdfGroup.DTYPE_STRING_FIX,    // dtype
    ///0,                         // string length, without null termination
    7,                         // string length, without null termination
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
