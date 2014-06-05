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

  int option = HdfFileWriter.OPT_ALLOW_OVERWRITE;
  HdfFileWriter hdfFile = new HdfFileWriter( fileName, option);
  prtln("hdfFile: " + hdfFile);

  HdfGroup rootGroup = hdfFile.getRootGroup();
  int numx = 10;
  int numy = 10;
  int[] dims = {numx, numy};      // dimension lengths

  // Add a variable with contiguous storage
  int[] specChunkDims = null;     // chunk lengths (contiguous, not chunked)
  HdfGroup humidity = rootGroup.addVariable(
    "humidity",                   // variable name
    HdfGroup.DTYPE_FLOAT64,       // double precision float
    0,                            // stgFieldLen
    dims,                         // dimension lengths
    specChunkDims,                // chunk lengths
    new Double(-999999),          // fill value or null
    0);                           // compression: 0 is none, 9 is max
  prtln("humidity: " + humidity);

  // Add an attribute to the variable.
  humidity.addAttribute("units", HdfGroup.DTYPE_STRING_FIX, 0,
    "celsius", false);

  // Add a variable with chunked storage
  specChunkDims = new int[] {5, 10};  // divide x into 2 sections of 5 each,
                                  // leave y entire
  HdfGroup temperature = rootGroup.addVariable(
    "temperature",                // variable name
    HdfGroup.DTYPE_FLOAT64,       // double precision float
    0,                            // stgFieldLen
    dims,                         // dimension lengths
    specChunkDims,                // chunk lengths
    new Double(-999999),          // fill value or null
    0);                           // compression: 0 is none, 9 is max
  prtln("temperature: " + temperature);

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hdfFile.endDefine();

  // Fill the humidityData array.
  // The type and dimensions must match those declared
  // in addVariable above.
  double[][] humidityData = new double[numx][numy];
  for (int ix = 0; ix < numx; ix++) {
    for (int iy = 0; iy < numy; iy++) {
      humidityData[ix][iy] = 100 * ix + iy;
    }
  }

  // Fill the temperatureData arrays, one for each chunk.
  // The size must match the declared CHUNK, not dimension, lengths.
  double[][] temperatureDataChunk0
    = new double [specChunkDims[0]] [specChunkDims[1]];
  double[][] temperatureDataChunk1
    = new double [specChunkDims[0]] [specChunkDims[1]];
  for (int ix = 0; ix < specChunkDims[0]; ix++) {
    for (int iy = 0; iy < specChunkDims[1]; iy++) {
      temperatureDataChunk0[ix][iy] = 100 * ix + iy + 1000;
      temperatureDataChunk1[ix][iy] = 100 * ix + iy + 2000;
    }
  }

  // Write out the humidityData array in one call.
  int[] startIxs = null;
  humidity.writeData( startIxs, humidityData, false);  // useLinear = false

  // Write out the temperatureData array in two chunks.
  startIxs = new int[] {0, 0};
  temperature.writeData( startIxs, temperatureDataChunk0, false);
  startIxs[0] = 5;
  temperature.writeData( startIxs, temperatureDataChunk1, false);

  hdfFile.close();
  prtln("All done");

} // end testIt


static void prtln( String msg) {
  System.out.println( msg);
}

} // end class
