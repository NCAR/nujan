
package hdfnet;


// Msg 11: filter pipeline

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
  HdfFile hdfFile)
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
  byte[] bytes = Util.encodeString( name, true, hdfGroup);
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
