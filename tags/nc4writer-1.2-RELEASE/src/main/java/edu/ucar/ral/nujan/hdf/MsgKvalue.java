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
 * HDF5 message type 19: MsgKvalue:
 * Used to keep some btree-related constants.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 *
 * About K values ...
 *
 * <pre>
 * The leafK and internalK values control group b-trees.
 * The storageK value controls chunk b-trees.
 * There are no separate leaf nodes for chunk b-trees.
 *
 * The max num children in the node is 2 * kvalue.
 *
 * Default values:
 *   leafK: 4
 *   internalK: 16
 *   storageK: 32
 * </pre>
 *
 */

class MsgKvalue extends MsgBase {



final int kvalueVersion = 0;



MsgKvalue(
  HdfGroup hdfGroup,              // the owning group
  HdfFileWriter hdfFile)
throws HdfException
{
  super( TP_K_VALUES, hdfGroup, hdfFile);
}




public String toString() {
  String res = super.toString();
  return res;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  //prtf("MsgKvalue: formatPass: %d  maxNumBtreeKid: %d",
  //  formatPass, hdfFile.maxNumBtreeKid);

  // This is twice as big as we need.
  int kvalue = hdfFile.maxNumBtreeKid;

  fmtBuf.putBufByte("MsgKvalue: kvalueVersion", kvalueVersion);
  fmtBuf.putBufShort("MsgKvalue: storageK", kvalue);
  fmtBuf.putBufShort("MsgKvalue: internalK", kvalue);
  fmtBuf.putBufShort("MsgKvalue: leafK", kvalue);
}

} // end class
