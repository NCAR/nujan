
package hdfnet;


// Msg 08: layout

class MsgLayout extends MsgBase {


static final int LY_COMPACT    = 0;
static final int LY_CONTIGUOUS = 1;
static final int LY_CHUNKED    = 2;
static final String[] classNames = {"COMPACT", "CONTIGUOUS", "CHUNKED"};


final int layoutVersion = 3;

int layoutClass;                  // one of LY_*
int compressionLevel;

// If chunked ...
BtreeNode chunkBtree;




MsgLayout(
  int layoutClass,
  int compressionLevel,
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
throws HdfException
{
  super( TP_LAYOUT, hdfGroup, hdfFile);
  this.layoutClass = layoutClass;
  this.compressionLevel = compressionLevel;
  if (compressionLevel > 0 && layoutClass != LY_CHUNKED)
    throwerr("if compressed must use chunked");

  if (layoutClass == LY_CHUNKED) {     // if chunked
    chunkBtree = new BtreeNode( compressionLevel, hdfGroup, hdfFile);
  }
  else chunkBtree = null;
}




public String toString() {
  String res = super.toString();
  res += "  layoutClass: " + layoutClass;
  res += "  grp.rawDataAddr: " + hdfGroup.rawDataAddr;
  res += "  grp.rawDataSize: " + hdfGroup.rawDataSize;
  return res;
}







// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgLayout: layoutVersion", layoutVersion);
  fmtBuf.putBufByte("MsgLayout: layoutClass", layoutClass);

  if (layoutClass == LY_COMPACT) {     // if compact
    throwerr("compact not yet tested");
    //fmtBuf.putBufShort("MsgLayout: compactSize", compactSize);
    //for (int ii = 0; ii < compactSize; ii++) {
    //  fmtBuf.putBufByte("MsgLayout: compactData", compactData[ii]);
    //}
  }
  else if (layoutClass == LY_CONTIGUOUS) {     // if contiguous
    // Data block
    fmtBuf.putBufLong("MsgLayout: contig rawDataAddr",
      hdfGroup.rawDataAddr);
    fmtBuf.putBufLong("MsgLayout: contig rawDataSize",
      hdfGroup.rawDataSize);
  }
  else if (layoutClass == LY_CHUNKED) {     // if chunked
    int rank = hdfGroup.msgDataSpace.rank;
    fmtBuf.putBufByte("MsgLayout: chunk rank+1", rank + 1);

    // External block
    fmtBuf.putBufLong(
      "MsgLayout: chunkBtree.pos", chunkBtree.blkPosition);
    if (formatPass != 0) hdfFile.addWork("MsgLayout", chunkBtree);

    for (int ii = 0; ii < rank; ii++) {
      fmtBuf.putBufInt("MsgLayout: chunk dim",
        hdfGroup.msgDataSpace.varDims[ii]);
    }
    fmtBuf.putBufInt("MsgLayout: chunk elementLen",
      hdfGroup.msgDataType.elementLen);
  }
  else throwerr("unknown layoutClass: %d", layoutClass);
}





} // end class
