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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * A misnomer: there can be many GlobalHeaps, each of which
 * is just a heap.
 * There are only two uses of GlobalHeaps:<ul>
 *   <li> The unique GlobalHeap hdfFile.mainGlobalHeap holds all:<ul>
 *       <li> attributes that are either VLEN and String
 *       <li> FillValues that are Strings.
 *     </ul>
 *   <li> If a variable is DTYPE_STRING_VAR, HdfGroup.gcol holds
 *        each element of the array as a separate heap item.
 * </ul>
 */

class GlobalHeap extends BaseBlk {


// Referenced blocks
// None.

// Header len = sum of: signature(4), version(1), reserved(3), heapSize(8)
static final int GLOB_HEAP_HDR_LEN = 16;

final int signa = 'G';
final int signb = 'C';
final int signc = 'O';
final int signd = 'L';

final int heapVersion = 1;
int numItem;

// Internal variables
ByteBuffer dataBuf;
int colSizeOffset;        // offset of the collectionSize field in dataBuf




GlobalHeap(
  HdfFileWriter hdfFile)
{
  super("GlobalHeap", hdfFile);
  dataBuf = ByteBuffer.allocate( 4096);
  dataBuf.order( ByteOrder.LITTLE_ENDIAN);
  clear();
}



/**
 * Removes all items from the heap.
 */

void clear() {
  numItem = 0;
  dataBuf.clear();

  dataBuf.put( (byte) signa);            // signature
  dataBuf.put( (byte) signb);
  dataBuf.put( (byte) signc);
  dataBuf.put( (byte) signd);
  dataBuf.put( (byte) 1);                // version
  dataBuf.put( (byte) 0);                // reserved
  dataBuf.putShort( (short) 0);          // reserved
  colSizeOffset = dataBuf.position();    // save offset for expand
  dataBuf.putLong( dataBuf.capacity());  // collectionSize
  if (hdfFile.bugs >= 10) {
    prtIndent("GlobalHeap.clear: capacity: %d", dataBuf.capacity());
  }
}




public String toString() {
  String res = super.toString();
  res += "  numItem: " + numItem;
  return res;
}




/**
 * Adds item to the heap and returns heap index number.
 */

int putHeapItem(
  String msg,
  byte[] item)
{
  numItem++;
  if (hdfFile.bugs >= 5)
    prtIndent("GlobalHeap.putHeapItem: %s  item len: %d", msg, item.length);

  // If need be, expand dataBuf in increments of 4096
  // Have extra space for the final free space entry.
  int needLen = dataBuf.position() + 100 + item.length;
  if (needLen > dataBuf.capacity()) {
    if (hdfFile.bugs >= 5)
      prtIndent("GlobalHeap.putHeapItem: expand gcol. needLen: " + needLen
        + "  capacity: " + dataBuf.capacity()
        + "  numItem: " + numItem);
    needLen = (1 + needLen / 4096) * 4096;  // round up to multiple of 4096
    ByteBuffer newBuf = ByteBuffer.allocate( needLen);
    newBuf.order( ByteOrder.LITTLE_ENDIAN);
    byte[] oldVals = new byte[ dataBuf.position()];
    dataBuf.flip();
    dataBuf.get( oldVals);
    newBuf.put( oldVals);
    dataBuf = newBuf;

    // Update the collectionSize
    dataBuf.putLong( colSizeOffset, dataBuf.capacity());
  }

  dataBuf.putShort( (short) numItem);    // object index
  dataBuf.putShort( (short) 0);          // reference count
  dataBuf.putInt( 0);                    // reserved
  dataBuf.putLong( item.length);

  dataBuf.put( item);                    // write item's bytes

  // Align end to next mult of 8
  long alignPos = HdfUtil.alignLong( 8, dataBuf.position());
  while (dataBuf.position() < alignPos) {
    dataBuf.put( (byte) 0);
  }
  return numItem;
}







/**
 * Adds a VLEN object to the global heap.
 * The object must be short[][] or int[][] or ...
 *
 * This must be called from within formatBuf
 * since the globalHeap is recreated in formatBufAll
 * for each formatPass.
 * We recreate the globalHeap because it may contain
 * references to other blocks, whose blkPositions are
 * updated as they are formatted.
 *
 * @return An array of heap reference indices, one per row of objValue.
 */

int[] putHeapVlenObject(
  HdfGroup hdfGroup,         // used only for error msgs
  int dtype,                 // should be VLEN
  int dsubType,
  int stgFieldLen,           // used for strings; without null term
  int[] varDims,             // must have one element: nrow
  Object objValue)           // must be short[][] or int[][] or ...
throws HdfException
{
  // Add the rows to the globalHeap.
  if (dtype != HdfGroup.DTYPE_VLEN) throwerr("wrong dtype");

  // Although we are given a two dimensional array,
  // internally HDF5 handles it as a one dimensional array.
  if (varDims.length != 1) throwerr("invalid rank for vlen");

  int nrow = varDims[0];
  int[] heapIxs = new int[nrow];
  for (int irow = 0; irow < nrow; irow++) {
    Object objRow = ((Object[]) objValue)[irow];

    byte[] heapItem = null;
    if (dsubType == HdfGroup.DTYPE_SFIXED08
      || dsubType == HdfGroup.DTYPE_UFIXED08)
    {
      int eleLen = 1;
      byte[] arow = (byte[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.put( arow[icol]);
      }
    }
    else if (dsubType == HdfGroup.DTYPE_FIXED16) {
      int eleLen = 2;
      short[] arow = (short[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.putShort( arow[icol]);
      }
    }
    else if (dsubType == HdfGroup.DTYPE_FIXED32) {
      int eleLen = 4;
      int[] arow = (int[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.putInt( arow[icol]);
      }
    }
    else if (dsubType == HdfGroup.DTYPE_FIXED64) {
      int eleLen = 8;
      long[] arow = (long[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.putLong( arow[icol]);
      }
    }
    else if (dsubType == HdfGroup.DTYPE_FLOAT32) {
      int eleLen = 4;
      float[] arow = (float[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.putFloat( arow[icol]);
      }
    }
    else if (dsubType == HdfGroup.DTYPE_FLOAT64) {
      int eleLen = 8;
      double[] arow = (double[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.putDouble( arow[icol]);
      }
    }
    else if (dsubType == HdfGroup.DTYPE_STRING_FIX) {
      String[] arow = (String[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ stgFieldLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        byte[] bytes = HdfUtil.encodeString( arow[icol], true, hdfGroup);
        tbuf.put( HdfUtil.truncPadNull( bytes, stgFieldLen));
      }
    }
    else if (dsubType == HdfGroup.DTYPE_REFERENCE) {
      int eleLen = HdfFileWriter.OFFSET_SIZE;
      HdfGroup[] arow = (HdfGroup[]) objRow;
      int ncol = arow.length;
      heapItem = new byte[ eleLen * ncol];
      ByteBuffer tbuf = ByteBuffer.wrap( heapItem);
      tbuf.order( ByteOrder.LITTLE_ENDIAN);
      for (int icol = 0; icol < ncol; icol++) {
        tbuf.putLong( arow[icol].blkPosition);
      }
    }
    else throwerr("unknown objValue type.  objValue: " + objValue
      + "  class: " + objValue.getClass());

    heapIxs[irow] = putHeapItem("vlen item", heapItem);
  } // for irow
  return heapIxs;
} // end putHeapVlenObject







/**
 * Formats this individual BaseBlk to the output buffer fmtBuf
 * (extends abstract BaseBlk).
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
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos
  if (hdfFile.bugs >= 5)
    prtIndent("GlobalHeap.formatBuf:"
      + "  capacity: " + dataBuf.capacity()
      + "  numItem: " + numItem);

  // Make free space entry
  long freeLen = dataBuf.capacity() - dataBuf.position();
  dataBuf.putShort( (short) 0);        // object index = 0 for free space
  dataBuf.putShort( (short) 0);        // reference count
  dataBuf.putInt( 0);                  // reserved
  dataBuf.putLong( freeLen);
  dataBuf.put( new byte[ (int) freeLen - 16]);

  if (hdfFile.bugs >= 5) {
    prtf("GlobalHeap.formatBuf: formatPass: %d  numItem: %d",
      formatPass, numItem);
  }
  fmtBuf.putBufBytes("GlobalHeap: dataBuf", dataBuf.array());

  noteFormatExit( fmtBuf);             // BaseBlk: print debug
} // end formatBuf




} // end class
