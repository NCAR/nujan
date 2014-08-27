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


/**
 * HDF5 message type 8: MsgLayout:
 * Specifies the raw data addr or the Btree that describes the raw data.
 */

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
  HdfFileWriter hdfFile)
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
  for (HdfChunk chunk : hdfGroup.hdfChunks) {
    res += "  chunk: addr: " + chunk.chunkDataAddr
      + " size: " + chunk.chunkDataSize;
  }
  return res;
}







// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgLayout: layoutVersion", layoutVersion);
  fmtBuf.putBufByte("MsgLayout: layoutClass", layoutClass);

  if (layoutClass == LY_COMPACT) {     // if compact
    throwerr("compact not yet implemented");
    //fmtBuf.putBufShort("MsgLayout: compactSize", compactSize);
    //for (int ii = 0; ii < compactSize; ii++) {
    //  fmtBuf.putBufByte("MsgLayout: compactData", compactData[ii]);
    //}
  }
  else if (layoutClass == LY_CONTIGUOUS) {     // if contiguous
    // Data block
    fmtBuf.putBufLong("MsgLayout: contig rawDataAddr",
      hdfGroup.hdfChunks[0].chunkDataAddr);
    fmtBuf.putBufLong("MsgLayout: contig rawDataSize",
      hdfGroup.hdfChunks[0].chunkDataSize);
  }
  else if (layoutClass == LY_CHUNKED) {     // if chunked
    fmtBuf.putBufByte("MsgLayout: chunk rank+1", hdfGroup.varRank + 1);

    // External block
    fmtBuf.putBufLong(
      "MsgLayout: chunkBtree.pos", chunkBtree.blkPosition);
    if (formatPass != 0) hdfFile.addWork("MsgLayout", chunkBtree);

    for (int ii = 0; ii < hdfGroup.varRank; ii++) {
      fmtBuf.putBufInt("MsgLayout: chunk dim",
        hdfGroup.specChunkDims[ii]);
    }
    fmtBuf.putBufInt("MsgLayout: chunk elementLen", hdfGroup.elementLen);
  }
  else throwerr("unknown layoutClass: %d", layoutClass);
}

} // end class
