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


package edu.ucar.ral.nujan.hdfTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import edu.ucar.ral.nujan.hdf.HdfFileWriter;
import edu.ucar.ral.nujan.hdf.HdfGroup;
import edu.ucar.ral.nujan.hdf.HdfException;


/**
 * Main test program for testing synthetic data files.
 * Tests fixed08, fixed16, fixed32, fixed64, float32, float64,
 * string, or reference type, with any number of dimensions.
 */


public class Thdfa {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs         <int>");
  prtf("  -dtype        one of: fixed08 ufixed08 fixed16/32/64");
  prtf("                        float32/64");
  prtf("                        stringN vstring");
  prtf("                           where N is the max string len without");
  prtf("                           null termination");
  prtf("                        reference compound");
  prtf("  -dims         <int,int,...>   or \"scalar\" if a scalar");
  prtf("  -chunks       <int,int,...>   or \"contiguous\" if contiguous");
  prtf("  -compress     compression level: 0==none, 1 - 9");
  prtf("  -utcModTime   either yyyy-mm-dd or yyyy-mm-ddThh:mm:ss");
  prtf("                or 0, meaning use the current time");
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
throws HdfException
{
  int bugs = -1;
  int dtype = -1;
  int stgFieldLen = 0;
  int[] dims = null;
  int[] chunks = null;
  String chunkedStg = null;
  int compressLevel = -1;
  long utcModTime = -1;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-dtype")) {
      if (val.equals("sfixed08")) dtype = HdfGroup.DTYPE_SFIXED08;
      else if (val.equals("ufixed08")) dtype = HdfGroup.DTYPE_UFIXED08;
      else if (val.equals("fixed16")) dtype = HdfGroup.DTYPE_FIXED16;
      else if (val.equals("fixed32")) dtype = HdfGroup.DTYPE_FIXED32;
      else if (val.equals("fixed64")) dtype = HdfGroup.DTYPE_FIXED64;
      else if (val.equals("float32")) dtype = HdfGroup.DTYPE_FLOAT32;
      else if (val.equals("float64")) dtype = HdfGroup.DTYPE_FLOAT64;
      else if (val.startsWith("string")) {
        dtype = HdfGroup.DTYPE_STRING_FIX;
        if (val.length() <= 6)
          badparms("must spec stgFieldLen after \"string\"");
        stgFieldLen = Integer.parseInt( val.substring(6));
      }
      else if (val.equals("vstring")) dtype = HdfGroup.DTYPE_STRING_VAR;
      else if (val.equals("reference")) dtype = HdfGroup.DTYPE_REFERENCE;
      else if (val.equals("compound")) dtype = HdfGroup.DTYPE_COMPOUND;
      else badparms("unknown dtype: " + val);
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
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }

  if (bugs < 0) badparms("missing parm: -bugs");
  if (dtype < 0) badparms("missing parm: -dtype");
  if (dims == null) badparms("missing parm: -dims");
  if (chunks == null) badparms("missing parm: -chunks");
  if (compressLevel < 0) badparms("missing parm: -compress");
  if (utcModTime < 0) badparms("missing parm: -utcModTime");
  if (outFile == null) badparms("missing parm: -outFile");

  if (chunks.length == 0) chunks = null;
  int rank = dims.length;
  prtf("Thdfa: bugs: %d", bugs);
  prtf("Thdfa: dtype: %s", HdfGroup.dtypeNames[dtype]);
  prtf("Thdfa: stgFieldLen: %d", stgFieldLen);
  prtf("Thdfa: rank: %d", rank);
  prtf("Thdfa: dims: %s", formatInts( dims));
  prtf("Thdfa: chunks: %s", formatInts( chunks));
  prtf("Thdfa: compress: %d", compressLevel);

  SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  utcSdf.setTimeZone( new SimpleTimeZone( 0, "UTC"));
  prtf("Thdfa: utcModTime: %d  %s", utcModTime, utcSdf.format( utcModTime));

  prtf("Thdfa: outFile: \"%s\"", outFile);

  HdfFileWriter hfile = new HdfFileWriter(
    outFile, HdfFileWriter.OPT_ALLOW_OVERWRITE,
    bugs, utcModTime);

  HdfGroup rootGroup = hfile.getRootGroup();

  Object fillValue = GenData.genFillValue( dtype, stgFieldLen);

  if (bugs >= 1) {
    String msg = "Thdfa: fillValue: ";
    if (fillValue == null) msg += "null";
    else if (fillValue instanceof String)
      msg += String.format("\"%s\"  class: String", fillValue);
    else
      msg += String.format("%s  class: %s",
        fillValue,
        fillValue.getClass().getName());
    prtf( msg);
  }

  HdfGroup alpha1 = rootGroup.addGroup("alpha1");
  HdfGroup alpha2 = rootGroup.addGroup("alpha2");
  HdfGroup alpha3 = rootGroup.addGroup("alpha3");

  int numVar = 2;
  HdfGroup[] testVars = new HdfGroup[ numVar];
  Object allData = GenData.genHdfData(
    dtype, stgFieldLen, rootGroup, dims, 0);
  for (int ivar = 0; ivar < numVar; ivar++) {
    testVars[ivar] = testDefineVariable(
      compressLevel,
      alpha2,                              // parentGroup
      ivar,
      String.format("testVar%04d", ivar),  // varName
      dtype,
      stgFieldLen,
      dims,
      chunks,
      allData,
      fillValue);
  }

  prtf("Thdfa: rootGroup: %s", rootGroup);
  prtf("Thdfa: alpha2: %s", alpha2);

  hfile.endDefine();

  for (int ivar = 0; ivar < numVar; ivar++) {
    if (chunks == null) {
      testVars[ivar].writeData(
        null,          // startIxs
        allData);
    }

    else {             // else use chunks
      int[] startIxs = new int[rank];
      boolean allDone = false;
      while (! allDone) {

        // Chunks at the edge may be partially valid
        int[] validDims = new int[rank];
        for (int ii = 0; ii < rank; ii++) {
          validDims[ii] = Math.min( chunks[ii], dims[ii] - startIxs[ii]);
        }

        Object chunkData = null;
        if (rank == 0) throwerr("Thdfa: chunking not ok for scalars");
        if (dtype == HdfGroup.DTYPE_FLOAT32) {
          float fillValueFloat = 0;
          if (fillValue != null)
            fillValueFloat = ((Float) fillValue).floatValue();

          if (rank == 1) {
            float[] vals = new float[ chunks[0]];
            for (int ia = 0; ia < chunks[0]; ia++) {
              float val = fillValueFloat;
              if (ia < validDims[0])
                val = startIxs[0] + ia;
              vals[ia] = val;
            }
            chunkData = vals;
          }
          else if (rank == 2) {
            float[][] vals = new float[ chunks[0]][ chunks[1]];
            for (int ia = 0; ia < chunks[0]; ia++) {
              for (int ib = 0; ib < chunks[1]; ib++) {
                float val = fillValueFloat;
                if (ia < validDims[0]
                  && ib < validDims[1])
                {
                  val = 10 * (startIxs[0] + ia) + startIxs[1] + ib;
                }
                vals[ia][ib] = val;
              }
            }
            chunkData = vals;
          }
          else if (rank == 3) {
            float[][][] vals = new float[ chunks[0]][ chunks[1]][ chunks[2]];
            for (int ia = 0; ia < chunks[0]; ia++) {
              for (int ib = 0; ib < chunks[1]; ib++) {
                for (int ic = 0; ic < chunks[2]; ic++) {
                  float val = fillValueFloat;
                  if (ia < validDims[0]
                    && ib < validDims[1]
                    && ic < validDims[2])
                  {
                    val =
                      100 * (startIxs[0] + ia)
                      + 10 * (startIxs[1] + ib)
                      + startIxs[2] + ic;
                  }
                  vals[ia][ib][ic] = val;
                }
              }
            }
            chunkData = vals;
          }

          // Tests for chunk lens < dims not ok for higher ranks
          else {
            if (! testEqualInts( dims, chunks))
              throwerr("Thdfa: chunks < dims not ok for higher ranks");
            chunkData = allData;
          }
        } // if dtype == HdfGroup.DTYPE_FLOAT32

        // Tests for chunk lens < dims not ok for other types
        else {
          if (! testEqualInts( dims, chunks))
            throwerr("Thdfa: chunks < dims not ok for other types");
          chunkData = allData;
        }

        testVars[ivar].writeData(
          startIxs,
          chunkData);

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
}










static HdfGroup testDefineVariable(
  int compressLevel,
  HdfGroup parentGroup,
  int ivar,
  String varName,
  int dtype,
  int stgFieldLen,        // string length, without null termination
  int[] dims,             // varDims
  int[] chunks,           // chunkLens.  May be null.
  Object allData,
  Object fillValue)
throws HdfException
{
  int rank = dims.length;

  HdfGroup vara = parentGroup.addVariable(
    varName,                   // varName
    dtype,                     // dtype
    stgFieldLen,               // string length, without null termination
    dims,                      // varDims
    chunks,
    fillValue,
    compressLevel);            // compressionLevel

  int numAttr = 2;
  if (ivar == 0) {
    for (int ii = 0; ii < numAttr; ii++) {
      vara.addAttribute(
        String.format("testAttr%04d", ii),   // attrName
        dtype,
        stgFieldLen,
        allData,
        false);             // isVlen
    }

    // Test 0-length attr
    if (numAttr > 0 && dims.length == 0) {
      Object emptyData = null;
      if (dtype == HdfGroup.DTYPE_SFIXED08) emptyData = new byte[0];
      else if (dtype == HdfGroup.DTYPE_UFIXED08) emptyData = new byte[0];
      else if (dtype == HdfGroup.DTYPE_FIXED16) emptyData = new short[0];
      else if (dtype == HdfGroup.DTYPE_FIXED32) emptyData = new int[0];
      else if (dtype == HdfGroup.DTYPE_FIXED64) emptyData = new long[0];
      else if (dtype == HdfGroup.DTYPE_FLOAT32) emptyData = new float[0];
      else if (dtype == HdfGroup.DTYPE_FLOAT64) emptyData = new double[0];
      else if (dtype == HdfGroup.DTYPE_TEST_CHAR) emptyData = new char[0];
      else if (dtype == HdfGroup.DTYPE_STRING_FIX) emptyData = new String[0];
      else if (dtype == HdfGroup.DTYPE_STRING_VAR) emptyData = new String[0];
      else if (dtype == HdfGroup.DTYPE_REFERENCE) emptyData = new HdfGroup[0];
      else throwerr("unknown dtype");

      vara.addAttribute(
        "testEmptyAttr",    // attrName
        dtype,
        0,                  // stgFieldLen
        emptyData,
        false);             // isVlen
    }
  }

  // If 2 dimensions, add VLEN attribute.
  // But vlen not allowed for DTYPE_STRING_VAR.
  // We cannot use either STRING_VAR or STRING_FIX for a
  // VLEN attr since the MsgAttribute call to
  //     HdfUtil.getDtypeAndDims( isVlen, attrValue);
  // will always return STRING_VAR for a string attrValue.

  if (rank == 2
    && dtype != HdfGroup.DTYPE_STRING_FIX
    && dtype != HdfGroup.DTYPE_STRING_VAR
    && dtype != HdfGroup.DTYPE_COMPOUND)
  {
    Object vlenData = null;
    if (dtype == HdfGroup.DTYPE_SFIXED08
      || dtype == HdfGroup.DTYPE_UFIXED08)
    {
      vlenData = new byte[][] {
        { (byte) 111, (byte) 112, (byte) 113 },
        { (byte) 111, (byte) 112 },
        { (byte) 111, (byte) 112, (byte) 113, (byte) 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FIXED16) {
      vlenData = new short[][] {
        { (short) 111, (short) 112, (short) 113 },
        { (short) 111, (short) 112 },
        { (short) 111, (short) 112, (short) 113, (short) 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FIXED32) {
      vlenData = new int[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FIXED64) {
      vlenData = new long[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT32) {
      vlenData = new float[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT64) {
      vlenData = new double[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }

    else if (dtype == HdfGroup.DTYPE_REFERENCE) {
      vlenData = new HdfGroup[][] {
        { vara, vara, vara },
        { vara, vara },
        { vara, vara, vara, vara }};
    }
    else badparms("unknown dtype: " + dtype);

    vara.addAttribute(
      "vlenAttr",
      dtype,
      stgFieldLen,
      vlenData,
      true);           // isVlen
  } // if rank == 2 and not STRING_*

  // If rank 1 reference, test COMPOUND.
  if (rank == 1 && dtype == HdfGroup.DTYPE_COMPOUND) {
    vara.addAttribute(
      "compoundAttr",
      dtype,
      stgFieldLen,
      new HdfGroup[] {
        vara,
        vara,
        vara},
      false);          // isVlen
  }

  prtf("Thdfa: defined vara: %s", vara);
  return vara;

} // end testDefineVariable





static int[] parseInts( String msg, String stg)
throws HdfException
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



static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}




static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class
