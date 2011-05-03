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


package edu.ucar.ral.nujan.netcdfUnitTest;

import java.util.Arrays;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import junitx.framework.FileAssert;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;
import edu.ucar.ral.nujan.hdf.HdfException;
import edu.ucar.ral.nujan.hdf.HdfGroup;
import edu.ucar.ral.nujan.hdf.HdfUtil;
import edu.ucar.ral.nujan.hdfTest.GenData;


public class TestUnita extends TestCase {


// Set defaults when called by Maven's use of JUnit.
int unitBugs = 0;
int nhBugs = 0;
int hdfBugs = 0;
String sourceDir = "src/test/resources/testUnitData";
String targetDir = "target/testDira";
boolean useCheck = true;

String dataTypeStg = "ubyte,int,float,double,vstring";

// Each string has the format:
// "d,d,d/c,c,c" or special case "d,d,d/null",
// where d is a dimLen and c is a chunkLen.

String[] dimStgs = {
  "/null",               // scalar

  "10/null",
  "10/10",
  "10/5",
  "10/7",

  "10,10/null",
  "10,10/10,10",
  "10,10/5,5",
  "10,10/7,7",

  "10,10,10/null",
  "10,10,10/10,10,10",
  "10,10,10/5,5,5",
  "10,10,10/7,7,7",

  "10,10,10,10/null",
  "10,10,10,10/10,10,10,10",
  "10,10,10,10/5,5,5,5",
  "10,10,10,10/7,7,7,7",

  "10,10,10,10,10/null",
  "10,10,10,10,10/10,10,10,10,10",
  "10,10,10,10,10/5,5,5,5,5",
  "10,10,10,10,10/7,7,7,7,7"
};

String compLevelStg = "0,5";

String useSmallStg = "false,true";

String useLinearStg = "false,true";




public TestUnita() {
}



public static void main( String[] args) {
  try {
    TestUnita tunit = new TestUnita();
    tunit.initAll( args);
    tunit.testIt();
  }
  catch( NhException exc) {
    exc.printStackTrace();
    String msg = "caught: " + exc;
    System.out.println( msg);
    System.err.println( msg);
    printUsageInfo();
    System.exit(1);
  }
}



static void printUsageInfo()
{
  prtf("");
  prtf("Parms:");
  prtf("  -unitBugs       int: debug level for TestUnita");
  prtf("  -nhBugs         int: debug level for the nujan.netcdf layer");
  prtf("  -hdfBugs        int: debug level for the nujan.hdf layer");
  prtf("  -sourceDir      stg: dir containing known good files");
  prtf("  -targetDir      stg: dir containing output files");
  prtf("  -check          true/false: if true, compare source vs target");
  prtf("  -dataType       comma sep list, each element of which is:");
  prtf("                    ubyte int float double vstring");
  prtf("  -dims           comma sep list, each element of which is:");
  prtf("                    stg like 10,10/5,5   varDims/chunkDims");
  prtf("                    If \"/null\", use a scalar.");
  prtf("                    If \"varDims/null\", use contiguous.");
  prtf("  -compLevel      comma sep list, each element of which is:");
  prtf("                    int compression level, 0 - 9");
  prtf("  -useSmall       comma sep list, each element of which is:");
  prtf("                    true/false: if true use small chunks");
  prtf("                    at region edges; else use full size chunks");
  prtf("  -useLinear       comma sep list, each element of which is:");
  prtf("                    true/false: if true use linear array with");
  prtf("                    writeData");
  prtf("");
}




// Called by main, not by unit test framework.

void initAll( String[] args)
throws NhException
{
  sourceDir = null;
  targetDir = null;

  if (args.length % 2 != 0) throwerr("args must be key/value pairs");

  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-unitBugs")) unitBugs = parseInt("-unitBugs", val);
    else if (key.equals("-nhBugs")) nhBugs = parseInt("-nhBugs", val);
    else if (key.equals("-hdfBugs")) hdfBugs = parseInt("-hdfBugs", val);
    else if (key.equals("-sourceDir")) sourceDir = val;
    else if (key.equals("-targetDir")) targetDir = val;
    else if (key.equals("-check")) useCheck = parseBoolean("-check", val);
    else if (key.equals("-dataType")) dataTypeStg = val;
    else if (key.equals("-dims")) dimStgs = new String[] {val};
    else if (key.equals("-compLevel")) compLevelStg = val;
    else if (key.equals("-useSmall")) useSmallStg = val;
    else if (key.equals("-useLinear")) useLinearStg = val;
    else throwerr("unknown parm: %s", key);
  }

  if (sourceDir == null) throwerr("parm not specified: -sourceDir");
  if (targetDir == null) throwerr("parm not specified: -targetDir");

} // end initAll












public void testIt()
throws NhException
{
  prtf("TestUnita: unitBugs: %d", unitBugs);
  prtf("TestUnita: nhBugs: %d", nhBugs);
  prtf("TestUnita: hdfBugs: %d", hdfBugs);
  prtf("TestUnita: sourceDir: \"%s\"", sourceDir);
  prtf("TestUnita: targetDir: \"%s\"", targetDir);
  prtf("TestUnita: useCheck: %s", useCheck);
  prtf("TestUnita: dataTypeStg: \"%s\"", dataTypeStg);
  for (String stg : dimStgs) {
    prtf("TestUnita: dimStg: \"%s\"", stg);
  }
  prtf("TestUnita: compLevelStg: \"%s\"", compLevelStg);
  prtf("TestUnita: useSmallStg: \"%s\"", useSmallStg);
  prtf("TestUnita: useLinearStg: \"%s\"", useLinearStg);

  new File( targetDir).mkdir();

  for (String dtypeStg : dataTypeStg.split(",")) {
    int dataType = 0;
    if (dtypeStg.equals("ubyte")) dataType = NhVariable.TP_UBYTE;
    else if (dtypeStg.equals("int")) dataType = NhVariable.TP_INT;
    else if (dtypeStg.equals("float")) dataType = NhVariable.TP_FLOAT;
    else if (dtypeStg.equals("double")) dataType = NhVariable.TP_DOUBLE;
    else if (dtypeStg.equals("vstring")) dataType = NhVariable.TP_STRING_VAR;
    else throwerr("invalid dataType: \"" + dtypeStg + "\"");
    if (unitBugs >= 1) prtf("dtypeStg: %s", dtypeStg);

    for (String dimStg : dimStgs) {
      int[][] dimChunks = parseDimChunks("-dims", dimStg);
      int[] dimLens = dimChunks[0];
      int[] chunkLens = dimChunks[1];
      int rank = dimLens.length;
      if (unitBugs >= 1) prtf("dimStg: %s  rank: %d", dimStg, rank);

      for (int compLevel : parseInts("compLevel", compLevelStg)) {
        if (unitBugs >= 1) prtf("compLevel: %d", compLevel);

        for (boolean useSmall : parseBooleans("useSmall", useSmallStg)) {
          if (unitBugs >= 1) prtf("useSmall: %s", useSmall);
          for (boolean useLinear : parseBooleans("useLinear", useLinearStg)) {
            if (unitBugs >= 1) prtf("useLinear: %s", useLinear);

            // Cannot compress a scalar; compression requires chunking.
            boolean useIt = true;
            if (compLevel > 0) {
              if (rank == 0) {
                useIt = false;
                if (unitBugs >= 1)
                  prtf("test omitted: compLevel > 0 && rank == 0");
              }
              if (chunkLens == null) {
                useIt = false;
                if (unitBugs >= 1)
                  prtf("test omitted: compLevel > 0 && chunkLens == null");
              }
              if (dataType == NhVariable.TP_STRING_VAR) {
                useIt = false;
                if (unitBugs >= 1)
                  prtf("test omitted: compLevel > 0 && dataType == STRING_VAR");
              }
            }
            // xxx 5-dim vstring works for
            // xxx 3,3,3,3,3/null through
            // xxx 9,9,9,9,9/null, but
            // xxx 10,10,10,10,10/null,
            // xxx gives: h5dump: H5HGcache.c:243: H5HG_load:
            // xxx    Assertion `p == heap->chunk + heap->size' failed.
            // xxx Sounds like HDF5 has a fixed and small heap
            // xxx for strings.
            if (dataType == NhVariable.TP_STRING_VAR && rank > 4) {
              useIt = false;
              if (unitBugs >= 1)
                prtf("test omitted: dataType == STRING_VAR && rank > 4");
            }
            if (useIt) {
              mkSingleTest( dataType, dimLens, chunkLens, compLevel,
                useSmall, useLinear, sourceDir, targetDir);
            } // if not compressing a scalar
          } // for useLinear
        } // for useSmall
      } // for compLevel
    } // for dimStg
  } // for dataTypeStg
} // end testIt





void mkSingleTest(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens,
  int[] chunkLens,
  int compLevel,
  boolean useSmall,
  boolean useLinear,
  String sourceDir,
  String targetDir)
throws NhException
{

  String namePart = "test." + NhVariable.nhTypeNames[dataType].toLowerCase()
    + ".dim";
  int rank = dimLens.length;
  if (rank == 0) namePart += "_0";
  else {
    for (int ival : dimLens) namePart += "_" + ival;
  }
  if (chunkLens == null) namePart += ".chunk_null";
  else {
    namePart += ".chunk";
    for (int ival : chunkLens) namePart += "_" + ival;
  }
  namePart += ".comp_" + compLevel;
  namePart += ".small_" + useSmall;
  
  // The sourceName always is linear_false, as both targets
  // linear false and true must be equal to it.
  String sourceName = sourceDir + "/" + namePart
    + ".linear_false.nc";
  String targetName = targetDir + "/" + namePart
    + ".linear_" + useLinear + ".nc";

  if (unitBugs >= 1) {
    prtf("mkSingleTest: namePart: %s", namePart);
    prtf("mkSingleTest: sourceName: %s", sourceName);
    prtf("mkSingleTest: targetName: %s", targetName);
  }

  createFile( dataType, dimLens, chunkLens,
    compLevel, useSmall, useLinear, targetName);

  try {
    prtf("TestUnita: sourceName: %s",
      new File( sourceName).getCanonicalPath());
    prtf("TestUnita: targetName: %s",
      new File( targetName).getCanonicalPath());
  }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
  if (useCheck) {
    File sourceFile = new File( sourceName);
    File targetFile = new File( targetName);
    FileAssert.assertBinaryEquals("file content mismatch",
      sourceFile, targetFile);
  }
} // end mkSingleTest






void createFile(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens,
  int[] chunkLens,
  int compLevel,
  boolean useSmall,
  boolean useLinear,
  String targetName)
throws NhException
{

  int rank = dimLens.length;
  if (unitBugs >= 1) {
    prtf("TestUnita.createFile:");
    prtf("  dataType: %s", NhVariable.nhTypeNames[ dataType]);
    prtf("  rank: %d", rank);
    prtf("  dimLens: %s", formatInts( dimLens));
    prtf("  chunkLens: %s", formatInts( chunkLens));
    prtf("  compLevel: %d", compLevel);
    prtf("  useSmall: %s", useSmall);
    prtf("  useLinear: %s", useLinear);
    prtf("  targetName: %s", targetName);
  }

  NhFileWriter hfile = new NhFileWriter(
    targetName,
    NhFileWriter.OPT_OVERWRITE,
    nhBugs,
    hdfBugs,
    1283444655,            // utcModTime date: milliseconds since 1970
    null,                  // statTag
    null);                 // logDir

  NhGroup rootGroup = hfile.getRootGroup();

  // Usually dimensions are added to the rootGroup, not a subGroup.
  NhDimension[] nhDims = new NhDimension[ rank];
  for (int ii = 0; ii < rank; ii++) {
    nhDims[ii] = rootGroup.addDimension( "dim" + ii, dimLens[ii]);
  }

  // Attributes may be added to any group and any variable.
  // Attribute values may be either a String or a 1 dimensional
  // array of: String, byte, short, int, long, float, or double.

  // Global attributes are added to the rootGroup.
  rootGroup.addAttribute(
    "rootAttribute",
    NhVariable.TP_STRING_VAR,
    "some long comment");

  // Groups may be nested arbitrarily
  NhGroup northernGroup = rootGroup.addGroup("northernData");

  northernGroup.addAttribute(
    "groupAttribute",
    NhVariable.TP_INT,
    new int[] { 1, 2, 3, 5, 7, 13, 17});

  // Variables may be added to any group.

  Object fillValue = null;
  if (dataType == NhVariable.TP_UBYTE)
    fillValue = new Byte( (byte) 99);
  else if (dataType == NhVariable.TP_INT)
    fillValue = new Integer( -999999);
  else if (dataType == NhVariable.TP_FLOAT)
    fillValue = new Float( -999999);
  else if (dataType == NhVariable.TP_DOUBLE)
    fillValue = new Double( -999999);
  else if (dataType == NhVariable.TP_STRING_VAR)
    fillValue = null;
  else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);

  NhVariable humidityVar = northernGroup.addVariable(
    "humidity",             // varName
    dataType,               // nhType: one of NhVariable.TP_DOUBLE, etc.
    nhDims,                 // varDims
    chunkLens,
    fillValue,
    compLevel);

  humidityVar.addAttribute(
    "varAttribute",
    NhVariable.TP_STRING_VAR,
    "fathoms per fortnight");

  // End the definition stage.
  // All groups, variables, and attributes are created before endDefine.
  // All calls to writeData occur after endDefine.
  hfile.endDefine();







  int hdfType = 0;            // one of HdfGroup.DTYPE_*.
  if (dataType == NhVariable.TP_UBYTE) hdfType = HdfGroup.DTYPE_UFIXED08;
  else if (dataType == NhVariable.TP_SHORT) hdfType = HdfGroup.DTYPE_FIXED16;
  else if (dataType == NhVariable.TP_INT) hdfType = HdfGroup.DTYPE_FIXED32;
  else if (dataType == NhVariable.TP_LONG) hdfType = HdfGroup.DTYPE_FIXED64;
  else if (dataType == NhVariable.TP_FLOAT) hdfType = HdfGroup.DTYPE_FLOAT32;
  else if (dataType == NhVariable.TP_DOUBLE) hdfType = HdfGroup.DTYPE_FLOAT64;
  else if (dataType == NhVariable.TP_STRING_VAR)
    hdfType = HdfGroup.DTYPE_STRING_VAR;
  else throwerr("unknown dataType: " + dataType);

  // Generate the test data
  Object testData = null;
  ///testData = generateCuboidData( dataType, dimLens);
  try {
    testData = GenData.genHdfData(
      false,                 // useLinear
      hdfType,
      0,                     // stgFieldLen
      null,                  // HdfGroup refGroup
      dimLens,
      0);                    // origin
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  // Write out the data
  if (chunkLens == null) {
    if (useLinear && rank >= 2) {
      try {
        testData = GenData.mkLinear( hdfType, dimLens, testData);
      }
      catch( HdfException exc) {
        exc.printStackTrace();
        throwerr("caught: " + exc);
      }
    }
    int[] startIxs = null;
    if (unitBugs >= 10)
      prtf("TestUnita: contig.  testData: %s",
        HdfUtil.formatObject( testData));
    humidityVar.writeData( startIxs, testData, useLinear);
  }

  else {
    Object dataObj = null;
    int[] startIxs = new int[rank];      // all 0
    boolean allDone = false;
    while (! allDone) {

      dataObj = mkSubset(
        0,                    // curIx
        false,                // isAllFill
        dataType,
        dimLens, chunkLens, startIxs,
        useSmall, testData);
      if (useLinear && rank >= 2) {
        int[] cdims = new int[rank];
        for (int ii = 0; ii < rank; ii++) {
          cdims[ii] = chunkLens[ii];
          if (useSmall && startIxs[ii] + cdims[ii] > dimLens[ii])
            cdims[ii] = dimLens[ii] - startIxs[ii];
        }
        try {
          dataObj = GenData.mkLinear( hdfType, cdims, dataObj);
        }
        catch( HdfException exc) {
          exc.printStackTrace();
          throwerr("caught: " + exc);
        }
      }

      if (unitBugs >= 10)
        prtf("TestUnita: chunk.  startIxs: %s  dataObj: %s",
          HdfUtil.formatInts( startIxs),
          HdfUtil.formatObject( dataObj));
      humidityVar.writeData( startIxs, dataObj, useLinear);

      // Increment startIxs
      for (int ii = rank - 1; ii >= 0; ii--) {
        startIxs[ii] += chunkLens[ii];
        if (startIxs[ii] < dimLens[ii]) break;
        startIxs[ii] = 0;
        if (ii == 0) allDone = true;
      }
      if (rank == 0) allDone = true;
    } // while ! allDone
  }

  hfile.close();

} // end createFile












//Object generateCuboidData(
//  boolean useLinear,
//  int dataType,            // NhVariable.TP_DOUBLE, etc
//  int[] dimLens)
//throws NhException
//{
//  int dtype = NhVariable.findDtype("TestUnita", dataType);
//  int stgFieldLen = 0;     // max string len for STRING_FIX, without null term
//  HdfGroup refGroup = null;
//  int origin = 0;
//
//  Object obj = genHdfData(
//    useLinear,
//    dtype,
//    stgFieldLen,
//    refGroup,                // group to use for references
//    dimLens,
//    origin);                 // origin 0.  e.g. 1000*ia + 100*ib + 10*ic + id
//
//  return obj;
//}














Object generateCuboidData(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens)
throws NhException
{
  int rank = dimLens.length;
  Object dataObj = null;

  if (rank == 0) {
    if (dataType == NhVariable.TP_UBYTE) dataObj = new Byte( (byte) 100);
    else if (dataType == NhVariable.TP_INT) dataObj = new Integer(100);
    else if (dataType == NhVariable.TP_FLOAT) dataObj = new Float(100);
    else if (dataType == NhVariable.TP_DOUBLE) dataObj = new Double(100);
    else if (dataType == NhVariable.TP_STRING_VAR) dataObj = new String("abc");
    else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  }

  else if (rank == 1) {
    if (dataType == NhVariable.TP_UBYTE) {
      byte[] vals = new byte[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = (byte) ia;
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_INT) {
      int[] vals = new int[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_FLOAT) {
      float[] vals = new float[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_DOUBLE) {
      double[] vals = new double[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_STRING_VAR) {
      String[] vals = new String[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = "stg" + ia;
      }
      dataObj = vals;
    }
    else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  } // if rank == 1

  else if (rank == 2) {
    if (dataType == NhVariable.TP_UBYTE) {
      byte[][] vals = new byte[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = (byte) (10 * ia + ib);
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_INT) {
      int[][] vals = new int[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = 10 * ia + ib;
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_FLOAT) {
      float[][] vals = new float[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = 10 * ia + ib;
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_DOUBLE) {
      double[][] vals = new double[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = 10 * ia + ib;
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_STRING_VAR) {
      String[][] vals = new String[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = "stg" + (10 * ia + ib);
        }
      }
      dataObj = vals;
    }
    else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  } // if rank == 2

  else if (rank == 3) {
    if (dataType == NhVariable.TP_UBYTE) {
      byte[][][] vals = new byte[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = (byte) (100 * ia + 10 * ib + ic);
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_INT) {
      int[][][] vals = new int[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = 100 * ia + 10 * ib + ic;
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_FLOAT) {
      float[][][] vals = new float[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = 100 * ia + 10 * ib + ic;
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_DOUBLE) {
      double[][][] vals = new double[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = 100 * ia + 10 * ib + ic;
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_STRING_VAR) {
      String[][][] vals = new String[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = "stg" + (100 * ia + 10 * ib + ic);
          }
        }
      }
      dataObj = vals;
    }
    else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  } // if rank == 3

  else if (rank == 4) {
    if (dataType == NhVariable.TP_UBYTE) {
      byte[][][][] vals = new byte[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              vals[ia][ib][ic][id]
                = (byte) (1000 * ia + 100 * ib + 10 * ic + id);
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_INT) {
      int[][][][] vals = new int[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              vals[ia][ib][ic][id] = 1000 * ia + 100 * ib + 10 * ic + id;
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_FLOAT) {
      float[][][][] vals = new float[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              vals[ia][ib][ic][id] = 1000 * ia + 100 * ib + 10 * ic + id;
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_DOUBLE) {
      double[][][][] vals = new double[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              vals[ia][ib][ic][id] = 1000 * ia + 100 * ib + 10 * ic + id;
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_STRING_VAR) {
      String[][][][] vals = new String[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              vals[ia][ib][ic][id]
                = "stg" + (1000 * ia + 100 * ib + 10 * ic + id);
            }
          }
        }
      }
      dataObj = vals;
    }
    else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  } // if rank == 4

  else if (rank == 5) {
    if (dataType == NhVariable.TP_UBYTE) {
      byte[][][][][] vals = new byte[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]][dimLens[4]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              for (int ie = 0; ie < dimLens[4]; ie++) {
                vals[ia][ib][ic][id][ie]
                  = (byte) (10000 * ia + 1000 * ib + 100 * ic + 10 * id + ie);
              }
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_INT) {
      int[][][][][] vals = new int[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]][dimLens[4]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              for (int ie = 0; ie < dimLens[4]; ie++) {
                vals[ia][ib][ic][id][ie]
                  = 10000 * ia + 1000 * ib + 100 * ic + 10 * id + ie;
              }
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_FLOAT) {
      float[][][][][] vals = new float[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]][dimLens[4]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              for (int ie = 0; ie < dimLens[4]; ie++) {
                vals[ia][ib][ic][id][ie]
                  = 10000 * ia + 1000 * ib + 100 * ic + 10 * id + ie;
              }
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_DOUBLE) {
      double[][][][][] vals = new double[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]][dimLens[4]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              for (int ie = 0; ie < dimLens[4]; ie++) {
                vals[ia][ib][ic][id][ie]
                  = 10000 * ia + 1000 * ib + 100 * ic + 10 * id + ie;
              }
            }
          }
        }
      }
      dataObj = vals;
    }
    else if (dataType == NhVariable.TP_STRING_VAR) {
      String[][][][][] vals = new String[dimLens[0]][dimLens[1]][dimLens[2]]
        [dimLens[3]][dimLens[4]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            for (int id = 0; id < dimLens[3]; id++) {
              for (int ie = 0; ie < dimLens[4]; ie++) {
                vals[ia][ib][ic][id][ie]
                  = "stg" + (10000 * ia + 1000 * ib + 100 * ic + 10 * id + ie);
              }
            }
          }
        }
      }
      dataObj = vals;
    }
    else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  } // if rank == 5



  else throwerr("unknown rank: %d", rank);

  return dataObj;
} // end generateCuboidData











Object mkSubset(
  int curIx,
  boolean isAllFill,
  int dataType,
  int[] dimLens,
  int[] chunkLens,
  int[] startIxs,
  boolean useSmall,
  Object genData)
throws NhException
{
  int FILLVAL = 8888;
  int rank = dimLens.length;
  int resLen = chunkLens[curIx];
  if (startIxs[curIx] + resLen > dimLens[curIx] && useSmall)
    resLen = dimLens[curIx] - startIxs[curIx];
  Object resObj = null;

  if (curIx < rank - 1) {
    Object[] resVec = new Object[ resLen];

    for (int ii = 0; ii < resLen; ii++) {
      Object objItem = null;
      if (isAllFill || startIxs[curIx] + ii >= dimLens[curIx]) {
        isAllFill = true;
        objItem = null;
      }
      else objItem = ((Object[]) genData) [startIxs[curIx]+ii];

      resVec[ii] = mkSubset(                   // recursion
        curIx + 1,
        isAllFill,
        dataType,
        dimLens,
        chunkLens,
        startIxs,
        useSmall,
        objItem);
    } // for ii
    resObj = resVec;
  } // if curIx < rank - 1

  else {               // else curIx == rank - 1
    if (dataType == NhVariable.TP_UBYTE) {
      byte[] res = new byte[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = (byte) FILLVAL;
        else res[ii] = ((byte[]) genData)[ ix];
      }
      resObj = res;
    }
    else if (dataType == NhVariable.TP_SHORT) {
      short[] res = new short[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = (short) FILLVAL;
        else res[ii] = ((short[]) genData)[ ix];
      }
      resObj = res;
    }
    else if (dataType == NhVariable.TP_INT) {
      int[] res = new int[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = (int) FILLVAL;
        else res[ii] = ((int[]) genData)[ ix];
      }
      resObj = res;
    }
    else if (dataType == NhVariable.TP_LONG) {
      long[] res = new long[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = (long) FILLVAL;
        else res[ii] = ((long[]) genData)[ ix];
      }
      resObj = res;
    }
    else if (dataType == NhVariable.TP_FLOAT) {
      float[] res = new float[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = (float) FILLVAL;
        else res[ii] = ((float[]) genData)[ ix];
      }
      resObj = res;
    }
    else if (dataType == NhVariable.TP_DOUBLE) {
      double[] res = new double[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = (double) FILLVAL;
        else res[ii] = ((double[]) genData)[ ix];
      }
      resObj = res;
    }
    else if (dataType == NhVariable.TP_STRING_VAR) {
      String[] res = new String[resLen];
      for (int ii = 0; ii < resLen; ii++) {
        int ix = startIxs[curIx] + ii;
        if (isAllFill || ix >= dimLens[curIx]) res[ii] = "stg" + FILLVAL;
        else res[ii] = ((String[]) genData)[ ix];
      }
      resObj = res;
    }
    else throwerr("unknown type: " + genData.getClass());
  } // else curIx == rank - 1

  return resObj;
} // end mkSubset












// Obsolete, but keep it.
// Occasionally it's useful as validation for mkSubset, above.

Object oldMkSubset(
  int[] dimLens,
  int[] chunkLens,
  int[] startIxs,
  boolean useSmall,
  Object genData)
throws NhException
{
  int rank = dimLens.length;
  int FILLVAL = 8888;

  if (rank == 0) throwerr("cannot subset a scalar");

  int[] subLens = new int[rank];
  for (int ii = 0; ii < rank; ii++) {
    subLens[ii] = chunkLens[ii];
    if (useSmall && startIxs[ii] + chunkLens[ii] > dimLens[ii])
      subLens[ii] = dimLens[ii] - startIxs[ii];
  }

  Object dataObj = null;

  if (rank == 1) {
    if (genData instanceof byte[]) {
      byte[] res = new byte[subLens[0]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        if (iaa < dimLens[0]) res[ia] = ((byte[]) genData)[ iaa];
        else res[ia] = (byte) FILLVAL;
      }
      dataObj = res;
    }
    else if (genData instanceof int[]) {
      int[] res = new int[subLens[0]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        if (iaa < dimLens[0]) res[ia] = ((int[]) genData)[ iaa];
        else res[ia] = FILLVAL;
      }
      dataObj = res;
    }
    else if (genData instanceof float[]) {
      float[] res = new float[subLens[0]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        if (iaa < dimLens[0]) res[ia] = ((float[]) genData)[ iaa];
        else res[ia] = FILLVAL;
      }
      dataObj = res;
    }
    else if (genData instanceof double[]) {
      double[] res = new double[subLens[0]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        if (iaa < dimLens[0]) res[ia] = ((double[]) genData)[ iaa];
        else res[ia] = FILLVAL;
      }
      dataObj = res;
    }
    else if (genData instanceof String[]) {
      String[] res = new String[subLens[0]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        if (iaa < dimLens[0]) res[ia] = ((String[]) genData)[ iaa];
        else res[ia] = "stg" + FILLVAL;
      }
      dataObj = res;
    }
    else throwerr("unknown type: " + genData.getClass());
  } // if rank == 1

  else if (rank == 2) {
    if (genData instanceof byte[][]) {
      byte[][] res = new byte[subLens[0]][subLens[1]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          if (iaa < dimLens[0] && ibb < dimLens[1])
            res[ia][ib] = ((byte[][]) genData)[iaa][ibb];
          else res[ia][ib] = (byte) FILLVAL;
        }
      }
      dataObj = res;
    }
    else if (genData instanceof int[][]) {
      int[][] res = new int[subLens[0]][subLens[1]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          if (iaa < dimLens[0] && ibb < dimLens[1])
            res[ia][ib] = ((int[][]) genData)[iaa][ibb];
          else res[ia][ib] = FILLVAL;
        }
      }
      dataObj = res;
    }
    else if (genData instanceof float[][]) {
      float[][] res = new float[subLens[0]][subLens[1]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          if (iaa < dimLens[0] && ibb < dimLens[1])
            res[ia][ib] = ((float[][]) genData)[iaa][ibb];
          else res[ia][ib] = FILLVAL;
        }
      }
      dataObj = res;
    }
    else if (genData instanceof double[][]) {
      double[][] res = new double[subLens[0]][subLens[1]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          if (iaa < dimLens[0] && ibb < dimLens[1])
            res[ia][ib] = ((double[][]) genData)[iaa][ibb];
          else res[ia][ib] = FILLVAL;
        }
      }
      dataObj = res;
    }
    else if (genData instanceof String[][]) {
      String[][] res = new String[subLens[0]][subLens[1]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          if (iaa < dimLens[0] && ibb < dimLens[1])
            res[ia][ib] = ((String[][]) genData)[iaa][ibb];
          else res[ia][ib] = "stg" + FILLVAL;
        }
      }
      dataObj = res;
    }
    else throwerr("unknown type: " + genData.getClass());
  } // if rank == 2

  else if (rank == 3) {
    if (genData instanceof byte[][][]) {
      byte[][][] res = new byte[subLens[0]][subLens[1]][subLens[2]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2])
              res[ia][ib][ic] = ((byte[][][]) genData)[iaa][ibb][icc];
            else res[ia][ib][ic] = (byte) FILLVAL;
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof int[][][]) {
      int[][][] res = new int[subLens[0]][subLens[1]][subLens[2]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2])
              res[ia][ib][ic] = ((int[][][]) genData)[iaa][ibb][icc];
            else res[ia][ib][ic] = FILLVAL;
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof float[][][]) {
      float[][][] res = new float[subLens[0]][subLens[1]][subLens[2]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2])
              res[ia][ib][ic] = ((float[][][]) genData)[iaa][ibb][icc];
            else res[ia][ib][ic] = FILLVAL;
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof double[][][]) {
      double[][][] res = new double[subLens[0]][subLens[1]][subLens[2]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2])
              res[ia][ib][ic] = ((double[][][]) genData)[iaa][ibb][icc];
            else res[ia][ib][ic] = FILLVAL;
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof String[][][]) {
      String[][][] res = new String[subLens[0]][subLens[1]][subLens[2]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2])
              res[ia][ib][ic] = ((String[][][]) genData)[iaa][ibb][icc];
            else res[ia][ib][ic] = "stg" + FILLVAL;
          }
        }
      }
      dataObj = res;
    }
    else throwerr("unknown type: " + genData.getClass());
  } // if rank == 3

  else if (rank == 4) {
    if (genData instanceof byte[][][][]) {
      byte[][][][] res = new byte[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                && idd < dimLens[3])
              {
                res[ia][ib][ic][id]
                  = ((byte[][][][]) genData)[iaa][ibb][icc][idd];
              }
              else res[ia][ib][ic][id] = (byte) FILLVAL;
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof int[][][][]) {
      int[][][][] res = new int[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                && idd < dimLens[3])
              {
                res[ia][ib][ic][id]
                  = ((int[][][][]) genData)[iaa][ibb][icc][idd];
              }
              else res[ia][ib][ic][id] = FILLVAL;
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof float[][][][]) {
      float[][][][] res = new float[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                && idd < dimLens[3])
              {
                res[ia][ib][ic][id]
                  = ((float[][][][]) genData)[iaa][ibb][icc][idd];
              }
              else res[ia][ib][ic][id] = FILLVAL;
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof double[][][][]) {
      double[][][][] res = new double[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                && idd < dimLens[3])
              {
                res[ia][ib][ic][id]
                  = ((double[][][][]) genData)[iaa][ibb][icc][idd];
              }
              else res[ia][ib][ic][id] = FILLVAL;
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof String[][][][]) {
      String[][][][] res = new String[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                && idd < dimLens[3])
              {
                res[ia][ib][ic][id]
                  = ((String[][][][]) genData)[iaa][ibb][icc][idd];
              }
              else res[ia][ib][ic][id] = "stg" + FILLVAL;
            }
          }
        }
      }
      dataObj = res;
    }
    else throwerr("unknown type: " + genData.getClass());
  } // if rank == 4

  else if (rank == 5) {
    if (genData instanceof byte[][][][][]) {
      byte[][][][][] res = new byte[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]][subLens[4]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              for (int ie = 0; ie < subLens[4]; ie++) {
                int iee = startIxs[4] + ie;
                if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                  && idd < dimLens[3] && iee < dimLens[4])
                {
                  res[ia][ib][ic][id][ie]
                    = ((byte[][][][][]) genData) [iaa][ibb][icc][idd][iee];
                }
                else res[ia][ib][ic][id][ie] = (byte) FILLVAL;
              }
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof int[][][][][]) {
      int[][][][][] res = new int[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]][subLens[4]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              for (int ie = 0; ie < subLens[4]; ie++) {
                int iee = startIxs[4] + ie;
                if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                  && idd < dimLens[3] && iee < dimLens[4])
                {
                  res[ia][ib][ic][id][ie]
                    = ((int[][][][][]) genData)[iaa][ibb][icc][idd][iee];
                }
                else res[ia][ib][ic][id][ie] = FILLVAL;
              }
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof float[][][][][]) {
      float[][][][][] res = new float[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]][subLens[4]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              for (int ie = 0; ie < subLens[4]; ie++) {
                int iee = startIxs[4] + ie;
                if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                  && idd < dimLens[3] && iee < dimLens[4])
                {
                  res[ia][ib][ic][id][ie]
                    = ((float[][][][][]) genData)[iaa][ibb][icc][idd][iee];
                }
                else res[ia][ib][ic][id][ie] = FILLVAL;
              }
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof double[][][][][]) {
      double[][][][][] res = new double[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]][subLens[4]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              for (int ie = 0; ie < subLens[4]; ie++) {
                int iee = startIxs[4] + ie;
                if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                  && idd < dimLens[3] && iee < dimLens[4])
                {
                  res[ia][ib][ic][id][ie]
                    = ((double[][][][][]) genData)[iaa][ibb][icc][idd][iee];
                }
                else res[ia][ib][ic][id][ie] = FILLVAL;
              }
            }
          }
        }
      }
      dataObj = res;
    }
    else if (genData instanceof String[][][][][]) {
      String[][][][][] res = new String[subLens[0]][subLens[1]][subLens[2]]
        [subLens[3]][subLens[4]];
      for (int ia = 0; ia < subLens[0]; ia++) {
        int iaa = startIxs[0] + ia;
        for (int ib = 0; ib < subLens[1]; ib++) {
          int ibb = startIxs[1] + ib;
          for (int ic = 0; ic < subLens[2]; ic++) {
            int icc = startIxs[2] + ic;
            for (int id = 0; id < subLens[3]; id++) {
              int idd = startIxs[3] + id;
              for (int ie = 0; ie < subLens[4]; ie++) {
                int iee = startIxs[4] + ie;
                if (iaa < dimLens[0] && ibb < dimLens[1] && icc < dimLens[2]
                  && idd < dimLens[3] && iee < dimLens[4])
                {
                  res[ia][ib][ic][id][ie]
                    = ((String[][][][][]) genData)[iaa][ibb][icc][idd][iee];
                }
                else res[ia][ib][ic][id][ie] = "stg" + FILLVAL;
              }
            }
          }
        }
      }
      dataObj = res;
    }
    else throwerr("unknown type: " + genData.getClass());
  } // if rank == 5


  else throwerr("unknown rank: " + rank);
  return dataObj;
} // end oldMkSubset














// Parm has format: "d,d,d/c,c,c" or special case "d,d,d/null",
// where d is a dimLen and c is a chunkLen.
//
// Returns [0][*] = dims, [1][*] = chunks.  chunks may be null.

static int[][] parseDimChunks( String msg, String stg)
throws NhException
{
  String[] toks = stg.split("/");
  if (toks.length != 2) throwerr("bad format for parm \"" + msg + "\".");

  int[] dims = null;
  if (toks[0].length() == 0) dims = new int[0];
  else dims = parseInts( msg, toks[0]);

  int[] chunks = null;
  if (! toks[1].equals("null")) chunks = parseInts( msg, toks[1]);
  return new int[][] { dims, chunks};
}








static int parseInt( String msg, String stg)
throws NhException
{
  int res = 0;
  try { res = Integer.parseInt( stg, 10); }
  catch( NumberFormatException exc) {
    throwerr("parm \"" + msg + "\" has invalid integer: \"" + stg + "\"");
  }
  return res;
}




static int[] parseInts( String msg, String stg)
throws NhException
{
  String[] toks = stg.split(",");
  if (toks.length == 0) throwerr("parm \"" + msg + "\" is empty.");
  int[] res = new int[toks.length];
  for (int ii = 0; ii < toks.length; ii++) {
    res[ii] = parseInt( msg, toks[ii]);
  }
  return res;
}




static boolean parseBoolean( String msg, String stg)
throws NhException
{
  boolean res = false;
  if (stg.equals("n") || stg.equals("false")) res = false;
  else if (stg.equals("y") || stg.equals("true")) res = true;
  else throwerr("parm \"" + msg + "\" has invalid boolean: \"" + stg + "\"");
  return res;
}



static boolean[] parseBooleans( String msg, String stg)
throws NhException
{
  String[] toks = stg.split(",");
  if (toks.length == 0) throwerr("parm \"" + msg + "\" is empty.");
  boolean[] res = new boolean[toks.length];
  for (int ii = 0; ii < toks.length; ii++) {
    res[ii] = parseBoolean( msg, toks[ii]);
  }
  return res;
}





public static String formatInts(
  int[] vals)
{
  String res = "";
  if (vals == null) res = "(null)";
  else {
    res = "[";
    for (int ii = 0; ii < vals.length; ii++) {
      if (ii > 0) res += " ";
      res += vals[ii];
    }
    res += "]";
  }
  return res;
}






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
