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


package edu.ucar.ral.nujan.hdf;


/**
 * HDF5 message type 2: MsgLinkInfo:
 * Used for fileVersion 2 to keep info on tracking and indexing
 * link creation order.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

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
  HdfFileWriter hdfFile)
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
  fmtBuf.putBufLong("MsgLinkInfo: fractalHeapAddr", HdfFileWriter.UNDEFINED_ADDR);
  fmtBuf.putBufLong("MsgLinkInfo: nameIndexAddr", HdfFileWriter.UNDEFINED_ADDR);
  fmtBuf.putBufLong("MsgLinkInfo: creOrderIndexAddr", HdfFileWriter.UNDEFINED_ADDR);

}

} // end class
