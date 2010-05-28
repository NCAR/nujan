
package hdfnet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;


// Msg 18: object modification time

class MsgModTime extends MsgBase {




final int version = 1;

// Yuch: HDF5 has a year 2038 date problem.
int utcModTimeSec;




MsgModTime(
  long utcModTimeMilliSec,
  HdfGroup hdfGroup,              // the owning group
  HdfFile hdfFile)
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
