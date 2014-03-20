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
 * Base class for Msg*.
 * Subclasses must implement formatMsgCore.
 * <pre>
 *
 * There are two main routes for calling formatMsgCore.
 *
 * 1. Via HdfGroup for its message table:
 *    HdfGroup.formatBuf (implements BaseBlk.formatBuf):
 *      calls layoutVersion2, which calls MsgBase.formatFullMsg
 *    MsgBase.formatFullMsg:
 *      Formats msg header, then calls abstract formatMsgCore.
 *
 * 2. Via MsgAttribute for its internal MsgDataType and MsgDataSpace:
 *    MsgAttribute.formatMsgCore
 *        (which is called from MsgBase.formatFullMsg, from HdfGroup.formatBuf)
 *      Calls msgDataType.formatNakedMsg.
 *        This resolves to MsgBase.formatNakedMsg, 
 *      Calls msgDataSpace.formatNakedMsg similarly.
 *    MsgBase.formatNakedMsg:
 *      Calls abstract formatMsgCore, without the msg header.
 * </pre>
 */

abstract class MsgBase extends BaseBlk {

// Lengths of header fields, in bytes:
//   hdrMsgType(1), hdrMsgSize(2), hdrMsgFlag(1), hdrMsgCreOrder(2)

static final int MSG_HDR_LEN_V2 = 6;

// set bit 0 of hdrMsgFlag: value is constant
static final int FLAG_CONSTANT = 1;


/**
 * HDF5 message type 0: MsgNil: NIL msg to be ignored;
 * not used in this package
 */
static final int TP_NIL              =  0;

/**
 * HDF5 message type 1: MsgDataSpace: contains dimension info
 */
static final int TP_DATASPACE        =  1;

/**
 * HDF5 message type 2: MsgLinkInfo:
 * Used to keep info on tracking and indexing
 * the link creation order.
 */
static final int TP_LINKINFO         =  2;

/**
 * HDF5 message type 3: MsgDataType:
 * contains data type info (fixed/float/string/etc, elementLen, etc).
 */
static final int TP_DATATYPE         =  3;

/**
 * HDF5 message type 4: obsolete fill value (not used in this package).
 */
static final int TP_OLD_FILL_VALUE   =  4;

/**
 * HDF5 message type 5: MsgFillValue: specify data fill value.
 */
static final int TP_FILL_VALUE       =  5;

/**
 * HDF5 message type 6: MsgLinkit:
 * Used by to specify a link to another group,
 * either from parent to child group or from group to variable.
 */
static final int TP_LINKIT           =  6;

/**
 * HDF5 message type 7: not used in this package; HDF5 uses it
 * for data objects stored in a separate file.
 */
static final int TP_EXTERNAL_FILE    =  7;

/**
 * HDF5 message type 8: MsgLayout:
 * Specifies the raw data addr or the Btree that describes the raw data.
 */
static final int TP_LAYOUT           =  8;

/**
 * HDF5 message type 9: not used in this package; HDF5 uses it
 * for internal testing.
 */
static final int TP_BOGUS            =  9;

/**
 * HDF5 message type 10: MsgGroupInfo:
 * Used to keep some group-related constants.
 */
static final int TP_GROUPINFO        = 10;

/**
 * HDF5 message type 11: MsgFilter:
 * Used to keep info on the encode/decode filter pipeline;
 * this package uses it only for the DEFLATE (compression) filter.
 */
static final int TP_FILTER           = 11;

/**
 * HDF5 message type 12: MsgAttribute:
 * Contains both the attribute name and value.
 */
static final int TP_ATTRIBUTE        = 12;

/**
 * HDF5 message type 13: comment.
 * Not used in this package.
 */
static final int TP_COMMENT          = 13;

/**
 * HDF5 message type 14: obsolete modification time.
 * Not used in this package.
 */
static final int TP_OLD_MOD_TIME     = 14;

/**
 * HDF5 message type 15: shared message table info.
 * Not used in this package: we don't use shared messages.
 */
static final int TP_SHARED_MESSAGE   = 15;

/**
 * HDF5 message type 16: object header continuation.
 * Not used in this package: we keep the headers all in
 * a contiguous block, so don't need continuations.
 */
static final int TP_OBJ_HDR_CONTIN   = 16;

/**
 * HDF5 message type 17: symbol table.
 * Used in obsolete fileVersion 1 to point to the symbol table's
 * root BtreeNode and to the LocalHeap.
 * Not used in this package.
 */
static final int TP_SYMBOL_TABLE     = 17;

/**
 * HDF5 message type 18: MsgModTime:
 * object modification time.
 */
static final int TP_MOD_TIME         = 18;

/**
 * HDF5 message type 19: non-default K values in superblock extension.
 * Not used in this package: we don't need superblock extensions,
 * and all K values we use are the defaults declared in the superblock.
 */
static final int TP_K_VALUES         = 19;

/**
 * HDF5 message type 20: file driver info.
 * Not used in this package: we don't use custom drivers.
 */
static final int TP_DRIVER_INFO      = 20;

/**
 * HDF5 message type 21: MsgAttrInfo:
 * attribute info (not the Attribute itself - see MsgAttribute).
 */
static final int TP_ATTR_INFO        = 21;

/**
 * HDF5 message type 22: object reference count.
 * Not used in this package.
 */
static final int TP_OBJ_REF_COUNT    = 22;

/**
 * Names of TP_* constants.
 */
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
// hdrMsgSize does NOT include the length of pad to a multiple of 8.

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






/**
 * @param hdrMsgType One of TP_*.
 * @param hdfGroup The group containing this message.
 * @param hdfFile The global owning HdfFileWriter.
 */

MsgBase(
  int hdrMsgType,
  HdfGroup hdfGroup,
  HdfFileWriter hdfFile)
{
  super( hdrMsgTypeNames[ hdrMsgType], hdfFile);
  this.hdrMsgType = hdrMsgType;

  this.hdfGroup = hdfGroup;

  // It looks like the hdrMsgCreationOrder field in the
  // group header is always 0.
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







/**
 * Formats this message including the header info:
 * <ul>
 *   <li> hdrMsgType, hdrMsgSize, hdrMsgFlag, hdrMsgCreOrder
 * </ul>
 * Called by HdfGroup.formatBuf, HdfGroup.layoutVersion2 (from formatBuf).
 */

void formatFullMsg( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  hdfFile.indent++;
  if (hdfFile.bugs >= 5) {
    String stg = String.format(
      "formatFullMsg: msgType: %d (%s)  size: %d  flag: 0x%x  creOrder: %d"
        + "  in grp: \"%s\"",
      hdrMsgType, hdrMsgTypeNames[hdrMsgType], hdrMsgSize, hdrMsgFlag,
      hdrMsgCreOrder, hdfGroup.getPath());

    if (this instanceof MsgAttribute)
      stg += "  attrName: " + ((MsgAttribute) this).attrName;

    prtIndent( stg);
  }

  long svPos = fmtBuf.getPos();
  blkPosition = svPos;     // not needed for internal block

  fmtBuf.putBufByte("MsgBase: hdrMsgType", hdrMsgType);
  fmtBuf.putBufShort("MsgBase: hdrMsgSize", hdrMsgSize);
  fmtBuf.putBufByte("MsgBase: hdrMsgFlag", hdrMsgFlag);
  fmtBuf.putBufShort("MsgBase: hdrMsgCreOrder", hdrMsgCreOrder);

  long svCore = fmtBuf.getPos();

  // Write the msg.  Implemented by subclass like MsgModTime.
  formatMsgCore( formatPass, fmtBuf);

  hdrMsgSize = (int) (fmtBuf.getPos() - svCore);
  int specHdrLen = 0;
  specHdrLen = MSG_HDR_LEN_V2;
  if (fmtBuf.getPos() != svPos + specHdrLen + hdrMsgSize)
    throwerr("formatFullMsg: len mismatch."
      + "  svPos: 0x%x  HLEN: 0x%x  hmsgSize: 0x%x  curPos: 0x%x",
      svPos, specHdrLen, hdrMsgSize, fmtBuf.getPos());

  hdfFile.indent--;
} // end formatFullMsg





/**
 * Formats this message without the header info.
 * This is used only by MsgAttribute.formatMsgCore
 * to format the attribute's internal MsgDataType and MsgDataSpace.
 */

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





/**
 * Extends abstract BaseBlk: it is illegal to call formatBuf
 * for a Msg* class since the Msg* classes are never formatted
 * alone.  They are always formatted within an HdfGroup
 * or MsgAttribute.
 *
 * @param formatPass: <ul>
 *   <li> 1: Initial formatting to determine the formatted length.
 *          In HdfGroup we add msgs to hdrMsgList.
 *   <li> 2: Final formatting.
 * </ul>
 * @param fmtBuf  output buffer
 */

void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  throwerr("formatBuf illegal for Msg* classes");
}



abstract void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException;


} // end class
