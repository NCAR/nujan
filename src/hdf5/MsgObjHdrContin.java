
package hdfnet;


// Msg 16: object header continuation

class MsgObjHdrContin extends MsgBase {

long continAddr;              // position of continuation
long continLen;              // len of continuation



MsgObjHdrContin(
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
{
  super( TP_OBJ_HDR_CONTIN, hdfGroup, hdfFile);
}




public String toString() {
  String res = super.toString();
  res += "  continAddr: " + continAddr;
  res += "  continLen: " + continLen;
  return res;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  throwerr("We do not use continuations");
  fmtBuf.putBufLong("MsgObjHdrContin: continAddr", continAddr);
  fmtBuf.putBufLong("MsgObjHdrContin: continLen", continLen);
}




} // end class
