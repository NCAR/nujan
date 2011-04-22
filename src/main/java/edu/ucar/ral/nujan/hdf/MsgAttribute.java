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
import java.util.Arrays;


/**
 * HDF5 message type 12: MsgAttribute:
 * Contains both the attribute name and value.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 * <p>
 * A new MsgAttribute is created in HdfGroup.addAttribute.
 */

class MsgAttribute extends MsgBase {

/**
 * The attribute's local name.
 */
String attrName;

/**
 * The type of attrValue: one of HdfGroup.DTYPE_*.
 * Must agree with dataDtype, which is the type of
 * attrValue as determined by HdfUtil.getDimLen.
 */
int attrType;

/**
 * String length for a DTYPE_STRING_FIX variable, without null termination.
 * Should be 0 for all other types, including DTYPE_STRING_VAR.
 */
int stgFieldLen;

/**
 * The attribute value.
 * The type of attrValue must agree with attrType.
 */
Object attrValue;

/**
 * True this array is a 2-dimensional array
 * in which rows may have differing lengths (ragged right edge).
 */
boolean isVlen;

/**
 * The internal MsgDataType (part of this MsgAttribute).
 */
MsgDataType msgDataType;

/**
 * The internal MsgDataSpace (part of this MsgAttribute).
 */
MsgDataSpace msgDataSpace;

/**
 * The type of attrValue (one of HdfGroup.DTYPE_) as determined
 * by HdfUtil.getDimLen.  Must agree with attrType.
 */
int dataDtype;

/**
 * The total number of elements in attrValue,
 * as determined by HdfUtil.getDimLen.
 */
int totNumEle;

/**
 * The length in bytes of the elements of attrValue,
 * as determined by HdfUtil.getDimLen.
 * For example, if Integer or int[] or int[][] or..., elementLen = 4.
 * For String and char[], elementLen = max len encountered.
 */
int elementLen;

/**
 * Length of the dimensions of attrValue,
 * as determined by HdfUtil.getDimLen.
 */
int[] dataVarDims;

/**
 * For DTYPE_VLEN or DTYPE_COMPOUND, the types of the contained
 * elements (values of HdfGroup.DTYPE_* ).
 */
int[] dsubTypes = null;



/**
 * Creates a new MsgAttribute.
 *
 * @param attrName
 * The attribute's local name.
 *
 * @param attrType
 * The type of attrValue: one of HdfGroup.DTYPE_*.
 * Must agree with dataDtype, which is the type of
 * attrValue as determined by HdfUtil.getDimLen.
 *
 * @param attrstgFieldLen
 * String length for a DTYPE_STRING_FIX variable, without null termination.
 * Should be 0 for all other types, including DTYPE_STRING_VAR.
 *
 * @param attrValue
 * The attribute value.
 * The type of attrValue must agree with attrType.
 *
 * @param isVlen
 * True this array is a 2-dimensional array
 * in which rows may have differing lengths (ragged right edge).
 *
 * @param hdfGroup The owning HdfGroup.
 *
 * @param hdfFile The global owning HdfFileWriter.
 */

MsgAttribute(
  String attrName,
  int attrType,              // one of DTYPE_*
  int stgFieldLenParm,
  Object attrValue,
  boolean isVlen,
  HdfGroup hdfGroup,         // the owning group
  HdfFileWriter hdfFile)
throws HdfException
{
  super( TP_ATTRIBUTE, hdfGroup, hdfFile);
  this.attrName = attrName;
  this.attrType = attrType;
  this.stgFieldLen = stgFieldLenParm;
  this.attrValue = attrValue;
  this.isVlen = isVlen;

  int[] dataInfo = HdfUtil.getDimLen( attrValue, isVlen);
  dataDtype = dataInfo[0];
  totNumEle = dataInfo[1];
  elementLen = dataInfo[2];
  dataVarDims = Arrays.copyOfRange( dataInfo, 3, dataInfo.length);

  if (hdfFile.bugs >= 1) {
    prtf("MsgAttribute: actual data:\n"
      + "  attrValue object: \"%s\"\n"
      + "  attrValue dtype: " + HdfGroup.dtypeNames[ dataDtype] + "\n"
      + "  attrValue totNumEle: " + totNumEle + "\n"
      + "  attrValue elementLen: " + elementLen + "\n"
      + "  attrValue rank: " + dataVarDims.length + "\n"
      + "  attrValue type and dims: "
      + HdfUtil.formatDtypeDim( dataDtype, dataVarDims),
      attrValue);
    if (attrValue != null)
      prtf("  attrValue class: " + attrValue.getClass());
  }

  if (totNumEle * elementLen > 65535 - 1000)   // -1000 for hdr, slack, etc.
    throwerr("Attribute total length too big.  totNumEle: %d"
      + "  elementLen: %d  attr path: %s",
      totNumEle, elementLen, getPath());

  // Check that the dataDtype and dataVarDims match what the user declared.
  if (attrValue != null) {
    HdfUtil.checkTypeMatch(
      getPath(),
      attrType,
      dataDtype,
      false,                          // useLinear
      dataVarDims,                    // var dims
      new int[dataVarDims.length],    // startIxs == all 0
      dataVarDims,                    // specChunkDims
      dataVarDims);                   // dataDims
  }

  String[] memberNames = null;
  if (isVlen) {
    if (attrType == HdfGroup.DTYPE_COMPOUND)
      throwerr("Attribute cannot be both isVlen and DTYPE_COMPOUND."
        + "  attribute named \"%s\"", attrName);
    if (dataVarDims.length != 2)
      throwerr("cannot have VLEN attribute with num dims != 2");
    dsubTypes = new int[] { attrType};
    attrType = HdfGroup.DTYPE_VLEN;
  }
  else if (attrType == HdfGroup.DTYPE_COMPOUND) {
    dsubTypes = new int[] { HdfGroup.DTYPE_REFERENCE, HdfGroup.DTYPE_FIXED32};
    memberNames = new String[] {"dataset", "dimension"};
  }

  // Func getMaxStgLen returns max string len,
  // without null termination, if attrValue is a
  // String, String[], String[][], etc.
  // Returns 0 otherwise.
  if (attrType == HdfGroup.DTYPE_STRING_FIX && stgFieldLen == 0) {
    stgFieldLen = HdfUtil.getMaxStgLen( attrValue);
    // The HDF5 C API complains "unknown datatype" if stgFieldLen = 0
    // for a fixed len string.
    if (stgFieldLen == 0) stgFieldLen = 1;
  }
  if (hdfFile.bugs >= 2) prtf("MsgAttribute: " + this);

  // Handle vlen dims.
  // Although we are given a two dimensional array,
  // internally HDF5 handles it as a one dimensional array.
  if (isVlen) {
    if (dataVarDims.length != 2) throwerr("wrong num dimensions for vlen");
    dataVarDims = Arrays.copyOfRange( dataVarDims, 0, 1);
  }

  // Create the MsgDataType
  msgDataType = new MsgDataType(
    attrType,
    dsubTypes,
    memberNames,         // member names for DTYPE_COMPOUND
    stgFieldLen,
    hdfGroup,
    hdfFile);

  // Create the MsgDataSpace
  // If totNumEle is 0, use H5S_NULL: null dataspace.
  int[] vdims = dataVarDims;
  if (totNumEle == 0) vdims = null;
  msgDataSpace = new MsgDataSpace(
    dataVarDims.length,      // rank
    totNumEle,
    vdims,
    hdfGroup,
    hdfFile);

} // end constructor








public String toString() {
  String res = "  attr path: \"" + getPath() + "\"\n"
    + "  type: " + HdfUtil.formatDtypeDim( attrType, dataVarDims) + "\n"
    + "  stgFieldLen: " + stgFieldLen + "\n"
    + "  attrValue: dataDtype: " + HdfGroup.dtypeNames[ dataDtype]
    + "  class: ";
  if (attrValue == null) res += "(null)\n";
  else res += attrValue.getClass().getName() + "\n";

  if (dsubTypes != null) {
    res +=  "  dsubTypes:";
    for (int ii = 0; ii < dsubTypes.length; ii++) {
      res += " " + HdfGroup.dtypeNames[ dsubTypes[ii]];
    }
    res +=  "\n";
  }
  if (hdfFile.bugs >= 10) {
    res += "  " + super.toString() + "\n";
  }
  return res;
}





/**
 * Returns the full path name of this attribute; for example
 * "/someGroup/someVariable/attrName".
 */
String getPath()
{
  String nm = hdfGroup.getPath();
  nm += "/" + attrName;
  return nm;
}





/**
 * Extends abstract MsgBase:
 * formats everything after the message header into fmtBuf.
 * Called by MsgBase.formatFullMsg and MsgBase.formatNakedMsg.
 */

void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  byte[] nameBytes = HdfUtil.encodeString( attrName, false, hdfGroup);
  fmtBuf.putBufByte("MsgAttribute: attrVersion", 3);

  // Flag bits:
  //   0  datatype is shared
  //   1  dataspace is shared
  fmtBuf.putBufByte("MsgAttribute: flag", 0);

  // Name must have null termination
  fmtBuf.putBufShort(
    "MsgAttribute: nameSize", nameBytes.length + 1);   // +1 for null term
  fmtBuf.putBufShort(
    "MsgAttribute: dataTypeSize", msgDataType.hdrMsgSize);
  fmtBuf.putBufShort(
    "MsgAttribute: dataSpaceSize", msgDataSpace.hdrMsgSize);
  fmtBuf.putBufByte("MsgAttribute: encoding", 0);

  fmtBuf.putBufBytes("MsgAttribute: attr name", nameBytes);
  fmtBuf.putBufByte("MsgAttribute: attr name null term", 0);

  msgDataType.formatNakedMsg( formatPass, fmtBuf);

  msgDataSpace.formatNakedMsg( formatPass, fmtBuf);

  int[] startIxs = new int[ dataVarDims.length];

  if (attrValue == null) {
    // Don't write null attrValue
  }
  else if (isVlen) {
    // Vlen: format the globalHeap references to hdfFile.mainGlobalHeap.
    // Get back heap ref indices, one per VLEN row.
    int[] heapIxs = hdfFile.mainGlobalHeap.putHeapVlenObject(
      hdfGroup,
      msgDataType.dtype,
      msgDataType.subMsgs[0].dtype,
      msgDataType.subMsgs[0].elementLen,    // stgLen, without null term
      dataVarDims,
      attrValue);
    if (hdfFile.bugs >= 2) {
      String tmsg = "MsgAttribute vlen: attrName: \"" + attrName + "\"\n"
        + "  heapIxs len: " + heapIxs.length + "\n"
        + "  heapIxs:";
      for (int ii = 0; ii < heapIxs.length; ii++) {
        tmsg += " " + heapIxs[ii];
      }
      prtf( tmsg);
    }

    hdfGroup.formatVlenRawData( heapIxs, attrValue, fmtBuf);
  }

  else if (msgDataType.dtype == HdfGroup.DTYPE_STRING_VAR) {
    // Vlen: format the globalHeap references to hdfFile.bbuf
    int compressionLevel = 0;      // cannot compress heap objects
    HBuffer refBuf = new HBuffer( null, compressionLevel, hdfFile);
    int[] curIxs = new int[dataVarDims.length];
    if (hdfFile.bugs >= 2)
      prtf("MsgAttribute: call formatRawData for string data");
    hdfGroup.formatRawData(
      "attrName: " + attrName,
      0,                   // curLev
      curIxs,
      false,               // useLinear
      attrType,
      stgFieldLen,
      dataVarDims,         // varDims
      dataVarDims,         // chnLens
      dataVarDims,         // dataDims
      elementLen,
      startIxs,
      attrValue,
      new HdfModInt(0),
      hdfFile.mainGlobalHeap.blkPosition,  // gcolAddr for DTYPE_STRING_VAR
      hdfFile.mainGlobalHeap,              // gcol for DTYPE_STRING_VAR
      refBuf);
    fmtBuf.putBufBuf("STRING_VAR refs", refBuf);    // write out references
  } // if DTYPE_STRING_VAR

  else {
    // Raw data: format the raw data to fmtBuf
    int[] curIxs = new int[dataVarDims.length];
    if (hdfFile.bugs >= 2)
      prtf("MsgAttribute: call formatRawData for numeric data");
    hdfGroup.formatRawData(
      "attrName: " + attrName,
      0,                   // curLev
      curIxs,
      false,               // useLinear
      attrType,
      stgFieldLen,
      dataVarDims,         // varDims
      dataVarDims,         // chnLens
      dataVarDims,         // dataDims
      elementLen,
      startIxs,
      attrValue,
      new HdfModInt(0),
      0,                   // gcolAddr for DTYPE_STRING_VAR
      null,                // gcol for DTYPE_STRING_VAR
      fmtBuf);

    // Kluge: When an attribute is null or length == 0,
    // the HDF5 C library still writes out a single scalar value: 0.
    // And the reader reads it.
    if (totNumEle == 0)
      fmtBuf.putBufLong( "MsgAttribute: empty attr kluge", 0);
  }

} // end formatMsgCore

} // end class
