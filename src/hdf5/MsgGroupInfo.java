
package hdfnet;


// Msg 10: constants for "new groups".

class MsgGroupInfo extends MsgBase {



final int groupInfoVersion = 0;

// Flag bits:
//   bit  mask  desc
//   0     1   link phase change values are stored
//   1     2   estimated entry info is stored
//   2-7       reserved

final int groupFlag = 0;



MsgGroupInfo(
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
throws HdfException
{
  super( TP_GROUPINFO, hdfGroup, hdfFile);
}




public String toString() {
  String res = super.toString();
  return res;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgGroupInfo: groupInfoVersion", groupInfoVersion);
  fmtBuf.putBufByte("MsgGroupInfo: groupFlag", groupFlag);
}

} // end class
