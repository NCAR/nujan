
package hdfnet;


import java.util.Arrays;


// Msg 01: dataSpace

class MsgDataSpace extends MsgBase {




final int msgVersion = 1;
int rank;                    // dimensionality == num dimensions

// Bits for spaceFlag:
//   0  maxSizes are present
//   1  permutations are present
int spaceFlag = 1;           // maxSizes are present

int[] varDims;               // size of each dimension

// Unlimited (-1) is not implemented here but would
// be easy: just pass in dimMaxSizes instead of copying varDims.
int[] dimMaxSizes;           // -1 means unlimited
long[] dimPermuations;



MsgDataSpace(
  int[] varDims,
  HdfGroup hdfGroup,                    // the owning group
  HdfFile hdfFile)
{
  super( TP_DATASPACE, hdfGroup, hdfFile);
  this.rank = varDims.length;
  this.varDims = Arrays.copyOf( varDims, varDims.length);
  this.dimMaxSizes = this.varDims;
}


public String toString() {
  String res = super.toString();
  res += "  rank: " + rank;
  res += "  varDims:";
  for (long ilen : varDims) {
    res += " " + ilen;
  }
  res += "  dimMaxSizes:";
  for (long ilen : dimMaxSizes) {
    res += " " + ilen;
  }
  return res;
}



// Format everything after the message header

void formatMsgCore( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  int msgVersion = hdfFile.fileVersion;      // 1 or 2
  fmtBuf.putBufByte("MsgDataSpace: msgVersion", msgVersion);
  fmtBuf.putBufByte("MsgDataSpace: rank", rank);
  fmtBuf.putBufByte("MsgDataSpace: spaceFlag", spaceFlag);
  if (hdfFile.fileVersion == 1) {
    fmtBuf.putBufByte("MsgDataSpace: reserved", 0);
    fmtBuf.putBufInt("MsgDataSpace: reserved", 0);
  }
  if (hdfFile.fileVersion == 2) {
    int stype;
    if (varDims.length == 0) stype = 0;        // scalar
    else stype = 1;                            // simple dataspace
    fmtBuf.putBufByte("MsgDataSpace: stype", stype);
  }

  for (int ii = 0; ii < rank; ii++) {
    fmtBuf.putBufLong("MsgDataSpace: varDims", varDims[ii]);
  }
  if ((spaceFlag & 1) != 0) {       // if maxSizes are present
    for (int ii = 0; ii < rank; ii++) {
      fmtBuf.putBufLong("MsgDataSpace: dimMaxSizes", dimMaxSizes[ii]);
    }
  }
  if ((spaceFlag & 2) != 0) {       // if permutations are present
    if (hdfFile.fileVersion != 1) throwerr("spaceFlag version mismatch");
    for (int ii = 0; ii < rank; ii++) {
      fmtBuf.putBufLong(
        "MsgDataSpace: dimPermuations", dimPermuations[ii]);
    }
  }
}




} // end class
