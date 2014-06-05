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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;



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



static String softwareVersion = "0.9.2";

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
 * The superblock extension
 */
HdfGroup extensionGroup;



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
 * The file path (name) of the output file on disk.
 */
String filePath;


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
 * The max num of kids in any Btree node.
 */
int maxNumBtreeKid = 100;

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

/** For performance testing only */
String logDir;

/** For performance testing only */
String statTag;

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
 * Current address of the end of file.
 * This is updated by endDefine and HdfGroup.writeDataSub.
 */
long eofAddr;


/** Used for debug statistics.  */
PrintWriter statOut;

/** Used for debug statistics.  */
long statTimea;
long statTimeOverall;



/**
 * Creates a new HDF5 output file.
 * @param filePath  The name or disk path of the file to create.
 * @param optFlag  The bitwise OR of one or more OPT_* flags.
 *     Currently the only one implemented is OPT_ALLOW_OVERWRITE.
 */

public HdfFileWriter(
  String filePath,               // file to create
  int optFlag)                   // zero or more OPT_* bit options
throws HdfException
{
  this( filePath, optFlag, 0, 0, null, null); // debug = 0, modTime = 0
}




/**
 * Creates a new HDF5 output file.
 * @param filePath  The name or disk path of the file to create.
 * @param optFlag  The bitwise OR of one or more OPT_* flags.
 *     Currently the only one implemented is OPT_ALLOW_OVERWRITE.
 */

public HdfFileWriter(
  String filePath,               // file to create
  int optFlag,                   // zero or more OPT_* bit options
  String logDir,
  String statTag)
throws HdfException
{
  this( filePath, optFlag, 0, 0, logDir, statTag); // debug = 0, modTime = 0
}




/**
 * Do not use: for internal testing only.
 * Creates a new HDF5 output file.
 * @param filePath  The name or disk path of the file to create.
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
  int optFlag,               // zero or more OPT_* bit options
  int debugLevel,
  long utcModTime,           // milliSecs since 1970, or if 0 use current time
  String logDir,
  String statTag)
throws HdfException
{
  super("HdfFileWriter", null);  // BaseBlk: hdfFile = null
  this.hdfFile = this;
  this.filePath = filePath;
  this.optFlag = optFlag;
  this.debugLevel = debugLevel;
  this.bugs = debugLevel;
  this.logDir = logDir;
  this.statTag = statTag;


  if (bugs >= 1) {
    prtf("HdfFileWriter.const: filePath: \"%s\"\n  softwareVersion: %s",
      filePath, getSoftwareVersion());
  }
  initStat();
  statTimea = printStat( 0, "wtr.const.entry", "filePath: " + filePath);
  statTimeOverall = statTimea;

  fileStatus = ST_DEFINING;
  utcModTimeMilliSec = utcModTime;
  if (utcModTimeMilliSec == 0)
    utcModTimeMilliSec = System.currentTimeMillis();
  utcModTimeSec = utcModTimeMilliSec / 1000;
  indent = 0;

  // Make the superBlock extension
  extensionGroup = new HdfGroup( this);

  // Make rootGroup
  String rootName = "";
  rootGroup = new HdfGroup( rootName, null, this);   // parent = null

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
  String res = "  filePath: \"" + filePath + "\""
    + "  status: " + statusNames[ fileStatus]
    + "  softwareVersion: " + getSoftwareVersion();
  return res;
}






static public String getSoftwareVersion() {
  return softwareVersion;
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
  statTimea = printStat( statTimea, "wtr.endDefine.entry",
    "filePath: " + filePath);
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

  //xxx
  //if (bugs >= 2)
  //  prtf("HdfFileWriter.endDefine: set bugs = 0 for formatPass 1");
  //bugs = 0;
  formatBufAll( 1);           // formatPass = 1

  bugs = debugLevel;
  if (bugs >= 2)
    prtf("HdfFileWriter.endDefine: after pass 1: mainBuf pos: %d",
      mainBuf.getPos());

  // Set eofAddr in superBlock
  eofAddr = mainBuf.getPos();
  statTimea = printStat( statTimea, "wtr.endDefine.exit",
    "filePath: " + filePath);

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
  statTimea = printStat( statTimea, "wtr.close.entry",
    "filePath: " + filePath);

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
    if (grp.isVariable && grp.msgDataSpace.totNumEle != 0) {
      for (HdfChunk chunk : grp.hdfChunks) {
        if (chunk.chunkDataAddr == 0)
          errMsg += "  " + grp.getPath()
            + "  chunk indices: "
            + HdfUtil.formatInts( chunk.chunkStartIxs) + "\n";
      }
    }
  }
  if (errMsg.length() > 0)
    throwerr("close: the following dataset chunks still need to written:\n%s",
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
  statTimea = printStat( statTimea, "wtr.close.exit.a",
    "filePath: " + filePath);
  statTimeOverall = printStat( statTimeOverall, "wtr.close.exit.overall",
    "filePath: " + filePath);
  closeStat();
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
 * Formats this individual BaseBlk to fmtBuf;
 * calls addWork to add any referenced BaseBlks (the rootGroup)
 * to workList; extends abstract BaseBlk.
 *
 * @param formatPass: <ul>
 *   <li> 1: Initial formatting to determine the formatted length.
 *          In HdfGroup we add msgs to hdrMsgList.
 *   <li> 2: Final formatting.
 * </ul>
 * @param fmtBuf  output buffer
 */

void formatBuf(
  int formatPass,
  HBuffer fmtBuf)
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

  int superBlockVersion = 2;
  fmtBuf.putBufByte("HdfFileWriter: superBlockVersion", superBlockVersion);

  fmtBuf.putBufByte("HdfFileWriter: OFFSET_SIZE", OFFSET_SIZE);
  fmtBuf.putBufByte("HdfFileWriter: LENGTH_SIZE", OFFSET_SIZE);
  fmtBuf.putBufByte("HdfFileWriter: consistencyFlag", consistencyFlag);
  fmtBuf.putBufLong("HdfFileWriter: baseAddress", baseAddress);

  long extenAddr = UNDEFINED_ADDR;    // -1
  if (extensionGroup != null) extenAddr = extensionGroup.blkPosition;
  fmtBuf.putBufLong("HdfFileWriter: superblockExtensionAddress", extenAddr);

  fmtBuf.putBufLong("HdfFileWriter: eofAddr", eofAddr);
  fmtBuf.putBufLong("HdfFileWriter: rootGroupAddr", rootGroup.blkPosition);
  long endPos = fmtBuf.getPos();
  byte[] chkBytes = fmtBuf.getBufBytes( startPos, endPos);
  int checkSumHack = new CheckSumHack().calcHackSum( chkBytes);
  fmtBuf.putBufInt("HdfFileWriter: checkSumHack", checkSumHack);

  // External block
  if (extensionGroup != null) addWork("HdfFileWriter", extensionGroup);
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




// Opens log file with name like:
// logDir/yyyy.mm.dd.hh/nujan.statTag.1300287887634.log

void initStat()
throws HdfException
{
  if (logDir != null) {
    // Change bad chars to underbar
    String tstatTag = statTag.replaceAll("[ /,.:;()<>\\]\\[]", "_");

    Date curDate = new Date();
    SimpleTimeZone utcZone = new SimpleTimeZone( 0, "UTC");
    SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy.MM.dd.HH");
    utcSdf.setTimeZone( utcZone);

    File dirFile;
    dirFile = new File(logDir);
    dirFile.mkdirs();
    if (! dirFile.isDirectory())
      throwerr("cannot init stats logDir: \"" + logDir + "\"");

    String sepChar = System.getProperty("file.separator");
    String dirName = logDir;
    if (! dirName.endsWith( sepChar)) dirName += sepChar;
    dirName += utcSdf.format( curDate);
    dirFile = new File(dirName);
    dirFile.mkdirs();
    if (! dirFile.isDirectory())
      throwerr("cannot init stats dir: \"" + dirName + "\"");

    String logName = dirName + sepChar + "nujan." + tstatTag
      + "." + System.currentTimeMillis()
      + "." + Thread.currentThread().getId()
      + ".log";
    try { statOut = new PrintWriter( logName); }
    catch( IOException exc) {
      exc.printStackTrace();
      throwerr("cannot open logName: \"" + logName + "\"");
    }
  }
}


void closeStat()
{
  if (logDir != null) statOut.close();
}
  


long printStat(
  long prevTime,
  String tag,
  String extraMsg)
{
  long curTime = System.currentTimeMillis();
  if (logDir != null) {
    Date curDate = new Date( curTime);
    SimpleTimeZone utcZone = new SimpleTimeZone( 0, "UTC");
    //xxxGregorianCalendar utcCal = new GregorianCalendar( utcZone);
    //xxxutcCal.setTimeInMillis( curTime);
    SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    utcSdf.setTimeZone( utcZone);

    String deltaMsg = "NA";
    if (prevTime != 0)
      deltaMsg = String.format("%.4f", 0.001 * (curTime - prevTime));

    Runtime rt = Runtime.getRuntime();
    rt.gc();
    long freeMem = rt.freeMemory();

    String msg = utcSdf.format( curDate)
      + " " + tag
      + "  delta: " + deltaMsg
      + "  freeMem: " + freeMem;
    if (extraMsg != null) msg += "  " + extraMsg;
    statOut.println( msg);
    statOut.flush(); //////// xxx del this
  }
  return curTime;
}




} // end class
