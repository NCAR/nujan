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


package hdfnet;



// Msg 21: attribute info

class MsgAttrInfo extends MsgBase {




final int version = 0;

// Flag bits:
//   0:  track creation order for attrs
//   1:  index creation order for attrs
final int flag = 3;


MsgAttrInfo(
  HdfGroup hdfGroup,              // the owning group
  HdfFileWriter hdfFile)
{
  super( TP_ATTR_INFO, hdfGroup, hdfFile);
}




public String toString() {
  String res = super.toString();
  return res;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgAttrInfo: version", version);
  fmtBuf.putBufByte("MsgAttrInfo: flag", flag);
  fmtBuf.putBufShort("MsgAttrInfo: maxCreIx",
    hdfGroup.getNumAttribute());
  fmtBuf.putBufLong("MsgAttrInfo: fractalHeap", HdfFileWriter.UNDEFINED_ADDR);
  fmtBuf.putBufLong("MsgAttrInfo: nameTree", HdfFileWriter.UNDEFINED_ADDR);
  fmtBuf.putBufLong("MsgAttrInfo: orderTree", HdfFileWriter.UNDEFINED_ADDR);
}




} // end class
