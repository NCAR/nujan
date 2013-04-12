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


package edu.ucar.ral.nujan.hdf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.SimpleTimeZone;
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







/**
 * Checks that dataType == specType.
 * Checks that dataDims[i] == chunkDims[i]
 *      or startIxs[i] == lastPos and dataDims == fullDim - lastPos
 * where lastPos is the last startIx in this dim.
 *
 * Else throws an HdfException.
 * Called by HdfGroup.writeDataSub and MsgAttribute.constructor.
 */

static void checkTypeMatch(
  String msg,
  int specType,               // declared type, one of HdfGroup.DTYPE_*
  int dataType,               // actual data type, one of HdfGroup.DTYPE_*
  boolean useLinear,
  int[] varDims,              // entire var dimensions
  int[] startIxs,             // current start indices
  int[] chunkDims,            // specified chunk dims
  int[] dataDims)             // dims of data object to be written
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

  // Check dimensions
  // If scalar ...
  if (varDims.length == 0) {
    if (dataDims.length != 0)
      throwerr("type mismatch for: " + msg + "\n"
        + "  declared rank: (null)\n"
        + "  data rank:     " + dataDims.length + "\n");
  }

  // Else if useLinear ...
  else if (useLinear) {
    if (dataDims.length != 1) throwerr("useLinear but dataDims rank != 1");
    int dlen = dataDims[0];

    long fullChunkVolume = 0;
    long remVolume = 0;
    if (varDims.length > 0) {
      fullChunkVolume = 1;
      remVolume = 1;
      for (int ii = 0; ii < varDims.length; ii++) {
        int remLen = chunkDims[ii];
        if (startIxs[ii] + chunkDims[ii] >= varDims[ii])
          remLen = varDims[ii] - startIxs[ii];
        fullChunkVolume *=  chunkDims[ii];
        remVolume *= remLen;
      }
    }

    if (! (dlen == fullChunkVolume || dlen == remVolume)) {
      throwerr("bad chunk size with useLinear.\n"
        + "  declared variable lens: " + formatInts( varDims) + "\n"
        + "  declared chunk lens:    " + formatInts( chunkDims) + "\n"
        + "  remVolume:              " + remVolume + "\n"
        + "  current startIxs:       " + formatInts( startIxs) + "\n"
        + "  data object dim lens:   " + formatInts( dataDims) + "\n");
    }
  } // if useLinear

  // Else not scalar or linear ...
  else {
    // Check rank
    if (dataDims.length != chunkDims.length)
      throwerr("Dimension mismatch for: " + msg + "\n"
        + "  declared variable lens: " + formatInts( varDims) + "\n"
        + "  declared chunk lens:    " + formatInts( chunkDims) + "\n"
        + "  current startIxs:       " + formatInts( startIxs) + "\n"
        + "  data object dim lens:   " + formatInts( dataDims) + "\n");

    // Check each dimension
    for (int ii = 0; ii < chunkDims.length; ii++) {
      boolean allOk = false;
      if (dataDims[ii] == chunkDims[ii]) allOk = true;
      else {
        if (startIxs[ii] + chunkDims[ii] >= varDims[ii]) {  // if at last pos
          int remLen = varDims[ii] - startIxs[ii];
          if (dataDims[ii] == remLen) allOk = true;
        }
      }
      if (! allOk) {
        throwerr("Dimension mismatch for: " + msg + "\n"
          + "  data dimension length mismatch for dimension " + ii + "\n"
          + "  declared variable lens: " + formatInts( varDims) + "\n"
          + "  declared chunk lens:    " + formatInts( chunkDims) + "\n"
          + "  current startIxs:       " + formatInts( startIxs) + "\n"
          + "  data object dim lens:   " + formatInts( dataDims) + "\n");
      }
    } // for ii
  } // else not scalar
} // end checkTypeMatch







/**
 * Checks that a name (for a group or attribute) is legal in HDF5;
 * else throws an HdfException.
 * Coord with netcdf/NhGroup.checkName.
 */

static void checkName(
  String name,
  String loc)
throws HdfException
{
  if (name == null || name.length() == 0)
    throwerr("Name for %s is empty", loc);
  if (! Pattern.matches("^[_a-zA-Z][-_: a-zA-Z0-9]*$", name))
    throwerr("Invalid name for %s.  Name: \"%s\"", loc, name);
}









/**
 * If obj is a String, String[], String[][], ... or char[], char[][], ...,
 * returns the max string len without null termination;
 * returns 0 otherwise.
 */

static int getMaxStgLen(
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






public static long parseUtcTime( String stg)
throws HdfException
{
  long utcTime = 0;
  if (stg.equals("0")) utcTime = 0;
  else {
    SimpleDateFormat utcSdf = null;
    if (stg.length() == 10)                         // yyyy-mm-dd
      utcSdf = new SimpleDateFormat("yyyy-MM-dd");
    else if (stg.length() == 19)                    // yyyy-MM-ddTHH:mm:ss
      utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    else throwerr("invalid -utcModTime: \"" + stg + "\"");

    utcSdf.setTimeZone( new SimpleTimeZone( 0, "UTC"));
    Date dt = null;
    try { dt = utcSdf.parse( stg); }
    catch( ParseException exc) {
      throwerr("invalid -utcModTime: \"" + stg + "\"");
    }
    utcTime = dt.getTime();
  }
  return utcTime;
}




public static String formatUtcTime( long tval) {
  SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  utcSdf.setTimeZone( new SimpleTimeZone( 0, "UTC"));
  String stg = utcSdf.format( tval);
  return stg;
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
    sbuf.append( String.format("%s(String) \"%s\"\n",
      mkIndent(indent), obj));
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
        mkIndent( indent), ii, vals[ii].getClass().getName()));
      formatObjectSub( vals[ii], indent + 1, sbuf);
    }
  }
  else {
    sbuf.append( String.format("%s(%s) %s\n",
      mkIndent(indent), obj.getClass().getName(), obj));
  }
}






/**
 * Formats an array of ints.
 */

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
