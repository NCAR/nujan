
package ncHdf;

import java.util.Arrays;

import hdfnet.HdfException;
import hdfnet.HdfGroup;


public class NcVariable {


public static final int TP_BYTE        =  1;
public static final int TP_SHORT       =  2;
public static final int TP_INT         =  3;
public static final int TP_LONG        =  4;
public static final int TP_FLOAT       =  5;
public static final int TP_DOUBLE      =  6;
public static final int TP_STRING_FIX  =  7;
public static final int TP_STRING_VAR  =  8;
public static final String[] ncTypeNames = {
  "UNKNOWN", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE",
  "STRING_FIX", "STRING_VAR"};


String varName;            // variable name
int ncType;                // one of TP_*
int stgFieldLen;           // max string len for STRING types,
                           // including null termination
NcDimension[] ncDims;      // shared dimensions
Object fillValue;
int compressionLevel;      // 0: no comp;  9: max comp
HdfGroup parentGroup;
NcFile ncFile;

int rank;
int dtype;                 // one of HdfGroup.DTYPE_*
int[] dimLens;             // len of each ncDims element

HdfGroup hdfVar;



NcVariable(
  String varName,            // variable name
  int ncType,                // one of TP_*
  int stgFieldLen,           // max string len for STRING types,
                             // including null termination
  NcDimension[] ncDims,      // shared dimensions
  Object fillValue,
  int compressionLevel,      // 0: no comp;  9: max comp
  HdfGroup parentGroup,
  NcFile ncFile)
throws NcException
{
  this.varName = varName;
  this.ncType = ncType;
  this.stgFieldLen = stgFieldLen;
  this.ncDims = Arrays.copyOf( ncDims, ncDims.length);
  this.fillValue = fillValue;
  this.compressionLevel = compressionLevel;
  this.parentGroup = parentGroup;
  this.ncFile = ncFile;

  rank = ncDims.length;
  prtf("######### NcVariable: rank: %d", rank);

  // Translate ncType to dtype
  dtype = 0;
  if (ncType == TP_BYTE) dtype = HdfGroup.DTYPE_FIXED08;
  else if (ncType == TP_SHORT) dtype = HdfGroup.DTYPE_FIXED16;
  else if (ncType == TP_INT) dtype = HdfGroup.DTYPE_FIXED32;
  else if (ncType == TP_LONG) dtype = HdfGroup.DTYPE_FIXED64;
  else if (ncType == TP_FLOAT) dtype = HdfGroup.DTYPE_FLOAT32;
  else if (ncType == TP_DOUBLE) dtype = HdfGroup.DTYPE_FLOAT64;
  else if (ncType == TP_STRING_FIX) dtype = HdfGroup.DTYPE_STRING_FIX;
  else if (ncType == TP_STRING_VAR) dtype = HdfGroup.DTYPE_STRING_VAR;
  else throwerr("NcVariable: variable \"%s\" has unknown type: %d",
    varName, ncType);

  // Build int[] dimLens from ncDims 
  dimLens = new int[ rank];
  for (int ii = 0; ii < rank; ii++) {
    int dlen = ncDims[ii].dimLen;
    if (dlen <= 0 || dlen >= Integer.MAX_VALUE) {
      throwerr("NcVariable: variable \"%s\", dimension %d,"
        + " has illegal value: %d",
        varName, ii, dlen);
    }
    dimLens[ii] = dlen;
  }

  // Scalars cannot be compressed or chunked.
  // Must be chunked for compression.
  if (dimLens.length == 0 && compressionLevel > 0)
    throwerr("cannot use compression with scalar data");

  boolean isChunked;
  if (ncDims.length == 0 || compressionLevel == 0) isChunked = false;
  else isChunked = true;

  prtf("########### fill: \"%s\"", fillValue);
  prtf("########### stgFieldLen: %d", stgFieldLen);
  try {
    hdfVar = parentGroup.addVariable(
      varName,
      dtype,
      stgFieldLen,         // max stg len, including null termination
      dimLens,              // dimension lengths
      fillValue,
      isChunked,
      compressionLevel);
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  // Add us to each dimension's refList
  for (NcDimension ncDim : ncDims) {
    ncDim.refList.add( this);
  }

  if (rank > 0) {
    // Create matrix of dimension variables, one dimVar per row,
    // so we can make vlen DIMENSION_LIST.
    HdfGroup[][] dimVarMat = new HdfGroup[rank][1];
    for (int ii = 0; ii < rank; ii++) {
      dimVarMat[ii][0] = ncDims[ii].hdfVar;
    }

    try {
      hdfVar.addAttribute(
        "DIMENSION_LIST",     // attrName
        dimVarMat,            // attrValue
        true,                 // isVlen
        false);               // isCompoundRef
    }
    catch( HdfException exc) {
      exc.printStackTrace();
      throwerr("caught: " + exc);
    }
  }
} // end constructor





public void addAttribute(
  String attrName,
  Object attrValue)
throws NcException
{
  try {
    hdfVar.addAttribute(
      attrName,
      attrValue,
      false,                // isVlen
      false);               // isCompoundRef
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}


public void writeData(
  Object vdata)
throws NcException
{
  try { hdfVar.writeData( vdata); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}



static void throwerr( String msg, Object... args)
throws NcException
{
  throw new NcException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}

} // end class
