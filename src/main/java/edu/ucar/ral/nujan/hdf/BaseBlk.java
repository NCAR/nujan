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
 * Represents a metadata structure (block).
 * <p>
 * Extended by BtreeNode GlobalHeap HdfFileWriter HdfGroup
 *   LocalHeap MsgBase SymbolTable SymTabEntry
 * <p>
 * The subclass must override formatBuf to format the
 * structure to the output buffer.
 */


abstract class BaseBlk {


/**
 * The offset of this BaseBlk in HdfFileWriter.mainBuf.
 */
long blkPosition;

/**
 * For debug: the name of the BaseBlk, such as "HdfGroup" or "MsgAttribute".
 */
String blkName;

/**
 * The global owning HdfFileWriter.
 */
HdfFileWriter hdfFile;




/**
 * @param blkName For debug: the name of the BaseBlk,
 *   such as "HdfGroup" or "MsgAttribute".
 * @param hdfFile The global owning HdfFileWriter.
 */

BaseBlk(
  String blkName,
  HdfFileWriter hdfFile)
{
  this.blkName = blkName;
  this.hdfFile = hdfFile;
}



public String toString() {
  String res = String.format("  blkName: %s  pos: 0x%x",
    blkName, blkPosition);
  return res;
}



/**
 * Formats this individual BaseBlk to the output buffer fmtBuf.
 * @param formatPass: <ul>
 *   <li> 1: Initial formatting to determine the formatted length.
 *          In HdfGroup we add msgs to hdrMsgList.
 *   <li> 2: Final formatting.
 * </ul>
 * @param fmtBuf  output buffer
 */

abstract void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException;







/**
 * Aligns fmtBuf position to multiple of 8 and sets our blkPosition
 * to the new position.
 * Prints debug message for formatBuf entry.
 * Should be called first thing in formatBuf() in every
 * class extending BaseBlk.
 */

void setFormatEntry(
  int formatPass,
  boolean useAlign,
  HBuffer fmtBuf)
throws HdfException
{
  hdfFile.indent++;

  fmtBuf.alignPos( "setFormatEntry for " + blkName, 8);
  blkPosition = fmtBuf.getPos();
  if (hdfFile.bugs >= 5)
    prtf( hdfFile.formatName("setFormatEntry: " + blkName, fmtBuf.getPos()));
}





/**
 * Prints debug message for formatBuf exit.
 * Should be called last thing in formatBuf() in every
 * class extending BaseBlk.
 */

void noteFormatExit(
  HBuffer fmtBuf)
throws HdfException
{
  if (hdfFile.bugs >= 5)
    prtf( hdfFile.formatName("noteFormatExit: " + blkName, fmtBuf.getPos()));
  hdfFile.indent--;
}




/**
 * Just throws HdfException
 */

static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}




/**
 * Prints a line indented by hdfFile.indent.
 */

void prtIndent( String msg, Object... args) {
  prtf( hdfFile.mkIndent() + msg, args);
}



/**
 * Prints a line.
 */

static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}

} // end class
