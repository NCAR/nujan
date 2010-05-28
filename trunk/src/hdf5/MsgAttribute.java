
package hdfnet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


// Msg 12: attribute

class MsgAttribute extends MsgBase {

boolean isVlen;
boolean isCompoundRef;
String attrName;
Object attrValue;

MsgDataType msgDataType;
MsgDataSpace msgDataSpace;

int dtype;
int[] varDims;



MsgAttribute(
  boolean isVlen,
  boolean isCompoundRef,
  String attrName,
  Object attrValue,
  HdfGroup hdfGroup,                    // the owning group
  HdfFile hdfFile)
throws HdfException
{
  super( TP_ATTRIBUTE, hdfGroup, hdfFile);
  this.isVlen = isVlen;
  this.isCompoundRef = isCompoundRef;
  this.attrName = attrName;
  this.attrValue = attrValue;

  if (isVlen && isCompoundRef)
    throwerr("Attribute cannot be both isVlen and isCompoundRef."
      + "  attribute named \"%s\"",
      attrName);

  // Find dtype and varDims
  int[] dtypeAndDims = Util.getDtypeAndDims( isVlen, attrValue);
  dtype = dtypeAndDims[0];
  varDims = Arrays.copyOfRange( dtypeAndDims, 1, dtypeAndDims.length);

  int[] dsubTypes = null;
  String[] memberNames = null;
  if (isVlen) {
    if (varDims.length != 2)
      throwerr("cannot have VLEN attribute with num dims != 2");
    dsubTypes = new int[] { dtype};
    dtype = HdfGroup.DTYPE_VLEN;
  }
  else if (isCompoundRef) {
    dtype = HdfGroup.DTYPE_COMPOUND;
    if (! (attrValue instanceof HdfGroup[]))
      throwerr("isCompoundRef attrValue must be HdfDataset[]");
    HdfGroup[] attrRefs = (HdfGroup[]) attrValue;
    varDims = new int[] { attrRefs.length};
    dsubTypes = new int[] { HdfGroup.DTYPE_REFERENCE, HdfGroup.DTYPE_FIXED32};
    memberNames = new String[] {"dataset", "dimension"};
  }

  // Use max stgLen + 1 for null termination.
  // Func getMaxStgLen returns max string len,
  // without null termination, if attrValue is a
  // String, String[], String[][], etc.
  // Returns 0 otherwise.
  int stgFieldLen = Util.getMaxStgLen( attrValue);
  if (stgFieldLen > 0) stgFieldLen++;     // add null termination

  if (hdfFile.bugs >= 2) {
    String tmsg = "MsgAttribute: attrName: \"" + attrName + "\"\n"
      + "  attrValue object: " + attrValue + "\n"
      + "  attrValue class: " + attrValue.getClass() + "\n"
      + "  attrValue dtype: " + HdfGroup.dtypeNames[ dtype] + "\n";
    tmsg +=  "  dsubTypes:";
    if (dsubTypes == null) tmsg += " (null)\n";
    else {
      for (int ii = 0; ii < dsubTypes.length; ii++) {
        tmsg += " " + HdfGroup.dtypeNames[ dsubTypes[ii]];
      }
      tmsg +=  "\n";
    }
    tmsg += "  attrValue stgFieldLen: " + stgFieldLen + "\n"
      + "  attrValue rank: " + varDims.length + "\n"
      + "  attrValue dims:";
    for (int ii = 0; ii < varDims.length; ii++) {
      tmsg += " " + varDims[ii];
    }
    tmsg +=  "\n";
    prtf( tmsg);
  }

  // Handle vlen dims.
  // Although we are given a two dimensional array,
  // internally HDF5 handles it as a one dimensional array.
  if (isVlen) {
    if (varDims.length != 2) throwerr("wrong num dimensions for vlen");
    varDims = Arrays.copyOfRange( varDims, 0, 1);
  }

  // Create the MsgDataType
  prtf("################# dtype: " + HdfGroup.dtypeNames[dtype]);
  msgDataType = new MsgDataType(
    dtype,
    dsubTypes,
    memberNames,         // member names for DTYPE_COMPOUND
    stgFieldLen,
    hdfGroup,
    hdfFile);

  // Create the MsgDataSpace
  msgDataSpace = new MsgDataSpace(
    varDims,
    hdfGroup,
    hdfFile);

} // end constructor








public String toString() {
  String res = super.toString();
  res += "  attrName: \"" + attrName + "\"";
  return res;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  byte[] nameBytes = Util.encodeString( attrName, false, hdfGroup);
  if (hdfFile.fileVersion == 1) {
    fmtBuf.putBufByte("MsgAttribute: attrVersion", 1);
    fmtBuf.putBufByte("MsgAttribute: reserved", 0);
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
      msgDataType.subMsgs[0].elementLen,      // stgLen, including null term
      varDims,
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

  else if (isCompoundRef) {
    if (! (attrValue instanceof HdfGroup[]))
      throwerr("isCompoundRef attrValue must be HdfDataset[]");
    HdfGroup[] attrRefs = (HdfGroup[]) attrValue;
    for (int ii = 0; ii < attrRefs.length; ii++) {
      fmtBuf.putBufLong("MsgAttribute: cmpnd pos",
        attrRefs[ii].blkPosition);
      fmtBuf.putBufInt("MsgAttribute: cmpnd num", ii + 1);
    }
  }

  else {
    // Raw data: format the raw data to fmtBuf
    hdfGroup.formatRawData( msgDataType.elementLen, attrValue, fmtBuf);
  }

} // end formatMsgCore


} // end class
