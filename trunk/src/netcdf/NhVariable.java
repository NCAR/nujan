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


package nhPkg;

import java.util.Arrays;

import hdfnet.HdfException;
import hdfnet.HdfGroup;
import hdfnet.Util;


public class NhVariable {


public static final int TP_SBYTE       =  1;
public static final int TP_UBYTE       =  2;
public static final int TP_SHORT       =  3;
public static final int TP_INT         =  4;
public static final int TP_LONG        =  5;
public static final int TP_FLOAT       =  6;
public static final int TP_DOUBLE      =  7;
public static final int TP_CHAR        =  8;
public static final int TP_STRING_VAR  =  9;
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
  Object fillValue,
  int compressionLevel,      // 0: no compression;  9: max compression
  NhGroup parentGroup,
  NhFileWriter nhFile)
throws NhException
{
  this.varName = varName;
  this.nhType = nhType;
  this.nhDims = Arrays.copyOf( nhDims, nhDims.length);
  this.fillValue = fillValue;
  this.compressionLevel = compressionLevel;
  this.parentGroup = parentGroup;
  this.nhFile = nhFile;

  rank = nhDims.length;

  //xxx make sure fillValue type corresponds with nhType

  // Translate nhType to dtype
  // Note: fixed len strings are not supported by the Netcdf API.
  // However, Netcdf TP_CHAR is translated into HDF5 DTYPE_STRING_FIX
  // with stgFieldLen = 1.
  // That is, an array of null-terminated strings all having length 1.
  // Although declared as H5T_STR_NULLTERM, the null-termination
  // isn't stored in the file.

  boolean isScalar = false;
  if (nhDims.length == 0) isScalar = true;

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
    //xxxif (nhType == TP_STRING_VAR) {
    //xxx  stgFieldLen = 
    //xxx}
  }

  // Build int[] dimLens from nhDims 
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

//xxx debug everywhere

//xxx cleaner way of "if bugs" ...


  // We don't allow compression of TP_STRING_* -
  // deliberately not implemented.
  // It turns out that HDF5 compresses the references to
  // variable length strings, but not the strings themselves.
  // The strings remain in the global heap GCOL, uncompressed.
  if (compressionLevel > 0 && nhType == TP_STRING_VAR)
    throwerr("cannot use compression with TP_STRING_*");

  // Scalars cannot be compressed or chunked.
  if (dimLens.length == 0 && compressionLevel > 0)
    throwerr("cannot use compression with scalar data");

  // HDF5 must be chunked for compression.
  boolean isChunked;
  if (compressionLevel == 0) isChunked = false;
  else isChunked = true;

  try {
    hdfVar = parentGroup.hdfGroup.addVariable(
      varName,
      dtype,
      stgFieldLen,          // max stg len, including null termination
      dimLens,              // dimension lengths
      hdfFillValue,
      isChunked,
      compressionLevel);
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  // Add us to each dimension's refList
  for (NhDimension nhDim : nhDims) {
    nhDim.refList.add( this);
  }

} // end constructor





public String toString() {
  String res = String.format(
    "name: \"%s\"  type: %s  compress: %d  rank: %d  dims: (",
    varName, nhTypeNames[nhType], compressionLevel, rank);
  for (NhDimension nd : nhDims) {
    res += "  " + nd.dimName + "(" + nd.dimLen + ")";
  }
  res += ")";
  return res;
}




public String getName() { return varName; }

public int getType() { return nhType; }

public NhDimension[] getDimensions() { return nhDims; }

public Object getFillValue() { return fillValue; }

public int getCompressionLevel() { return compressionLevel; }

public NhGroup getParentGroup() { return parentGroup; }

public NhFileWriter getFileWriter() { return nhFile; }







//xxx unify logic with nhgroup.addattr.

public void addAttribute(
  String attrName,
  int atType,                // one of TP_*
  Object attrValue,
  boolean isVlen)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhVariable.addAttribute: var: \"" + varName + "\""
      + "  attrName: \"" + attrName
      + "\"  value: " + Util.formatObject( attrValue));
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
  if (dtype == HdfGroup.DTYPE_STRING_VAR && testScalar( attrValue))
    dtype = HdfGroup.DTYPE_STRING_FIX;

  int stgFieldLen = 0;     // max string len for STRING_FIX, without null term
  if (atType == TP_CHAR) stgFieldLen = 1;

  try {
    hdfVar.addAttribute(
      attrName,
      dtype,
      stgFieldLen,
      attrValue,
      isVlen);
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}




//xxx clean up makefiles

//xxx testall: if debug, display java output.


public void writeData(
  Object rawData)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhVariable.writeData: nhType: " + NhVariable.nhTypeNames[nhType]
      + "  rawData: " + Util.formatObject( rawData));
  }
  if (rawData == null) throwerr("rawData is null");
  Object vdata = null;

  // Yet another special case ...
  // TP_CHAR is represented by HDF5 DTYPE_STRING_FIX with stgFieldLen=1.
  //
  // Externally a 3 x 4 array of TP_CHAR is represented as
  //   String[3] stgs, with stgs[ii].length() = 4.
  //
  // Internally in HDF5, the array is represented as a
  // 3 x 4 array of DTYPE_STRING_FIX, with each string having stgFieldLen=1.

  if (nhType == TP_CHAR)
    vdata = convertCharsToStrings( dimLens, rawData, nhFile.bugs);
  else vdata = rawData;

  try { hdfVar.writeData( vdata); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
} // end writeData





// xxx
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

//xxx del:
static Object getAttrValue(
  String attrName,
  Object attrValue,
  String loc,
  int bugs)
throws NhException
{
  if (attrValue == null) throwerr("attribute value is null");
  Object resValue = null;
  boolean valOk = true;
  if (attrValue instanceof byte[]
    || attrValue instanceof short[]
    || attrValue instanceof int[]
    || attrValue instanceof long[]
    || attrValue instanceof float[]
    || attrValue instanceof double[])
  {
    resValue = attrValue;
  }
  else if (attrValue instanceof char[]) {
    //int[] dimLens = new int[] {((char[]) attrValue).length};
    //resValue = NhVariable.convertCharsToStrings( dimLens, attrValue, bugs);
    resValue = new String( (char[]) attrValue);
  }
  //xxx recall netcdf only allows 1dim vec attrs.

  else if (attrValue instanceof String[]) {    // allow vec of String
    resValue = attrValue;
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

  if ((! valOk) || resValue == null)
    throwerr("Invalid type for the value of attribute \"%s\""
      + " in %s.  Type: %s",
      attrName, loc, attrValue.getClass().toString());

  if (bugs >= 1) {
    prtf("getAttrValue: loc: " + loc + "  attrName: " + attrName);
    prtf("  attrValue: " + Util.formatObject( attrValue));
    prtf("  resValue: " + Util.formatObject( resValue));
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
    prtf("convertCharsToStrings: new vdata: " + Util.formatObject( vdata));
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
  if (val instanceof Byte
    || val instanceof Short
    || val instanceof Integer
    || val instanceof Long
    || val instanceof Float
    || val instanceof Double
    || val instanceof Character
    || val instanceof String)
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
