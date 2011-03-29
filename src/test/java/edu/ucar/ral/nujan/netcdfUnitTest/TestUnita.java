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
import junit.framework.TestCase;
import junitx.framework.FileAssert;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;


public class TestUnita extends TestCase {


int nhBugs;
int hdfBugs;
String sourceDir;
String targetDir;
boolean useCheck;


public TestUnita() {
  // Set defaults when called by Maven's use of JUnit.
  nhBugs = 0;
  hdfBugs = 0;
  sourceDir = "src/test/resources/testUnitData/test001.nc";
  targetDir = "target/testDira";
  useCheck = false;
}



public static void main( String[] args) {
  try {
    TestUnita tunit = new TestUnita();
    tunit.initAll( args);
    tunit.mkTestFiles();
  }
  catch( NhException exc) {
    exc.printStackTrace();
    String msg = "caught: " + exc;
    System.out.println( msg);
    System.err.println( msg);
    System.exit(1);
  }
}






void initAll( String[] args)
throws NhException
{
  nhBugs = -1;
  hdfBugs = -1;
  sourceDir = null;
  targetDir = null;

  if (args.length % 2 != 0) throwerr("args must be key/value pairs");

  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-nhBugs")) nhBugs = Integer.parseInt( val, 10);
    else if (key.equals("-hdfBugs")) hdfBugs = Integer.parseInt( val, 10);
    else if (key.equals("-sourceDir")) sourceDir = val;
    else if (key.equals("-targetDir")) targetDir = val;
    else if (key.equals("-useCheck")) {
      if (val.equals("false")) useCheck = false;
      else if (val.equals("true")) useCheck = true;
      else throwerr("bad value for -useCheck");
    }
    else throwerr("unknown parm: %s", key);
  }

  if (nhBugs < 0) throwerr("parm not specified: -nhBugs");
  if (hdfBugs < 0) throwerr("parm not specified: -hdfBugs");
  if (sourceDir == null) throwerr("parm not specified: -sourceDir");
  if (targetDir == null) throwerr("parm not specified: -targetDir");

  prtf("TestUnita: nhBugs: %d", nhBugs);
  prtf("TestUnita: hdfBugs: %d", hdfBugs);
  prtf("TestUnita: sourceDir: %s", sourceDir);
  prtf("TestUnita: targetDir: %s", targetDir);
} // end initAll






public void testAll() throws NhException {
  nhBugs = 0;
  hdfBugs = 0;
  sourceDir = "src/test/resources/testUnitData/test001.nc";
  targetDir = "target/testDira";

  mkTestFiles();
}








void mkTestFiles()
throws NhException
{
  new File( targetDir).mkdir();

  int[] dataTypes = {
    NhVariable.TP_DOUBLE,       // nhType
    NhVariable.TP_FLOAT,
    NhVariable.TP_INT};

  for (int dataType : dataTypes) {
    for (int rank : new int[] { 0, 1, 2, 3}) {
      for (int dimLen : new int[] { 10, 100}) {      // small, big
        for (String chunkTp : new String[] {"null", "full", "div", "nonDiv"}) {
          for (int compLevel : new int[] {0, 5, 9}) {
            // Cannot compress a scalar; compression requires chunking.
            if (! (compLevel > 0 && (rank == 0 || chunkTp.equals("null")))) {
              for (boolean useLinear : new boolean[] { false, true}) {

                int[] dimLens = new int[rank];
                for (int ii = 0; ii < rank; ii++) {
                  dimLens[ii] = dimLen;
                }

                mkSingleTest( dataType, dimLens, chunkTp,
                  compLevel, useLinear, sourceDir, targetDir);
              } // for useLinear
            } // if not compressing a scalar
          } // for compLevel
        }
      } // for dimLen
    }
  } // for dataType
} // end mkTestFiles





void mkSingleTest(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens,
  String chunkTp,          // "null", "full", "div", "nonDiv"
  int compLevel,
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
  namePart += ".chunk_" + chunkTp;
  namePart += ".comp_" + compLevel;
  namePart += ".linear_" + useLinear;
  namePart += ".nc";
  
  String sourceName = sourceDir + "/" + namePart;
  String targetName = targetDir + "/" + namePart;

  createFile( dataType, dimLens, chunkTp,
    compLevel, useLinear, targetName);

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
  String chunkTp,          // "null", "full", "div", "nonDiv"
  int compLevel,
  boolean useLinear,
  String targetName)
throws NhException
{

  int rank = dimLens.length;
  if (nhBugs >= 1) {
    prtf("TestUnita.createFile:");
    prtf("  dataType: %s", NhVariable.nhTypeNames[ dataType]);
    prtf("  rank: %d", rank);
    for (int ival : dimLens) {
      prtf("    dimLen: %d", ival);
    }
    prtf("  chunkTp: \"%s\"", chunkTp);
    prtf("  compLevel: %d", compLevel);
    prtf("  useLinear: %s", useLinear);
    prtf("  targetName: %s", targetName);
  }

  int[] chunkLens = null;

  if (chunkTp.equals("null")) {}

  else if (chunkTp.equals("full")) {
    chunkLens = dimLens;
  }

  else if (chunkTp.equals("div")) {
    chunkLens = new int[rank];
    for (int ii = 0; ii < rank; ii++) {
      chunkLens[ii] = dimLens[ii] / 10;
      if ( 10 * chunkLens[ii] != dimLens[ii])
        throwerr("dimLens value is not divisible");
    }
  }

  else if (chunkTp.equals("nonDiv")) {
    chunkLens = new int[rank];
    for (int ii = 0; ii < rank; ii++) {
      chunkLens[ii] = dimLens[ii] / 10 + 1;
      if ( 10 * chunkLens[ii] == dimLens[ii])
        throwerr("dimLens value is divisible");
    }
  }
  else throwerr("invalid chunkTp: %s", chunkTp);
  if (chunkLens == null) {
    if (nhBugs >= 1) prtf("TestUnita.createFile: chunkLens: null");
  }
  else {
    for (int ival : chunkLens) {
      if (nhBugs >= 1) prtf("TestUnita.createFile: chunkLen: %d", ival);
    }
  }

  Object dataObj = null;
  int[] genLens;
  if (chunkTp.equals("null")) genLens = dimLens;
  else genLens = chunkLens;

  if (useLinear) dataObj = generateLinearData( dataType, genLens);
  else dataObj = generateCuboidData( dataType, genLens);


  NhFileWriter hfile = new NhFileWriter(
    targetName,
    NhFileWriter.OPT_OVERWRITE,
    nhBugs,
    hdfBugs,
    1283444655,            // utcModTime: milliseconds since 1970
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
  if (dataType == NhVariable.TP_DOUBLE)
    fillValue = new Double( -999999);
  else if (dataType == NhVariable.TP_FLOAT)
    fillValue = new Float( -999999);
  else if (dataType == NhVariable.TP_INT)
    fillValue = new Integer( -999999);
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




  // Write out the data
  if (chunkTp.equals("null")) {
    int[] startIxs = null;
    humidityVar.writeData( startIxs, dataObj, useLinear);
  }
  else if (chunkTp.equals("full")) {
    int[] startIxs = new int[rank];      // all 0
    humidityVar.writeData( startIxs, dataObj, useLinear);
  }
  else if (chunkTp.equals("div") || chunkTp.equals("nonDiv")) {
    int[] startIxs = new int[rank];      // all 0
    boolean allDone = false;
    while (! allDone) {
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
  } // if chunkTp is "div" or "nonDiv"

  hfile.close();

} // end createFile




Object generateCuboidData(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens)
throws NhException
{
  int rank = dimLens.length;
  Object dataObj = null;

  if (dataType == NhVariable.TP_DOUBLE) {
    if (rank == 0) dataObj = new Double(100);
    else if (rank == 1) {
      double[] vals = new double[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
    else if (rank == 2) {
      double[][] vals = new double[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = 100 * ia + ib;
        }
      }
      dataObj = vals;
    }
    else if (rank == 3) {
      double[][][] vals = new double[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = 10000 * ia + 100 * ib + ic;
          }
        }
      }
      dataObj = vals;
    }
    else throwerr("unknown rank: %d", rank);
  } // if TP_DOUBLE

  else if (dataType == NhVariable.TP_FLOAT) {
    if (rank == 0) dataObj = new Float(100);
    else if (rank == 1) {
      float[] vals = new float[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
    else if (rank == 2) {
      float[][] vals = new float[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = 100 * ia + ib;
        }
      }
      dataObj = vals;
    }
    else if (rank == 3) {
      float[][][] vals = new float[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = 10000 * ia + 100 * ib + ic;
          }
        }
      }
      dataObj = vals;
    }
    else throwerr("unknown rank: %d", rank);
  } // if TP_FLOAT

  else if (dataType == NhVariable.TP_INT) {
    if (rank == 0) dataObj = new Integer(100);
    else if (rank == 1) {
      int[] vals = new int[dimLens[0]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
    else if (rank == 2) {
      int[][] vals = new int[dimLens[0]][dimLens[1]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          vals[ia][ib] = 100 * ia + ib;
        }
      }
      dataObj = vals;
    }
    else if (rank == 3) {
      int[][][] vals = new int[dimLens[0]][dimLens[1]][dimLens[2]];
      for (int ia = 0; ia < dimLens[0]; ia++) {
        for (int ib = 0; ib < dimLens[1]; ib++) {
          for (int ic = 0; ic < dimLens[2]; ic++) {
            vals[ia][ib][ic] = 10000 * ia + 100 * ib + ic;
          }
        }
      }
      dataObj = vals;
    }

    else throwerr("unknown rank: %d", rank);
  } // if TP_INT
  else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  return dataObj;
} // end generateCuboidData





Object generateLinearData(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens)
throws NhException
{
  int rank = dimLens.length;
  Object dataObj = null;

  if (dataType == NhVariable.TP_DOUBLE) {
    if (rank == 0) dataObj = new Double(100);
    else {
      int totLen = 1;
      for (int ii = 0; ii < rank; ii++) {
        totLen *= dimLens[ii];
      }
      double[] vals = new double[totLen];
      for (int ia = 0; ia < totLen; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
  } // if TP_DOUBLE

  else if (dataType == NhVariable.TP_FLOAT) {
    if (rank == 0) dataObj = new Float(100);
    else {
      int totLen = 1;
      for (int ii = 0; ii < rank; ii++) {
        totLen *= dimLens[ii];
      }
      float[] vals = new float[totLen];
      for (int ia = 0; ia < totLen; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
  } // if TP_FLOAT

  else if (dataType == NhVariable.TP_INT) {
    if (rank == 0) dataObj = new Integer(100);
    else {
      int totLen = 1;
      for (int ii = 0; ii < rank; ii++) {
        totLen *= dimLens[ii];
      }
      int[] vals = new int[totLen];
      for (int ia = 0; ia < totLen; ia++) {
        vals[ia] = ia;
      }
      dataObj = vals;
    }
  } // if TP_INT
  else throwerr("unknown dataType: %s", NhVariable.nhTypeNames[dataType]);
  return dataObj;
} // end generateLinearData





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
