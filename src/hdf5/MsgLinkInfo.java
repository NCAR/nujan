
package hdfnet;


// Msg 2: variables for "new groups".

class MsgLinkInfo extends MsgBase {



final int linkInfoVersion = 0;

// Flag bits:
//   bit  mask  desc
//   0     1   track link creation order
//   1     2   index link creation order
//   2-7         reserved

final int linkInfoFlag = 1 | 2;  // track and index link order



MsgLinkInfo(
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
throws HdfException
{
  super( TP_LINKINFO, hdfGroup, hdfFile);
}




public String toString() {
  String res = super.toString();
  return res;
}







// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgLinkInfo: linkInfoVersion", linkInfoVersion);
  fmtBuf.putBufByte("MsgLinkInfo: linkInfoFlag", linkInfoFlag);

  if ((linkInfoFlag & 1) != 0) {
    fmtBuf.putBufLong("MsgLinkInfo: maxCreIx", hdfGroup.linkCreationOrder);
  }
  fmtBuf.putBufLong("MsgLinkInfo: fractalHeapAddr", HdfFile.UNDEFINED_ADDR);
  fmtBuf.putBufLong("MsgLinkInfo: nameIndexAddr", HdfFile.UNDEFINED_ADDR);
  fmtBuf.putBufLong("MsgLinkInfo: creOrderIndexAddr", HdfFile.UNDEFINED_ADDR);

}

} // end class
