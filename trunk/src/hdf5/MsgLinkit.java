
package hdfnet;


// Msg 6: link to another group

class MsgLinkit extends MsgBase {



final int linkitVersion = 1;

// Flag bits:
//   bit  mask  desc
//   0-1   3   len of LinkNameLen field:  0: 1 byte, 1: 2, 2: 4, 3: 8
//   2     4   creation order field is present
//   3     8   link type field is present.  If not present, is hard link.
//   4    16   char set field is present.  If not present, ASCII.
//   5-7         reserved

final int linkFlag = 3 | 4;
  // linkNameLen is 8 bytes; creation order is present
  // No link type, no char set.

long linkOrder;              // creation order in the owning group
HdfGroup linkGroup;          // the sub group


MsgLinkit(
  long linkOrder,                 // creation order in the owning group
  HdfGroup linkGroup,             // the sub group
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
throws HdfException
{
  super( TP_LINKIT, hdfGroup, hdfFile);
  this.linkOrder = linkOrder;
  this.linkGroup = linkGroup;
}




public String toString() {
  String res = super.toString();
  res += "  linkOrder: " + linkOrder;
  res += "  linkGroup name: " + linkGroup.groupName;
  return res;
}







// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgLinkit: linkitVersion", linkitVersion);
  fmtBuf.putBufByte("MsgLinkit: linkFlag", linkFlag);
  fmtBuf.putBufLong("MsgLinkit: linkOrder", linkOrder);

  byte[] nameEnc = Util.encodeString(
    linkGroup.groupName, false, hdfGroup);  // no null term
  fmtBuf.putBufLong("MsgLinkit: linkName len", nameEnc.length);
  fmtBuf.putBufBytes("MsgLinkit: linkName", nameEnc);

  // External block
  fmtBuf.putBufLong("MsgLinkit: linkGroup", linkGroup.blkPosition);
  if (formatPass != 0) hdfFile.addWork("MsgLinkit", linkGroup);
}

} // end class
