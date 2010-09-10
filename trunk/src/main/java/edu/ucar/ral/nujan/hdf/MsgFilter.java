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
 * HDF5 message type 11: MsgFilter:
 * Used to keep info on the encode/decode filter pipeline;
 * this package uses it only for the DEFLATE (compression) filter.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

class MsgFilter extends MsgBase {


static final int FILT_DEFLATE      = 1;
static final int FILT_SHUFFLE      = 2;
static final int FILT_FLETCHER32   = 3;
static final int FILT_SZIP         = 4;
static final int FILT_NBIT         = 5;
static final int FILT_SCALEOFFSET  = 6;

static final String[] filtNames = {"UNKNOWN", "deflate", "shuffle",
  "fletcher32", "szip", "nbit", "scaleoffset"};



final int filterVersion = 1;
int filterId;                   // one of FILT_*
int compressionLevel;               // 0 is fastest; 9 is smallest


MsgFilter(
  int filterId,
  int compressionLevel,
  HdfGroup hdfGroup,              // the owning group
  HdfFileWriter hdfFile)
throws HdfException
{
  super( TP_FILTER, hdfGroup, hdfFile);
  this.filterId = filterId;
  this.compressionLevel = compressionLevel;
  if (filterId != FILT_DEFLATE) throwerr("unsupported filterId");
  if (compressionLevel < 0 || compressionLevel > 9)
    throwerr("invalid compressionLevel");
}




public String toString() {
  String res = super.toString();
  res += "  filterId: " + filterId;
  res += "  compressionLevel: " + compressionLevel;
  return res;
}







// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  // Filter message
  fmtBuf.putBufByte("MsgFilter: filterVersion", filterVersion);
  fmtBuf.putBufByte("MsgFilter: numFilter", 1);
  fmtBuf.putBufShort("MsgFilter: reserved", 0);
  fmtBuf.putBufInt("MsgFilter: reserved", 0);

  // Single filter description
  String name = filtNames[ filterId];
  byte[] bytes = HdfUtil.encodeString( name, true, hdfGroup);
  int encLen = bytes.length;        // includes null termination

  fmtBuf.putBufShort("MsgFilter: filterId", filterId);
  fmtBuf.putBufShort("MsgFilter: name len", encLen);

  // Flags: if bit 0 is set, filter is optional.
  fmtBuf.putBufShort("MsgFilter: flags", 0);

  fmtBuf.putBufShort("MsgFilter: num client vals", 1);
  fmtBuf.putBufBytes("MsgFilter: name", bytes);

  // Client values for deflate
  fmtBuf.putBufInt("MsgFilter: compressionLevel", compressionLevel);
  fmtBuf.putBufInt("MsgFilter: reserved", 0);
}





} // end class
