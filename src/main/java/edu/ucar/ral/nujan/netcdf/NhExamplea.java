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


package edu.ucar.ral.nujan.netcdf;

import java.util.Arrays;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;


/**
 * Example program.
 */


public class NhExamplea {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -outFile      <fname>");
  System.exit(1);
}



public static void main( String[] args) {
  try { testIt( args); }
  catch( NhException exc) {
    exc.printStackTrace();
    prtf("main: caught: %s", exc);
    System.exit(1);
  }
}


static void testIt( String[] args)
throws NhException
{
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }
  if (outFile == null) badparms("missing parm: -outFile");

  NhFileWriter hfile = new NhFileWriter(
    outFile, NhFileWriter.OPT_OVERWRITE);

  NhGroup rootGroup = hfile.getRootGroup();

  // Usually dimensions are added to the rootGroup, not a subGroup.
  int rank = 2;             // 2 dimensional data: x, y
  NhDimension[] nhDims = new NhDimension[ rank];
  int numx = 10;
  int numy = 10;
  nhDims[0] = rootGroup.addDimension( "xdim", numx);
  nhDims[1] = rootGroup.addDimension( "ydim", numy);

  // Attributes may be added to any group and any variable.
  // Attribute values may be either a String or a 1 dimensional
  // array of: String, byte, short, int, long, float, or double.

  // Global attributes are added to the rootGroup.
  rootGroup.addAttribute(
    "someName",
    NhVariable.TP_STRING_VAR,
    "some long comment");

  // Groups may be nested arbitrarily
  NhGroup northernGroup = rootGroup.addGroup("northernData");

  northernGroup.addAttribute(
    "cityIndices",
    NhVariable.TP_INT,
    new int[] { 1, 2, 3, 5, 7, 13, 17});

  // Variables may be added to any group.
  Double fillValue = new Double( -999999);
  int compressLevel = 0;        // compression level: 0==none, 1 - 9
  int[] chunkLens = null;       // chunk lengths (contiguous, not chunked)

  // Add a variable with contiguous storage
  NhVariable humidity = northernGroup.addVariable(
    "humidity",                 // varName
    NhVariable.TP_DOUBLE,       // nhType
    nhDims,                     // varDims
    chunkLens,                  // chunk lengths
    fillValue,
    compressLevel);

  // Add an attribute to the variable.
  humidity.addAttribute(
    "someUnits",
    NhVariable.TP_STRING_VAR,
    "celsius");

  // Add a variable with chunked storage
  chunkLens = new int[] { 5, 10};
  NhVariable temperature = northernGroup.addVariable(
    "temperature",              // varName
    NhVariable.TP_DOUBLE,       // nhType
    nhDims,                     // varDims
    chunkLens,                  // chunk lengths
    fillValue,
    compressLevel);

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hfile.endDefine();

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
  double[][] temperatureDataChunk0 = new double[chunkLens[0]][chunkLens[1]];
  double[][] temperatureDataChunk1 = new double[chunkLens[0]][chunkLens[1]];
  for (int ix = 0; ix < chunkLens[0]; ix++) {
    for (int iy = 0; iy < chunkLens[1]; iy++) {
      temperatureDataChunk0[ix][iy] = 100 * ix + iy + 1000;
      temperatureDataChunk1[ix][iy] = 100 * ix + iy + 2000;
    }
  }

  // Write out the humidityData array in one call.
  int[] startIxs = null;
  humidity.writeData( startIxs, humidityData);

  // Write out the temperatureData array in two chunks.
  startIxs = new int[] {0, 0};
  temperature.writeData( startIxs, temperatureDataChunk0);
  startIxs[0] = 5;
  temperature.writeData( startIxs, temperatureDataChunk1);

  hfile.close();

} // end testit






static void throwerr( String msg, Object... args)
throws NhException
{
  throw new NhException( String.format( msg, args));
}





static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class NhExamplea
