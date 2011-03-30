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


// Set defaults when called by Maven's use of JUnit.
int nhBugs = 0;
int hdfBugs = 0;
String sourceDir = "src/test/resources/testUnitData";
String targetDir = "target/testDira";
boolean useCheck = true;

String dataTypeStg = "double,float,int";

// Each string has the format:
// "d,d,d/c,c,c" or special case "d,d,d/null",
// where d is a dimLen and c is a chunkLen.

String[] dimStgs = {
  "10,10,10/null",
  "10,10,10/10,10,10",
  "10,10,10/5,5,5",
  "10,10,10/7,7,7"
};

String compLevelStg = "0,5,9";

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
    System.exit(1);
  }
}






void initAll( String[] args)
throws NhException
{
  sourceDir = null;
  targetDir = null;

  if (args.length % 2 != 0) throwerr("args must be key/value pairs");

  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-nhBugs")) nhBugs = parseInt("-nhBugs", val);
    else if (key.equals("-hdfBugs")) hdfBugs = parseInt("-hdfBugs", val);
    else if (key.equals("-sourceDir")) sourceDir = val;
    else if (key.equals("-targetDir")) targetDir = val;
    else if (key.equals("-check")) useCheck = parseBoolean("-check", val);
    else if (key.equals("-dataType")) dataTypeStg = val;
    else if (key.equals("-dims")) dimStgs = new String[] {val};
    else if (key.equals("-compLevel")) compLevelStg = val;
    else if (key.equals("-useLinear")) useLinearStg = val;
    else throwerr("unknown parm: %s", key);
  }

  if (nhBugs < 0) throwerr("parm not specified: -nhBugs");
  if (hdfBugs < 0) throwerr("parm not specified: -hdfBugs");
  if (sourceDir == null) throwerr("parm not specified: -sourceDir");
  if (targetDir == null) throwerr("parm not specified: -targetDir");

} // end initAll












public void testIt()
throws NhException
{
  prtf("TestUnita: nhBugs: %d", nhBugs);
  prtf("TestUnita: hdfBugs: %d", hdfBugs);
  prtf("TestUnita: sourceDir: %s", sourceDir);
  prtf("TestUnita: targetDir: %s", targetDir);
  prtf("TestUnita: dataType: %s", dataTypeStg);
  for (String stg : dimStgs) {
    prtf("TestUnita: dim: %s", stg);
  }
  prtf("TestUnita: compLevel: %s", compLevelStg);

  new File( targetDir).mkdir();

  int[] testDataTypes = {
    NhVariable.TP_DOUBLE,       // nhType
    NhVariable.TP_FLOAT,
    NhVariable.TP_INT};
  int[] testRanks = { 0, 1, 2, 3};
  int[] testDimLens = { 10, 100};
  int[] testCompLevels = {0, 5, 9};
  boolean[] testUseLinears = { false, true};

  for (String dtypeStg : dataTypeStg.split(",")) {
    int dataType = 0;
    if (dtypeStg.equals("double")) dataType = NhVariable.TP_DOUBLE;
    else if (dtypeStg.equals("float")) dataType = NhVariable.TP_FLOAT;
    else if (dtypeStg.equals("int")) dataType = NhVariable.TP_INT;
    else throwerr("invalid dataType: \"" + dtypeStg + "\"");

    for (String dimStg : dimStgs) {
      int[][] dimChunks = parseDimChunks("-dims", dimStg);
      int[] dimLens = dimChunks[0];
      int[] chunkLens = dimChunks[1];
      int rank = dimLens.length;

      for (int compLevel : parseInts("compLevel", compLevelStg)) {
        for (boolean useLinear : parseBooleans("useLinear", useLinearStg)) {

          // Cannot compress a scalar; compression requires chunking.
          if (! (compLevel > 0 && (rank == 0 || chunkLens == null))) {
            mkSingleTest( dataType, dimLens, chunkLens,
              compLevel, useLinear, sourceDir, targetDir);
          } // if not compressing a scalar
        } // for useLinear
      } // for compLevel
    } // for dimStg
  } // for dataTypeStg
} // end testIt





void mkSingleTest(
  int dataType,            // NhVariable.TP_DOUBLE, etc
  int[] dimLens,
  int[] chunkLens,
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
  if (chunkLens == null) namePart += ".chunk_null";
  else {
    namePart += ".chunk";
    for (int ival : chunkLens) namePart += "_" + ival;
  }
  namePart += ".comp_" + compLevel;
  namePart += ".linear_" + useLinear;
  namePart += ".nc";
  
  String sourceName = sourceDir + "/" + namePart;
  String targetName = targetDir + "/" + namePart;

  createFile( dataType, dimLens, chunkLens,
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
  int[] chunkLens,
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
    prtf("  dimLens: %s", formatInts( dimLens));
    prtf("  chunkLens: %s", formatInts( chunkLens));
    prtf("  compLevel: %d", compLevel);
    prtf("  useLinear: %s", useLinear);
    prtf("  targetName: %s", targetName);
  }

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

  if (chunkLens == null) {
    Object dataObj = null;
    if (useLinear) dataObj = generateLinearData( dataType, dimLens);
    else dataObj = generateCuboidData( dataType, dimLens);
    int[] startIxs = null;
    humidityVar.writeData( startIxs, dataObj, useLinear);
  }

  else {
    Object dataObj = null;
    int[] startIxs = new int[rank];      // all 0
    boolean allDone = false;
    while (! allDone) {
      int[] genLens = new int[rank];
      for (int ii = 0; ii < rank; ii++) {
        if (startIxs[ii] + chunkLens[ii] <= dimLens[ii])
          genLens[ii] = chunkLens[ii];
        else genLens[ii] = dimLens[ii] - startIxs[ii];
      }
      if (useLinear) dataObj = generateLinearData( dataType, genLens);
      else dataObj = generateCuboidData( dataType, genLens);
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








// Parm has format: "d,d,d/c,c,c" or special case "d,d,d/null",
// where d is a dimLen and c is a chunkLen.
//
// Returns [0][*] = dims, [1][*] = chunks.  chunks may be null.

static int[][] parseDimChunks( String msg, String stg)
throws NhException
{
  String[] toks = stg.split("/");
  if (toks.length != 2) throwerr("bad format for parm \"" + msg + "\".");
  int[] dims = parseInts( msg, toks[0]);
  int[] chunks = null;
  if (! toks[1].equals("null")) chunks = parseInts( msg, toks[1]);
  return new int[][] { dims, chunks};
}



// Returns [0][*] = dims, [1][*] = chunks.

static int[][] oldParseIntPairs( String msg, String stg)
throws NhException
{
  String[] toks = stg.split(",");
  if (toks.length == 0) throwerr("parm \"" + msg + "\" is empty.");
  int[] dims = new int[toks.length];
  int[] chunks = new int[toks.length];
  for (int ii = 0; ii < toks.length; ii++) {
    String[] subToks = toks[ii].split("/");
    if (subToks.length != 2)
      throwerr("parm \"" + msg + "\" has invalid spec \""
        + toks[ii] + "\" in string \"" + stg + "\"");
    try { dims[ii] = Integer.parseInt( subToks[0], 10); }
    catch( NumberFormatException exc) {
      throwerr("parm \"" + msg + "\" has invalid integer in \""
        + toks[ii] + "\" in string \"" + stg + "\"");
    }
    try { chunks[ii] = Integer.parseInt( subToks[1], 10); }
    catch( NumberFormatException exc) {
      throwerr("parm \"" + msg + "\" has invalid integer in \""
        + toks[ii] + "\" in string \"" + stg + "\"");
    }
  }
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
