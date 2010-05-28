
package hdfnet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;



class Util {



static long alignLong( long bound, long val) {
  if (val % bound != 0)
    val += bound - val % bound;
  return val;
}





// Returns array: [dtype, dimensions]

static int[] getDtypeAndDims(
  boolean isVlen,
  Object obj)
throws HdfException
{
  // Find rank and varDims
  int rank = 0;
  int[] varDims = new int[1000];
  while (obj instanceof Object[]) {
    Object[] objVec = (Object[]) obj;
    varDims[rank++] = objVec.length;

    // Check that arrays are regular (all rows have the same length)
    if (! isVlen) {
      // Find number of columns of objVec[0].
      // Make sure all other rows have the same num columns.
      if (objVec[0] instanceof Object[]) {
        int ncol = ((Object[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((Object[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      else if (objVec[0] instanceof byte[]) {
        int ncol = ((byte[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((byte[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      else if (objVec[0] instanceof short[]) {
        int ncol = ((short[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((short[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      else if (objVec[0] instanceof int[]) {
        int ncol = ((int[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((int[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      else if (objVec[0] instanceof long[]) {
        int ncol = ((long[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((long[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      else if (objVec[0] instanceof float[]) {
        int ncol = ((float[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((float[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      else if (objVec[0] instanceof double[]) {
        int ncol = ((double[]) objVec[0]).length;
        for (int irow = 0; irow < objVec.length; irow++) {
          if (((double[]) objVec[irow]).length != ncol)
            throwerr("array is ragged");
        }
      }
      // Else it's a Byte or Short or etc, handled below.
    } // if ! isVlen

    // Move to next level of dimensions
    obj = objVec[0];
  }

  // Finally done penetrating dimensions.
  // Here obj is no longer instanceof Object[].
  // Find the length of the last dimension for
  // arrays of primitive types like float[].
  if (obj instanceof Byte
    || obj instanceof Short
    || obj instanceof Integer
    || obj instanceof Long
    || obj instanceof Float
    || obj instanceof Double
    || obj instanceof String
    || obj instanceof HdfGroup)
  {
    // No last dimension
  }
  else if (obj instanceof byte[]) varDims[rank++] = ((byte[]) obj).length;
  else if (obj instanceof short[]) varDims[rank++] = ((short[]) obj).length;
  else if (obj instanceof int[]) varDims[rank++] = ((int[]) obj).length;
  else if (obj instanceof long[]) varDims[rank++] = ((long[]) obj).length;
  else if (obj instanceof float[]) varDims[rank++] = ((float[]) obj).length;
  else if (obj instanceof double[]) varDims[rank++] = ((double[]) obj).length;
  else throwerr("unknown obj type.  obj: " + obj
    + "  element class: " + obj.getClass());

  varDims = Arrays.copyOf( varDims, rank);

  // Find dtype
  int dtype = -1;
  if (obj instanceof Byte || obj instanceof byte[])
    dtype = HdfGroup.DTYPE_FIXED08;
  else if (obj instanceof Short || obj instanceof short[])
    dtype = HdfGroup.DTYPE_FIXED16;
  else if (obj instanceof Integer || obj instanceof int[])
    dtype = HdfGroup.DTYPE_FIXED32;
  else if (obj instanceof Long || obj instanceof long[])
    dtype = HdfGroup.DTYPE_FIXED64;
  else if (obj instanceof Float || obj instanceof float[])
    dtype = HdfGroup.DTYPE_FLOAT32;
  else if (obj instanceof Double || obj instanceof double[])
    dtype = HdfGroup.DTYPE_FLOAT64;
  else if (obj instanceof String) dtype = HdfGroup.DTYPE_STRING_FIX;
  else if (obj instanceof HdfGroup) dtype = HdfGroup.DTYPE_REFERENCE;
  else throwerr("unknown obj type.  obj: " + obj
    + "  element class: " + obj.getClass());

  int[] dtypeAndDims = new int[1 + rank];
  dtypeAndDims[0] = dtype;
  for (int ii = 0; ii < rank; ii++) {
    dtypeAndDims[1+ii] = varDims[ii];
  }
  return dtypeAndDims;
} // end getDtypeAndDims




// Returns max string len, without null termination,
// if obj is a String, String[], String[][], etc.
// Returns 0 otherwise.

static int getMaxStgLen(
  Object obj)
throws HdfException
{
  int maxStgLen = 0;
  if (obj instanceof String) maxStgLen = ((String) obj).length();
  else if (obj instanceof Object[]) {
    Object[] objVec = (Object[]) obj;
    for (int ii = 0; ii < objVec.length; ii++) {
      maxStgLen = Math.max( maxStgLen, getMaxStgLen( objVec[ii]));
    }
  }
  // Else some other type: ignore it.

  return maxStgLen;
}





static byte[] encodeString(
  String stg,
  boolean addNullTerm,
  HdfGroup group)          // used only for error msgs
throws HdfException
{
  Charset charset = Charset.forName("US-ASCII");
  byte[] bytes = stg.getBytes( charset);
  if (addNullTerm) {
    bytes = Arrays.copyOf( bytes, bytes.length + 1);
    bytes[bytes.length - 1] = 0;
  }
  return bytes;
}





static byte[] padNull(
  byte[] bytes,
  int fieldLen)
throws HdfException
{
  if (bytes.length > fieldLen) {
    throwerr(
      "padNull fieldLen exceeded.  fieldLen: %d  bytesLen: %d  bytes: %s",
      fieldLen, bytes.length, formatBytes( bytes, 0, bytes.length));
  }
  byte[] res = Arrays.copyOf( bytes, fieldLen);
  return res;
}




static String formatBytes(
  byte[] bytes,
  int istart,
  int iend)
{
  StringBuilder sbuf = new StringBuilder();
  sbuf.append("hex:");
  for (int ii = istart; ii < iend; ii++) {
    sbuf.append(" ");
    String stg = Integer.toHexString( bytes[ii] & 0xff);
    if (stg.length() == 1) sbuf.append("0");
    sbuf.append( stg);
  }
  return sbuf.toString();
}



static String formatDtypeDim(
  int dtype,
  int[] dims)
{
  String res = HdfGroup.dtypeNames[dtype];
  if (dims.length > 0) {
    res += '[';
    for (int ii = 0; ii < dims.length; ii++) {
      if (ii > 0) res += ",";
      res += "" + dims[ii];
    }
    res += ']';
  }
  return res;
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
