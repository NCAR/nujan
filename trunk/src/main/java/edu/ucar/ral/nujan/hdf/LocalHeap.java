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

import java.util.ArrayList;
import java.util.Arrays;



/**
 * Represents a local heap.  These are used in fileVersion==1
 * SymbolTables to keep the names of subgroups and variables.
 */

class LocalHeap extends BaseBlk {

final int signa = 'H';
final int signb = 'E';
final int signc = 'A';
final int signd = 'P';

final int heapVersion = 0;

/**
 * Offset of the next free bytee.
 */
int curHeapLen = 0;

/**
 * Array of the actual items to be stored in the heap.
 * Parallel array with offsetList.
 */
ArrayList<byte[]> itemList;

/**
 * Array of the offsets of the heap items.
 * Parallel array with itemList.
 */
ArrayList<Integer> offsetList;




LocalHeap(
  HdfFileWriter hdfFile)
{
  super("LocalHeap", hdfFile);

  curHeapLen = 0;
  itemList = new ArrayList<byte[]>();
  offsetList = new ArrayList<Integer>();
}




public String toString() {
  String res = super.toString();
  res += "  itemList.size: " + itemList.size();
  return res;
}




/**
 * Adds itemBare to our itemList, updates our offsetList,
 * and returns offsetList[n-1] == the byte offset of itemBare.
 */

int putHeapItem(
  String msg,
  byte[] itemBare)                 // without null termination
throws HdfException
{
  if (itemBare == null) throwerr("invalid item");

  // Add null term
  byte[] itemTerm = Arrays.copyOf( itemBare, itemBare.length + 1);

  int toffset = getHeapOffsetSub( itemTerm);
  if (toffset >= 0) {        // found it
    if (hdfFile.bugs >= 5)
      prtf("putHeapString: FOUND: msg: %s:  offset: %d  itemTerm: %s",
        msg, toffset, HdfUtil.formatBytes( itemTerm, 0, itemTerm.length));
  }
  else {
    toffset = curHeapLen;
    if (hdfFile.bugs >= 5)
      prtf("putHeapString: CREATE: msg: %s:  offset: %d  itemTerm: %s",
        msg, toffset, HdfUtil.formatBytes( itemTerm, 0, itemTerm.length));

    itemList.add( Arrays.copyOf( itemTerm, itemTerm.length));
    offsetList.add( new Integer( toffset));
    curHeapLen += itemTerm.length;

    if (curHeapLen % 8 != 0)
      curHeapLen += (8 - curHeapLen % 8);   // align to 8
  }
  return toffset;
}



/**
 * Scans itemList for a matching item; returns the item's offset
 * in the heap, or throws an HdfException if not found.
 */

// Throws exc if not found
int getHeapOffset(
  byte[] itemBare)                 // without null termination
throws HdfException
{
  // Add null term
  byte[] itemTerm = Arrays.copyOf( itemBare, itemBare.length + 1);

  int ires = getHeapOffsetSub( itemTerm);
  if (ires < 0)
    throwerr("getHeapOffset: item not found: %s",
      HdfUtil.formatBytes( itemBare, 0, itemBare.length));
  return ires;
}






/**
 * Scans itemList for a matching item; returns the item's offset
 * in the heap, or -1 if not found.
 */

int getHeapOffsetSub(
  byte[] itemTerm)          // null terminated
throws HdfException
{
  if (itemTerm == null) throwerr("itemTerm is null");
  if (itemTerm[itemTerm.length - 1] != 0)
    throwerr("itemTerm not null terminated");
  int ires = -1;
  for (int ii = 0; ii < itemList.size(); ii++) {
    if (Arrays.equals( itemList.get(ii), itemTerm)) {
      ires = offsetList.get(ii).intValue();
      break;
    }
  }
  return ires;
}






/**
 * Extends abstract BaseBlk: formats this individual BaseBlk
 * to fmtBuf.
 */

void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  fmtBuf.putBufByte("LocalHeap: signa", signa);
  fmtBuf.putBufByte("LocalHeap: signb", signb);
  fmtBuf.putBufByte("LocalHeap: signc", signc);
  fmtBuf.putBufByte("LocalHeap: signd", signd);

  fmtBuf.putBufByte("LocalHeap: heapVersion", heapVersion);
  fmtBuf.putBufByte("LocalHeap: reserved", 0);
  fmtBuf.putBufShort("LocalHeap: reserved", 0);

  fmtBuf.putBufLong("LocalHeap: curHeapLen", curHeapLen);

  // H5HLcache.c:#define H5HL_FREE_NULL  1  // End of free list
  fmtBuf.putBufLong("LocalHeap: freeListOffset", 1);
  fmtBuf.putBufLong("LocalHeap: dataSegAddr", fmtBuf.getPos() + 8);

  // Format the individual strings
  int curOffset = 0;
  for (int ii = 0; ii < itemList.size(); ii++) {
    if (curOffset != offsetList.get(ii).intValue())
      throwerr("offset mismatch");
    byte[] item = itemList.get(ii);
    fmtBuf.putBufBytes( "LocalHeap: heap string", item);
    curOffset += item.length;

    while (curOffset % 8 != 0) {
      fmtBuf.putBufByte("LocalHeap: heap string pad", 0);   // pad to 8
      curOffset++;
    }
  }

  noteFormatExit( fmtBuf);         // BaseBlk: print debug
} // end formatBuf




} // end class
