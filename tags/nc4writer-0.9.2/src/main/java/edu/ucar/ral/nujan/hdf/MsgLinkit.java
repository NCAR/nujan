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
 * HDF5 message type 6: MsgLinkit:
 * Used to specify a link to another group,
 * either from parent to child group or from group to variable.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

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
  HdfFileWriter hdfFile)
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

  byte[] nameEnc = HdfUtil.encodeString(
    linkGroup.groupName, false, hdfGroup);  // no null term
  fmtBuf.putBufLong("MsgLinkit: linkName len", nameEnc.length);
  fmtBuf.putBufBytes("MsgLinkit: linkName", nameEnc);

  // External block
  fmtBuf.putBufLong("MsgLinkit: linkGroup", linkGroup.blkPosition);
  if (formatPass != 0) hdfFile.addWork("MsgLinkit", linkGroup);
}

} // end class
