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
  prtln("Error: " + msg);
  prtln("Parms:");
  prtln("  -outFile      <fname>");
  System.exit(1);
}



public static void main( String[] args) {
  try { testIt( args); }
  catch( NhException exc) {
    exc.printStackTrace();
    prtln("main: caught: " + exc);
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
    outFile,
    NhFileWriter.OPT_OVERWRITE);

  prtln("hfile: " + hfile);

  NhGroup rootGroup = hfile.getRootGroup();

  // Usually dimensions are added to the rootGroup, not a subGroup.
  int rank = 2;          // 2 dimensional data: y, x
  int[] dimLens = new int[rank];

  dimLens[0] = 10;      // num y
  dimLens[1] = 10;      // num x

  NhDimension[] nhDims = new NhDimension[ rank];
  nhDims[0] = rootGroup.addDimension( "ydim", dimLens[0]);
  nhDims[1] = rootGroup.addDimension( "xdim", dimLens[1]);

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
  prtln("humidity: " + humidity);

  // Add an attribute to the variable.
  humidity.addAttribute(
    "someUnits",
    NhVariable.TP_STRING_VAR,
    "celsius");

  // Add a variable with chunked storage
  chunkLens = new int[] { 7, 7};     // numY, numX
  NhVariable temperature = northernGroup.addVariable(
    "temperature",              // varName
    NhVariable.TP_DOUBLE,       // nhType
    nhDims,                     // varDims
    chunkLens,                  // chunk lengths
    fillValue,
    compressLevel);
  prtln("temperature: " + temperature);

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hfile.endDefine();

  // Fill the humidityData array.
  // The type and dimensions must match those declared
  // in addVariable above.
  double[][] humidityData = new double[dimLens[0]][dimLens[1]];
  for (int iy = 0; iy < dimLens[0]; iy++) {
    for (int ix = 0; ix < dimLens[1]; ix++) {
      humidityData[iy][ix] = 100 * iy + ix;
    }
  }

  // Write out the humidityData array in one call.
  int[] startIxs = null;
  humidity.writeData( startIxs, humidityData, false);  // useLinear = false

  // Write out the temperatureData array in multiple chunks.
  startIxs = new int[] {0, 0};    // y, x
  while (true) {           // one iteration per chunk written

    int chunkLenY = Math.min( chunkLens[0], dimLens[0] - startIxs[0]);
    int chunkLenX = Math.min( chunkLens[1], dimLens[1] - startIxs[1]);
    prtln("NhExamplea: write chunk: startIxs: "
      + startIxs[0] + "  " + startIxs[1]
      + "  chunkLenY: " + chunkLenY
      + "  chunkLenX: " + chunkLenX);
    double[][] temperatureChunk = new double[chunkLenY][chunkLenX];

    // Fill the chunk with synthetic data
    for (int iy = 0; iy < chunkLenY; iy++) {
      for (int ix = 0; ix < chunkLenX; ix++) {
        temperatureChunk[iy][ix] = 100 * startIxs[0]
          + startIxs[1]
          + 100 * iy + ix;
        prtln("    iy: " + iy + "  ix: " + ix + "  temperature: "
          + temperatureChunk[iy][ix]);
      }
    }

    temperature.writeData( startIxs, temperatureChunk, false);
    // useLinear = false

    // Increment startIxs for the next chunk
    for (int jj = rank - 1; jj >= 0; jj--) {
      startIxs[jj] += chunkLens[jj];
      if (jj > 0 && startIxs[jj] >= dimLens[jj]) startIxs[jj] = 0;
      else break;
    }
    if (startIxs[0] >= dimLens[0]) break;    // if all chunks were written
  }



  hfile.close();
  prtln("All done");

} // end testit






static void throwerr( String msg, Object... args)
throws NhException
{
  throw new NhException( String.format( msg, args));
}





static void prtln( String msg) {
  System.out.println( msg);
}

} // end class NhExamplea
