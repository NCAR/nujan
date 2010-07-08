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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.ArrayList;


public class HdfGroup extends BaseBlk {



// Overall type summary
public static final int DTYPE_UNKNOWN       =  0;
public static final int DTYPE_SFIXED08      =  1;
public static final int DTYPE_UFIXED08      =  2;
public static final int DTYPE_FIXED16       =  3;
public static final int DTYPE_FIXED32       =  4;
public static final int DTYPE_FIXED64       =  5;
public static final int DTYPE_FLOAT32       =  6;
public static final int DTYPE_FLOAT64       =  7;
public static final int DTYPE_TEST_CHAR     =  8;  // for internal test only
public static final int DTYPE_STRING_FIX    =  9;
public static final int DTYPE_STRING_VAR    = 10;
public static final int DTYPE_REFERENCE     = 11;
public static final int DTYPE_VLEN          = 12;
public static final int DTYPE_COMPOUND      = 13;
public static final String[] dtypeNames = {
  "UNKNOWN",
  "SFIXED08", "UFIXED08", "FIXED16", "FIXED32", "FIXED64",
  "FLOAT32", "FLOAT64", "TEST_CHAR",
  "STRING_FIX", "STRING_VAR", "REFERENCE",
  "VLEN", "COMPOUND"};

// Signature is used for fileVersion==2 only:
final int signa = 'O';
final int signb = 'H';
final int signc = 'D';
final int signd = 'R';

// Referenced blocks
ArrayList<HdfGroup> subGroupList = null;
ArrayList<HdfGroup> subVariableList = null;

// fileVersion==1 only:
BtreeNode btreeNode;
LocalHeap localHeap;



// Blocks contained in hdrMsgList
MsgDataType msgDataType;
MsgDataSpace msgDataSpace;
MsgLayout msgLayout;
MsgFillValue msgFillValue;
MsgModTime msgModTime;
MsgAttrInfo msgAttrInfo;
MsgFilter msgFilter;

// fileVersion==1 only:
MsgSymbolTable msgSymbolTable;



boolean isVariable;               // false: group;  true: represents data
String groupName;
HdfGroup parentGroup;
int stgFieldLen;
int compressionLevel;

ArrayList<MsgBase> hdrMsgList;

// Variables
final boolean isVlen = false;    // VLEN variables are not supported.
                                 // (although VLEN attributes are supported).

int dtype;                       // one of DTYPE*


long rawDataAddr;
long rawDataSize;
boolean isWritten = false;       // has this variable been written

// fileVersion==2 only:
int linkCreationOrder = 0;






// Create a group, not a variable
HdfGroup(
  String groupName,
  HdfGroup parentGroup,        // is null when creating the rootGroup
  HdfFileWriter hdfFile)
throws HdfException
{
  super("HdfGroup: " + groupName, hdfFile);
  this.isVariable = false;
  this.groupName = groupName;
  this.parentGroup = parentGroup;

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup: new group at path: \"" + getPath() + "\"");
  }
  if (parentGroup != null) {     // if not creating root group
    HdfUtil.checkName( groupName,
      "subGroup in group \"" + parentGroup.groupName + "\"");
  }

  subGroupList = new ArrayList<HdfGroup>();
  subVariableList = new ArrayList<HdfGroup>();
  msgModTime = new MsgModTime( hdfFile.utcModTimeMilliSec, this, hdfFile);

  // Build message list
  hdrMsgList = new ArrayList<MsgBase>();
  hdrMsgList.add( msgModTime);

  if (hdfFile.fileVersion == 1) {
    localHeap = new LocalHeap( hdfFile);
    btreeNode = new BtreeNode( localHeap, hdfFile);
    msgSymbolTable = new MsgSymbolTable( btreeNode, localHeap, this, hdfFile);
    hdrMsgList.add( msgSymbolTable);
  }
  else if (hdfFile.fileVersion == 2) {
    msgAttrInfo = new MsgAttrInfo( this, hdfFile);
    hdrMsgList.add( msgAttrInfo);
  }
}




// Create a variable
HdfGroup(
  String groupName,
  HdfGroup parentGroup,
  int dtype,                 // one of DTYPE*
  int[] dsubTypes,           // subType for DTYPE_VLEN
                             // or DTYPE_COMPOUND
  String[] subNames,         // member names for DTYPE_COMPOUND

  int stgFieldLen,           // string length for DTYPE_STRING_FIX.
                             // Without null termination.
                             // Should be 0 for all other types,
                             // including DTYPE_STRING_VAR.

  int[] varDims,
  Object fillValue,          // null, Byte, Short, Int, Long,
                             // Float, Double, String, etc.
  boolean isChunked,
  int compressionLevel,
  HdfFileWriter hdfFile)
throws HdfException
{
  super("HdfGroup: (var) " + groupName, hdfFile);
  this.isVariable = true;
  this.groupName = groupName;
  this.parentGroup = parentGroup;
  this.dtype = dtype;
  this.stgFieldLen = stgFieldLen;
  this.compressionLevel = compressionLevel;

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup: new dataset at path: \"" + getPath() + "\""
      + "  type: " + HdfUtil.formatDtypeDim( dtype, varDims));
  }
  HdfUtil.checkName( groupName,
    "dataset in group \"" + parentGroup.groupName + "\"");

  if (varDims == null) {
    if (isChunked) throwerr("cannot use chunked with null data");
    if (compressionLevel > 0)
      throwerr("cannot use compression with null data");
  }
  else if (varDims.length == 0) {
    if (isChunked) throwerr("cannot use chunked with scalar data");
    if (compressionLevel > 0)
      throwerr("cannot use compression with scalar data");
  }
  if (compressionLevel > 0 && ! isChunked) {
    throwerr("if compressed, must use chunked");
  }

  msgDataType = new MsgDataType(
    dtype, dsubTypes, subNames, stgFieldLen, this, hdfFile);

  msgDataSpace = new MsgDataSpace( varDims, this, hdfFile);

  int layoutClass = MsgLayout.LY_CONTIGUOUS;
  if (isChunked) layoutClass = MsgLayout.LY_CHUNKED;
  msgLayout = new MsgLayout( layoutClass, compressionLevel, this, hdfFile);

  boolean isFillExtant = false;
  int fillLen = 0;
  if (fillValue != null) {
    isFillExtant = true;
    fillLen = msgDataType.elementLen;
  }
  msgFillValue = new MsgFillValue(
    dtype, isFillExtant, fillValue, fillLen, this, hdfFile);
  msgModTime = new MsgModTime(
    hdfFile.utcModTimeMilliSec, this, hdfFile);

  // Build message list
  hdrMsgList = new ArrayList<MsgBase>();
  hdrMsgList.add( msgDataType);
  hdrMsgList.add( msgDataSpace);
  hdrMsgList.add( msgLayout);
  hdrMsgList.add( msgFillValue);
  hdrMsgList.add( msgModTime);
  if (compressionLevel > 0) {
    msgFilter = new MsgFilter(
      MsgFilter.FILT_DEFLATE,
      compressionLevel,
      this, hdfFile);
    hdrMsgList.add( msgFilter);
  }

  if (hdfFile.fileVersion == 2) {
    msgAttrInfo = new MsgAttrInfo( this, hdfFile);
    hdrMsgList.add( msgAttrInfo);
  }
}








/**
 * Creates a sub-group of the current group.
 */

public HdfGroup addGroup(
  String subName)                   // name of new subGroup
throws HdfException
{
  if (hdfFile.fileStatus != HdfFileWriter.ST_DEFINING)
    throwerr("cannot define after calling endDefine");
  if (isVariable) throwerr("cannot add a group to a variable");
  if (findSubItem( subName) != null)
    throwerr("Duplicate subgroup.  The group \"%s\" already contains"
      + "  a subgroup or variable named \"%s\"",
      groupName, subName);

  HdfGroup subGroup = new HdfGroup( subName, this, hdfFile);

  addSubGroup( subGroup);
  return subGroup;
}




/**
 * Creates a variable in the current group.
 */

public HdfGroup addVariable(
  String varName,
  int dtype,                 // one of DTYPE*
  int stgFieldLen,           // string length for DTYPE_STRING_FIX.
                             // Without null termination.
                             // Should be 0 for all other types,
                             // including DTYPE_STRING_VAR.

  int[] varDims,             // dimension lengths
  Object fillValue,          // fill value or null
  boolean isChunked,
  int compressionLevel)
throws HdfException
{
  if (hdfFile.fileStatus != HdfFileWriter.ST_DEFINING)
    throwerr("cannot define after calling endDefine");
  if (isVariable) throwerr("cannot add a variable to a variable");
  if (findSubItem( varName) != null)
    throwerr("Duplicate subgroup.  The group \"%s\" already contains"
      + "  a subgroup or variable named \"%s\"",
      groupName, varName);

  int[] dsubTypes = null;
  String[] subNames = null;
  if (dtype == HdfGroup.DTYPE_COMPOUND) {
    dsubTypes = new int[] { HdfGroup.DTYPE_REFERENCE, HdfGroup.DTYPE_FIXED32};
    subNames = new String[] {"dataset", "dimension"};
  }

  HdfGroup var = new HdfGroup(
    varName,
    this,
    dtype,
    dsubTypes,
    subNames,
    stgFieldLen,
    varDims,
    fillValue,
    isChunked,
    compressionLevel,
    hdfFile);

  addSubGroup( var);
  return var;
} // endVariable









/**
 * Creates an attribute in the current group or variable.
 */

public void addAttribute(
  String attrName,
  int attrType,              // one of DTYPE_*
  int stgFieldLen,
  Object attrValue,
  boolean isVlen)
throws HdfException
{
  if (hdfFile.bugs >= 1) {
    String tmsg = "HdfGroup.addAttribute: \"" + getPath()
      + "/" + attrName + "\"" + "  cls: ";
    if (attrValue == null) tmsg += "(null)";
    else tmsg += attrValue.getClass().getName();
    prtf( tmsg);
  }
  if (hdfFile.bugs >= 5) {
    prtf("  attr isVlen: " + isVlen);
    prtf("  attrValue: " + HdfUtil.formatObject( attrValue));
  }
  HdfUtil.checkName( attrName,
    "attribute in group \"" + groupName + "\"");

  if (findAttribute( attrName) != null)
    throwerr("Duplicate attribute.  The group \"%s\" already contains"
      + "  an attribute named \"%s\"",
      getPath(), attrName);

  // If attrType==DTYPE_STRING_FIX and stgFieldLen==0,
  // MsgAttribute will find the max stg len in attrValue.
  MsgAttribute msgAttr = new MsgAttribute(
    attrName,
    attrType,
    stgFieldLen,
    attrValue,
    isVlen,
    this,
    hdfFile);
  hdrMsgList.add( msgAttr);
  if (hdfFile.bugs >= 5) {
    prtf("HdfGroup.addAttribute: added name: \"" + attrName + "\"\n"
      + "  at path: \"" + getPath() + "\"\n"
      + "  dtype: " + dtypeNames[attrType] + "\n"
      + "  type: " + HdfUtil.formatDtypeDim(
        attrType, msgAttr.dataVarDims) + "\n"
      + "  isVlen: " + isVlen);
  }
}








public String toString() {
  String res = "HdfGroup: " + getPath()
    + "  isVariable: " + isVariable + "\n"
    + "  blkPosition: " + blkPosition + "\n";
  if (isVariable) {
    res += "  dtype: " + dtypeNames[ dtype] + "\n"
      + "  msgDataType: " + msgDataType + "\n"
      + "  msgDataSpace: " + msgDataSpace;
  }
  else {
    res += "  subGroupList: (";
    for (HdfGroup grp : subGroupList) {
      res += " " + grp.groupName;
    }
    res += ")";
    res += "  subVariableList: (";
    for (HdfGroup grp : subVariableList) {
      res += " " + grp.groupName;
    }
    res += ")";
  }
  return res;
}




String getPath()
{
  String nm = "";
  HdfGroup grp = this;
  while (grp != null) {
    if (nm.length() != 0) nm = "/" + nm;
    nm = grp.groupName + nm;
    grp = grp.parentGroup;
  }
  return nm;
}



void addSubGroup(
  HdfGroup subGroup)
throws HdfException
{
  if (subGroup.isVariable) subVariableList.add( subGroup);
  else subGroupList.add( subGroup);

  if (hdfFile.fileVersion == 1) {
    btreeNode.addTreeName( subGroup);
  }
}










// Search for subGroup or variable
// Return null if not found.

HdfGroup findSubItem( String subName)
{
  HdfGroup resGroup = null;
  for (HdfGroup subGroup : subGroupList) {
    if (subGroup.groupName.equals( subName)) {
      resGroup = subGroup;
      break;
    }
  }
  if (resGroup == null) {
    for (HdfGroup subGroup : subVariableList) {
      if (subGroup.groupName.equals( subName)) {
        resGroup = subGroup;
        break;
      }
    }
  }
  return resGroup;
}





// Search for attribute.
// Return null if not found.

public MsgAttribute findAttribute( String attrName)
{
  MsgAttribute resMsg = null;
  for (MsgBase baseMsg : hdrMsgList) {
    if (baseMsg instanceof MsgAttribute) {
      MsgAttribute attrMsg = (MsgAttribute) baseMsg;
      if (attrMsg.attrName.equals( attrName)) {
        resMsg = attrMsg;
        break;
      }
    }
  }
  return resMsg;
}






// Return number of attributes.

int getNumAttribute()
{
  int ires = 0;
  for (MsgBase baseMsg : hdrMsgList) {
    if (baseMsg instanceof MsgAttribute) ires++;
  }
  return ires;
}










public void writeData( Object vdata)
throws HdfException
{
  try { writeDataSub( vdata); }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: %s", exc);
  }
}



void writeDataSub( Object vdata)
throws HdfException, IOException
{
  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup.writeData entry: group: " + getPath() + "\n"
      + "  specified type: "
      + HdfUtil.formatDtypeDim( dtype, msgDataSpace.varDims) + "\n"
      + "  eofAddr: %d", hdfFile.eofAddr);
  }

  if (hdfFile.fileStatus != HdfFileWriter.ST_WRITEDATA)
    throwerr("must call endDefine first");
  if (! isVariable) throwerr("cannot write data to a group");
  if (isWritten) throwerr("variable has already been written: ", getPath());
  isWritten = true;

  // Find dtype and varDims of vdata
  /// xxx old:
  ///int[] dataInfo = HdfUtil.getDtypeAndDimsOld( isVlen, vdata);
  ///int dataDtype = dataInfo[0];
  ///int[] dataVarDims = Arrays.copyOfRange( dataInfo, 1, dataInfo.length);

  int[] dataInfo = HdfUtil.getDimLen( vdata, isVlen);
  int dataDtype = dataInfo[0];
  int totNumEle = dataInfo[1];
  int[] dataVarDims = Arrays.copyOfRange( dataInfo, 2, dataInfo.length);

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup.writeData: actual data:" + "\n"
      + "  vdata object: " + vdata + "\n"
      + "  vdata class: " + vdata.getClass() + "\n"
      + "  vdata dtype: " + dtypeNames[ dataDtype] + "\n"
      + "  vdata totNumEle: " + totNumEle + "\n"
      + "  vdata rank: " + dataVarDims.length + "\n"
      + "  vdata type and dims: "
      + HdfUtil.formatDtypeDim( dataDtype, dataVarDims));
  }

  // Check that dtype and varDims match what the user
  // declared in the earlier addVariable call.
  HdfUtil.checkTypeMatch( getPath(), msgDataType.dtype, dataDtype,
    msgDataSpace.varDims, dataVarDims);

  rawDataAddr = HdfUtil.alignLong( 8, hdfFile.eofAddr);
  hdfFile.outChannel.position( rawDataAddr);

  // As outbuf fills, it gets written to outChannel.
  HBuffer outbuf = new HBuffer( hdfFile.outChannel, compressionLevel, hdfFile);


  // xxx future: DTYPE_VLEN is not supported for datasets.
  // Only for attributes.
  // However, DTYPE_STRING_VAR is supported for both attrs and datasets.
  //
  // For DTYPE_VLEN we could use a scheme like we do below
  // for DTYPE_STRING_VAR, writing a new gcol and refBuf.

  if (msgDataType.dtype == HdfGroup.DTYPE_VLEN)
    throwerr("DTYPE_VLEN datasets are not supported");


  // Format and the data to outbuf and write to outChannel.

  // Special case for DTYPE_STRING_VAR
  //
  // We don't allow compression of DTYPE_STRING_VAR -
  // deliberately not implemented.
  // It turns out that HDF5 compresses the references to
  // variable length strings, but not the strings themselves.
  // The strings remain in the global heap GCOL, uncompressed.

  if (dtype == DTYPE_STRING_VAR) {

    if (compressionLevel > 0)
      throwerr("compression not supported for DTYPE_STRING_VAR");

    // Write two areas into fmtBuf:
    //   1.  A GCOL (global heap) area.
    //       There is a separate gcol for each DTYPE_STRING_VAR variable.
    //       The term "global heap" is a misnomer.
    //   2.  A list of references to the GCOL entries.
    //       The variables rawDataAddr, rawDataSize refer to this
    //       list of references.

    // xxx We cannot write an array of strings whose total length
    // exceeds 2GB, since the GlobalHeap uses a ByteBuffer internally
    // to hold the entire heap.

    GlobalHeap gcol = new GlobalHeap( hdfFile);
    HBuffer refBuf = new HBuffer( null, compressionLevel, hdfFile);
    long gcolAddr = hdfFile.outChannel.position();

    formatRawData(
      dtype,
      0,               // stgFieldLen for DTYPE_STRING_FIX
      vdata,
      null,            // cntr for DTYPE_COMPOUND
      gcolAddr,        // addr where we will write this gcol
      gcol,            // output: holds strings
      refBuf);         // output: holds references to strings

    if (hdfFile.bugs >= 5) {
      prtf("  writeDataSub.STRING_VAR: gcol: %s", gcol);
      prtf("  writeDataSub.STRING_VAR: refBuf: %s", refBuf);
    }

    gcol.formatBuf( 0, outbuf);       // formatPass = 0
    outbuf.flush();                   // write remaining data to outChannel

    rawDataAddr = HdfUtil.alignLong( 8, hdfFile.outChannel.position());
    hdfFile.outChannel.position( rawDataAddr);

    refBuf.writeChannel( hdfFile.outChannel);
    long endPos = hdfFile.outChannel.position();
    rawDataSize = endPos - rawDataAddr;
  }

  else {          // else not DTYPE_STRING_VAR
    formatRawData(
      dtype,
      stgFieldLen,
      vdata,
      new Counter(),
      -1,               // gcolAddr for DTYPE_STRING_VAR
      null,             // gcol for DTYPE_STRING_VAR
      outbuf);

    outbuf.flush();        // write remaining data to outChannel

    long endPos = hdfFile.outChannel.position();

    // Set rawDataSize
    // For non-compressed data we could use something like ...
    //   rawDataSize = msgDataType.elementLen;
    //   for (int ii = 0; ii < msgDataSpace.varDims.length; ii++) {
    //     long isize = msgDataSpace.varDims[ii];
    //     if (hdfFile.bugs >= 5) prtf("  dimension %d len: %d", ii, isize);
    //     rawDataSize *= isize;
    //   }
    //
    // However compressed data can be any length,
    // so we just use the output length.

    rawDataSize = endPos - rawDataAddr;

    if (hdfFile.bugs >= 5) {
      prtf("HdfGroup.writeData: rawDataAddr: %d  endPos: %d  rawDataSize: %d",
        rawDataAddr, endPos, rawDataSize);
      prtf("HdfGroup.writeData: old eofAddr: %d", hdfFile.eofAddr);
    }
  }

  hdfFile.eofAddr = hdfFile.outChannel.position();
  if (hdfFile.bugs >= 2)
    prtf("HdfGroup.writeData.end: new eofAddr: %d", hdfFile.eofAddr);

} // end writeDataSub












void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  // We need to use version 2 to support the
  // messages: link, link info, group info
  int groupVersion = hdfFile.fileVersion;     // 1 or 2

  // Write it out
  if (hdfFile.fileVersion == 1) {
    if (! isVariable) {
      hdfFile.addWork("HdfGroup", btreeNode);
      hdfFile.addWork("HdfGroup", localHeap);
    }

    fmtBuf.putBufByte("HdfGroup: groupVersion", groupVersion);
    fmtBuf.putBufByte("HdfGroup: reserved", 0);
    fmtBuf.putBufShort("HdfGroup: numMsg", hdrMsgList.size());

    // The refCount must be > 0 for object references to work.
    // Otherwise the code at the following stack trace
    // fails to find the object:
    //   #0  H5O_link_oh  at H5O.c:1501
    //   #1  H5O_link  at H5O.c:1545
    //   #2  H5R_dereference  at H5R.c:418
    //   #3  H5Rdereference  at H5R.c:532
    //   #4  h5tools_str_sprint  at h5tools_str.c:952
    //   #5  h5tools_dump_simple_data  at h5tools.c:945
    //   #6  h5tools_dump_simple_dset at h5tools.c:2480
    //   #7  h5tools_dump_dset at h5tools.c:2656
    //   #8  dump_data at h5dump.c:2532
    //   #9  dump_dataset at h5dump.c:2266
    //   #10 dump_all_cb at h5dump.c:1678
    //   #11 H5G_iterate_cb  at H5G.c:1470
    //   #12 H5G_node_iterate at H5Gnode.c:1008
    //   #13 H5B_iterate_helper  at H5B.c:1223
    //   #14 H5B_iterate  at H5B.c:1301
    //   #15 H5G_stab_iterate  at H5Gstab.c:521
    //   #16 H5G_obj_iterate  at H5Gobj.c:598
    //   #17 H5G_iterate  at H5G.c:1531
    //   #18 H5Literate at H5L.c:1183
    //   #19 dump_group at h5dump.c:2184
    //   #20 main at h5dump.c:4473

    int refCount = 1;     // link or reference count; must be > 0.
    fmtBuf.putBufInt("HdfGroup: refCount", refCount);

    // Set headSize to the sum of msg sizes
    // On the first pass all hdrMsgSize are 0, so leave headSize = 0.
    int headSize = 0;
    if (formatPass == 2) {
      for (MsgBase hmsg : hdrMsgList) {
        if (hmsg.hdrMsgSize % 8 != 0) throwerr("hmsg.hdrMsgSize % 8 != 0");
        if (hmsg.hdrMsgSize > 0)
          headSize += MsgBase.MSG_HDR_LEN_V1 + hmsg.hdrMsgSize;
      }
    }
    fmtBuf.putBufInt("HdfGroup: headSize", headSize);
    fmtBuf.putBufInt("HdfGroup: reserved", 0);
    long endPos = fmtBuf.getPos() + headSize;

    for (MsgBase hmsg : hdrMsgList) {
      // Internal block
      hmsg.formatFullMsg( formatPass, fmtBuf);
      if (hdfFile.bugs >= 5) {
        prtIndent(
          "Group write: above hmsg type: 0x%x == %d  size: 0x%x == %d",
          hmsg.hdrMsgType, hmsg.hdrMsgType, hmsg.hdrMsgSize,
          hmsg.hdrMsgSize);
      }
    }
  } // if fileVersion == 1


  if (hdfFile.fileVersion == 2) {

    if (formatPass == 1) {
      if (! isVariable) {
        hdrMsgList.add( new MsgGroupInfo( this, hdfFile));
        hdrMsgList.add( new MsgLinkInfo( this, hdfFile));
      }
      if (subGroupList != null) {
        for (HdfGroup subGroup : subGroupList) {
          hdrMsgList.add( new MsgLinkit(
            linkCreationOrder++,
            subGroup,
            this,
            hdfFile));
        }
      }
      if (subVariableList != null) {
        for (HdfGroup subGroup : subVariableList) {
          hdrMsgList.add( new MsgLinkit(
            linkCreationOrder++,
            subGroup,
            this,
            hdfFile));
        }
      }
    }

    // For fileVersion==2 there is some needlessly twisted code
    // in the HDF5 H5Odbg.c.
    // It requires that the length of the chunklen field
    // be "appropriate" for the chunk0Len value, meaning
    // if  0 <= chunk0Len <= 255 the chunklen field must be
    // exactly 1 byte, if chunk0Len <= 65535 the field must be
    // exactly 2 bytes, etc.
    //
    // So we cannot always specify that the chunklen field size = 8.
    // This implies we must know chunk0Len before laying out
    // chunk0 ... as I said, a bit twisted.
    //
    // So we lay out the HdfGroup twice on each formatPass.
    // The first layout uses a temp HBuffer and just gets
    // us the chunk0Len.
    // The second layout is for real and uses fmtBuf.

    HBuffer tempHbuf = new HBuffer(
      null,                 // outChannel
      compressionLevel,
      hdfFile);

    int svIndent = hdfFile.indent;
    hdfFile.indent += 6;
    if (hdfFile.bugs >= 5) prtIndent("Start HdfGroup temp layout");

    // Find chunk0Len
    // Use formatPass = 0 so MsgLayout and MsgLinkit don't call addWork.
    long chunk0Len = 0;
    chunk0Len = layoutVersion2(
      0, groupVersion, chunk0Len, tempHbuf);  // formatPass = 0
    if (hdfFile.bugs >= 5) prtIndent("End HdfGroup temp layout");
    hdfFile.indent = svIndent;

    // Layout for real
    layoutVersion2( formatPass, groupVersion, chunk0Len, fmtBuf);

  } // if fileVersion == 2

  noteFormatExit( fmtBuf);         // BaseBlk: print debug
}






long layoutVersion2(
  int formatPass,
  int groupVersion,
  long prevChunk0Len,
  HBuffer fmtBuf)
throws HdfException
{

  long startAllPos = fmtBuf.getPos();
  fmtBuf.putBufByte("HdfGroup: signa", signa);
  fmtBuf.putBufByte("HdfGroup: signb", signb);
  fmtBuf.putBufByte("HdfGroup: signc", signc);
  fmtBuf.putBufByte("HdfGroup: signd", signd);

  fmtBuf.putBufByte("HdfGroup: groupVersion", groupVersion);

  // Flags:
  //   bits  mask  desc
  //   0-1    3  len of chunk#0 field: 0: 1 byte, 1: 2 bytes, 2: 4, 3: 8
  //   2      4  1: track attr creation order
  //   3      8  1: index attr creation order
  //   4     16  1: store non-default attr storage phase values
  //   5     32  1: store set, access, mod, change, birth times
  //   6-7: reserved

  int lenMask;
  if (prevChunk0Len <= 255) lenMask = 0;
  else if (prevChunk0Len <= 65535) lenMask = 1;
  else if (prevChunk0Len <= 4294967295L) lenMask = 2;
  else lenMask = 3;

  int flag = lenMask | 4 | 8 | 32;
    // chunklen=8, track attrs, index attrs, store all times

  fmtBuf.putBufByte("HdfGroup: flags", flag);

  // xxx Caution: HDF5 has a possible year 2038 problem,
  // using a 4 byte date, if handled as a signed int.
  if ((flag & 32) != 0) {
    fmtBuf.putBufInt(
      "HdfGroup: accessTime", (int) hdfFile.utcModTimeSec);
    fmtBuf.putBufInt(
      "HdfGroup: modTime", (int) hdfFile.utcModTimeSec);
    fmtBuf.putBufInt(
      "HdfGroup: changeTime", (int) hdfFile.utcModTimeSec);
    fmtBuf.putBufInt(
      "HdfGroup: birthTime", (int) hdfFile.utcModTimeSec);
  }

  if ((flag & 16) != 0) {
    fmtBuf.putBufShort("HdfGroup: maxNumCompact", 30000);
    fmtBuf.putBufShort("HdfGroup: minNumDense", 0);
  }

  if (lenMask == 0)
    fmtBuf.putBufByte("HdfGroup: chunk0Len", (int) prevChunk0Len);
  else if (lenMask == 1)
    fmtBuf.putBufShort("HdfGroup: chunk0Len", (int) prevChunk0Len);
  else if (lenMask == 2)
    fmtBuf.putBufInt("HdfGroup: chunk0Len", (int) prevChunk0Len);
  else if (lenMask == 3)
    fmtBuf.putBufLong("HdfGroup: chunk0Len", prevChunk0Len);

  // Write out all the messages
  long startMsgPos = fmtBuf.getPos();
  for (MsgBase hmsg : hdrMsgList) {
    // Internal block
    hmsg.formatFullMsg( formatPass, fmtBuf);
    if (hdfFile.bugs >= 5) {
      prtIndent(
        "Group write: above hmsg type: 0x%x == %d  size: 0x%x == %d",
        hmsg.hdrMsgType, hmsg.hdrMsgType, hmsg.hdrMsgSize, hmsg.hdrMsgSize);
    }
  }

  long endPos = fmtBuf.getPos();

  byte[] chkBytes = fmtBuf.getBufBytes( startAllPos, endPos);
  int checkSumHack = new CheckSumHack().calcHackSum( chkBytes);
  fmtBuf.putBufInt("HdfGroup: checkSumHack", checkSumHack);

  long chunk0Len = endPos - startMsgPos;
  return chunk0Len;
} // end layoutVersion2









// Format a regular array of raw data.  Not ragged array.
// If strings, they are all fixed len: they are padded to elementLen.
//
// The data, vdata, may be one of:
//   Byte,      byte[],      [][],  [][][],  etc.
//   Short,     short[],     [][],  [][][],  etc.
//   Integer,   int[],       [][],  [][][],  etc.
//   Long,      long[],      [][],  [][][],  etc.
//   Float,     float[],     [][],  [][][],  etc.
//   Double,    double[],    [][],  [][][],  etc.
//   Character, char[],      [][],  [][][],  etc.
//   String,    String[],    [][],  [][][],  etc.
//   HdfGroup,  HdfGroup[],  [][],  [][][],  etc.  (reference)
//
// The scalar types (Short, Integer, Float, etc)
// and the 1 dimensional types (short[], int[], float[], etc)
// are handled explicitly below.
//
// The higher dimension types, [][], [][][], etc, are handled
// by recursive calls in the test: if vdata instanceof Object[].
//
// String[] is handled recursively as Object[], then as scalar String.
// Similarly for HdfGroup[].

void formatRawData(
  int dtp,             // one of DTYPE_*
  int stgFieldLen,     // used for DTYPE_STRING_FIX
  Object vdata,
  Counter cntr,        // used for the index of compound types
  long gcolAddr,       // used for DTYPE_STRING_VAR
  GlobalHeap gcol,     // used for DTYPE_STRING_VAR
  HBuffer fmtBuf)      // output buffer
throws HdfException
{

// xxx someday we might validate dtp inside each case clause.

  if (vdata == null) throwerr("vdata is null");

  if (vdata instanceof Object[]) {
    Object[] objVec = (Object[]) vdata;
    for (int ii = 0; ii < objVec.length; ii++) {
      formatRawData(                               // recursion
        dtp,
        stgFieldLen,
        objVec[ii],
        cntr,
        gcolAddr,
        gcol,
        fmtBuf);
    }
  }
  else if (vdata instanceof Byte) {
    byte aval = ((Byte) vdata).byteValue();
    fmtBuf.putBufByte("formatRawData", aval);
  }
  else if (vdata instanceof Short) {
    short aval = ((Short) vdata).shortValue();
    fmtBuf.putBufShort("formatRawData", aval);
  }
  else if (vdata instanceof Integer) {
    int aval = ((Integer) vdata).intValue();
    fmtBuf.putBufInt("formatRawData", aval);
  }
  else if (vdata instanceof Long) {
    long aval = ((Long) vdata).longValue();
    fmtBuf.putBufLong("formatRawData", aval);
  }
  else if (vdata instanceof Float) {
    float aval = ((Float) vdata).floatValue();
    fmtBuf.putBufFloat("formatRawData", aval);
  }
  else if (vdata instanceof Double) {
    double aval = ((Double) vdata).doubleValue();
    fmtBuf.putBufDouble("formatRawData", aval);
  }

  else if (vdata instanceof Character) {
    // xxx Normally we would handle character encoding by
    // calling HdfUtil.encodeString.
    // However the final length is fixed, so we must
    // do 1 char -> 1 byte encoding.
    fmtBuf.putBufByte("formatRawData", ((Character) vdata).charValue());
  }

  else if (vdata instanceof String) {
    String aval = (String) vdata;
    if (dtp == DTYPE_STRING_FIX) {
      byte[] bytes = HdfUtil.encodeString( aval, false, this);
      fmtBuf.putBufBytes(
        "formatRawData", HdfUtil.truncPadNull( bytes, stgFieldLen));
    }
    else if (dtp == DTYPE_STRING_VAR) {
      byte[] bytes = HdfUtil.encodeString(
        (String) vdata, false, this);     // addNull = false
      int gcolIx = gcol.putHeapItem("vlen string data", bytes);
      fmtBuf.putBufInt("vlen len", bytes.length);
      fmtBuf.putBufLong("vlen gcol addr", gcolAddr);
      fmtBuf.putBufInt("vlen gcol ix", gcolIx);
    }
    else throwerr("dtp mismatch");
  }

  else if (vdata instanceof HdfGroup) {
    // For object refs, data is:
    //   addr of group header, 8 bytes.
    // For region refs, data is:
    //   globalHeapReference:
    //     addr of globalHeap, 8 bytes
    //     global heap index, 4 bytes
    //   The object on the global heap is:
    //     addr of group header, 8 bytes
    //     some sort of info on type, dimension, start and end indices
    long aval = ((HdfGroup) vdata).blkPosition;
    fmtBuf.putBufLong("formatRawData", aval);
    if (dtp == DTYPE_COMPOUND) {
      fmtBuf.putBufInt("formatRawData ref ix", cntr.getValue());
      cntr.increment();
    }
  }

  else if (vdata instanceof byte[]) {
    byte[] avec = (byte[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufByte("formatRawData", 0xff & avec[ii]);
    }
  }
  else if (vdata instanceof short[]) {
    short[] avec = (short[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufShort("formatRawData", 0xffff & avec[ii]);
    }
  }
  else if (vdata instanceof int[]) {
    int[] avec = (int[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufInt("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof long[]) {
    long[] avec = (long[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufLong("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof float[]) {
    float[] avec = (float[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufFloat("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof double[]) {
    double[] avec = (double[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufDouble("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof char[]) {
    // xxx Normally we would handle character encoding by
    // calling HdfUtil.encodeString.
    // However the final length is fixed, so we must
    // do 1 char -> 1 byte encoding.
    char[] avec = (char[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufByte("formatRawData", 0xff & avec[ii]);
    }
  }
  else throwerr("unknown raw data type.  class: " + vdata.getClass());
} // end formatRawData





// For irow = 0, < len(vdata):
//   format to fmtBuf:
//     len of ele == ncol
//     blkPosition of mainGlobalHeap
//     heapIx[irow]
//
// Called by MsgAttribute.formatMsgCore.

void formatVlenRawData(
  int[] heapIxs,
  Object vdata,
  HBuffer fmtBuf)      // output buffer
throws HdfException
{
  Object[] vdataVec = (Object[]) vdata;

  int nrow = vdataVec.length;
  for (int irow = 0; irow < nrow; irow++) {
    Object vrow = vdataVec[irow];
    int ncol = -1;
    if (vrow instanceof Object[]) ncol = ((Object[]) vrow).length;
    else if (vrow instanceof byte[]) ncol = ((byte[]) vrow).length;
    else if (vrow instanceof short[]) ncol = ((short[]) vrow).length;
    else if (vrow instanceof int[]) ncol = ((int[]) vrow).length;
    else if (vrow instanceof long[]) ncol = ((long[]) vrow).length;
    else if (vrow instanceof float[]) ncol = ((float[]) vrow).length;
    else if (vrow instanceof double[]) ncol = ((double[]) vrow).length;
    else if (vrow instanceof char[]) ncol = ((char[]) vrow).length;
    else throwerr("unknown vlen type");

    // Format numVal, global heap ID
    fmtBuf.putBufInt("vlen.ncol", ncol);
    fmtBuf.putBufLong("globalHeap.pos", hdfFile.mainGlobalHeap.blkPosition);
    fmtBuf.putBufInt("outputGlobalHeap.ix", heapIxs[irow]);
  } // for irow
} // end formatVlenRawData





} // end class
