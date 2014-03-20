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

import java.util.Arrays;

import edu.ucar.ral.nujan.hdf.HdfFileWriter;
import edu.ucar.ral.nujan.hdf.HdfGroup;
import edu.ucar.ral.nujan.hdf.HdfException;


// Create test data.
//   genHdfData is used by Thdfa.java and netcdf/netcdfTest/Tnetcdfa.java.
//   genNhData is a thin wrapper to interpret type strings like "short",
//     and is used by netcdf/TestNetcdfa.java.


public class GenData {






public static Object genHdfData(
  boolean useLinear,
  int dtype,                // one of HdfGroup.DTYPE_*
  int stgFieldLen,
  HdfGroup refGroup,        // group to use for references
  int[] dims,
  int ival)                 // origin 0.  e.g. 1000*ia + 100*ib + 10*ic + id
throws HdfException
{
  Object vdata = genNonLinear(
    dtype,
    stgFieldLen,
    refGroup,        // group to use for references
    dims,
    ival);           // origin 0.  e.g. 1000*ia + 100*ib + 10*ic + id

  if (useLinear) vdata = mkLinear( dtype, dims, vdata);

  return vdata;
}







public static Object mkLinear(
  int dtype,
  int[] dims,
  Object vdata)
throws HdfException
{

  int dprod = 1;
  for (int dim : dims) {
    dprod *= dim;
  }

  Object linData = null;
  if (dtype == HdfGroup.DTYPE_UFIXED08) linData = new byte[dprod];
  else if (dtype == HdfGroup.DTYPE_SFIXED08) linData = new byte[dprod];
  else if (dtype == HdfGroup.DTYPE_FIXED16) linData = new short[dprod];
  else if (dtype == HdfGroup.DTYPE_FIXED32) linData = new int[dprod];
  else if (dtype == HdfGroup.DTYPE_FIXED64) linData = new long[dprod];
  else if (dtype == HdfGroup.DTYPE_FLOAT32) linData = new float[dprod];
  else if (dtype == HdfGroup.DTYPE_FLOAT64) linData = new double[dprod];
  else if (dtype == HdfGroup.DTYPE_STRING_VAR) linData = new String[dprod];
  else throwerr("unsupported dtype: " + HdfGroup.dtypeNames[dtype]);

  int rank = dims.length;
  int[] curIxs = new int[rank];
  int curLevel = 0;
  int linIx = 0;
  linIx = appendLinear( curLevel, curIxs, vdata, linIx, linData);
  if (linIx != dprod) throwerr("linIx mismatch");
  return linData;
}













private static int appendLinear(
  int curLevel,
  int[] curIxs,
  Object vdata,
  int linIx,
  Object linData)
throws HdfException
{
  int rank = curIxs.length;
  if (curLevel < rank - 1) {
    if (! (vdata instanceof Object[])) throwerr("wrong type");
    Object[] objVec = (Object[]) vdata;
    for (int ii = 0; ii < objVec.length; ii++) {
      curIxs[curLevel] = ii;
      linIx = appendLinear(                  // recursion
        curLevel + 1,
        curIxs,
        objVec[ii],
        linIx,
        linData);
    }
  }

  else {
    if (vdata instanceof byte[]) {
      if (! (linData instanceof byte[])) throwerr("type mismatch");
      byte[] vvec = (byte[]) vdata;
      byte[] lvec = (byte[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else if (vdata instanceof short[]) {
      if (! (linData instanceof short[])) throwerr("type mismatch");
      short[] vvec = (short[]) vdata;
      short[] lvec = (short[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else if (vdata instanceof int[]) {
      if (! (linData instanceof int[])) throwerr("type mismatch");
      int[] vvec = (int[]) vdata;
      int[] lvec = (int[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else if (vdata instanceof long[]) {
      if (! (linData instanceof long[])) throwerr("type mismatch");
      long[] vvec = (long[]) vdata;
      long[] lvec = (long[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else if (vdata instanceof float[]) {
      if (! (linData instanceof float[])) throwerr("type mismatch");
      float[] vvec = (float[]) vdata;
      float[] lvec = (float[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else if (vdata instanceof double[]) {
      if (! (linData instanceof double[])) throwerr("type mismatch");
      double[] vvec = (double[]) vdata;
      double[] lvec = (double[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else if (vdata instanceof String[]) {
      if (! (linData instanceof String[])) throwerr("type mismatch");
      String[] vvec = (String[]) vdata;
      String[] lvec = (String[]) linData;
      for (int ii = 0; ii < vvec.length; ii++) {
        lvec[linIx++] = vvec[ii];
      }
    }
    else throwerr("unknown type: %s", vdata.getClass());
  }
  return linIx;
} // end appendLinear









private static Object genNonLinear(
  int dtype,
  int stgFieldLen,
  HdfGroup refGroup,        // group to use for references
  int[] dims,
  int ival)                 // origin 0.  e.g. 1000*ia + 100*ib + 10*ic + id
throws HdfException
{
  String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  Object vdata = null;

  if (dims.length == 0) {
    ival = 100;                                // force scalers to be 100
    if (dtype == HdfGroup.DTYPE_SFIXED08
      || dtype == HdfGroup.DTYPE_UFIXED08)
    {
      vdata = new Byte( (byte) (0xff & ival));
    }
    else if (dtype == HdfGroup.DTYPE_FIXED16)
      vdata = new Short( (short) (0xffff & ival));
    else if (dtype == HdfGroup.DTYPE_FIXED32) vdata = new Integer( ival);
    else if (dtype == HdfGroup.DTYPE_FIXED64) vdata = new Long( ival);
    else if (dtype == HdfGroup.DTYPE_FLOAT32) vdata = new Float( ival);
    else if (dtype == HdfGroup.DTYPE_FLOAT64) vdata = new Double( ival);
    else if (dtype == HdfGroup.DTYPE_TEST_CHAR)
      vdata = new Character( alphabet.charAt( ival % alphabet.length()));
    else if (dtype == HdfGroup.DTYPE_STRING_FIX) {
      if (stgFieldLen <= 0) throwerr("stgFieldLen <= 0");
      String stg = "" + ival;
      int deltaLen = stgFieldLen - stg.length();
      if (deltaLen > 0) stg += alphabet.substring( 0, deltaLen);
      else if (deltaLen < 0) stg = stg.substring( stg.length() - stgFieldLen);
      vdata = stg;
    }
    else if (dtype == HdfGroup.DTYPE_STRING_VAR) {
      if (stgFieldLen != 0) throwerr("stgFieldLen != 0");
      vdata = "abc";
    }
    else if (dtype == HdfGroup.DTYPE_REFERENCE
      || dtype == HdfGroup.DTYPE_COMPOUND)
    {
      vdata = refGroup;
    }
    else throwerr("unsupported dtype: " + HdfGroup.dtypeNames[dtype]);
  } // if scalar

  else if (dims.length == 1
    && dtype != HdfGroup.DTYPE_REFERENCE
    && dtype != HdfGroup.DTYPE_COMPOUND)
  {
    if (dtype == HdfGroup.DTYPE_SFIXED08
      || dtype == HdfGroup.DTYPE_UFIXED08)
    {
      byte[] vals = new byte[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = (byte) (0xff & (ival + ii));
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FIXED16) {
      short[] vals = new short[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = (short) (0xffff & (ival + ii));
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FIXED32) {
      int[] vals = new int[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FIXED64) {
      long[] vals = new long[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT32) {
      float[] vals = new float[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT64) {
      double[] vals = new double[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_TEST_CHAR) {
      char[] vals = new char[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = alphabet.charAt( (ival + ii) % alphabet.length());
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_STRING_FIX
      || dtype == HdfGroup.DTYPE_STRING_VAR)
    {
      String[] vals = new String[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = "stg" + (ival + ii);
      }
      vdata = vals;
    }
    else throwerr("unsupported dtype: " + HdfGroup.dtypeNames[dtype]);
  } // else if dims.len==1 && numeric

  else {
    // Chop off the first dimension and recurse.
    int nn = dims[0];
    int[] subDims = Arrays.copyOfRange( dims, 1, dims.length);
    Object[] objs = new Object[nn];
    for (int ii = 0; ii < nn; ii++) {
      objs[ii] = genNonLinear(               // recursion
        dtype,
        stgFieldLen,
        refGroup,
        subDims,
        10 * (ival + ii));
    }
    vdata = objs;
  }

  return vdata;
} // end genNonLinear





public static Object genFillValue(
  int dtype,
  int stgFieldLen)

throws HdfException
{
  Object fillValue = null;
  if (dtype == HdfGroup.DTYPE_SFIXED08)
    fillValue = new Byte( (byte) 255);
  else if (dtype == HdfGroup.DTYPE_UFIXED08)
    fillValue = new Byte( (byte) 255);
  else if (dtype == HdfGroup.DTYPE_FIXED16)
    fillValue = new Short( (short) -9999);
  else if (dtype == HdfGroup.DTYPE_FIXED32)
    fillValue = new Integer( -999999);
  else if (dtype == HdfGroup.DTYPE_FIXED64)
    fillValue = new Long( -999999);
  else if (dtype == HdfGroup.DTYPE_FLOAT32)
    fillValue = new Float( -999999);
  else if (dtype == HdfGroup.DTYPE_FLOAT64)
    fillValue = new Double( -999999);
  else if (dtype == HdfGroup.DTYPE_TEST_CHAR)
    fillValue = new Character('x');
  else if (dtype == HdfGroup.DTYPE_STRING_FIX
    || dtype == HdfGroup.DTYPE_STRING_VAR)
  {
    String fillStg = "999";
    if (stgFieldLen > 0 && fillStg.length() > stgFieldLen)
      fillStg = fillStg.substring( 0, stgFieldLen);
    fillValue = fillStg;
  }
  else if (dtype == HdfGroup.DTYPE_REFERENCE
    || dtype == HdfGroup.DTYPE_COMPOUND)
  {
    fillValue = null;
  }
  else throwerr("unsupported dtype: " + HdfGroup.dtypeNames[dtype]);
  return fillValue;
}




static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class
