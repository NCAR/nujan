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


abstract class MsgBase extends BaseBlk {

// Len of head fields:
// fileVersion==1:
//   hdrMsgType(2), hdrMsgSize(2), hdrMsgFlag(1), reserved(3)
// fileVersion==2:
//   hdrMsgType(1), hdrMsgSize(2), hdrMsgFlag(1), hdrMsgCreOrder(2)
static final int MSG_HDR_LEN_V1 = 8;
static final int MSG_HDR_LEN_V2 = 6;

// set bit 0 of hdrMsgFlag: value is constant
static final int FLAG_CONSTANT = 1;



static final int TP_NIL              =  0;
static final int TP_DATASPACE        =  1;
static final int TP_LINKINFO         =  2;
static final int TP_DATATYPE         =  3;
static final int TP_OLD_FILL_VALUE   =  4;
static final int TP_FILL_VALUE       =  5;
static final int TP_LINKIT           =  6;
static final int TP_EXTERNAL_FILE    =  7;
static final int TP_LAYOUT           =  8;
static final int TP_BOGUS            =  9;
static final int TP_GROUPINFO        = 10;
static final int TP_FILTER           = 11;
static final int TP_ATTRIBUTE        = 12;
static final int TP_COMMENT          = 13;
static final int TP_OLD_MOD_TIME     = 14;
static final int TP_SHARED_MESSAGE   = 15;
static final int TP_OBJ_HDR_CONTIN   = 16;
static final int TP_SYMBOL_TABLE     = 17;
static final int TP_MOD_TIME         = 18;
static final int TP_K_VALUES         = 19;
static final int TP_DRIVER_INFO      = 20;
static final int TP_ATTR_INFO        = 21;
static final int TP_OBJ_REF_COUNT    = 22;

static final String[] hdrMsgTypeNames = {
  "Nil",
  "Dataspace",
  "LinkInfo",
  "Datatype",
  "Obsolete fill value",
  "Fill value",
  "Linkit",
  "External data files",
  "Layout",
  "Bogus - testing only",
  "GroupInfo",
  "Filter pipeline",
  "Attribute",
  "Object comment",
  "Obsolete object modification time",
  "Shared message table",
  "Object header continuation",
  "Symbol table message",
  "Object modification time",
  "Btree K values",
  "Driver info",
  "Attribute info",
  "Object reference count"
};


HdfGroup hdfGroup;                 // the owning group


int hdrMsgType;                    // see doc in Object Header
int hdrMsgCreOrder;                // creation order

// Caution:
// group V1: hdrMsgSize INCLUDES the length of pad to multiple of 8,
//   unlike MsgAttribute.dataTypeSize and dataSpaceSize.
// group V2: hdrMsgSize does NOT include the length of pad to multiple of 8.

int hdrMsgSize;                    // size of data part of msg, in bytes

// Bits for hdrMsgFlag:
//   0  msg data is constant
//   1  msg is shared
//   2  msg should not be shared
//   3  hdf5 decoder should fail if it doesn't understand the msg type
//   4  hdf5 decoder should set bit 5 if it doesn't understand the msg type
//   5  if set, this object was modified by software that didn't understand
//   6  if set, object is shareable
//   7  reserved
int hdrMsgFlag;








MsgBase(
  int hdrMsgType,
  HdfGroup hdfGroup,
  HdfFileWriter hdfFile)
{
  super( hdrMsgTypeNames[ hdrMsgType], hdfFile);
  this.hdrMsgType = hdrMsgType;

  this.hdfGroup = hdfGroup;

  // It looks like the hdrMsgCreationOrder field in the
  // V2 group header is always 0.
  // If some day we need to make it count the header messages,
  // have HdfGroup.formatBuf use:
  //    hdfMsgCreOrder = 0;
  //    for (MsgBase hmsg : hdrMsgList) {
  //      hmsg.formatFullMsg( formatPass, fmtBuf);
  //      hdfMsgCreOrder++;
  //    }
  // And here copy in hdfGroup.hdrMsgCreOrder.

  hdrMsgCreOrder = 0;

  hdrMsgSize = 0;
  hdrMsgFlag = FLAG_CONSTANT;
}



public String toString() {
  String res = super.toString();
  res += "  hdrMsgType: " + hdrMsgTypeNames[ hdrMsgType]
    + "  hdrMsgSize: " + hdrMsgSize
    + "  hdrMsgFlag: " + hdrMsgFlag;
  return res;
}








// Format the msg including the header info:
//   V1: hdrMsgType, hdrMsgSize, hdrMsgFlag
//   V2: hdrMsgType, hdrMsgSize, hdrMsgFlag, hdrMsgCreOrder

void formatFullMsg( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  hdfFile.indent++;
  if (hdfFile.bugs >= 5) {
    prtIndent("formatFullMsg: msgType: %d (%s)  size: %d  flag: 0x%x  creOrder: %d",
      hdrMsgType, hdrMsgTypeNames[hdrMsgType], hdrMsgSize, hdrMsgFlag,
      hdrMsgCreOrder);
  }

  long svPos = fmtBuf.getPos();
  blkPosition = svPos;     // not needed for internal block

  if (hdfFile.fileVersion == 1) {
    fmtBuf.putBufShort("MsgBase: hdrMsgType", hdrMsgType);
    if (hdrMsgSize % 8 != 0) throwerr("hdrMsgSize % 8 != 0");
    fmtBuf.putBufShort("MsgBase: hdrMsgSize", hdrMsgSize);
    fmtBuf.putBufByte("MsgBase: hdrMsgFlag", hdrMsgFlag);
    fmtBuf.putBufByte("MsgBase: reserved", 0);
    fmtBuf.putBufShort("MsgBase: reserved", 0);
  }
  if (hdfFile.fileVersion == 2) {
    fmtBuf.putBufByte("MsgBase: hdrMsgType", hdrMsgType);
    fmtBuf.putBufShort("MsgBase: hdrMsgSize", hdrMsgSize);
    fmtBuf.putBufByte("MsgBase: hdrMsgFlag", hdrMsgFlag);
    fmtBuf.putBufShort("MsgBase: hdrMsgCreOrder", hdrMsgCreOrder);
  }

  long svCore = fmtBuf.getPos();

  // Write the msg.  Implemented by subclass like MsgModTime.
  formatMsgCore( formatPass, fmtBuf);

  if (hdfFile.fileVersion == 1) {
    // Pad length up to multiple of 8
    while ((fmtBuf.getPos() - svPos) % 8 != 0) {
      fmtBuf.putBufByte("MsgBase: msgPad", 0);
    }
  }

  hdrMsgSize = (int) (fmtBuf.getPos() - svCore);
  int specHdrLen = 0;
  if (hdfFile.fileVersion == 1) specHdrLen = MSG_HDR_LEN_V1;
  if (hdfFile.fileVersion == 2) specHdrLen = MSG_HDR_LEN_V2;
  if (fmtBuf.getPos() != svPos + specHdrLen + hdrMsgSize)
    throwerr("formatFullMsg: len mismatch."
      + "  svPos: 0x%x  HLEN: 0x%x  hmsgSize: 0x%x  curPos: 0x%x",
      svPos, specHdrLen, hdrMsgSize, fmtBuf.getPos());

  hdfFile.indent--;
} // end formatFullMsg





// Format the msg without hdrMsgType, hdrMsgSize, hdrMsgFlag

void formatNakedMsg( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  hdfFile.indent++;
  if (hdfFile.bugs >= 5) {
    prtIndent("formatNakedMsg: msgType: %d (%s)  size: %d  flag: 0x%x",
      hdrMsgType, hdrMsgTypeNames[hdrMsgType], hdrMsgSize, hdrMsgFlag);
  }

  long svPos = fmtBuf.getPos();
  blkPosition = svPos;     // not needed for internal block

  // Write the msg.  Implemented by subclass like MsgModTime.
  formatMsgCore( formatPass, fmtBuf);
  hdrMsgSize = (int) (fmtBuf.getPos() - svPos);

  hdfFile.indent--;
} // end formatNakedMsg




void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  throwerr("formatBuf illegal for Msg* classes");
}



abstract void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException;


} // end class
