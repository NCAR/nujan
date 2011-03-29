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


package edu.ucar.ral.nujan.netcdfTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.SimpleTimeZone;
import ucar.ma2.ArrayFloat;


import edu.ucar.ral.nujan.hdf.HdfUtil;
import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;

import edu.ucar.ral.nujan.hdf.HdfException;  // used for sample data generation
import edu.ucar.ral.nujan.hdf.HdfGroup;      // used for sample data generation
import edu.ucar.ral.nujan.hdfTest.GenData;   // used for sample data generation


// Test byte / ubyte / short / int / long / float / double / char / vstring,
// with any number of dimensions.


public class Tnetcdfa {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs         <int>");
  prtf("  -nhType       byte / ubyte / short / int / long / float / double / char / vstring");
  prtf("  -dims         <int,int,...>   or \"scalar\" if a scalar");
  prtf("  -chunks       <int,int,...>   or \"contiguous\" if contiguous");
  prtf("  -compress     compression level: 0==none, 1 - 9");
  prtf("  -utcModTime   either yyyy-mm-dd or yyyy-mm-ddThh:mm:ss");
  prtf("                or 0, meaning use the current time");
  prtf("  -numThread    1 <= numThread <= 100");
  prtf("  -outFile      <fname>");
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
throws NhException
{
  int bugs = -1;
  int nhType = -1;
  int[] dims = null;
  int[] chunks = null;
  int compressLevel = -1;
  long utcModTime = -1;
  int numThread = -1;
  boolean useLinear = false;
  boolean useArray = false;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-nhType")) {
      if (val.equals("sbyte")) nhType = NhVariable.TP_SBYTE;
      else if (val.equals("ubyte")) nhType = NhVariable.TP_UBYTE;
      else if (val.equals("short")) nhType = NhVariable.TP_SHORT;
      else if (val.equals("int")) nhType = NhVariable.TP_INT;
      else if (val.equals("long")) nhType = NhVariable.TP_LONG;
      else if (val.equals("float")) nhType = NhVariable.TP_FLOAT;
      else if (val.equals("double")) nhType = NhVariable.TP_DOUBLE;
      else if (val.equals("char")) nhType = NhVariable.TP_CHAR;
      else if (val.equals("vstring")) nhType = NhVariable.TP_STRING_VAR;
      else badparms("unknown nhType: " + val);
    }
    else if (key.equals("-dims")) {
      if (val.equals("scalar")) dims = new int[0];
      else dims = parseInts("dimension", val);
    }
    else if (key.equals("-chunks")) {
      if (val.equals("contiguous")) chunks = new int[0];
      else chunks = parseInts("chunk length", val);
    }
    else if (key.equals("-compress")) compressLevel = Integer.parseInt( val);
    else if (key.equals("-utcModTime")) {
      if (val.equals("0")) utcModTime = 0;
      else {
        SimpleDateFormat utcSdf = null;
        if (val.length() == 10)      // yyyy-mm-dd
          utcSdf = new SimpleDateFormat("yyyy-MM-dd");
        else if (val.length() == 19)      // yyyy-MM-ddTHH:mm:ss
          utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        else badparms("invalid -utcModTime: \"" + val + "\"");

        utcSdf.setTimeZone( new SimpleTimeZone( 0, "UTC"));
        Date dt = null;
        try { dt = utcSdf.parse( val); }
        catch( ParseException exc) {
          badparms("invalid -utcModTime: \"" + val + "\"");
        }
        utcModTime = dt.getTime();
      }
    }
    else if (key.equals("-useLinear"))
      useLinear = parseBoolean("useLinear", val);
    else if (key.equals("-useArray"))
      useArray = parseBoolean("useLinear", val);
    else if (key.equals("-numThread")) numThread = Integer.parseInt( val);
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }

  if (bugs < 0) badparms("missing parm: -bugs");
  if (nhType < 0) badparms("missing parm: -nhType");
  if (dims == null) badparms("missing parm: -dims");
  if (chunks == null) badparms("missing parm: -chunks");
  if (compressLevel < 0) badparms("missing parm: -compress");
  if (utcModTime < 0) badparms("missing parm: -utcModTime");
  if (numThread < 0) badparms("missing parm: -numThread");
  if (outFile == null) badparms("missing parm: -outFile");

  if (numThread < 1 || numThread > 100) badparms("invalid numThread");

  if (chunks.length == 0) chunks = null;
  prtf("Tnetcdfa: bugs: %d", bugs);
  prtf("Tnetcdfa: nhType: \"%s\"", NhVariable.nhTypeNames[nhType]);
  prtf("Tnetcdfa: rank: %d", dims.length);
  for (int ii = 0; ii < dims.length; ii++) {
    prtf("Tnetcdfa: dims[%d]: %d", ii, dims[ii]);
    if (dims[ii] < 1) badparms("invalid dims");
  }
  prtf("Tnetcdfa: dims: %s", formatInts( dims));
  prtf("Tnetcdfa: chunks: %s", formatInts( chunks));
  prtf("Tnetcdfa: compress: %d", compressLevel);

  SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  utcSdf.setTimeZone( new SimpleTimeZone( 0, "UTC"));
  prtf("Tnetcdfa: utcModTime: %d  %s", utcModTime, utcSdf.format( utcModTime));

  prtf("Tnetcdfa: useLinear: %s", useLinear);
  prtf("Tnetcdfa: useArray: %s", useArray);
  prtf("Tnetcdfa: outFile: \"%s\"", outFile);

  final long startTime = System.currentTimeMillis();
  if (numThread == 1)
    testOne( bugs, nhType, dims, chunks, compressLevel,
      utcModTime, useLinear, useArray, outFile);
  else {
    Thread[] threads = new Thread[numThread];
    for (int ii = 0; ii < numThread; ii++) {
      final String fnameFinal = String.format("%s.%02d", outFile, ii);
      final int iiFinal = ii;
      final int bugsFinal = bugs;
      final int nhTypeFinal = nhType;
      final int[] dimsFinal = dims;
      final int[] chunksFinal = chunks;
      final int compressLevelFinal = compressLevel;
      final long utcModTimeFinal = utcModTime;
      final boolean useLinearFinal = useLinear;
      final boolean useArrayFinal = useArray;
      threads[ii] = new Thread() {
        public void run() {
          prtf("%8.4f  starting thread %d",
            0.001 * (System.currentTimeMillis() - startTime), iiFinal);
          try {
            testOne(
              bugsFinal,
              nhTypeFinal,
              dimsFinal,
              chunksFinal,
              compressLevelFinal,
              utcModTimeFinal,
              useLinearFinal,
              useArrayFinal,
              fnameFinal);
          }
          catch( NhException exc) {
            exc.printStackTrace();
            System.exit(1);
          }
        }
      };
      threads[ii].start();
    } // for ii
    for (int ii = 0; ii < numThread; ii++) {
      prtf("%8.4f  waiting on thread %d",
        0.001 * (System.currentTimeMillis() - startTime), ii);
      prtf("waiting on thread %d", ii);
      try { threads[ii].join(); }
      catch( InterruptedException exc) {
        exc.printStackTrace();
        throwerr("caught: " + exc);
      }
      prtf("%3.7f  joined thread %d",
        0.001 * (System.currentTimeMillis() - startTime), ii);
    }
  }
} // end runit








static void testOne(
  int bugs,
  int nhType,
  int[] dims,
  int[] chunks,
  int compressLevel,
  long utcModTime,           // milliSecs since 1970, or if 0 use current time
  boolean useLinear,
  boolean useArray,
  String fname)
throws NhException
{
  NhFileWriter hfile = new NhFileWriter(
    fname, NhFileWriter.OPT_OVERWRITE,
    bugs, bugs,            // nhBugs, hdfBugs
    utcModTime,            // utcModTime: use current time.
    null,                  // logDir
    null);                 // statTag

  NhGroup rootGroup = hfile.getRootGroup();

  // Create test data and fill value.
  int dtype = 0;
  int stgFieldLen = 0;
  if (nhType == NhVariable.TP_SBYTE) dtype = HdfGroup.DTYPE_SFIXED08;
  else if (nhType == NhVariable.TP_UBYTE) dtype = HdfGroup.DTYPE_UFIXED08;
  else if (nhType == NhVariable.TP_SHORT) dtype = HdfGroup.DTYPE_FIXED16;
  else if (nhType == NhVariable.TP_INT) dtype = HdfGroup.DTYPE_FIXED32;
  else if (nhType == NhVariable.TP_LONG) dtype = HdfGroup.DTYPE_FIXED64;
  else if (nhType == NhVariable.TP_FLOAT) dtype = HdfGroup.DTYPE_FLOAT32;
  else if (nhType == NhVariable.TP_DOUBLE) dtype = HdfGroup.DTYPE_FLOAT64;
  else if (nhType == NhVariable.TP_CHAR) dtype = HdfGroup.DTYPE_TEST_CHAR;
  else if (nhType == NhVariable.TP_STRING_VAR)
    dtype = HdfGroup.DTYPE_STRING_VAR;
  else throwerr("unknown nhType: " + NhVariable.nhTypeNames[nhType]);

  // Generate the test data
  int[] dataDims = dims;
  if (chunks != null) dataDims = chunks;
  ///if (useLinear) {
  ///  int totalLen = 1;
  ///  for (int ival : dims) {
  ///    totalLen *= ival;
  ///  }
  ///  dataDims = new int[] { totalLen};
  ///}
  prtf("Tnetcdfa: dataDims: " + HdfUtil.formatInts( dataDims));
  Object allData = null;
  Object fillValue = null;
  try {
    allData = GenData.genHdfData(
      dtype,
      stgFieldLen,
      null,           // refGroup
      dataDims,
      0);             // ival, origin 0.

    fillValue = GenData.genFillValue(
      dtype,
      0);             // stgFieldLen.  If 0 and FIXED, MsgAttribute calcs it.
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
  prtf("Tnetcdfa: allData: " + HdfUtil.formatObject( allData));

  // Netcdf doesn't support fill values for Strings or scalars.
  if (nhType == NhVariable.TP_STRING_VAR) fillValue = null;
  if (dims.length == 0) fillValue = null;

  NhGroup alpha1 = rootGroup.addGroup("alpha1");
  NhGroup alpha2 = rootGroup.addGroup("alpha2");
  NhGroup alpha3 = rootGroup.addGroup("alpha3");

  int rank = dims.length;
  NhDimension[] nhDims = new NhDimension[rank];
  for (int ii = 0; ii < rank; ii++) {
    nhDims[ii] = rootGroup.addDimension(
      String.format("dim%02d", ii),
      dims[ii]);
  }

  int numAttr = 2;
  // NetCDF attributes all have rank == 1, or be a simple String.
  // For nhType == TP_CHAR, must have isVlen = false.
  if (rank <= 1) {
    for (int ii = 0; ii < numAttr; ii++) {
      rootGroup.addAttribute(
        String.format("globAttr%04d", ii),   // attrName
        nhType,
        allData);
    }
  }
  if (numAttr > 0 && rank == 0) {
    rootGroup.addAttribute(
      "globTextAttr",
      NhVariable.TP_STRING_VAR,
      "globTextValue");

    rootGroup.addAttribute(
      "emptyAttr",
      NhVariable.TP_SHORT,
      new short[0]);

    rootGroup.addAttribute(
      "nullAttr",
      NhVariable.TP_SHORT,
      null);
  }


// xxx Definitely weird.
//
// String valued attributes must be encoded with isVstring=false.
// If a String valued attribute is encoded as isVstring=true,
// ncdump fails:
//   ncdump: tempa.nc: Can't open HDF5 attribute
// Even though h5dump reads it just fine.
//
// But if the attr value is an array of String, it should be
// encoded as isVstring=true.  If it's encoded as isVstring=false,
// then ncdump may look for null termination.  Example:
//       string testVar0000:varAttr0000 = "0", "1a", "2ab?\177" ;
//                                                   * should be "2ab".
//
// One alternative would be to store add null term to all the
// fixed len strings, but that increases the storage len and
// the Unidata reader then returns the incremented length.
 


  int numVar = 2;
  NhVariable[] testVars = new NhVariable[ numVar];
  for (int ivar = 0; ivar < numVar; ivar++) {
    testVars[ivar] = testDefineVariable(
      numAttr,
      alpha2,                            // parentGroup
      ivar,
      String.format("testVar%04d", ivar),  // varName
      nhType,
      nhDims,
      chunks,
      compressLevel,
      allData,
      fillValue);
  }

  hfile.endDefine();

  for (int ivar = 0; ivar < numVar; ivar++) {
    if (chunks == null) {
      testVars[ivar].writeData( null, allData, useLinear);
      // startIxs = null
    }

    else {             // else use chunks
      int[] startIxs = new int[rank];
      boolean allDone = false;
      while (! allDone) {

        // Chunks at the edge may be partially valid
        int[] testDims = new int[rank];
        for (int ii = 0; ii < rank; ii++) {
          testDims[ii] = Math.min( chunks[ii], dims[ii] - startIxs[ii]);
        }

        Object testData = null;
        if (rank == 0)
          throwerr("Tnetcdfa: chunking not ok for scalars");
        if (nhType == NhVariable.TP_FLOAT) {
          float fillValueFloat = 0;
          if (fillValue != null)
            fillValueFloat = ((Float) fillValue).floatValue();

          if (useLinear || rank == 1) {
            int totLen = 1;
            for (int ilen : testDims) {
              totLen *= ilen;
            }
            if (useArray) {
              ArrayFloat.D1 arr = new ArrayFloat.D1( totLen);
              for (int ia = 0; ia < totLen; ia++) {
                arr.set( ia, ia);
              }
              testData = arr;
            }
            else {
              float[] vals = new float[ totLen];
              for (int ia = 0; ia < totLen; ia++) {
                vals[ia] = ia;
              }
              testData = vals;
            }
          }
          else if (rank == 2) {
            if (useArray) {
              ArrayFloat.D2 arr = new ArrayFloat.D2(
                testDims[0], testDims[1]);
              for (int ia = 0; ia < testDims[0]; ia++) {
                for (int ib = 0; ib < testDims[1]; ib++) {
                  arr.set( ia, ib, 10*ia + ib);
                }
              }
              testData = arr;
            }
            else {
              float[][] vals = new float[ testDims[0]][ testDims[1]];
              for (int ia = 0; ia < testDims[0]; ia++) {
                for (int ib = 0; ib < testDims[1]; ib++) {
                  vals[ia][ib] = 10*ia + ib;
                }
              }
              testData = vals;
            }
          }
          else if (rank == 3) {
            if (useArray) {
              ArrayFloat.D3 arr = new ArrayFloat.D3(
                testDims[0], testDims[1], testDims[2]);
              for (int ia = 0; ia < testDims[0]; ia++) {
                for (int ib = 0; ib < testDims[1]; ib++) {
                  for (int ic = 0; ic < testDims[2]; ic++) {
                    arr.set( ia, ib, ic, 100*ia + 10*ib + ic);
                  }
                }
              }
              testData = arr;
            }
            else {
              float[][][] vals = new float[ testDims[0]][ testDims[1]]
                [ testDims[2]];
              for (int ia = 0; ia < testDims[0]; ia++) {
                for (int ib = 0; ib < testDims[1]; ib++) {
                  for (int ic = 0; ic < testDims[2]; ic++) {
                    vals[ia][ib][ic] = 100*ia + 10*ib + ic;
                  }
                }
              }
              testData = vals;
            }
          }

          // Tests for chunk lens < dims not ok for higher ranks
          else {
            if (! testEqualInts( dims, chunks))
              throwerr("Tnetcdfa: chunks < dims not ok for higher ranks");
            testData = allData;
          }

        } // if nhType == NhVariable.TP_FLOAT

        // Tests for chunk lens < dims not ok for other types
        else {
          if (! testEqualInts( dims, chunks))
            throwerr("Tnetcdfa: chunks < dims not ok for other types");
          testData = allData;
        }

        testVars[ivar].writeData(
          startIxs,
          testData,
          useLinear);

        // Increment startIxs
        for (int ii = rank - 1; ii >= 0; ii--) {
          startIxs[ii] += chunks[ii];
          if (startIxs[ii] < dims[ii]) break;
          startIxs[ii] = 0;
          if (ii == 0) allDone = true;
        }
      }
    } // else use chunks
  } // for ivar

  hfile.close();
} // end testOne



// Weird.  For chars ...
// For a variable, netcdf stores array of string len 1.
// For an attribute, netcdf stores array of signed byte,
//   for both nc_put_attr_schar / uchar / ubyte.
//
//    call:                 parm:      ncdump
//
//    nc_def_var NC_CHAR 3  "abcdef..."      vara = "abc" ;
//
//    nc_put_attr_schar     "abc"      :sattrNamea = 97b, 98b, 99b ;
//    nc_put_attr_uchar     "abc"      :sattrNamea = 97b, 98b, 99b ;
//    nc_put_attr_ubyte     "abc"      :sattrNamea = 97b, 98b, 99b ;
//
//    
//       DATASET "vara" {
//          DATATYPE  H5T_STRING {
//                STRSIZE 1;
//                STRPAD H5T_STR_NULLTERM;
//                CSET H5T_CSET_ASCII;
//                CTYPE H5T_C_S1;
//             }
//          DATASPACE  SIMPLE { ( 3 ) / ( 3 ) }
//          DATA {
//          (0): "a", "b", "c"
//          }
// 
//
//       ATTRIBUTE "attrSchar" {
//          DATATYPE  H5T_STD_I8LE
//          DATASPACE  SIMPLE { ( 3 ) / ( 3 ) }
//          DATA {
//          (0): 97, 98, 99
//          }
//       }
//       ATTRIBUTE "attrUbyte" {
//          DATATYPE  H5T_STD_I8LE
//          DATASPACE  SIMPLE { ( 3 ) / ( 3 ) }
//          DATA {
//          (0): 97, 98, 99
//          }
//       }
//       ATTRIBUTE "attrUchar" {
//          DATATYPE  H5T_STD_I8LE
//          DATASPACE  SIMPLE { ( 3 ) / ( 3 ) }
//          DATA {
//          (0): 97, 98, 99
//          }
//       }







static NhVariable testDefineVariable(
  int numAttr,
  NhGroup parentGroup,
  int ivar,
  String varName,
  int nhType,
  NhDimension[] nhDims,   // varDims
  int[] chunks,
  int compressLevel,      // compression level: 0==none, 1 - 9
  Object allData,
  Object fillValue)
throws NhException
{
  int rank = nhDims.length;

  NhVariable vara = parentGroup.addVariable(
    varName,             // varName
    nhType,              // nhType
    nhDims,              // varDims
    chunks,
    fillValue,
    compressLevel);

  // NetCDF attributes must have rank == 1, or be a simple String.
  if (ivar == 0 && rank <= 1) {
    for (int ii = 0; ii < numAttr; ii++) {
      vara.addAttribute(
        String.format("varAttr%04d", ii),   // attrName
        nhType,
        allData);
    }
  }

  if (numAttr > 0 && rank == 0) {
    vara.addAttribute(
      "varaTextAttr",
      NhVariable.TP_STRING_VAR,
      "varaTextValue");
  }

  prtf("Tnetcdfa: parentGroup: %s", parentGroup);
  prtf("Tnetcdfa: vara: %s", vara);
  return vara;

} // end testDefineVariable





static int[] parseInts( String msg, String stg)
throws NhException
{
  if (stg == null) throwerr("stg is null");
  int[] vals;
  String[] stgs = stg.split(",");
  vals = new int[ stgs.length];
  for (int ii = 0; ii < stgs.length; ii++) {
    vals[ii] = Integer.parseInt( stgs[ii]);
    if (vals[ii] < 1) badparms(
      "invalid " + msg + ": " + vals[ii]);
  }
  return vals;
}





static boolean testEqualInts(
  int[] avals,
  int[] bvals)
{
  boolean bres = true;
  for (int ii = 0; ii < avals.length; ii++) {
    if (avals[ii] != bvals[ii]) bres = false;
  }
  return bres;
}




/**
 * Formats an array of ints.
 */

static String formatInts(
  int[] vals)
{
  String res = "";
  if (vals == null) res = "(null)";
  else {
    for (int ii = 0; ii < vals.length; ii++) {
      if (ii > 0) res += " ";
      res += vals[ii];
    }
  }
  return res;
}



static boolean parseBoolean( String msg, String stg)
throws NhException
{
  boolean bres = false;
  if (stg.equals("false")) bres = false;
  else if (stg.equals("true")) bres = true;
  else badparms("unknown value for " + msg + ": " + stg);
  return bres;
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
