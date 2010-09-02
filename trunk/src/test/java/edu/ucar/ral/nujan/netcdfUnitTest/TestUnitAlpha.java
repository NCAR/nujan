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


package edu.ucar.ral.nujan.netcdfUnitTest;

import java.util.Arrays;

import java.io.File;
import junit.framework.TestCase;
import junitx.framework.FileAssert;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;


public class TestUnitAlpha extends TestCase {


public void testa() throws Exception {
  String nameExpected = "src/test/resources/testUnitData/test001.nc";
  String nameActual = "target/temp.test001.nc";
  createFile( nameActual);

  File fileExpected = new File( nameExpected);
  File fileActual = new File( nameActual);
  FileAssert.assertBinaryEquals("file content mismatch",
    fileExpected, fileActual);
}






static void createFile( String outFile)
throws NhException
{
  int bugs = 0;
  int fileVersion = 2;
  NhFileWriter hfile = new NhFileWriter(
    outFile, NhFileWriter.OPT_OVERWRITE,
    fileVersion,
    bugs, bugs,            // nhBugs, hdfBugs
    1283444655);           // utcModTime: milliseconds since 1970

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

  NhVariable humidityVar = northernGroup.addVariable(
    "humidity",                 // varName
    NhVariable.TP_DOUBLE,       // nhType
    nhDims,                     // varDims
    fillValue,
    compressLevel);

  humidityVar.addAttribute(
    "someUnits",
    NhVariable.TP_STRING_VAR,
    "fathoms per fortnight");

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hfile.endDefine();

  // The data type must match that declared in the addVariable call.
  // The data shape must match xdim, ydim.
  double[][] testData = {
    { 11, 12, 13, 14, 15},
    { 21, 22, 23, 24, 25},
    { 31, 32, 33, 34, 35}
  };

  humidityVar.writeData( testData);

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

} // end class
