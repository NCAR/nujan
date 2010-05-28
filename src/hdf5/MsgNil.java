
package hdfnet;


// Msg 00: NIL msg, to be ignored

class MsgNil extends MsgBase {

int nilSize = 0;

MsgNil(
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
{
  super( TP_NIL, hdfGroup, hdfFile);
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  for (int ii = 0; ii < nilSize; ii++) {
    fmtBuf.putBufByte("MsgNil: msg 0 content", 0);
  }
}




} // end class
