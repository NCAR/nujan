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

import java.util.ArrayList;
import java.util.Arrays;


class LocalHeap extends BaseBlk {

final int signa = 'H';
final int signb = 'E';
final int signc = 'A';
final int signd = 'P';

final int heapVersion = 0;
long dataSegLen;
long dataSegAddr;

// Internal variables
int curHeapLen = 0;          // offset of next free byte
ArrayList<byte[]> itemList;
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





int putHeapItem(
  String msg,
  byte[] item)                 // must include null termination
                               // xxx stgnullterm: change this?
throws HdfException
{
  if (item == null || item.length == 0 || item[item.length-1] != 0)
    throwerr("invalid item");

  int toffset = getHeapOffsetSub( item);
  if (toffset >= 0) {        // found it
    if (hdfFile.bugs >= 5)
      prtf("putHeapString: FOUND: msg: %s:  offset: %d  item: %s",
        msg, toffset, Util.formatBytes( item, 0, item.length));
  }
  else {
    toffset = curHeapLen;
    if (hdfFile.bugs >= 5)
      prtf("putHeapString: CREATE: msg: %s:  offset: %d  item: %s",
        msg, toffset, Util.formatBytes( item, 0, item.length));

    itemList.add( Arrays.copyOf( item, item.length));
    offsetList.add( new Integer( toffset));
    curHeapLen += item.length;

    if (curHeapLen % 8 != 0)
      curHeapLen += (8 - curHeapLen % 8);   // align to 8
  }
  return toffset;
}



// Scan itemList for matching item
// Returns -1 if not found
int getHeapOffsetSub( byte[] item)
{
  int ires = -1;
  for (int ii = 0; ii < itemList.size(); ii++) {
    if (Arrays.equals( itemList.get(ii), item)) {
      ires = offsetList.get(ii).intValue();
      break;
    }
  }
  return ires;
}




// Throws exc if not found
int getHeapOffset( byte[] item)
throws HdfException
{
  int ires = getHeapOffsetSub( item);
  if (ires < 0)
    throwerr("getHeapOffset: item not found: %s",
      Util.formatBytes( item, 0, item.length));
  return ires;
}





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
