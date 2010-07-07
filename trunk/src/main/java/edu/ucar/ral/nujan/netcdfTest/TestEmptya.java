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


package edu.ucar.ral.nujan.netcdfTest;

import java.util.Arrays;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;


/**
 * Test empty data, empty attributes.
 */


public class TestEmptya {


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

  NhFileWriter hfile = new NhFileWriter(
    outFile, NhFileWriter.OPT_OVERWRITE);

  hfile.setDebugLevel( bugs);
  hfile.setHdfDebugLevel( bugs);

  NhGroup rootGroup = hfile.getRootGroup();

  // Usually dimensions are added to the rootGroup, not a subGroup.
  int rank = 2;             // 2 dimensional data: x, y
  NhDimension[] nhDims = new NhDimension[ rank];
  nhDims[0] = rootGroup.addDimension( "xdim", 3);
  nhDims[1] = rootGroup.addDimension( "ydim", 5);

  // Attributes may be added to any group and any variable.
  // Attribute values may be either a String or a 1 dimensional
  // array of: String, byte, short, int, long, float, or double.

  // Global attributes are added to the rootGroup.
  rootGroup.addAttribute(
    "emptyRootAttr",
    NhVariable.TP_STRING_VAR,
    null);

  Double fillValue = null;
  int compressLevel = 0;        // compression level: 0==none, 1 - 9

  NhVariable humidityVar = rootGroup.addVariable(
    "humidity",                 // varName
    NhVariable.TP_DOUBLE,       // nhType
    null,                       // varDims
    fillValue,
    compressLevel);

  humidityVar.addAttribute(
    "emptyVarAttr",
    NhVariable.TP_STRING_VAR,
    new String[0]);

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hfile.endDefine();

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

} // end class TestEmptya
