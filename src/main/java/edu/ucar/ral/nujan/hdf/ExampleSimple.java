// The MIT License
// 
// Copyright (c) 2010 University Corporation for Atmospheric Research
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


package edu.ucar.ral.nujan.hdf;

import edu.ucar.ral.nujan.hdf.HdfFileWriter;
import edu.ucar.ral.nujan.hdf.HdfGroup;
import edu.ucar.ral.nujan.hdf.HdfException;


/**
 * Simple example driver for HdfFileWriter.
 */

public class ExampleSimple {


public static void main( String[] args) {
  try { testIt( args); }
  catch( Exception exc) {
    exc.printStackTrace();
    prtln("caught: " + exc);
    System.exit(1);
  }
}


static void badParms( String msg) {
  prtln("Error: " + msg);
  prtln("Parms:");
  prtln("  -outFile  <fileName>");
  System.exit(1);
}


static void testIt( String[] args)
throws Exception
{
  if (args.length != 2) badParms("wrong num parms");
  if (! args[0].equals("-outFile")) badParms("missing parm: -outFile");
  String fileName = args[1];

  int fileVersion = 2;
  int option = HdfFileWriter.OPT_ALLOW_OVERWRITE;
  HdfFileWriter hdfFile = new HdfFileWriter(
    fileName, fileVersion, option);
  HdfGroup rootGroup = hdfFile.getRootGroup();
  int numx = 10;
  int numy = 5;
  int[] dims = {numx, numy};         // dimension lengths
  HdfGroup temperature = rootGroup.addVariable(
    "temperature",                 // variable name
    HdfGroup.DTYPE_FLOAT64,        // double precision float
    0,                             // stgFieldLen
    dims,                          // dimension lengths
    new Double(-999999),           // fill value or null
    false,                         // isChunked
    0);                            // compression: 0 is none, 9 is max

  temperature.addAttribute("units", HdfGroup.DTYPE_STRING_FIX, 0,
    "celsius", false);

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hdfFile.endDefine();

  // Fill the data array.
  double[][] data = new double[numx][numy];
  for (int ix = 0; ix < numx; ix++) {
    for (int iy = 0; iy < numy; iy++) {
      data[ix][iy] = 1000 * ix + iy;
    }
  }

  // Write out the data array
  temperature.writeData( data);

  hdfFile.close();
} // end testIt


static void prtln( String msg) {
  System.out.println( msg);
}

} // end class
