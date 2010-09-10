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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;


/**
 * HDF5 message type 18: MsgModTime:
 * object modification time.
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

class MsgModTime extends MsgBase {




final int version = 1;

// Caution: HDF5 has a possible year 2038 problem,
// using a 4 byte date, if handled as a signed int.
int utcModTimeSec;




MsgModTime(
  long utcModTimeMilliSec,
  HdfGroup hdfGroup,              // the owning group
  HdfFileWriter hdfFile)
{
  super( TP_MOD_TIME, hdfGroup, hdfFile);
  this.utcModTimeSec = (int) (utcModTimeMilliSec / 1000);
}




public String toString() {
  String res = super.toString();
  res += "  utcModTimeSec: " + utcModTimeSec;
  Date dt = new Date( utcModTimeSec * 1000);
  SimpleDateFormat utcFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  utcFmt.setTimeZone( new SimpleTimeZone( 0, "UTC"));
  res += "  " + utcFmt.format( dt);
  return res;
}





// Format everything after the message header
void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  fmtBuf.putBufByte("MsgModTime: version", version);
  fmtBuf.putBufByte("MsgModTime: reserved", 0);
  fmtBuf.putBufShort("MsgModTime: reserved", 0);
  fmtBuf.putBufInt("MsgModTime: utcModTimeSec", utcModTimeSec);
}




} // end class
