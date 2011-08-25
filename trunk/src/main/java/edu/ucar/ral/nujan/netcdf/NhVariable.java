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


package edu.ucar.ral.nujan.netcdf;

import java.util.Arrays;
import ucar.ma2.Array;
import edu.ucar.ral.nujan.hdf.HdfException;
import edu.ucar.ral.nujan.hdf.HdfGroup;


/**
 * Represents a NetCDF4 variable (HDF5 calls it a "dataset").
 * For typical use see {@link NhFileWriter}.
 * @see NhFileWriter
 */

public class NhVariable {

/**
 * Data type for signed bytes (1 byte integers).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java byte[].<br>
 * <li> Variables of this type must have data that is Java
 * Byte(scalar) or byte[] or byte[][] or byte[][][] or ...
 * <li> Fill values for variables of this type must be Java Byte.
 * <ul>
 */
public static final int TP_SBYTE       =  1;

/**
 * Data type for unsigned bytes (1 byte unsigned integers).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java byte[].<br>
 * <li> Variables of this type must have data that is Java
 * Byte(scalar) or byte[] or byte[][] or byte[][][] or ...
 * <li> Fill values for variables of this type must be Java Byte.
 * <ul>
 */
public static final int TP_UBYTE       =  2;

/**
 * Data type for short integers (2 byte integers).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java short[].<br>
 * <li> Variables of this type must have data that is Java
 * Short(scalar) or short[] or short[][] or short[][][] or ...
 * <li> Fill values for variables of this type must be Java Short.
 * <ul>
 */
public static final int TP_SHORT       =  3;

/**
 * Data type for integers (4 byte integers).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java int[].<br>
 * <li> Variables of this type must have data that is Java
 * Integer(scalar) or int[] or int[][] or int[][][] or ...
 * <li> Fill values for variables of this type must be Java Integer.
 * <ul>
 */
public static final int TP_INT         =  4;

/**
 * Data type for long integers (8 byte integers).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java int[].<br>
 * <li> Variables of this type must have data that is Java
 * Integer(scalar) or int[] or int[][] or int[][][] or ...
 * <li> Fill values for variables of this type must be Java Integer.
 * <ul>
 */
public static final int TP_LONG        =  5;

/**
 * Data type for standard floats (4 byte floats).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java float[].<br>
 * <li> Variables of this type must have data that is Java
 * Float(scalar) or float[] or float[][] or float[][][] or ...
 * <li> Fill values for variables of this type must be Java Float.
 * <ul>
 */
public static final int TP_FLOAT       =  6;

/**
 * Data type for double precision floats (8 byte floats).<br>
 * <ul>
 * <li> Attributes of this type must have a value that is Java double[].<br>
 * <li> Variables of this type must have data that is Java
 * Double(scalar) or double[] or double[][] or double[][][] or ...
 * <li> Fill values for variables of this type must be Java Double.
 * <ul>
 */
public static final int TP_DOUBLE      =  7;

/**
 * Data type for character arrays.
 * <ul>
 * <li> Attributes of this type must have a value that is Java char[].<br>
 * <li> Variables of this type must have data that is Java
 * Character(scalar) or char[] or char[][] or char[][][] or ...
 * <li> Fill values for variables of this type must be Java Character.
 * <ul>
 */
public static final int TP_CHAR        =  8;

/**
 * Data type for String (text) data.
 * <ul>
 * <li> Attributes of this type must have a value that is Java String
 * or String[].<br>
 * <li> Variables of this type must have data that is Java
 * String(scalar) or String[] or String[][] or String[][][] or ...
 * <li> Fill values for variables of this type must be Java String.
 * <ul>
 */
public static final int TP_STRING_VAR  =  9;

/**
 * Names of the TP_ constants.
 */
public static final String[] nhTypeNames = {
  "UNKNOWN", "SBYTE", "UBYTE", "SHORT", "INT",
  "LONG", "FLOAT", "DOUBLE", "CHAR", "STRING_VAR"};



String varName;            // variable name
int nhType;                // one of TP_*

NhDimension[] nhDims;      // shared dimensions
Object fillValue;
int compressionLevel;      // 0: no compression;  9: max compression
NhGroup parentGroup;
NhFileWriter nhFile;

int rank;
int dtype;                 // one of HdfGroup.DTYPE_*
int[] dimLens;             // len of each nhDims element

HdfGroup hdfVar;





NhVariable(
  String varName,            // variable name
  int nhType,                // one of TP_*
  NhDimension[] nhDims,      // shared dimensions
  int[] chunkLens,
  Object fillValue,
  int compressionLevel,      // 0: no compression;  9: max compression
  NhGroup parentGroup,
  NhFileWriter nhFile)
throws NhException
{
  this.varName = varName;
  this.nhType = nhType;
  if (nhDims == null) this.nhDims = null;
  else this.nhDims = Arrays.copyOf( nhDims, nhDims.length);
  this.fillValue = fillValue;
  this.compressionLevel = compressionLevel;
  this.parentGroup = parentGroup;
  this.nhFile = nhFile;

  if (nhFile.bugs >= 1) {
    String msg = "NhVariable.const:"
      + "  var name: \"" + varName + "\"\n"
      + "  nhType: " + NhVariable.nhTypeNames[ nhType] + "\n"
      + "  dims: ";
    if (nhDims == null) msg += "(null)";
    else {
      for (NhDimension dm : nhDims) {
        msg += "  \"" + dm.dimName + "\"(" + dm.dimLen + ")";
      }
    }
    msg += "\n";
    msg += "  chunkLens: " + NhGroup.formatInts( chunkLens) + "\n";
    msg += "  fill: " + fillValue + "\n";
    msg += "  compressionLevel: " + compressionLevel;
    prtf( msg);
  }

  if (nhDims == null) rank = 0;
  else rank = nhDims.length;

  // Translate nhType to dtype
  // Note: fixed len strings are not supported by the Netcdf API.
  // However, Netcdf TP_CHAR is translated into HDF5 DTYPE_STRING_FIX
  // with stgFieldLen = 1.
  // That is, an array of null-terminated strings all having length 1.
  // Although declared as H5T_STR_NULLTERM, the null-termination
  // isn't stored in the file.

  boolean isScalar = false;
  if (nhDims != null && nhDims.length == 0) isScalar = true;

  dtype = findDtype( varName, nhType);

  int stgFieldLen = 0;     // max string len for STRING_FIX, without null term
  Object hdfFillValue = fillValue;

  if (dtype == HdfGroup.DTYPE_STRING_FIX) {
    if (nhType == TP_CHAR) {
      stgFieldLen = 1;

      if (hdfFillValue == null) {}
      else if (hdfFillValue instanceof Character) {
        char cc = ((Character) hdfFillValue).charValue();
        hdfFillValue = new String( new char[] { cc});  // stg len = 1
      }
      else if (hdfFillValue instanceof String) {
        String stg = (String) hdfFillValue;
        if (stg.length() > 1) throwerr("char fillValue is String len > 1");
      }
      else throwerr("unknown char fillValue class: " + hdfFillValue.getClass());
    }
  }

  // Build int[] dimLens from nhDims 
  if (nhDims == null) dimLens = null;
  else {
    dimLens = new int[ rank];
    for (int ii = 0; ii < rank; ii++) {
      // Check that nhDims[ii] is in our ancestors.
      NhDimension tdim = parentGroup.findAncestorDimension(
        nhDims[ii].getName());
      if (tdim != nhDims[ii])
        throwerr("dimension not found.  var: %s  dim: %s", varName, nhDims[ii]);
      
      int dlen = nhDims[ii].dimLen;
      if (dlen <= 0 || dlen >= Integer.MAX_VALUE) {
        throwerr("NhVariable: variable \"%s\", dimension %d,"
          + " has illegal value: %d",
          varName, ii, dlen);
      }
      dimLens[ii] = dlen;
    }
  }

  // If the variable name equals that of some dimension,
  // any dimension (even if not used in this variable's nhDims),
  // flag that dimension as also being a variable,
  // meaning this is a coordinate variable.
  NhDimension tdim = parentGroup.findAncestorDimension( varName);
  if (tdim != null) {
    if (nhFile.bugs >= 1)
      prtf("NhVariable: coordVar: %s  dim: %s", this, tdim);
    tdim.coordVar = this;
  }



  // We don't allow compression of TP_STRING_* -
  // deliberately not implemented.
  // It turns out that HDF5 compresses the references to
  // variable length strings, but not the strings themselves.
  // The strings remain in the global heap GCOL, uncompressed.
  if (compressionLevel > 0 && nhType == TP_STRING_VAR)
    throwerr("cannot use compression with TP_STRING_*");

  // Scalars cannot be compressed or chunked.
  if ((dimLens == null || dimLens.length == 0) && compressionLevel > 0)
    throwerr("cannot use compression with scalar data");

  try {
    hdfVar = parentGroup.hdfGroup.addVariable(
      varName,
      dtype,
      stgFieldLen,          // max stg len, including null termination
      dimLens,              // dimension lengths
      chunkLens,
      hdfFillValue,
      compressionLevel);
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  // Add us to each dimension's refList
  if (nhDims != null) {
    for (NhDimension nhDim : nhDims) {
      nhDim.refList.add( this);
    }
  }

} // end constructor





public String toString() {
  String res = String.format(
    "path: \"%s\"  type: %s  compress: %d  rank: %d  dims: (",
    getPath(), nhTypeNames[nhType], compressionLevel, rank);
  for (NhDimension nd : nhDims) {
    res += "  " + nd.dimName + "(" + nd.dimLen + ")";
  }
  res += ")";
  return res;
}



/**
 * Returns the type (one of TP_*) specified in the constructor.
 */
public int getType() { return nhType; }

/**
 * Returns the dimensions specified in the constructor.
 */
public NhDimension[] getDimensions() { return nhDims; }

/**
 * Returns the fill value specified in the constructor.
 */
public Object getFillValue() { return fillValue; }

/**
 * Returns the compression level specified in the constructor.
 */
public int getCompressionLevel() { return compressionLevel; }

/**
 * Returns the group containing this variable.
 */
public NhGroup getParentGroup() { return parentGroup; }

/**
 * Returns the open file containing this NhGroup.
 */
public NhFileWriter getFileWriter() { return nhFile; }



/**
 * Returns the name of this variable.
 */
public String getName() { return varName; }

/**
 * Returns the full path name of this variable, starting with
 * the root group.
 */
public String getPath() {
  return parentGroup.getPath() + "/" + varName;
}






/**
 * Returns true if an attribute with the given names exists
 * in this variable; false otherwise.
 */

public boolean attributeExists(
  String attrName)
{
  boolean bres = false;
  if (hdfVar.findAttribute( attrName) != null) bres = true;
  return bres;
}







/**
 * Adds an attribute to this group.
 * Although HDF5 supports attributes of any dimsionality, 0, 1, 2, ...,
 * the NetCDF data model only supports attributes that
 * are a String or a 1 dimensional
 * array of: String, byte, short, int, long, float, or double.
 * <p>
 * See {@link HdfGroup#addAttribute} for documentation on the legal
 * types of attrValue.
 * <p>
 *
 * @param attrName The name of the new attribute.
 * @param atType The type of the new variable: one of NhVariable.TP_*.
 * @param attrValue The value of the new attribute.
 */

public void addAttribute(
  String attrName,
  int atType,                // one of TP_*
  Object attrValue)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhVariable.addAttribute: var: \"" + varName + "\""
      + "  nm: \"" + attrName + "\""
      + "  type: " + NhVariable.nhTypeNames[atType]);
  }
  if (nhFile.bugs >= 10) {
    prtf("  attrValue: \"%s\"", attrValue);
  }
  NhGroup.checkName( attrName, "attribute in variable \"" + varName + "\"");

  attrValue = getAttrValue(
    attrName,
    attrValue, 
    "variable \"" + varName + "\"",
    nhFile.bugs);

  int dtype = NhVariable.findDtype( attrName, atType);

  // Netcdf cannot read HDF5 attributes that are Scalar STRING_VAR.
  // They must be encoded as STRING_FIX.
  // However datasets can be a scalar STRING_VAR.
  if (dtype == HdfGroup.DTYPE_STRING_VAR
    && testScalar( attrValue))
    dtype = HdfGroup.DTYPE_STRING_FIX;

  // If attrType==DTYPE_STRING_FIX and stgFieldLen==0,
  // MsgAttribute will find the max stg len in attrValue.
  int stgFieldLen = 0;     // max string len for STRING_FIX, without null term

  try {
    hdfVar.addAttribute(
      attrName,
      dtype,
      stgFieldLen,
      attrValue,
      false);              // isVlen
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}




/**
 * Writes the data array for this variable to disk.
 * <p>
 * See {@link HdfGroup#addVariable} for documentation on the legal
 * types of rawData.
 * <p>
 * @param startIxs  The indices of the starting point (lower left corner)
 *    of the hyperslab to be written.  For contiguous storage,
 *    startIxs should be all zeros.
 *    Must have startIxs.length == varDims.length.
 * @param rawData the data array or Object (for a scalar variable)
 *  to be written.
 */

public void writeData(
  int[] startIxs,
  Object rawData)
throws NhException
{
  writeData( startIxs, rawData, false);    // useLinear = false
}




/**
 * Writes the data array for this variable to disk.
 * <p>
 * See {@link HdfGroup#addVariable} for documentation on the legal
 * types of rawData.
 * <p>
 * @param startIxs  The indices of the starting point (lower left corner)
 *    of the hyperslab to be written.  For contiguous storage,
 *    startIxs should be all zeros.
 *    Must have startIxs.length == varDims.length.
 * @param rawData the data array or Object (for a scalar variable)
 *  to be written.
 */

public void writeData(
  int[] startIxs,
  Object rawData,
  boolean useLinear)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhVariable.writeData: nhType: "
      + NhVariable.nhTypeNames[nhType] + "\n"
      + "  startIxs: " + NhGroup.formatInts( startIxs) + "\n"
      + "  useLinear: " + useLinear + "\n"
      + "  rawData class: " + rawData.getClass());
  }
  if (rawData == null) throwerr("rawData is null");

  if (rawData instanceof Array) {
    Array arr = (Array) rawData;
    if (arr.getRank() == 0) {
      // Wow, what a hack.
      if (arr.getSize() != 1) throwerr("unknown array size");
      Object copy1d = arr.copyTo1DJavaArray();
      rawData = java.lang.reflect.Array.get( copy1d, 0);
    }
    else if (useLinear) {
      // copyTo1DJavaArray just calls cp = Array.copy(), cp.getStorage().
      rawData = arr.copyTo1DJavaArray();
    }
    else {
      rawData = ((Array) rawData).copyToNDJavaArray();
    }
  }

  // Yet another special case ...
  // TP_CHAR is represented by HDF5 DTYPE_STRING_FIX with stgFieldLen=1.
  //
  // Externally a 3 x 4 array of TP_CHAR is represented as
  //   String[3] stgs, with stgs[ii].length() = 4.
  //
  // Internally in HDF5, the array is represented as a
  // 3 x 4 array of DTYPE_STRING_FIX, with each string having stgFieldLen=1.

  Object vdata = null;
  if (nhType == TP_CHAR) {
    vdata = convertCharsToStrings( dimLens, rawData, nhFile.bugs);
  }
  else {
    vdata = rawData;
  }

  try { hdfVar.writeData( startIxs, vdata, useLinear); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
} // end writeData





// Caution ...
// In Netcdf attributes are handled differently than variables,
// in particular for char[].
// In HDF5 attributes can have the same datatypes and dimensions
// as datasets (variables).
//
// But in Netcdf only the following types of attribute values
// are legal:
//   - String
//   - 1 dimension array of String
//   - 1 dimension array of numeric type (not char)
//
// In this software when the user passes a 1 dim array char[] ...
//   For a variable we convert it to String[],
//     where each string has length 1, in convertCharsToStrings.
//   For an attribute we convert it to a single String, in
//     getAttrValue.

static Object getAttrValue(
  String attrName,
  Object attrValue,
  String loc,
  int bugs)
throws NhException
{
  Object resValue = null;
  boolean valOk = true;

  if (attrValue == null)
    resValue = null;
  else if (attrValue instanceof Byte)
    resValue = new byte[] { ((Byte) attrValue).byteValue() };
  else if (attrValue instanceof Short)
    resValue = new short[] { ((Short) attrValue).shortValue() };
  else if (attrValue instanceof Integer)
    resValue = new int[] { ((Integer) attrValue).intValue() };
  else if (attrValue instanceof Long)
    resValue = new long[] { ((Long) attrValue).longValue() };
  else if (attrValue instanceof Float)
    resValue = new float[] { ((Float) attrValue).floatValue() };
  else if (attrValue instanceof Double)
    resValue = new double[] { ((Double) attrValue).doubleValue() };
  else if (attrValue instanceof Character) {
    resValue = new String( new char[] {
      ((Character) attrValue).charValue() });
  }

  else if (attrValue instanceof String[])      // allow vec of String
    resValue = attrValue;

  else if (attrValue instanceof byte[]
    || attrValue instanceof short[]
    || attrValue instanceof int[]
    || attrValue instanceof long[]
    || attrValue instanceof float[]
    || attrValue instanceof double[])
  {
    resValue = attrValue;
  }
  else if (attrValue instanceof char[]) {
    char[] vals = (char[]) attrValue;
    if (vals.length == 0) resValue = new String[0];
    else resValue = new String( vals);
  }

  else if (attrValue instanceof Object[]) {
    // Allow vec of objs, each of which is a String
    Object[] objs = (Object[]) attrValue;
    for (Object obj : objs) {
      if (obj == null || ! (obj instanceof String)) valOk = false;
    }
    resValue = attrValue;
  }
  else if (attrValue instanceof String) {      // allow naked String
    resValue = attrValue;
  }
  else valOk = false;

  if (! valOk)
    throwerr("Invalid type for the value of attribute \"%s\""
      + " in %s.  Type: %s",
      attrName, loc, attrValue.getClass().toString());

  if (bugs >= 10) {
    prtf("getAttrValue: loc: " + loc + "  attrName: " + attrName);
    prtf("  specd attrValue: \"%s\"", attrValue);
    prtf("  final resValue:  %s", resValue);
  }

  return resValue;
} // end getAttrValue





// Translate:
//   from: array of char
//   to: array of same rank and shape, of string length 1.
//
//   from: array of strings having arbitrary length
//   to: array with one higher rank where each string has length 1.
//
// Recursively peel off the first dlen and the first dimension of rawData.

static Object convertCharsToStrings(
  int[] dlens,
  Object rawData,
  int bugs)
throws NhException
{
  if (rawData == null) throwerr("rawData is null");
  Object vdata = null;

  // Special case for scalars
  if (dlens.length == 0) {
    if (rawData instanceof Character) {
      Character spec = (Character) rawData;
      vdata = new String( new char[] {spec.charValue()});   // stg len = 1
    }
    else if (rawData instanceof String) {
      String rawStg = (String) rawData;
      if (rawStg.length() > 1) throwerr("scalar data has len > 1");
      vdata = rawStg;       // stg len = 0 or 1
    }
    else throwerr("unknown rawData class: " + rawData.getClass());
  }

  else if (dlens.length == 1) {
    if (rawData instanceof char[]) {
      // Convert char[] to String[] where each element has length 1.
      int nn = dlens[0];
      char[] rawChars = (char[]) rawData;
      if (rawChars.length > nn) throwerr("data len exceeds bounds");

      String[] stgs = new String[nn];
      for (int ii = 0; ii < nn; ii++) {
        if (ii < rawChars.length)
          stgs[ii] = new String( rawChars, ii, 1);   // stg len = 1
        else stgs[ii] = "";
      }
      vdata = stgs;
    }
    else if (rawData instanceof String) {
      // Convert String to String[] where each element has length 1.
      int nn = dlens[0];
      String rawStg = (String) rawData;
      if (rawStg.length() > nn) throwerr("data len exceeds bounds");

      String[] stgs = new String[nn];
      for (int ii = 0; ii < nn; ii++) {
        if (ii < rawStg.length())
          stgs[ii] = rawStg.substring( ii, ii+1);
        else stgs[ii] = "";
      }
      vdata = stgs;
    }
    else throwerr("unknown rawData class: " + rawData.getClass());
  } // else if dlens.length == 1

  else {      // else dlens.length > 1
    // Strip off the first dimension and recurse
    int nn = dlens[0];
    int[] subDlens = Arrays.copyOfRange( dlens, 1, dlens.length);

    if (! (rawData instanceof Object[]))
      throwerr("rawData wrong class: " + rawData.getClass());
    Object[] rawObjs = (Object[]) rawData;
    if (rawObjs.length > dlens[0]) throwerr("data len exceeds bounds");

    Object[] objs = new Object[ nn];
    for (int ii = 0; ii < nn; ii++) {
      objs[ii] = convertCharsToStrings( subDlens, rawObjs[ii], bugs);
    }
    vdata = objs;
  }

  if (bugs >= 1) {
    prtf("convertCharsToStrings: new vdata: " + vdata);
  }

  return vdata;
} // end convertCharsToStrings





static int findDtype(
  String nm,
  int nhTp)
throws NhException
{
  int dtype = 0;
  if (nhTp == TP_SBYTE) dtype = HdfGroup.DTYPE_SFIXED08;
  else if (nhTp == TP_UBYTE) dtype = HdfGroup.DTYPE_UFIXED08;
  else if (nhTp == TP_SHORT) dtype = HdfGroup.DTYPE_FIXED16;
  else if (nhTp == TP_INT) dtype = HdfGroup.DTYPE_FIXED32;
  else if (nhTp == TP_LONG) dtype = HdfGroup.DTYPE_FIXED64;
  else if (nhTp == TP_FLOAT) dtype = HdfGroup.DTYPE_FLOAT32;
  else if (nhTp == TP_DOUBLE) dtype = HdfGroup.DTYPE_FLOAT64;
  else if (nhTp == TP_CHAR) dtype = HdfGroup.DTYPE_STRING_FIX;
  else if (nhTp == TP_STRING_VAR) dtype = HdfGroup.DTYPE_STRING_VAR;
  else throwerr("NhVariable: variable or attr \"%s\" has unknown type: %d",
    nm, nhTp);
  return dtype;
}



static boolean testScalar( Object val) {
  boolean bres = false;
  if (val != null
    && (val instanceof Byte
      || val instanceof Short
      || val instanceof Integer
      || val instanceof Long
      || val instanceof Float
      || val instanceof Double
      || val instanceof Character
      || val instanceof String))
  {
    bres = true;
  }
  return bres;
}





static void throwerr( String msg, Object... args)
throws NhException
{
  throw new NhException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}

} // end class
