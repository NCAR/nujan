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


/**
 * HDF5 message type 5: MsgFillValue: specify data fill value.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

class MsgFillValue extends MsgBase {


int dtype;                      // one of HdfGroup.DTYPE*
boolean isFillExtant;           // if extant but fillValue==null: default
Object fillValue;
int elementLen;


final int fillVersion = 2;

// Meanings for allocTime:
//   0  not used
//   1  early: alloc space when dataset is created
//   2  late: alloc space when dataset is written
//   3  incremental: alloc space chunk by chunk
final int fillAllocTime = 2;

// Meanings for writeTime:
//   0  on alloc: write fill value when space is allocated
//   1  never
//   2  if user has a non-default fill value, write on alloc.
final int fillWriteTime = 2;

// defined == 1 iff a fill value is defined for this dataset
//   For version 0, 1: we still have fillSize, fillValue fields
//   For version 2: fillSize, fillValue exist only if defined == 1.
int fillDefined;

byte[] fillBytes;


/**
 * @param dtype The fill value type - one of HdfGroup.DTYPE*.
 * @param isFillExtant There are 4 cases:
 *   <table border="1'>
 *   <tr><th> isFillExtant </th><th> fillValue </th><th> HDF5 result </th></tr>
 *   <tr><td> false </td><td> null     </td><td> no fill value </td></tr>
 *   <tr><td> true  </td><td> null     </td><td> use default fill value (fillDefined=1, fillLen=0)</td></tr>
 *   <tr><td> false </td><td> Non-null </td><td> error </td></tr>
 *   <tr><td> true  </td><td> Non-null </td><td> specified fill value </td></tr>
 *   </table>
 *
 * @param fillValue The fill value.  They type must agree with dtype:
 *   <table border="1'>
 *   <tr><th> dtype                     </th><th> fillValue Java type </th></tr>
 *   <tr><td> HdfGroup.DTYPE_SFIXED08   </td><td> Byte    </td></tr>
 *   <tr><td> HdfGroup.DTYPE_UFIXED08   </td><td> Byte    </td></tr>
 *   <tr><td> HdfGroup.DTYPE_FIXED16    </td><td> Short   </td></tr>
 *   <tr><td> HdfGroup.DTYPE_FIXED32    </td><td> Integer </td></tr>
 *   <tr><td> HdfGroup.DTYPE_FIXED64    </td><td> Long    </td></tr>
 *   <tr><td> HdfGroup.DTYPE_FLOAT32    </td><td> Float   </td></tr>
 *   <tr><td> HdfGroup.DTYPE_FLOAT64    </td><td> Double  </td></tr>
 *   <tr><td> HdfGroup.DTYPE_STRING_FIX </td><td> String  </td></tr>
 *   <tr><td> HdfGroup.DTYPE_STRING_VAR </td><td> String  </td></tr>
 *   </table>
 *
 * @param hdfGroup The owning HdfGroup.
 * @param hdfFile The global owning HdfFileWriter.
 */

// If isFillExtant but fillValue==null,
// we output fillDefined=1, fillLen=0,
// which h5dump interprets as "fill value defined: default".


MsgFillValue(
  int dtype,                      // one of HdfGroup.DTYPE*
  boolean isFillExtant,           // if extant but fillValue==null: default
  Object fillValue,               // null, Byte, Short, Int, Long,
                                  // Float, Double, String, etc.
  HdfGroup hdfGroup,              // the owning group
  HdfFileWriter hdfFile)
throws HdfException
{
  super( TP_FILL_VALUE, hdfGroup, hdfFile);
  this.dtype = dtype;
  this.isFillExtant = isFillExtant;
  this.fillValue = fillValue;

  elementLen = 0;
  if (! isFillExtant) {
    if (fillValue != null) throwerr("not extant but fillValue != null");
    fillDefined = 0;
    fillBytes = null;
  }

  else if (fillValue == null) {
    fillDefined = 1;
    fillBytes = null;
  }

  else {

    // Set elementLen
    if (dtype == HdfGroup.DTYPE_SFIXED08) elementLen = 1;
    else if (dtype == HdfGroup.DTYPE_UFIXED08) elementLen = 1;
    else if (dtype == HdfGroup.DTYPE_FIXED16) elementLen = 2;
    else if (dtype == HdfGroup.DTYPE_FIXED32) elementLen = 4;
    else if (dtype == HdfGroup.DTYPE_FIXED64) elementLen = 8;
    else if (dtype == HdfGroup.DTYPE_FLOAT32) elementLen = 4;
    else if (dtype == HdfGroup.DTYPE_FLOAT64) elementLen = 8;
    else if (dtype == HdfGroup.DTYPE_STRING_FIX) {
      byte[] bytes = HdfUtil.encodeString( (String) fillValue, true, hdfGroup);
      elementLen = bytes.length;
    }
    else if (dtype == HdfGroup.DTYPE_STRING_VAR) {
      // Element is: len(4), gcolAddr(8), gcolIndex(4)
      elementLen = 4 + 8 + 4;
    }
    else throwerr("illegal dtype for fill value: " + dtype);



    fillDefined = 1;
    fillBytes = new byte[elementLen];
    ByteBuffer tbuf = ByteBuffer.wrap( fillBytes);
    tbuf.order( ByteOrder.LITTLE_ENDIAN);

    if (dtype == HdfGroup.DTYPE_SFIXED08
      || dtype == HdfGroup.DTYPE_UFIXED08)
    {
      if (! (fillValue instanceof Byte))
        throwerr("fill type mismatch.  Expected: Byte.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      tbuf.put( ((Byte) fillValue).byteValue());
    }
    else if (dtype == HdfGroup.DTYPE_FIXED16) {
      if (! (fillValue instanceof Short))
        throwerr("fill type mismatch.  Expected: Short.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      tbuf.putShort( ((Short) fillValue).shortValue());
    }
    else if (dtype == HdfGroup.DTYPE_FIXED32) {
      if (! (fillValue instanceof Integer))
        throwerr("fill type mismatch.  Expected: Integer.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      tbuf.putInt( ((Integer) fillValue).intValue());
    }
    else if (dtype == HdfGroup.DTYPE_FIXED64) {
      if (! (fillValue instanceof Long))
        throwerr("fill type mismatch.  Expected: Long.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      tbuf.putLong( ((Long) fillValue).longValue());
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT32) {
      if (! (fillValue instanceof Float))
        throwerr("fill type mismatch.  Expected: Float.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      tbuf.putFloat( ((Float) fillValue).floatValue());
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT64) {
      if (! (fillValue instanceof Double))
        throwerr("fill type mismatch.  Expected: Double.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      tbuf.putDouble( ((Double) fillValue).doubleValue());
    }
    else if (dtype == HdfGroup.DTYPE_STRING_FIX) {
      if (! (fillValue instanceof String))
        throwerr("fill type mismatch.  Expected: String.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
      byte[] bytes = HdfUtil.encodeString( (String) fillValue, true, hdfGroup);
      tbuf.put( bytes);
    }
    else if (dtype == HdfGroup.DTYPE_STRING_VAR) {
      // We will fill the fillBytes later, in formatMsgCore below,
      // since it depends on the gcol addr.
      // Element is: len(4), gcolAddr(8), gcolIndex(4)

      if (! (fillValue instanceof String))
        throwerr("fill type mismatch.  Expected: String.  Found: "
          + fillValue.getClass() + "  for group: " + hdfGroup.getPath());
    }
    else throwerr("unknown dtype for fillValue."
      + "  dtype: " + dtype
      + "  for group: " + hdfGroup.getPath());
  }
}




public String toString() {
  String res = super.toString();
  if (fillValue == null) res += "  fillValue: (null)";
  else res += "  fillValue: " + fillValue + " (" + fillValue.getClass() + ")";
  return res;
}






// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgFillValue: fillVersion", fillVersion);
  fmtBuf.putBufByte("MsgFillValue: fillAllocTime", fillAllocTime);
  fmtBuf.putBufByte("MsgFillValue: fillWriteTime", fillWriteTime);
  fmtBuf.putBufByte("MsgFillValue: fillDefined", fillDefined);
  if (fillVersion == 1
    || fillVersion == 2 && fillDefined == 1)
  {
    fmtBuf.putBufInt("MsgFillValue: elementLen", elementLen);

    if (dtype == HdfGroup.DTYPE_STRING_VAR) {
      byte[] bytes = HdfUtil.encodeString( (String) fillValue, false, hdfGroup);
      int heapIx = hdfFile.mainGlobalHeap.putHeapItem("fillValue", bytes);
      fmtBuf.putBufInt("MsgFillValue: vstring len", bytes.length);
      fmtBuf.putBufLong(
        "MsgFillValue: vstring gcol", hdfFile.mainGlobalHeap.blkPosition);
      fmtBuf.putBufInt("MsgFillValue: vstring heapIx", heapIx);
    }
    else {
      for (int ii = 0; ii < elementLen; ii++) {
        fmtBuf.putBufByte("MsgFillValue: fillBytes", 0xff & fillBytes[ii]);
      }
    }
  }
}



} // end class
