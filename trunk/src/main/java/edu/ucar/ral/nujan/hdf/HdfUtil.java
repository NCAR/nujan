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


package edu.ucar.ral.nujan.hdf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;



/**
 * A variety of utility functions.
 */

public class HdfUtil {



/**
 * Returns the next multiple of bound that is &gt;= val.
 */
static long alignLong( long bound, long val) {
  if (val % bound != 0)
    val += bound - val % bound;
  return val;
}



/**
 * Recursively inspects obj to determine the dimensions and
 * base element type.
 * Calls getDimLenSub to do the real work.
 * @param obj The object to inspect.
 * @param isVlen If false, all subarrays at every level must have the
 *    same length.  If true, we allow ragged arrays.
 * @return An array containing: elementType, totNumEle, dim0, dim1, dim2, ....
 */

static int[] getDimLen(
  Object obj,
  boolean isVlen)
throws HdfException
{
  HdfModInt eleType = new HdfModInt( 0);
  ArrayList<Integer> dimList = new ArrayList<Integer>();
  HdfModInt totNumEle = new HdfModInt( 0);
  HdfModInt elementLen = new HdfModInt( 0);
  int curDim = 0;

  getDimLenSub( obj, isVlen, curDim, eleType, totNumEle, elementLen, dimList);
  int[] res = new int[ 3 + dimList.size()];
  res[0] = eleType.getValue();
  res[1] = totNumEle.getValue();
  res[2] = elementLen.getValue();
  for (int ii = 0; ii < dimList.size(); ii++) {
    res[3+ii] = dimList.get(ii).intValue();
  }
  return res;
}







/**
 * Recursively inspects obj to determine the dimensions and
 * base element type.
 *
 * @param obj The object to inspect.
 * @param isVlen If false, all subarrays at every level must have the
 *    same length.  If true, we allow ragged arrays.
 * @param curDim The current dimension we're examining.  For a
 *    two dimensional array, curDim takes on the values 0, 1.
 *    The curDim value is incremented in the recursion call.
 * @param eleType  Returned value set by the bottommost recursion:
 *    one of HdfGroup.DTYPE_*.
 * @param eleType  Returned value: total number of elements in the array.
 * @param elementLen  Returned value set by the bottommost recursion:
 *    length in bytes of a single element.
 *    For example, if Integer or int[] or int[][] or..., elementLen = 4.
 *    For String and char[], elementLen = max len encountered.
 * @param dimList  Returned value: length of each dimension.
 */

static void getDimLenSub(
  Object obj,
  boolean isVlen,
  int curDim,
  HdfModInt eleType,
  HdfModInt totNumEle,
  HdfModInt elementLen,
  ArrayList<Integer> dimList)
throws HdfException
{

  int dtype = -1;
  int dimLen = -1;
  int deltaNum = 0;

  if (obj instanceof byte[]) {
    byte[] vals = (byte[]) obj;
    dtype = HdfGroup.DTYPE_UFIXED08;
    elementLen.setValue( 1);
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Byte) {
    dtype = HdfGroup.DTYPE_UFIXED08;
    elementLen.setValue( 1);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof short[]) {
    short[] vals = (short[]) obj;
    elementLen.setValue( 2);
    dtype = HdfGroup.DTYPE_FIXED16;
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Short) {
    dtype = HdfGroup.DTYPE_FIXED16;
    elementLen.setValue( 2);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof int[]) {
    int[] vals = (int[]) obj;
    dtype = HdfGroup.DTYPE_FIXED32;
    elementLen.setValue( 4);
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Integer) {
    dtype = HdfGroup.DTYPE_FIXED32;
    elementLen.setValue( 4);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof long[]) {
    long[] vals = (long[]) obj;
    dtype = HdfGroup.DTYPE_FIXED64;
    elementLen.setValue( 8);
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Long) {
    dtype = HdfGroup.DTYPE_FIXED64;
    elementLen.setValue( 8);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof float[]) {
    float[] vals = (float[]) obj;
    dtype = HdfGroup.DTYPE_FLOAT32;
    elementLen.setValue( 4);
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Float) {
    dtype = HdfGroup.DTYPE_FLOAT32;
    elementLen.setValue( 4);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof double[]) {
    double[] vals = (double[]) obj;
    dtype = HdfGroup.DTYPE_FLOAT64;
    elementLen.setValue( 8);
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Double) {
    dtype = HdfGroup.DTYPE_FLOAT64;
    elementLen.setValue( 4);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof char[]) {
    char[] vals = (char[]) obj;
    dtype = HdfGroup.DTYPE_STRING_FIX;
    elementLen.setValue( Math.max( elementLen.getValue(), vals.length));
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof Character) {
    dtype = HdfGroup.DTYPE_STRING_FIX;
    elementLen.setValue( 1);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }


  // These cases in arrays are Object[]

  else if (obj instanceof String[]) {
    String[] vals = (String[]) obj;
    dtype = HdfGroup.DTYPE_STRING_VAR;
    for (String val : vals) {
      elementLen.setValue( Math.max( elementLen.getValue(), val.length()));
    }
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof String) {
    String val = (String) obj;
    dtype = HdfGroup.DTYPE_STRING_VAR;
    elementLen.setValue( Math.max( elementLen.getValue(), val.length()));
    dimLen = -1;       // scalar
    deltaNum = 1;
  }
  else if (obj instanceof HdfGroup[]) {    // reference
    HdfGroup[] vals = (HdfGroup[]) obj;
    dtype = HdfGroup.DTYPE_REFERENCE;
    elementLen.setValue( HdfFileWriter.OFFSET_SIZE);
    dimLen = vals.length;
    deltaNum = vals.length;
  }
  else if (obj instanceof HdfGroup) {      // reference
    dtype = HdfGroup.DTYPE_REFERENCE;
    elementLen.setValue( HdfFileWriter.OFFSET_SIZE);
    dimLen = -1;       // scalar
    deltaNum = 1;
  }

  // Main recursion
  else if (obj instanceof Object[]) {
    Object[] vals = (Object[]) obj;
    dimLen = vals.length;
  }


  // If first time, set eleType.  Else check type match.
  if (dtype > 0) {
    if (eleType.getValue() <= 0) eleType.setValue( dtype);
    else if (dtype != eleType.getValue()) throwerr("internal type mismatch");
  }

  // If first time, expand dimList.  Else check dimList match.
  if (curDim > dimList.size()) throwerr("invalid curDim");
  else if (curDim == dimList.size()) {
    if (dimLen > 0)     // if not scalar (-1) and empty dimension (0)
      dimList.add( new Integer( dimLen));
  }
  else if (dimLen != dimList.get( curDim).intValue()) {
    if (! isVlen) throwerr("dimension mismatch");
  }

  // Increment totNumEle.
  totNumEle.setValue( totNumEle.getValue() + deltaNum);

  // Recursion
  if (obj instanceof Object[]) {
    Object[] objs = (Object[]) obj;
    for (Object subObj : objs) {
      getDimLenSub( subObj, isVlen, curDim + 1, eleType, totNumEle,
        elementLen, dimList);
    }
  }
} // end getDimLenSub












//// OBSOLETE:
////
//// Returns array: [dtype, dimensions]
//// For Strings, we always return DTYPE_STRING_VAR, not STRING_FIX.
//// For chars we always return DTYPE_STRING_FIX.
//// For Bytes, we always return DTYPE_UFIXED08, not SFIXED08.
//
//static int[] getDtypeAndDimsOld(
//  boolean isVlen,
//  Object obj)         // Short or short[] or short[][] or ... Float or ...
//throws HdfException
//{
//  if (obj == null) throwerr("obj == null");
//
//  // Find rank and varDims
//  int rank = 0;
//  int[] varDims = new int[1000];
//  while (obj instanceof Object[] && ((Object[]) obj).length > 0) {
//    Object[] objVec = (Object[]) obj;
//    for (int ii = 0; ii < objVec.length; ii++) {
//      if (objVec[ii] == null) throwerr("objVec[%d] == null", ii);
//    }
//    varDims[rank++] = objVec.length;
//
//    // Check that arrays are regular (all rows have the same length)
//    if (! isVlen) {
//      // Find number of columns of objVec[0].
//      // Make sure all other rows have the same num columns.
//      if (objVec[0] instanceof Object[]) {
//        int ncol = ((Object[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((Object[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      else if (objVec[0] instanceof byte[]) {
//        int ncol = ((byte[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((byte[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      else if (objVec[0] instanceof short[]) {
//        int ncol = ((short[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((short[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      else if (objVec[0] instanceof int[]) {
//        int ncol = ((int[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((int[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      else if (objVec[0] instanceof long[]) {
//        int ncol = ((long[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((long[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      else if (objVec[0] instanceof float[]) {
//        int ncol = ((float[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((float[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      else if (objVec[0] instanceof double[]) {
//        int ncol = ((double[]) objVec[0]).length;
//        for (int irow = 0; irow < objVec.length; irow++) {
//          if (((double[]) objVec[irow]).length != ncol)
//            throwerr("array is ragged");
//        }
//      }
//      // Else it's a Byte or Short or etc, handled below.
//    } // if ! isVlen
//
//    // Move to next level of dimensions
//    obj = objVec[0];
//  } // while obj instanceof Object[]
//
//  // Finally done penetrating dimensions.
//  // Here obj is no longer instanceof Object[].
//  // Find the length of the last dimension for
//  // arrays of primitive types like float[].
//  if (obj instanceof Byte
//    || obj instanceof Short
//    || obj instanceof Integer
//    || obj instanceof Long
//    || obj instanceof Float
//    || obj instanceof Double
//    || obj instanceof Character
//    || obj instanceof String
//    || obj instanceof HdfGroup)
//  {
//    // No last dimension
//  }
//  else if (obj instanceof byte[]) varDims[rank++] = ((byte[]) obj).length;
//  else if (obj instanceof short[]) varDims[rank++] = ((short[]) obj).length;
//  else if (obj instanceof int[]) varDims[rank++] = ((int[]) obj).length;
//  else if (obj instanceof long[]) varDims[rank++] = ((long[]) obj).length;
//  else if (obj instanceof float[]) varDims[rank++] = ((float[]) obj).length;
//  else if (obj instanceof double[]) varDims[rank++] = ((double[]) obj).length;
//  else if (obj instanceof char[]) varDims[rank++] = ((char[]) obj).length;
//  else throwerr("unknown obj type.  obj: " + obj
//    + "  element class: " + obj.getClass());
//
//  varDims = Arrays.copyOf( varDims, rank);
//
//  // Find dtype
//  int dtype = -1;
//  if (obj instanceof Byte || obj instanceof byte[])
//    dtype = HdfGroup.DTYPE_UFIXED08;
//  else if (obj instanceof Short || obj instanceof short[])
//    dtype = HdfGroup.DTYPE_FIXED16;
//  else if (obj instanceof Integer || obj instanceof int[])
//    dtype = HdfGroup.DTYPE_FIXED32;
//  else if (obj instanceof Long || obj instanceof long[])
//    dtype = HdfGroup.DTYPE_FIXED64;
//  else if (obj instanceof Float || obj instanceof float[])
//    dtype = HdfGroup.DTYPE_FLOAT32;
//  else if (obj instanceof Double || obj instanceof double[])
//    dtype = HdfGroup.DTYPE_FLOAT64;
//  else if (obj instanceof Character || obj instanceof char[])
//    dtype = HdfGroup.DTYPE_STRING_FIX;
//  else if (obj instanceof String) dtype = HdfGroup.DTYPE_STRING_VAR;
//  else if (obj instanceof HdfGroup) dtype = HdfGroup.DTYPE_REFERENCE;
//  else throwerr("unknown obj type.  obj: " + obj
//    + "  element class: " + obj.getClass());
//
//  int[] dtypeAndDims = new int[1 + rank];
//  dtypeAndDims[0] = dtype;
//  for (int ii = 0; ii < rank; ii++) {
//    dtypeAndDims[1+ii] = varDims[ii];
//  }
//  return dtypeAndDims;
//} // end getDtypeAndDimsOld









/**
 * Checks that specType == dataType and specDims == dataDims; else
 * throws an HdfException.
 * Called by HdfGroup.writeDataSub and MsgAttribute.constructor.
 */

static void checkTypeMatch(
  String msg,
  int specType,               // declared type, one of HdfGroup.DTYPE_*
  int dataType,               // actual data type, one of HdfGroup.DTYPE_*
  int[] specDims,
  int[] dataDims)
throws HdfException
{
  // Check that dtype and varDims match what the user
  // declared in the earlier addVariable call.
  if (specType == HdfGroup.DTYPE_STRING_FIX
    || specType == HdfGroup.DTYPE_STRING_VAR)
  {
    if (dataType != HdfGroup.DTYPE_STRING_FIX
      && dataType != HdfGroup.DTYPE_STRING_VAR)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared type: " + HdfGroup.dtypeNames[ specType] + "\n"
        + "  data type:     " + HdfGroup.dtypeNames[ dataType] + "\n");
  }
  else if (specType == HdfGroup.DTYPE_SFIXED08
    || specType == HdfGroup.DTYPE_UFIXED08)
  {
    if (dataType != HdfGroup.DTYPE_UFIXED08)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared type: " + HdfGroup.dtypeNames[ specType] + "\n"
        + "  data type:     " + HdfGroup.dtypeNames[ dataType] + "\n");
  }
  else if (specType == HdfGroup.DTYPE_COMPOUND
    || specType == HdfGroup.DTYPE_REFERENCE)
  {
    if (dataType != HdfGroup.DTYPE_REFERENCE)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared type: " + HdfGroup.dtypeNames[ specType] + "\n"
        + "  data type:     " + HdfGroup.dtypeNames[ dataType] + "\n");
  }
  else {
    if (dataType != specType)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared type: " + HdfGroup.dtypeNames[ specType] + "\n"
        + "  data type:     " + HdfGroup.dtypeNames[ dataType] + "\n");
  }

  if (specDims == null) {
    if (dataDims.length != 0)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared rank: (null)\n"
        + "  data rank:     " + dataDims.length + "\n");
  }
  else {
    if (dataDims.length != specDims.length)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared rank: " + specDims.length + "\n"
        + "  data rank:     " + dataDims.length + "\n");

    for (int ii = 0; ii < specDims.length; ii++) {
      if (dataDims[ii] != specDims[ii])
        throwerr("type mismatch for: " + msg + "\n"
          + "  data dimension length mismatch for dimension " + ii + "\n"
          + "  declared dim: " + specDims[ii] + "\n"
          + "  data dim: " + dataDims[ii] + "\n");
    }
  }
} // end checkTypeMatch







/**
 * Checks that a name (for a group or attribute) is legal in HDF5;
 * else throws an HdfException.
 */

public static void checkName(
  String name,
  String loc)
throws HdfException
{
  if (name == null || name.length() == 0)
    throwerr("Name for %s is empty", loc);
  if (! Pattern.matches("^[_a-zA-Z][-_a-zA-Z0-9]*$", name))
    throwerr("Invalid name for %s.  Name: \"%s\"", loc, name);
}









/**
 * If obj is a String, String[], String[][], ... or char[], char[][], ...,
 * returns the max string len without null termination;
 * returns 0 otherwise.
 */

public static int getMaxStgLen(
  Object obj)
throws HdfException
{
  int maxStgLen = 0;
  if (obj instanceof String) maxStgLen = ((String) obj).length();
  else if (obj instanceof char[]) {
    maxStgLen = Math.max( maxStgLen, ((char[]) obj).length);
  }
  else if (obj instanceof Object[]) {       // String[] or char[][]
    Object[] objVec = (Object[]) obj;
    for (int ii = 0; ii < objVec.length; ii++) {
      maxStgLen = Math.max( maxStgLen, getMaxStgLen( objVec[ii]));
    }
  }
  // Else some other type: ignore it.

  return maxStgLen;
}






/**
 * Encodes a String to byte[] using the US-ASCII character set.
 */

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





/**
 * Returns a copy of bytes, truncated or extended to the specified
 * fieldLen.
 */

static byte[] truncPadNull(
  byte[] bytes,
  int fieldLen)
throws HdfException
{
  // We allow truncation.  Although the page
  //   http://www.hdfgroup.org/HDF5/doc/H5.user/Datatypes.html
  // indicates that H5T_STR_NULLTERM is always null terminated,
  // apparently not during storage.
  // Perhaps it's always null terminated after retrieval by the
  // HDF5 software.

  //if (bytes.length > fieldLen) {
  //  throwerr(
  //    "padNull fieldLen exceeded.  fieldLen: %d  bytesLen: %d  bytes: %s",
  //    fieldLen, bytes.length, formatBytes( bytes, 0, bytes.length));
  //}

  byte[] res = Arrays.copyOf( bytes, fieldLen);
  return res;
}





/**
 * Formats bytes[istart &lt;= i &lt; iend] as a hex string.
 */

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





/**
 * Formats the name of dtype and the dimension lengths.
 */

static String formatDtypeDim(
  int dtype,
  int[] dims)
{
  String res = HdfGroup.dtypeNames[dtype];
  if (dims == null) res += " (dims==null)";
  else if (dims.length == 0) res += " scalar";
  else {
    res += " [";
    for (int ii = 0; ii < dims.length; ii++) {
      if (ii > 0) res += ",";
      res += "" + dims[ii];
    }
    res += "]";
  }
  return res;
}




/**
 * Formats a general Object by recursively examining it
 * if it's an array.  Calls formatObjectSub to do the real work.
 */

public static String formatObject( Object obj) {
  StringBuilder sbuf = new StringBuilder();
  if (obj == null) sbuf.append("  (null)");
  else {
    sbuf.append("  cls: " + obj.getClass().getName() + "\n");
    formatObjectSub( obj, 2, sbuf);
  }
  return sbuf.toString();
}



/**
 * Formats a general Object by recursively examining it
 * if it's an array.
 */

static void formatObjectSub(
  Object obj,
  int indent,
  StringBuilder sbuf)
{
  if (obj == null)
    sbuf.append( String.format( "%s(null)\n", mkIndent( indent)));
  else if (obj instanceof String) {
    sbuf.append( String.format("%s(%s) \"%s\"\n",
      mkIndent(indent), obj.getClass().getName(), obj));
  }
  else if (obj instanceof byte[]) {
    sbuf.append( mkIndent( indent) + "(bytes)");
    byte[] vals = (byte[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof short[]) {
    sbuf.append( mkIndent( indent) + "(shorts)");
    short[] vals = (short[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof int[]) {
    sbuf.append( mkIndent( indent) + "(ints)");
    int[] vals = (int[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof long[]) {
    sbuf.append( mkIndent( indent) + "(longs)");
    long[] vals = (long[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof float[]) {
    sbuf.append( mkIndent( indent) + "(floats)");
    float[] vals = (float[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof double[]) {
    sbuf.append( mkIndent( indent) + "(doubles)");
    double[] vals = (double[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof char[]) {
    sbuf.append( mkIndent( indent) + "(chars)");
    char[] vals = (char[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append("  " + vals[ii]);
    }
    sbuf.append("\n");
  }
  else if (obj instanceof Object[]) {
    Object[] vals = (Object[]) obj;
    for (int ii = 0; ii < vals.length; ii++) {
      sbuf.append( String.format( "%s%d  cls: %s:\n",
        mkIndent( indent), ii, obj.getClass().getName()));
      formatObjectSub( vals[ii], indent + 1, sbuf);
    }
  }
  else {
    sbuf.append( String.format("%s(%s) %s\n",
      mkIndent(indent), obj.getClass().getName(), obj));
  }
}




/**
 * Returns an indentation string having length 2*indent.
 */

static String mkIndent( int indent) {
  String res = "";
  for (int ii = 0; ii < indent; ii++) {
    res += "  ";
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
