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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class HdfFileWriter extends BaseBlk {



// Bit flags for optFlag
public static final int OPT_ALLOW_OVERWRITE = 1;




static final int OFFSET_SIZE = 8;
static final int UNDEFINED_ADDR = -1;


// Define constants for fileStatus
static final int ST_DEFINING  = 1;
static final int ST_WRITEDATA = 2;
static final int ST_CLOSED    = 3;
static final String[] statusNames = {
  "UNKNOWN", "DEFINING", "WRITEDATA", "CLOSED"};


// Referenced blocks
HdfGroup rootGroup;


GlobalHeap mainGlobalHeap = null;

// fileVersion==1 only:
SymTabEntry symTabEntry;



String filePath;
int fileVersion;
int optFlag;                    // zero or more OPT_* bit options

int fileStatus;                 // one of ST_*

long utcModTimeMilliSec;        // java date: milliseconds after 1970
long utcModTimeSec;             // java date: seconds after 1970

int indent = 0;
int debugLevel = 0;             // main debug level
int bugs = 0;                   // current debug level, may be < debugLevel

private HBuffer mainBuf;

FileOutputStream outStream;
FileChannel outChannel;

ArrayList<BaseBlk> workList = null;



final int signa = 0x89;
final int signb = 'H';
final int signc = 'D';
final int signd = 'F';
final int signe = '\r';
final int signf = '\n';
final int signg = 0x1a;
final int signh = '\n';

final int offsetSize = OFFSET_SIZE;       // size of offsets
final int lengthSize = OFFSET_SIZE;       // size of lengths

// Consistency bits: (The low order bit is 0)
//    0: file is open for write
//    1: file has been verified as consistent
final int consistencyFlag = 0;

final long baseAddress = 0;

final long superblockExtensionAddress = UNDEFINED_ADDR;    // -1

long eofAddr;




// fileVersion==1 only:
// A leaf btree node (a symbol table) should have:
//   k_leaf <= numUsedEntries <= 2 * k_leaf
int k_leaf = 4;          // This is set in SymbolTable.addSymName

// fileVersion==1 only:
// An internal btree node should have:
//   k_internal <= numUsedEntries <= 2 * k_internal
int k_internal = 16;





// Debug levels:
//    0   none
//    1   add: HdfGroup: addVariable, add addAttribute, writeData
//    2   add: endDefine, close, formatBufAll, more writeData, flush
//    5   add: all written data
//   10   add: HBuffer.expandBuf

public HdfFileWriter(
  String filePath,               // file to create
  int fileVersion,
  int optFlag)                   // zero or more OPT_* bit options
throws HdfException
{
  super("HdfFileWriter", null);
  this.hdfFile = this;
  this.filePath = filePath;
  this.optFlag = optFlag;
  this.fileVersion = fileVersion;

  debugLevel = 0;
  bugs = debugLevel;

  if (fileVersion != 1 && fileVersion != 2)
    throwerr("invalid fileVersion: %d", fileVersion);
  fileStatus = ST_DEFINING;
  utcModTimeMilliSec = System.currentTimeMillis();
  utcModTimeSec = utcModTimeMilliSec / 1000;
  indent = 0;

  // Make rootGroup
  String rootName = "";
  rootGroup = new HdfGroup( rootName, null, this);
  if (fileVersion == 1) {
    int grpOffset = rootGroup.localHeap.putHeapItem("rootName",
      HdfUtil.encodeString( rootName, false, null));  //xxx all enc: no null term
    symTabEntry = new SymTabEntry(
      grpOffset,
      rootGroup,
      this);

    // Special symTabEntry for SuperBlock has scratchpad info
    symTabEntry.cacheType = 1;
    symTabEntry.scratchBtree = rootGroup.btreeNode;
    symTabEntry.scratchHeap = rootGroup.localHeap;
  }

  try {
    if ( ((optFlag & OPT_ALLOW_OVERWRITE) == 0)
      && new File( filePath).exists())
      throwerr("file \"%s\" already exists", filePath);
    outStream = new FileOutputStream( filePath);
  }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: %s", exc);
  }
  outChannel = outStream.getChannel();
}



public String toString() {
  String res = super.toString();
  res += "  filePath: " + filePath;
  res += "  fileVersion: " + fileVersion;
  res += "  optFlag: " + optFlag;
  res += "  status: " + statusNames[fileStatus];
  return res;
}



public HdfGroup getRootGroup() {
  return rootGroup;
}



public int getDebugLevel() {
  return debugLevel;
}




public void setDebugLevel( int debugLevel) {
  this.debugLevel = debugLevel;     // main debugLevel
  this.bugs = debugLevel;           // current debugLevel
}




public void endDefine()
throws HdfException
{
  if (bugs >= 1) {
    prtf("HdfFileWriter.endDefine: filePath: \"" + filePath + "\"\n");
  }
  if (fileStatus != ST_DEFINING) throwerr("already called endDefine");
  fileStatus = ST_WRITEDATA;

  // Format metadata to buffer, pass 1 of 2  (pass 2 is in close)
  mainGlobalHeap = new GlobalHeap( this);
  mainBuf = new HBuffer(
    null,         // outChannel
    0,            // compressionLevel
    this);
  if (bugs >= 2)
    prtf("\nendDefine: start pass 1: mainBuf pos: %d", mainBuf.getPos());

  if (bugs >= 2)
    prtf("HdfFileWriter.endDefine: set bugs = 0 for formatPass 1");
  bugs = 0;
  formatBufAll( 1);           // formatPass = 1

  bugs = debugLevel;
  if (bugs >= 2)
    prtf("endDefine: after pass 1: mainBuf pos: %d", mainBuf.getPos());

  if (fileVersion == 1) {
    // Set Btree and Heap addresses in superBlock's SymTabEntry scratchPad.
    symTabEntry.scratchBtree = rootGroup.btreeNode;
    symTabEntry.scratchHeap  = rootGroup.localHeap;
  }

  // Set eofAddr in superBlock
  eofAddr = mainBuf.getPos();

} // end endDefine









public void close()
throws HdfException
{
  if (bugs >= 1) {
    prtf("HdfFileWriter.close: filePath: \"" + filePath + "\"\n");
  }
  if (fileStatus == ST_DEFINING)
    throwerr("must call endDefine before calling close");
  else if (fileStatus == ST_CLOSED) throwerr("file is already closed");
  else if (fileStatus != ST_WRITEDATA) throwerr("invalid fileStatus");
  fileStatus = ST_CLOSED;

  // Insure all defined datasets have been written
  ArrayList<HdfGroup> grpList = new ArrayList<HdfGroup>();
  findAllGroups( rootGroup, grpList);
  String errMsg = "";
  for (HdfGroup grp : grpList) {
    if (grp.isVariable && ! grp.isWritten) {
      errMsg += "  " + grp.getPath();
    }
  }
  if (errMsg.length() > 0)
    throwerr("close: the following dataset(s) still need to written: %s",
      errMsg);

  // Format metadata to buffer, pass 2 of 2  (pass 1 is in endDefine)
  // This time all the pointers should be accurate.
  if (bugs >= 2)
    prtf("\nclose: start pass 2: mainBuf pos: %d", mainBuf.getPos());

  mainGlobalHeap.clear();
  mainBuf.clear();

  formatBufAll( 2);           // formatPass = 2

  if (bugs >= 2)
    prtf("close: after pass 2: mainBuf pos: %d", mainBuf.getPos());

  // Write buffer to outfile
  try {
    outChannel.position( 0);
    mainBuf.writeChannel( outChannel);
    outChannel.close();
    outStream.close();
  }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: %s", exc);
  }
} // end close






void findAllGroups(
  HdfGroup grp,
  ArrayList<HdfGroup> groupList)
{
  groupList.add( grp);
  if (grp.subGroupList != null) {
    for (HdfGroup subGrp : grp.subGroupList) {
      findAllGroups( subGrp, groupList);
    }
  }
  if (grp.subVariableList != null) {
    for (HdfGroup subGrp : grp.subVariableList) {
      findAllGroups( subGrp, groupList);
    }
  }
}





void formatBufAll( int formatPass)
throws HdfException
{
  if (bugs >= 2) {
    prtf("\nformatBufAll: entry. formatPass: %d  mainBuf pos: 0x%x",
      formatPass, mainBuf.getPos());
  }

  HBuffer fmtBuf = mainBuf;

  // Format everything
  workList = new ArrayList<BaseBlk>();
  addWork("HdfFileWriter", this);
  while (workList.size() > 0) {
    BaseBlk blk = workList.remove(0);
    if (bugs >= 5) prtf(
      "\nformatBufAll pop: %s  pos 0x%x  new list len: %d",
      blk.blkName, blk.blkPosition, workList.size());
    blk.formatBuf( formatPass, fmtBuf);
  }

  // Format globalHeap last since other blocks may add items
  if (bugs >= 2)
    prtf("\nformatBufAll: format globalHeap.  fmtBuf pos: 0x%x",
      fmtBuf.getPos());
  mainGlobalHeap.formatBuf( formatPass, fmtBuf);

  if (bugs >= 2) {
    prtf("\nformatBufAll: exit. formatPass: %d  fmtBuf pos: 0x%x",
      formatPass, fmtBuf.getPos());
  }
} // end formatBufAll






void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  long startPos = fmtBuf.getPos();
  fmtBuf.putBufByte("HdfFileWriter: signa", signa);
  fmtBuf.putBufByte("HdfFileWriter: signb", signb);
  fmtBuf.putBufByte("HdfFileWriter: signc", signc);
  fmtBuf.putBufByte("HdfFileWriter: signd", signd);
  fmtBuf.putBufByte("HdfFileWriter: signe", signe);
  fmtBuf.putBufByte("HdfFileWriter: signf", signf);
  fmtBuf.putBufByte("HdfFileWriter: signg", signg);
  fmtBuf.putBufByte("HdfFileWriter: signh", signh);

  int superBlockVersion = 0;
  if (fileVersion == 1) superBlockVersion = 0;
  if (fileVersion == 2) superBlockVersion = 2;
  fmtBuf.putBufByte("HdfFileWriter: superBlockVersion", superBlockVersion);

  if (fileVersion == 1) {
    fmtBuf.putBufByte("HdfFileWriter: freeSpaceVersion", 0);
    fmtBuf.putBufByte("HdfFileWriter: symTabEntryVersion", 0);
    fmtBuf.putBufByte("HdfFileWriter: reserved", 0);
    fmtBuf.putBufByte("HdfFileWriter: sharedHdrMsgVersion", 0);
    fmtBuf.putBufByte("HdfFileWriter: offsetSize", offsetSize);
    fmtBuf.putBufByte("HdfFileWriter: lengthSize", lengthSize);
    fmtBuf.putBufByte("HdfFileWriter: reserved", 0);
    fmtBuf.putBufShort("HdfFileWriter: k_leaf", k_leaf);
    fmtBuf.putBufShort("HdfFileWriter: k_internal", k_internal);
    fmtBuf.putBufInt("HdfFileWriter: consistencyFlag", consistencyFlag);
    fmtBuf.putBufLong("HdfFileWriter: baseAddress", baseAddress);
    fmtBuf.putBufLong("HdfFileWriter: freeSpaceInfo", UNDEFINED_ADDR);
    fmtBuf.putBufLong("HdfFileWriter: eofAddr", eofAddr);
    fmtBuf.putBufLong("HdfFileWriter: driverInfoAddr", UNDEFINED_ADDR);
    // Internal block
    symTabEntry.formatBuf( formatPass, fmtBuf);
  }
  if (fileVersion == 2) {
    fmtBuf.putBufByte("HdfFileWriter: offsetSize", offsetSize);
    fmtBuf.putBufByte("HdfFileWriter: lengthSize", lengthSize);
    fmtBuf.putBufByte("HdfFileWriter: consistencyFlag", consistencyFlag);
    fmtBuf.putBufLong("HdfFileWriter: baseAddress", baseAddress);
    fmtBuf.putBufLong("HdfFileWriter: superblockExtensionAddress",
      superblockExtensionAddress);
    fmtBuf.putBufLong("HdfFileWriter: eofAddr", eofAddr);
    fmtBuf.putBufLong("HdfFileWriter: rootGroupAddr", rootGroup.blkPosition);
    long endPos = fmtBuf.getPos();
    byte[] chkBytes = fmtBuf.getBufBytes( startPos, endPos);
    int checkSumHack = new CheckSumHack().calcHackSum( chkBytes);
    fmtBuf.putBufInt("HdfFileWriter: checkSumHack", checkSumHack);
  }

  // External block
  addWork("HdfFileWriter", rootGroup);

  noteFormatExit( fmtBuf);         // BaseBlk: print debug
} // end formatBuf





void addWork( String msg, BaseBlk blk) {
  workList.add( blk);
  if (bugs >= 5) prtIndent(
    "addWork: %s added: %s  pos 0x%x  new list len: %d",
    msg, blk.blkName, blk.blkPosition, workList.size());
}








String formatName(
  String name,
  int bufPos)
{
  StringBuilder sbuf = new StringBuilder();
  sbuf.append( mkIndent());
  sbuf.append( String.format("hex %04x", bufPos));
  sbuf.append( "  ");
  sbuf.append( String.format("%-30s", name));
  sbuf.append( "  ");
  return sbuf.toString();
}






String mkIndent() {
  String res = "";
  for (int ii = 0; ii < indent; ii++) {
    res += "  ";
  }
  return res;
}


} // end class
