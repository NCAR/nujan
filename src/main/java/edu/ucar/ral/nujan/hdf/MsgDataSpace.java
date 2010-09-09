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


import java.util.Arrays;


/**
 * HDF5 message type 1: MsgDataSpace: contains dimension info
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 * <p>
 * Note that for empty attributes, only the fileVersion 2 file format
 * has the null dataspace message.
 * The fileVersion 1 file format does not have a null dataspace.
 */

class MsgDataSpace extends MsgBase {




final int msgVersion = 1;

// Bits for spaceFlag:
//   0  maxSizes are present (decimal 1)
//   1  permutations are present (decimal 2)
int spaceFlag = 0;           // no maxSizes, no permutations


int[] varDims;               // size of each dimension
                             // If null, use stype = H5S_NULL: no data.
                             //   This is a special case used
                             //   for empty attributes.
                             // if varDims.length==0, it's a scalar.

int rank;                    // dimensionality == num dimensions
long totNumEle;              // total num elements, calculated from varDims

// Unlimited (-1) is not implemented here but would
// be easy: just pass in dimMaxSizes instead of copying varDims.
int[] dimMaxSizes;           // -1 means unlimited
long[] dimPermuations;



/**
 * @param varDims The length of each dimension.
 * @param hdfGroup The owning HdfGroup.
 * @param hdfFile The global owning HdfFileWriter.
 */

MsgDataSpace(
  int[] varDims,
  HdfGroup hdfGroup,                    // the owning group
  HdfFileWriter hdfFile)
{
  super( TP_DATASPACE, hdfGroup, hdfFile);
  if (varDims == null) {
    this.varDims = null;
    rank = 0;
    totNumEle = 0;
  }
  else {
    this.varDims = Arrays.copyOf( varDims, varDims.length);
    rank = varDims.length;
    if (varDims.length == 0) {
      totNumEle = 0;
    }
    else {
      totNumEle = 1;
      for (int ii : varDims) {
        totNumEle *= ii;
      }
    }
  }
  this.dimMaxSizes = this.varDims;
  if (hdfFile.bugs >= 5) prtf("MsgDataSpace: " + this);
}


public String toString() {
  String res = "rank: " + rank;
  res += "  totNumEle: " + totNumEle;
  res += "  dims:";
  if (varDims == null) res += " null";
  else {
    res += " (";
    for (long ilen : varDims) {
      res += " " + ilen;
    }
    res += ")";
  }
  if (hdfFile.bugs >= 10) {
    res += "  dimMaxSizes:";
    if (dimMaxSizes == null) res += " null";
    else {
      res += " (";
      for (long ilen : dimMaxSizes) {
        res += " " + ilen;
      }
      res += ")";
    }
    res += "  " + super.toString();
  }
  return res;
}






/**
 * Extends abstract MsgBase:
 * formats everything after the message header into fmtBuf.
 * Called by MsgBase.formatFullMsg and MsgBase.formatNakedMsg.
 */

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
    if (varDims == null) stype = 2;            // H5S_NULL: null dataspace
    else if (varDims.length == 0) stype = 0;   // scalar
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
