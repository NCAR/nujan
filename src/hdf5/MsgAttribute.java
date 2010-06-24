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


package hdfnet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


// Msg 12: attribute

class MsgAttribute extends MsgBase {

String attrName;
int attrType;              // one of DTYPE_*
int stgFieldLen;
Object attrValue;
boolean isVlen;

MsgDataType msgDataType;
MsgDataSpace msgDataSpace;

int dataDtype;
int[] attrDims;
int[] dsubTypes = null;




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

  //xxx if we are passed an empty array, Object[],
  // for example for the reflist for an unused dimension,
  // there is no way to find the dataDype.

  // Find dtype and attrDims
  int[] dtypeAndDims = HdfUtil.getDtypeAndDims( isVlen, attrValue);
  dataDtype = dtypeAndDims[0];
  attrDims = Arrays.copyOfRange( dtypeAndDims, 1, dtypeAndDims.length);

  // Check that the dataDtype and attrDims match what the user declared.
  HdfUtil.checkTypeMatch( getPath(), attrType, dataDtype, attrDims, attrDims);

  String[] memberNames = null;
  if (isVlen) {
    if (attrType == HdfGroup.DTYPE_COMPOUND)
      throwerr("Attribute cannot be both isVlen and DTYPE_COMPOUND."
        + "  attribute named \"%s\"", attrName);
    if (attrDims.length != 2)
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
    if (attrDims.length != 2) throwerr("wrong num dimensions for vlen");
    attrDims = Arrays.copyOfRange( attrDims, 0, 1);
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
  msgDataSpace = new MsgDataSpace(
    attrDims,
    hdfGroup,
    hdfFile);

} // end constructor








public String toString() {
  String res = "  attr path: \"" + getPath() + "\"\n"
    + "  type: " + HdfUtil.formatDtypeDim( attrType, attrDims) + "\n"
    + "  stgFieldLen: " + stgFieldLen + "\n"
    + "  attrValue: dataDtype: " + HdfGroup.dtypeNames[ dataDtype]
    + "  class: " + attrValue.getClass() + "\n";
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





String getPath()
{
  String nm = hdfGroup.getPath();
  nm += "/" + attrName;
  return nm;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  byte[] nameBytes = HdfUtil.encodeString( attrName, false, hdfGroup);
  if (hdfFile.fileVersion == 1) {
    fmtBuf.putBufByte("MsgAttribute: attrVersion", 1);
    fmtBuf.putBufByte("MsgAttribute: reserved", 0);
    // Name must have null termination
    fmtBuf.putBufShort(
      "MsgAttribute: nameSize", nameBytes.length + 1);   // +1 for null term
    fmtBuf.putBufShort(
      "MsgAttribute: dataTypeSize", msgDataType.hdrMsgSize);
    fmtBuf.putBufShort(
      "MsgAttribute: dataSpaceSize", msgDataSpace.hdrMsgSize);

    fmtBuf.putBufBytes("MsgAttribute: attr name", nameBytes);
    fmtBuf.putBufByte("MsgAttribute: attr name null term", 0);
    fmtBuf.alignPos( "attr align", 8);
  }
  if (hdfFile.fileVersion == 2) {
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
  }

  msgDataType.formatNakedMsg( formatPass, fmtBuf);
  if (hdfFile.fileVersion == 1) fmtBuf.alignPos( "attr align", 8);

  msgDataSpace.formatNakedMsg( formatPass, fmtBuf);
  if (hdfFile.fileVersion == 1) fmtBuf.alignPos( "attr align", 8);

  if (isVlen) {
    // Vlen: format the globalHeap references to hdfFile.bbuf
    int[] heapIxs = hdfFile.mainGlobalHeap.putHeapVlenObject(
      hdfGroup,
      msgDataType.dtype,
      msgDataType.subMsgs[0].dtype,
      msgDataType.subMsgs[0].elementLen,    // stgLen, without null term
      attrDims,
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
    hdfGroup.formatRawData(
      attrType,
      stgFieldLen,
      attrValue,
      new Counter(),
      hdfFile.mainGlobalHeap.blkPosition,  // gcolAddr for DTYPE_STRING_VAR
      hdfFile.mainGlobalHeap,              // gcol for DTYPE_STRING_VAR
      refBuf);
    fmtBuf.putBufBuf("STRING_VAR refs", refBuf);    // write out references
  } // if DTYPE_STRING_VAR

  else {
    // Raw data: format the raw data to fmtBuf
    hdfGroup.formatRawData(
      attrType,
      stgFieldLen,
      attrValue,
      new Counter(),
      0,                       // gcolAddr for DTYPE_STRING_VAR
      null,                    // gcol for DTYPE_STRING_VAR
      fmtBuf);
  }

} // end formatMsgCore

} // end class
