
package hdfnet;


// Msg 17: symbol table

class MsgSymbolTable extends MsgBase {

BtreeNode btreeNode;
LocalHeap localHeap;



MsgSymbolTable(
  BtreeNode btreeNode,
  LocalHeap localHeap,
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
{
  super( TP_SYMBOL_TABLE, hdfGroup, hdfFile);
  this.btreeNode = btreeNode;
  this.localHeap = localHeap;
}







// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufLong(
    "MsgSymbolTable: btreeNode.pos", btreeNode.blkPosition);
  fmtBuf.putBufLong(
    "MsgSymbolTable: localHeap.pos", localHeap.blkPosition);
}



} // end class
