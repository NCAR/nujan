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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


// xxx all xxx

/**
 * Represents an open output file.
 * There is exactly one HdfFileWriter per output file.
 * <p>
 * For an example of use see {@link ExampleSimple}.
 */


public class HdfFileWriter extends BaseBlk {


// Internal logic:
// HdfFileWriter:
//   Constructor:
//     Initialize rootGroup.
//     Set eofAddr = 0
//   endDefine:
//     Call formatBufAll to format all metadata into mainBuf, pass 1 of 2.
//     Set eofAddr = mainBuf.getPos() == length of formatted metadata
//
//   (Here the user's calls to HdfGroup.writeData update our eofAddr).
//       
//   close:
//     Call formatBufAll to format all metadata into mainBuf, pass 2 of 2.
//     Write mainBuf to outChannel == outFile starting at file offset 0.
//       The variable data previously written in HdfGroup.writeData
//       follow the metadata.


/**
 * Bit flag for optFlag: allow overwrite of an existing file.
 */
public static final int OPT_ALLOW_OVERWRITE = 1;




/**
 * The size in bytes of all internal offsets and addresses
 */
static final int OFFSET_SIZE = 8;

/**
 * A tag for an undefined internal address.
 */
static final int UNDEFINED_ADDR = -1;


// Define constants for fileStatus
/**
 * fileStatus: we are defining the file (before calling endDefine).
 */
static final int ST_DEFINING  = 1;
/**
 * fileStatus: definition is complete; we are writing data (after calling endDefine).
 */
static final int ST_WRITEDATA = 2;
/**
 * fileStatus: writing data is complete (after calling close).
 */
static final int ST_CLOSED    = 3;

/**
 * Names for fileStatus values.
 */
static final String[] statusNames = {
  "UNKNOWN", "DEFINING", "WRITEDATA", "CLOSED"};


/**
 * The one and only root Group.
 */
HdfGroup rootGroup;


/**
 * This GlobalHeap contains all: attributes that are either VLEN and String,
 * and FillValues that are Strings.
 * Variables that are DTYPE_STRING_VAR contain their own so-called
 * GlobalHeap for storing the strings.
 */
GlobalHeap mainGlobalHeap = null;

/**
 * For fileVersion==1, the symbol table entry pointing
 * to the rootGroup.
 */
SymTabEntry symTabEntry;



/**
 * The file path (name) of the output file on disk.
 */
String filePath;

/**
 * Internal HDF5 version: 1==old, 2==new.
 */
int fileVersion;

/**
 * Options passed to the constructor.
 * Currently the only possible option is OPT_ALLOW_OVERWRITE.
 */
int optFlag;                    // zero or more OPT_* bit options

/**
 * Current file status: ST_DEFINING, ST_WRITEDATA, or ST_CLOSED.
 */
int fileStatus;

/**
 * The time of the file open, in milliseconds after 1970.
 */
long utcModTimeMilliSec;

/**
 * The time of the file open, in seconds after 1970.
 */
long utcModTimeSec;

/**
 * Current indentation level, for debug only.
 */
int indent = 0;

/**
 * Main debug logging level.  Default is 0.
 * Meaningful levels are: 0, 1, 2, 5, 10.
 */
int debugLevel = 0;             // main debug level

/**
 * Current debug logging level.  Default is 0.
 * Meaningful levels are: 0, 1, 2, 5, 10.
 * In some cases we set bugs < debugLevel to omit
 * a section of tedious output.
 */
int bugs = 0;

/**
 * The in-memory buffer used to construct all metadata (HdfGroups,
 * messages, attributes, Btrees, etc).
 * In endDefine() we write this buffer out to the start of the file.
 */
private HBuffer mainBuf;

/**
 * Output stream for outFile, underneath outChannel.
 */
FileOutputStream outStream;

/**
 * Output channel for outFile, on top of outStream.
 */
FileChannel outChannel;

/**
 * List of BaseBlks that need to be formatted to mainBuf.
 * Used by formatBufAll, which is called by endDefine (formatPass=1)
 * and close (formatPass=2).
 */
ArrayList<BaseBlk> workList = null;



/** HdfFileWriter (HDF5 file) signature byte 0 */
final int signa = 0x89;
/** HdfFileWriter (HDF5 file) signature byte 1 */
final int signb = 'H';
/** HdfFileWriter (HDF5 file) signature byte 2 */
final int signc = 'D';
/** HdfFileWriter (HDF5 file) signature byte 3 */
final int signd = 'F';
/** HdfFileWriter (HDF5 file) signature byte 4 */
final int signe = '\r';
/** HdfFileWriter (HDF5 file) signature byte 5 */
final int signf = '\n';
/** HdfFileWriter (HDF5 file) signature byte 6 */
final int signg = 0x1a;
/** HdfFileWriter (HDF5 file) signature byte 7 */
final int signh = '\n';


/**
 * HDF5 file format: consistency bits: (The low order bit is 0).
 *    0: file is open for write.
 *    1: file has been verified as consistent.
 * Always 0; not used by this software.
 */
final int consistencyFlag = 0;

/**
 * HDF5 file format: starting offset of header (superblock).
 * Always 0; not used by this software.
 */
final long baseAddress = 0;

/**
 * HDF5 file format: offset of superblock extension.
 * Always 0; not used by this software.
 */
final long superblockExtensionAddress = UNDEFINED_ADDR;    // -1

/**
 * Current address of the end of file.
 * This is updated by endDefine and HdfGroup.writeDataSub.
 */
long eofAddr;




/**
 * Used by fileVersion==1 only:
 * A leaf btree node (a symbol table) should have:<br>
 *   k_leaf &lt;= numUsedEntries &lt;= 2 * k_leaf
 */
int k_leaf = 4;          // This is set in SymbolTable.addSymName

/**
 * Used by fileVersion==1 only:
 * An internal btree node should have:<br>
 *   k_internal &lt;= numUsedEntries &lt;= 2 * k_internal
 */
int k_internal = 16;



/**
 * Creates a new HDF5 output file.
 * @param filePath  The name or disk path of the file to create.
 * @param fileVersion  Either 1 (deprecated HDF5 format)
 *     or 2 (new HDF5 format).
 *     Using 2 is strongly recommended.
 * @param optFlag  The bitwise OR of one or more OPT_* flags.
 *     Currently the only one implemented is OPT_ALLOW_OVERWRITE.
 */

public HdfFileWriter(
  String filePath,               // file to create
  int fileVersion,
  int optFlag)                   // zero or more OPT_* bit options
throws HdfException
{
  this( filePath, fileVersion, optFlag, 0, 0);   // debug = 0, modTime = 0
}




/**
 * Do not use: for internal testing only.
 * Creates a new HDF5 output file.
 * @param filePath  The name or disk path of the file to create.
 * @param fileVersion  Either 1 (old HDF5 format) or 2 (new HDF5 format).
 *     Using 2 is strongly recommended.
 * @param optFlag  The bitwise OR of one or more OPT_* flags.
 *     Currently the only one implemented is OPT_ALLOW_OVERWRITE.
 * @param debugLevel  Level for logging debug messages to stdout:<ul>
 *   <li>   0:   none
 *   <li>   1:   HdfGroup: addVariable, addAttribute, writeData
 *   <li>   2:   also: endDefine, close, formatBufAll, more writeData, flush
 *   <li>   5:   also: all written data
 *   <li>  10:   also: HBuffer.expandBuf
 * @param utcModTime Specify internal file modification time
 *   in milliSeconds since 1970.  If 0, we use the current time.
 *  </ul>
 */

public HdfFileWriter(
  String filePath,           // file to create
  int fileVersion,
  int optFlag,               // zero or more OPT_* bit options
  int debugLevel,
  long utcModTime)           // milliSecs since 1970, or if 0 use current time
throws HdfException
{
  super("HdfFileWriter", null);  // BaseBlk: hdfFile = null
  this.hdfFile = this;
  this.filePath = filePath;
  this.fileVersion = fileVersion;
  this.optFlag = optFlag;
  this.debugLevel = debugLevel;
  this.bugs = debugLevel;

  if (fileVersion != 1 && fileVersion != 2)
    throwerr("invalid fileVersion: %d", fileVersion);
  fileStatus = ST_DEFINING;
  utcModTimeMilliSec = utcModTime;
  if (utcModTimeMilliSec == 0)
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
} // end constructor



public String toString() {
  String res = super.toString();
  res += "  filePath: " + filePath;
  res += "  fileVersion: " + fileVersion;
  res += "  optFlag: " + optFlag;
  res += "  status: " + statusNames[fileStatus];
  return res;
}



/**
 * Returns the unique root Group.
 */

public HdfGroup getRootGroup() {
  return rootGroup;
}



/**
 * Returns the main debug level = debugLevel.
 */
public int getDebugLevel() {
  return debugLevel;
}



/**
 * Sets both the main and the current debug levels.
 */

public void setDebugLevel( int debugLevel) {
  this.debugLevel = debugLevel;     // main debugLevel
  this.bugs = debugLevel;           // current debugLevel
}




/**
 * Indicates the end of definition phase for the client.
 * <ul>
 *   <li> Calls formatBufAll to format all metadata into
 *       mainBuf, using formatPass==1
 *       (formatPass==2 happens later in endDefine).
 *   <li> Sets eofAddr = mainBuf.getPos().
 * </ul>
 */

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
    prtf("\nHdfFileWriter.endDefine: start pass 1: mainBuf pos: %d",
      mainBuf.getPos());

  if (bugs >= 2)
    prtf("HdfFileWriter.endDefine: set bugs = 0 for formatPass 1");
  bugs = 0;
  formatBufAll( 1);           // formatPass = 1

  bugs = debugLevel;
  if (bugs >= 2)
    prtf("HdfFileWriter.endDefine: after pass 1: mainBuf pos: %d",
      mainBuf.getPos());

  if (fileVersion == 1) {
    // Set Btree and Heap addresses in superBlock's SymTabEntry scratchPad.
    symTabEntry.scratchBtree = rootGroup.btreeNode;
    symTabEntry.scratchHeap  = rootGroup.localHeap;
  }

  // Set eofAddr in superBlock
  eofAddr = mainBuf.getPos();

} // end endDefine








/**
 * Indicates the end of writing data for the client.
 * Previously the eofAddr was updated by each call to
 * HdfGroup.writeData, and we don't alter it.
 * <ul>
 *   <li> Calls formatBufAll to format all metadata into
 *       mainBuf, using formatPass==2
 *       (formatPass==1 happened earlier in endDefine).
 *   <li> Writes mainBuf to outChannel (the output file)
 *       starting at position 0 (before the raw data for variables)
 *   <li> Closes outChannel and outStream.
 * </ul>
 */


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
    if (grp.isVariable
      && (! grp.isWritten)
      && grp.msgDataSpace.totNumEle != 0)
    {
      errMsg += "  " + grp.getPath();
    }
  }
  if (errMsg.length() > 0)
    throwerr("close: the following dataset(s) still need to written: %s",
      errMsg);

  // Format metadata to buffer, pass 2 of 2  (pass 1 is in endDefine)
  // This time all the pointers should be accurate.
  if (bugs >= 2)
    prtf("\nHdfFileWriter.close: start pass 2: mainBuf pos: %d", mainBuf.getPos());

  mainGlobalHeap.clear();
  mainBuf.clear();

  formatBufAll( 2);           // formatPass = 2

  if (bugs >= 2)
    prtf("HdfFileWriter.close: after pass 2: mainBuf pos: %d", mainBuf.getPos());

  // Write mainBuf to outfile
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





/**
 * Recursively adds all groups in the tree headed by grp to groupList.
 * @param grp    group to start depth first tree search
 * @param groupList  output list of groups
 */

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


// xxx make everything private


/**
 * Formats all  metadata (HdfGroups, messages, attributes, Btrees, etc)
 * to mainBuf, the in-memory buffer used to format all metadata.
 * This is called by endDefine with formatPass == 1,
 * and by close with formatPass == 2.
 * Essentially formatBufAll does a breadth first search of
 * BaseBlks, using workList to keep the list of BaseBlks to format.
 */

void formatBufAll( int formatPass)
throws HdfException
{
  if (bugs >= 2) {
    prtf("\nHdfFileWriter.formatBufAll: entry. formatPass: %d  mainBuf pos: 0x%x",
      formatPass, mainBuf.getPos());
  }

  HBuffer fmtBuf = mainBuf;

  // Format everything
  workList = new ArrayList<BaseBlk>();
  addWork("HdfFileWriter", this);
  while (workList.size() > 0) {
    BaseBlk blk = workList.remove(0);
    if (bugs >= 5) prtf(
      "\nHdfFileWriter.formatBufAll pop: %s  pos 0x%x  new list len: %d",
      blk.blkName, blk.blkPosition, workList.size());
    blk.formatBuf( formatPass, fmtBuf);
  }

  // Format globalHeap last since other blocks may add items
  if (bugs >= 2)
    prtf("\nHdfFileWriter.formatBufAll: format globalHeap.  fmtBuf pos: 0x%x",
      fmtBuf.getPos());
  mainGlobalHeap.formatBuf( formatPass, fmtBuf);

  if (bugs >= 2) {
    prtf("\nHdfFileWriter.formatBufAll: exit. formatPass: %d  fmtBuf pos: 0x%x",
      formatPass, fmtBuf.getPos());
  }
} // end formatBufAll





/**
 * Extends abstract BaseBlk: formats this individual BaseBlk
 * to fmtBuf and calls addWork to add any referenced BaseBlks
 * to workList for future formatting.
 */

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
    fmtBuf.putBufByte("HdfFileWriter: OFFSET_SIZE", OFFSET_SIZE);
    fmtBuf.putBufByte("HdfFileWriter: LENGTH_SIZE", OFFSET_SIZE);
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
    fmtBuf.putBufByte("HdfFileWriter: OFFSET_SIZE", OFFSET_SIZE);
    fmtBuf.putBufByte("HdfFileWriter: LENGTH_SIZE", OFFSET_SIZE);
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





/**
 * Adds blk to workList, so formatBufAll will call blk's formatBuf
 * in the future.
 */

void addWork( String msg, BaseBlk blk)
throws HdfException
{
  boolean foundIt = false;
  for (BaseBlk wblk : workList) {
    if (wblk == blk) {
      foundIt = true;
      break;
    }
  }
  if (! foundIt) workList.add( blk);
  if (bugs >= 5) {
    if (foundIt) prtIndent("addWork: %s ignored duplicate addWork.  blk: %s",
      msg, blk);
    else prtIndent(
      "addWork: %s added: %s  pos 0x%x  new list len: %d",
      msg, blk.blkName, blk.blkPosition, workList.size());
  }
}






/**
 * For debug: format indent, current position in mainBuf, and name.
 */

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






/**
 * For debug: returns indent String.
 */

String mkIndent() {
  String res = "";
  for (int ii = 0; ii < indent; ii++) {
    res += "  ";
  }
  return res;
}


} // end class
