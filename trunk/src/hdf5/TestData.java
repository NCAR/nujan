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


package hdfnetTest;

import java.util.Arrays;

import hdfnet.HdfFileWriter;
import hdfnet.HdfGroup;
import hdfnet.HdfException;


// Create test data.
//   genHdfData is used by Testa.java and netcdf/TestNetcdfa.java.
//   genNhData is a thin wrapper to interpret type strings like "short",
//     and is used by netcdf/TestNetcdfa.java.


public class TestData {



public static Object genHdfData(
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
      vdata = "" + ival + alphabet.substring( 0, ival % 4);
    }
    else if (dtype == HdfGroup.DTYPE_REFERENCE
      || dtype == HdfGroup.DTYPE_COMPOUND)
    {
      vdata = refGroup;
    }
    else throwerr("unsupported dtype: " + HdfGroup.dtypeNames[dtype]);
  }

  else if (dims.length == 1
    && dtype != HdfGroup.DTYPE_STRING_FIX
    && dtype != HdfGroup.DTYPE_STRING_VAR
    && dtype != HdfGroup.DTYPE_REFERENCE
    && dtype != HdfGroup.DTYPE_COMPOUND)
  {
    if (dtype == HdfGroup.DTYPE_SFIXED08
      || dtype == HdfGroup.DTYPE_UFIXED08)
    {
      byte[] vals = new byte[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = (byte) (0xff & (10 * ival + ii));
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FIXED16) {
      short[] vals = new short[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = (short) (0xffff & (10 * ival + ii));
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FIXED32) {
      int[] vals = new int[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = 10 * ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FIXED64) {
      long[] vals = new long[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = 10 * ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT32) {
      float[] vals = new float[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = 10 * ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT64) {
      double[] vals = new double[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = 10 * ival + ii;
      }
      vdata = vals;
    }
    else if (dtype == HdfGroup.DTYPE_TEST_CHAR) {
      char[] vals = new char[dims[0]];
      for (int ii = 0; ii < dims[0]; ii++) {
        vals[ii] = alphabet.charAt( (10 * ival + ii) % alphabet.length());
      }
      vdata = vals;
    }
    else throwerr("unsupported dtype: " + HdfGroup.dtypeNames[dtype]);
  } // else if dims.len==1 && numeric

  else {
    // Chop off the first dimension
    int nn = dims[0];
    int[] subDims = Arrays.copyOfRange( dims, 1, dims.length);
    Object[] objs = new Object[nn];
    for (int ii = 0; ii < nn; ii++) {
      objs[ii] = genHdfData(
        dtype,
        stgFieldLen,
        refGroup,
        subDims,
        10 * ival + ii);       // origin 0
    }
    vdata = objs;
  }

  return vdata;
} // end genHdfData





public static Object genFillValue(
  int dtype,
  int stgFieldLen)

throws HdfException
{
  Object fillValue = null;
  if (dtype == HdfGroup.DTYPE_SFIXED08) fillValue = new Byte( (byte) 99);
  else if (dtype == HdfGroup.DTYPE_UFIXED08) fillValue = new Byte( (byte) 99);
  else if (dtype == HdfGroup.DTYPE_FIXED16) fillValue = new Short( (short) 999);
  else if (dtype == HdfGroup.DTYPE_FIXED32) fillValue = new Integer( 999);
  else if (dtype == HdfGroup.DTYPE_FIXED64) fillValue = new Long( 999);
  else if (dtype == HdfGroup.DTYPE_FLOAT32) fillValue = new Float( 999);
  else if (dtype == HdfGroup.DTYPE_FLOAT64) fillValue = new Double( 999);
  else if (dtype == HdfGroup.DTYPE_TEST_CHAR) fillValue = new Character('x');
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








//xxx del:
///public static Object[] genHdfData(
///  int dtype,
///  int stgFieldLen,
///  int[] dims,
///  HdfGroup refGroup)              // group to use for references
///throws HdfException
///{
///  int rank = dims.length;
///  Object vdata = null;
///  Object fillValue = null;
///
///  if (dtype == HdfGroup.DTYPE_FIXED08) {
///    fillValue = new Byte( (byte) 99);
///    if (rank == 0) vdata = new Byte( mkByte0( dims, 0, 0));
///    else if (rank == 1) vdata = mkByte1( dims, 0, 0);
///    else if (rank == 2) vdata = mkByte2( dims, 0, 0);
///    else if (rank == 3) vdata = mkByte3( dims, 0, 0);
///    else if (rank == 4) vdata = mkByte4( dims, 0, 0);
///    else if (rank == 5) vdata = mkByte5( dims, 0, 0);
///    else if (rank == 6) vdata = mkByte6( dims, 0, 0);
///    else if (rank == 7) vdata = mkByte7( dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_FIXED16) {
///    fillValue = new Short( (short) 999);
///    if (rank == 0) vdata = new Short( mkShort0( dims, 0, 0));
///    else if (rank == 1) vdata = mkShort1( dims, 0, 0);
///    else if (rank == 2) vdata = mkShort2( dims, 0, 0);
///    else if (rank == 3) vdata = mkShort3( dims, 0, 0);
///    else if (rank == 4) vdata = mkShort4( dims, 0, 0);
///    else if (rank == 5) vdata = mkShort5( dims, 0, 0);
///    else if (rank == 6) vdata = mkShort6( dims, 0, 0);
///    else if (rank == 7) vdata = mkShort7( dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_FIXED32) {
///    fillValue = new Integer(999);
///    if (rank == 0) vdata = new Integer( mkInt0( dims, 0, 0));
///    else if (rank == 1) vdata = mkInt1( dims, 0, 0);
///    else if (rank == 2) vdata = mkInt2( dims, 0, 0);
///    else if (rank == 3) vdata = mkInt3( dims, 0, 0);
///    else if (rank == 4) vdata = mkInt4( dims, 0, 0);
///    else if (rank == 5) vdata = mkInt5( dims, 0, 0);
///    else if (rank == 6) vdata = mkInt6( dims, 0, 0);
///    else if (rank == 7) vdata = mkInt7( dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_FIXED64) {
///    fillValue = new Long(999);
///    if (rank == 0) vdata = new Long( mkLong0( dims, 0, 0));
///    else if (rank == 1) vdata = mkLong1( dims, 0, 0);
///    else if (rank == 2) vdata = mkLong2( dims, 0, 0);
///    else if (rank == 3) vdata = mkLong3( dims, 0, 0);
///    else if (rank == 4) vdata = mkLong4( dims, 0, 0);
///    else if (rank == 5) vdata = mkLong5( dims, 0, 0);
///    else if (rank == 6) vdata = mkLong6( dims, 0, 0);
///    else if (rank == 7) vdata = mkLong7( dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_FLOAT32) {
///    fillValue = new Float(999);
///    if (rank == 0) vdata = new Float( mkFloat0( dims, 0, 0));
///    else if (rank == 1) vdata = mkFloat1( dims, 0, 0);
///    else if (rank == 2) vdata = mkFloat2( dims, 0, 0);
///    else if (rank == 3) vdata = mkFloat3( dims, 0, 0);
///    else if (rank == 4) vdata = mkFloat4( dims, 0, 0);
///    else if (rank == 5) vdata = mkFloat5( dims, 0, 0);
///    else if (rank == 6) vdata = mkFloat6( dims, 0, 0);
///    else if (rank == 7) vdata = mkFloat7( dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_FLOAT64) {
///    fillValue = new Double(999);
///    if (rank == 0) vdata = new Double( mkDouble0( dims, 0, 0));
///    else if (rank == 1) vdata = mkDouble1( dims, 0, 0);
///    else if (rank == 2) vdata = mkDouble2( dims, 0, 0);
///    else if (rank == 3) vdata = mkDouble3( dims, 0, 0);
///    else if (rank == 4) vdata = mkDouble4( dims, 0, 0);
///    else if (rank == 5) vdata = mkDouble5( dims, 0, 0);
///    else if (rank == 6) vdata = mkDouble6( dims, 0, 0);
///    else if (rank == 7) vdata = mkDouble7( dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_STRING_FIX
///    || dtype == HdfGroup.DTYPE_STRING_VAR)
///  {
///    String fillStg = "999";
///    // If fixed len, take only the last few chars.
///    if (stgFieldLen > 0 && fillStg.length() > stgFieldLen) {
///      fillStg = fillStg.substring( fillStg.length() - stgFieldLen);
///    }
///    fillValue = fillStg;
///
///    if (rank == 0) vdata = mkString0( stgFieldLen, dims, 0, 0);
///    else if (rank == 1) vdata = mkString1( stgFieldLen, dims, 0, 0);
///    else if (rank == 2) vdata = mkString2( stgFieldLen, dims, 0, 0);
///    else if (rank == 3) vdata = mkString3( stgFieldLen, dims, 0, 0);
///    else if (rank == 4) vdata = mkString4( stgFieldLen, dims, 0, 0);
///    else if (rank == 5) vdata = mkString5( stgFieldLen, dims, 0, 0);
///    else if (rank == 6) vdata = mkString6( stgFieldLen, dims, 0, 0);
///    else if (rank == 7) vdata = mkString7( stgFieldLen, dims, 0, 0);
///    else throwerr("unknown rank: " + rank);
///  }
///  else if (dtype == HdfGroup.DTYPE_REFERENCE) {
///    fillValue = null;
///    if (rank == 0) vdata = mkReference0( dims, 0, 0, refGroup);
///    else if (rank == 1) vdata = mkReference1( dims, 0, 0, refGroup);
///    else if (rank == 2) vdata = mkReference2( dims, 0, 0, refGroup);
///    else if (rank == 3) vdata = mkReference3( dims, 0, 0, refGroup);
///    else if (rank == 4) vdata = mkReference4( dims, 0, 0, refGroup);
///    else if (rank == 5) vdata = mkReference5( dims, 0, 0, refGroup);
///    else if (rank == 6) vdata = mkReference6( dims, 0, 0, refGroup);
///    else if (rank == 7) vdata = mkReference7( dims, 0, 0, refGroup);
///    else throwerr("unknown rank: " + rank);
///  }
///  else throwerr("unknown dtype: " + dtype);
///  return new Object[] { vdata, fillValue};
///} // end genHdfData
///
///
///
///
///
///
///
///
///static byte mkByte0( int[] dims, int idim, int ix) {
///  return (byte) (ix+1);
///}
///
///static byte[] mkByte1( int[] dims, int idim, int ix) {
///  byte[] vv = new byte[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkByte0( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static byte[][] mkByte2( int[] dims, int idim, int ix) {
///  byte[][] vv = new byte[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkByte1( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static byte[][][] mkByte3( int[] dims, int idim, int ix) {
///  byte[][][] vv = new byte[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkByte2( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static byte[][][][] mkByte4( int[] dims, int idim, int ix) {
///  byte[][][][] vv = new byte[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkByte3( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static byte[][][][][] mkByte5( int[] dims, int idim, int ix) {
///  byte[][][][][] vv = new byte[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    ///vv[jj] = mkByte4( dims, idim+1, 10*(ix+1)+jj);
///    vv[jj] = mkByte4( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///static byte[][][][][][] mkByte6( int[] dims, int idim, int ix) {
///  byte[][][][][][] vv = new byte[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkByte5( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///static byte[][][][][][][] mkByte7( int[] dims, int idim, int ix) {
///  byte[][][][][][][] vv = new byte[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkByte6( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///
///
///
///
///
///
///
///
///
///static short mkShort0( int[] dims, int idim, int ix) {
///  return (short) (ix+1);
///}
///
///static short[] mkShort1( int[] dims, int idim, int ix) {
///  short[] vv = new short[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkShort0( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static short[][] mkShort2( int[] dims, int idim, int ix) {
///  short[][] vv = new short[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkShort1( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static short[][][] mkShort3( int[] dims, int idim, int ix) {
///  short[][][] vv = new short[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkShort2( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static short[][][][] mkShort4( int[] dims, int idim, int ix) {
///  short[][][][] vv = new short[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkShort3( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static short[][][][][] mkShort5( int[] dims, int idim, int ix) {
///  short[][][][][] vv = new short[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    ///vv[jj] = mkShort4( dims, idim+1, 10*(ix+1)+jj);
///    vv[jj] = mkShort4( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///static short[][][][][][] mkShort6( int[] dims, int idim, int ix) {
///  short[][][][][][] vv = new short[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkShort5( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///static short[][][][][][][] mkShort7( int[] dims, int idim, int ix) {
///  short[][][][][][][] vv = new short[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkShort6( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///
///
///
///
///
///
///
///
///
///
///static int mkInt0( int[] dims, int idim, int ix) {
///  return (int) (ix+1);
///}
///
///static int[] mkInt1( int[] dims, int idim, int ix) {
///  int[] vv = new int[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt0( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static int[][] mkInt2( int[] dims, int idim, int ix) {
///  int[][] vv = new int[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt1( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static int[][][] mkInt3( int[] dims, int idim, int ix) {
///  int[][][] vv = new int[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt2( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static int[][][][] mkInt4( int[] dims, int idim, int ix) {
///  int[][][][] vv = new int[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt3( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static int[][][][][] mkInt5( int[] dims, int idim, int ix) {
///  int[][][][][] vv = new int[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt4( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static int[][][][][][] mkInt6( int[] dims, int idim, int ix) {
///  int[][][][][][] vv = new int[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt5( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static int[][][][][][][] mkInt7( int[] dims, int idim, int ix) {
///  int[][][][][][][] vv = new int[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkInt6( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///
///
///
///
///
///
///static long mkLong0( int[] dims, int idim, int ix) {
///  return (long) (ix+1);
///}
///
///static long[] mkLong1( int[] dims, int idim, int ix) {
///  long[] vv = new long[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong0( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static long[][] mkLong2( int[] dims, int idim, int ix) {
///  long[][] vv = new long[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong1( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static long[][][] mkLong3( int[] dims, int idim, int ix) {
///  long[][][] vv = new long[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong2( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static long[][][][] mkLong4( int[] dims, int idim, int ix) {
///  long[][][][] vv = new long[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong3( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static long[][][][][] mkLong5( int[] dims, int idim, int ix) {
///  long[][][][][] vv = new long[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong4( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static long[][][][][][] mkLong6( int[] dims, int idim, int ix) {
///  long[][][][][][] vv = new long[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong5( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static long[][][][][][][] mkLong7( int[] dims, int idim, int ix) {
///  long[][][][][][][] vv = new long[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkLong6( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///
///
///
///
///
///
///
///
///
///
///
///static float mkFloat0( int[] dims, int idim, int ix) {
///  return (float) (ix+1);
///}
///
///static float[] mkFloat1( int[] dims, int idim, int ix) {
///  float[] vv = new float[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat0( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static float[][] mkFloat2( int[] dims, int idim, int ix) {
///  float[][] vv = new float[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat1( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static float[][][] mkFloat3( int[] dims, int idim, int ix) {
///  float[][][] vv = new float[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat2( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static float[][][][] mkFloat4( int[] dims, int idim, int ix) {
///  float[][][][] vv = new float[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat3( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static float[][][][][] mkFloat5( int[] dims, int idim, int ix) {
///  float[][][][][] vv = new float[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat4( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static float[][][][][][] mkFloat6( int[] dims, int idim, int ix) {
///  float[][][][][][] vv = new float[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat5( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static float[][][][][][][] mkFloat7( int[] dims, int idim, int ix) {
///  float[][][][][][][] vv = new float[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkFloat6( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///
///
///
///
///
///
///
///static double mkDouble0( int[] dims, int idim, int ix) {
///  return (double) (ix+1);
///}
///
///static double[] mkDouble1( int[] dims, int idim, int ix) {
///  double[] vv = new double[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble0( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static double[][] mkDouble2( int[] dims, int idim, int ix) {
///  double[][] vv = new double[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble1( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static double[][][] mkDouble3( int[] dims, int idim, int ix) {
///  double[][][] vv = new double[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble2( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static double[][][][] mkDouble4( int[] dims, int idim, int ix) {
///  double[][][][] vv = new double[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble3( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static double[][][][][] mkDouble5( int[] dims, int idim, int ix) {
///  double[][][][][] vv = new double[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble4( dims, idim+1, 0);
///  }
///  return vv;
///}
///
///static double[][][][][][] mkDouble6( int[] dims, int idim, int ix) {
///  double[][][][][][] vv = new double[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble5( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static double[][][][][][][] mkDouble7( int[] dims, int idim, int ix) {
///  double[][][][][][][] vv = new double[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkDouble6( dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///
///
///
///
///
///
///
///
///
///static String mkString0(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  // Make string like  aaa37
///  String res = "";
///  for (int ii = 0; ii < ix % 4; ii++) {
///    res += "a";
///  }
///  res += String.format("%d", ix + 1);
///
///  // If fixed len, take only the last few chars.
///  if (stgFieldLen > 0 && res.length() > stgFieldLen) {
///    res = res.substring( res.length() - stgFieldLen);
///  }
///  return res;
///}
///
///static String[] mkString1(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[] vv = new String[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString0( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static String[][] mkString2(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[][] vv = new String[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString1( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static String[][][] mkString3(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[][][] vv = new String[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString2( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static String[][][][] mkString4(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[][][][] vv = new String[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString3( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static String[][][][][] mkString5(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[][][][][] vv = new String[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString4( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static String[][][][][][] mkString6(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[][][][][][] vv = new String[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString5( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  return vv;
///}
///
///static String[][][][][][][] mkString7(
///  int stgFieldLen, int[] dims, int idim, int ix)
///{
///  String[][][][][][][] vv = new String[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkString6( stgFieldLen, dims, idim+1, 10*(ix+1)+jj);
///  }
///  ///for (int ia = 0; ia < dims[0]; ia++) {
///  ///  for (int ib = 0; ib < dims[0]; ib++) {
///  ///    for (int ic = 0; ic < dims[0]; ic++) {
///  ///      for (int id = 0; id < dims[0]; id++) {
///  ///        for (int ie = 0; ie < dims[0]; ie++) {
///  ///          for (int ig = 0; ig < dims[0]; ig++) {
///  ///            for (int ih = 0; ih < dims[0]; ih++) {
///  ///              prtf("    #### %d %d %d %d %d %d %d    %g",
///  ///                ia, ib, ic, id, ie, ig, ih,
///  ///                vv[ia][ib][ic][id][ie][ig][ih]);
///  ///            }
///  ///          }
///  ///        }
///  ///      }
///  ///    }
///  ///  }
///  ///}
///  return vv;
///}
///
///
///
///
///
///static HdfGroup mkReference0(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  return grp;
///}
///
///static HdfGroup[] mkReference1(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[] vv = new HdfGroup[dims[idim]];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference0( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}
///
///static HdfGroup[][] mkReference2(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[][] vv = new HdfGroup[dims[idim]][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference1( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}
///
///static HdfGroup[][][] mkReference3(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[][][] vv = new HdfGroup[dims[idim]][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference2( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}
///
///static HdfGroup[][][][] mkReference4(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[][][][] vv = new HdfGroup[dims[idim]][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference3( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}
///
///static HdfGroup[][][][][] mkReference5(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[][][][][] vv = new HdfGroup[dims[idim]][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference4( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}
///
///static HdfGroup[][][][][][] mkReference6(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[][][][][][] vv = new HdfGroup[dims[idim]][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference5( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}
///
///static HdfGroup[][][][][][][] mkReference7(
///  int[] dims, int idim, int ix, HdfGroup grp)
///{
///  HdfGroup[][][][][][][] vv = new HdfGroup[dims[idim]][][][][][][];
///  for (int jj = 0; jj < dims[idim]; jj++) {
///    vv[jj] = mkReference6( dims, idim+1, 10*(ix+1)+jj, grp);
///  }
///  return vv;
///}






static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class
