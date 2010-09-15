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
 * HDF5 message type 10: MsgGroupInfo:
 * Used to keep some group-related constants.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

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
  HdfFileWriter hdfFile)
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
