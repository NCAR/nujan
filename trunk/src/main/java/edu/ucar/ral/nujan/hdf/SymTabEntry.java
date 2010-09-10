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
 * Represents a single symbol in a SymbolTable.
 * Used only for fileVersion 1 symbols: the names of the subgroups
 * and variables within a given group.
 */

class SymTabEntry extends BaseBlk {


// Referenced blocks
// None.  (groups are traversed via HdfGroup.subGroupList)


long heapOffset;

HdfGroup hdfGroup;

int cacheType;           // Cache type.  Default is 0.
                         //  0: no data is cached
                         //  1: group object header metadata is cached in
                         //     scratch, which implies the symbol table
                         //     refers to another group.
                         //  2: entry is a sym link....

BtreeNode scratchBtree;   // scratchPad contents
LocalHeap scratchHeap;    // scratchPad contents


/**
 * @param heapOffset Offset of the symbol in the owning group's local heap.
 * @param hdfGroup The owning HdfGroup.
 * @param hdfFile The global owning HdfFileWriter.
 */

SymTabEntry(
  long heapOffset,
  HdfGroup hdfGroup,
  HdfFileWriter hdfFile)
{
  super("SymTabEntry", hdfFile);
  this.heapOffset = heapOffset;
  this.hdfGroup = hdfGroup;
  cacheType = 0;
  scratchBtree = null;
  scratchHeap = null;
}




public String toString() {
  String res = super.toString();
  res += "  heapOffset: " + heapOffset;
  res += "  hdfGroup: " + hdfGroup.groupName;
  return res;
}





/**
 * Formats this individual BaseBlk to fmtBuf;
 * calls addWork to add any referenced BaseBlks (the hdfGroup)
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

  fmtBuf.putBufLong("SymTabEntry: heapOffset", heapOffset);

  // External block
  fmtBuf.putBufLong("SymTabEntry: hdfGroup.pos", hdfGroup.blkPosition);
  hdfFile.addWork("SymTabEntry", hdfGroup);

  fmtBuf.putBufInt("SymTabEntry: cacheType", cacheType);
  fmtBuf.putBufInt("SymTabEntry: reserved", 0);

  if (cacheType == 0) {
    fmtBuf.putBufLong("SymTabEntry: scratchPad", 0);
    fmtBuf.putBufLong("SymTabEntry: scratchPad", 0);
  }
  else if (cacheType == 1) {        // if root
    fmtBuf.putBufLong("SymTabEntry: scratchBtreeAddr",
      scratchBtree.blkPosition);
    fmtBuf.putBufLong("SymTabEntry: scratchHeapAddr",
      scratchHeap.blkPosition);
  }
  else throwerr("unknown cacheType: %d", cacheType);

  noteFormatExit( fmtBuf);             // BaseBlk: print debug
}



} // end class
