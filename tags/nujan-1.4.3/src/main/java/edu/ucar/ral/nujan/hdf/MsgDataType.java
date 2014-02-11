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
 * HDF5 message type 3: MsgDataType:
 * contains data type info (fixed/float/string/etc, elementLen, etc).
 * <p>
 * Extends abstract MsgBase, so we must implement formatMsgCore -
 * see the documentation for class {@link MsgBase}.
 */

class MsgDataType extends MsgBase {


int dtype;                 // one of HdfGroup.DTYPE*
int[] dsubTypes;           // subType for DTYPE_VLEN or DTYPE_COMPOUND
String[] subNames;         // member names for DTYPE_COMPOUND


// Len of versClass(1), bitFields(3), elementLen(4)
static final int DATATYPE_HDR_LEN = 8;

static final int TCLS_FIXED       =  0;
static final int TCLS_FLOAT       =  1;
static final int TCLS_TIME        =  2;
static final int TCLS_STRING      =  3;
static final int TCLS_BITFIELD    =  4;
static final int TCLS_OPAQUE      =  5;
static final int TCLS_COMPOUND    =  6;
static final int TCLS_REFERENCE   =  7;
static final int TCLS_ENUMERATED  =  8;
static final int TCLS_VLEN        =  9;
static final int TCLS_ARRAY       = 10;

static final String[] typeClassNames = {
  "FIXED",
  "FLOAT",
  "TIME",
  "STRING",
  "BITFIELD",
  "OPAQUE",
  "COMPOUND",
  "REFERENCE",
  "ENUMERATED",
  "VLEN",
  "ARRAY"
};


// Little-endian tests:
// short:    vers: 1  cls: 0  flag: 0x8  size: 2
// int:      vers: 1  cls: 0  flag: 0x8  size: 4
// float:
//           vers: 1  cls: 1  flag: 0x1f20  size: 4
//           bitOffset: 0  precision: 32
//           exponPos: 23  exponLen: 8  exponBias: 127
//           mantissaPos: 0  mantissaLen: 23
// double:
//           vers: 1  cls: 1  flag: 0x3f20  size: 8
//           bitOffset: 0  precision: 64
//           exponPos: 52  exponLen: 11  exponBias: 1023
//           mantissaPos: 0  mantissaLen: 52
//
// typeVersion:
//   0  never used
//   1  compound datatypes with explicit array fields
//   2  array datatype
//   3  VAX
int typeVersion;

// typeClass:
//   0  Fixed-Point
//   1  Floating-Point
//   2  Time
//   3  String
//   4  Bitfield
//   5  Opaque
//   6  Compound
//   7  Reference
//   8  Enumerated
//   9  Variable-Length
//   10  Array
int typeClass;


int typeFlag;                      // combo of 3 bytes: bits 0-7, 18-5, 16.23
//
// Flags for fixed point:
//   0  if 0, little endian; if 1, big endian
//   1  value for low padding
//   2  value for high padding
//   3  if 0 unsigned.  If 1, number is signed 2's complement form.
//
// Flags for floating point:
//   bit0==0 && bit6==0   little endian
//   bit0==1 && bit6==0   big endian
//   bit0==0 && bit6==1   reserved
//   bit0==1 && bit6==1   VAX
//   1  value for low padding
//   2  value for high padding
//   3  value for internal padding
//   bits4,5 == 0   no mantissa normalization
//   bits4,5 == 1   mantissa most signif bit is always set (except for 0)
//   bits4,5 == 2   mantissa most signif is not stored, but implied to be 1
//   bits4,5 == 3   reserved
//   7  reserved
//   8-15  sign location: bit pos of the sign bit.
//   16-23  reserved
//
// Flags for string:
//   value of bits 0-3:
//     0  always null terminated
//     1  null added when converting short to long, but not vice versa
//     2  like 1, but use space pad.  Fortran convention.
//     3-15: reserved
//   value of bits 4-7:
//     0    ascii char set
//     1    utf8 char set
//     2-15: reserved
//
// Flags for reference:
//   value of bits 0-3:
//     0  object reference
//     1  dataset region reference


/**
 * Element length in bytes.
 */
int elementLen;

// Class 00: fixed point
int fixptBitOffset;                  // num low order pad bits
int fixptPrecision;                  // num bits of data

// Class 01: floating point
int floatBitOffset;                  // num low order pad bits
int floatPrecision;                  // num bits of data
int floatExponPos;                   // bit pos of exponent
int floatExponLen;                   // bit len of exponent
int floatMantissaPos;                // bit pos of mantissa
int floatMantissaLen;                // bit len of mantissa
int floatExponBias;                  // exponent bias


// Class 06: compound
// Class 09: variable length

MsgDataType[] subMsgs;    // sub types for DTYPE_VLEN, DTYPE_COMPOUND
int stgFieldLen;          // stg len for DTYPE_STRING_FIX, without null term





/**
 * @param dtype The element type - one of HdfGroup.DTYPE*.
 * @param dsubTypes Array of subtypes for for DTYPE_VLEN or DTYPE_COMPOUND.
 *        Each element is one of HdfGroup.DTYPE*.
 * @param subNames  Member names for DTYPE_COMPOUND.
 * @param stgFieldLen String length for a DTYPE_STRING_FIX variable,
 *        without null termination.
 *        Should be 0 for all other types, including DTYPE_STRING_VAR.
 * @param hdfGroup The owning HdfGroup.
 * @param hdfFile The global owning HdfFileWriter.
 */

MsgDataType(
  int dtype,                 // one of HdfGroup.DTYPE*
  int[] dsubTypes,           // subType for DTYPE_VLEN or DTYPE_COMPOUND
  String[] subNames,         // member names for DTYPE_COMPOUND
  int stgFieldLen,           // stg len for DTYPE_STRING_FIX, without null term
  HdfGroup hdfGroup,         // the owning group
  HdfFileWriter hdfFile)
throws HdfException
{
  super( TP_DATATYPE, hdfGroup, hdfFile);
  this.dtype = dtype;
  this.dsubTypes = dsubTypes;
  this.subNames = subNames;
  typeVersion = 1;
  if (hdfFile.bugs >= 5) prtf("MsgDataType: " + this);

  if (dtype == HdfGroup.DTYPE_VLEN) {
    if (dsubTypes == null || dsubTypes.length == 0)
      throwerr("DTYPE_VLEN: missing dsubTypes");
    if (subNames != null)
      throwerr("DTYPE_VLEN: subNames not null");
  }
  else if (dtype == HdfGroup.DTYPE_COMPOUND) {
    if (dsubTypes == null || dsubTypes.length == 0)
      throwerr("DTYPE_COMPOUND: missing dsubTypes");
    if (subNames == null || subNames.length == 0)
      throwerr("DTYPE_COMPOUND: missing subNames");
    if (dsubTypes.length != subNames.length)
      throwerr("DTYPE_COMPOUND: subTypes len != subNames len");
  }
  else {
    if (dsubTypes != null) throwerr("dsubTypes not null");
  }

  if (dtype == HdfGroup.DTYPE_STRING_FIX
    || dtype == HdfGroup.DTYPE_VLEN
       && dsubTypes[0] == HdfGroup.DTYPE_STRING_FIX)
  {
    if (stgFieldLen <= 0)
      throwerr("Invalid stgFieldLen for DTYPE_STRING_FIX: must be > 0");
  }
  else {
    if (stgFieldLen != 0) throwerr("Invalid stgFieldLen: must be 0");
  }

  if (dtype == HdfGroup.DTYPE_SFIXED08) elementLen = 1;
  else if (dtype == HdfGroup.DTYPE_UFIXED08) elementLen = 1;
  else if (dtype == HdfGroup.DTYPE_FIXED16) elementLen = 2;
  else if (dtype == HdfGroup.DTYPE_FIXED32) elementLen = 4;
  else if (dtype == HdfGroup.DTYPE_FIXED64) elementLen = 8;
  else if (dtype == HdfGroup.DTYPE_FLOAT32) elementLen = 4;
  else if (dtype == HdfGroup.DTYPE_FLOAT64) elementLen = 8;
  else if (dtype == HdfGroup.DTYPE_STRING_FIX) elementLen = stgFieldLen;
  else if (dtype == HdfGroup.DTYPE_REFERENCE) {
    // If object ref, len is 8.  If region ref, len is 12.
    elementLen = HdfFileWriter.OFFSET_SIZE;
  }
  else if (dtype == HdfGroup.DTYPE_VLEN)
    elementLen = 2 * HdfFileWriter.OFFSET_SIZE;
  else if (dtype == HdfGroup.DTYPE_STRING_VAR)
    elementLen = 2 * HdfFileWriter.OFFSET_SIZE;
  else if (dtype == HdfGroup.DTYPE_COMPOUND) {
    // Here elementLen is the total len of the compound structure:
    //   Reference is long, 8 bytes
    //   Id is fixed32, 4 bytes
    elementLen = HdfFileWriter.OFFSET_SIZE + 4;
  }
  else throwerr("unknown dtype: " + dtype);

  // Set up the message values
  if (dtype == HdfGroup.DTYPE_SFIXED08
    || dtype == HdfGroup.DTYPE_UFIXED08
    || dtype == HdfGroup.DTYPE_FIXED16
    || dtype == HdfGroup.DTYPE_FIXED32
    || dtype == HdfGroup.DTYPE_FIXED64)
  {
    typeClass = TCLS_FIXED;
    typeFlag = 0;                      // bit0==0: little-endian
    if (dtype != HdfGroup.DTYPE_UFIXED08)
      typeFlag |= 8;                   // bit3==1: two's complement

    fixptBitOffset = 0;
    if (dtype == HdfGroup.DTYPE_SFIXED08) fixptPrecision = 8;
    else if (dtype == HdfGroup.DTYPE_UFIXED08) fixptPrecision = 8;
    else if (dtype == HdfGroup.DTYPE_FIXED16) fixptPrecision = 16;
    else if (dtype == HdfGroup.DTYPE_FIXED32) fixptPrecision = 32;
    else if (dtype == HdfGroup.DTYPE_FIXED64) fixptPrecision = 64;
    else throwerr("unknown type");
  }

  // Float:
  // bit0==0 && bit6==0: little-endian
  // bits 1,2,3 == 000: low,hi,middle pad value
  // bits 4,5 == 10: most significant bit of mantissa is not stored
  //                 but is implied to be 1
  // float:  bits 8-15 = 00011111 = dec 31 = bit position of sign bit
  // double: bits 8-15 = 00111111 = dec 64 = bit position of sign bit

  else if (dtype == HdfGroup.DTYPE_FLOAT32) {
    typeClass = TCLS_FLOAT;
    typeFlag = 0x1f20;
    floatBitOffset = 0;
    floatPrecision = 32;
    floatExponPos = 23;
    floatExponLen = 8;
    floatExponBias = 127;
    floatMantissaPos = 0;
    floatMantissaLen = 23;
  }
  else if (dtype == HdfGroup.DTYPE_FLOAT64) {
    typeClass = TCLS_FLOAT;
    typeFlag = 0x3f20;
    floatBitOffset = 0;
    floatPrecision = 64;
    floatExponPos = 52;
    floatExponLen = 11;
    floatExponBias = 1023;
    floatMantissaPos = 0;
    floatMantissaLen = 52;
  }
  else if (dtype == HdfGroup.DTYPE_STRING_FIX) {
    typeClass = TCLS_STRING;
    typeFlag = 0x0;         // null termination guaranteed, US-ASCII char set
  }
  else if (dtype == HdfGroup.DTYPE_REFERENCE) {
    typeClass = TCLS_REFERENCE;
    typeFlag = 0x0;         // Object ref is 0; region ref is 1.
  }

  else if (dtype == HdfGroup.DTYPE_STRING_VAR) {
    typeClass = TCLS_VLEN;

    // Flag bits:
    // bits 0-3: data type
    //   0: sequence of some datatype.
    //   1: variable len string
    // bits 4-7: pad type (for var len stg only (tp 1))
    //   0: always null term
    //   1: null pad if not full length
    //   2: blank pad if not full length
    // bits 8-11: char set (for var len stg only (tp 1))
    //   0: ascii
    //   1: utf-8

    typeFlag = 0x1;               // variable length string

    subMsgs = new MsgDataType[] {
      new MsgDataType(
        HdfGroup.DTYPE_UFIXED08,     // dtype: one of HdfGroup.DTYPE*
        null,               // subSubType
        null,               // member names for DTYPE_COMPOUND
        0,                  // stgFieldLen
        hdfGroup,           // the owning group
        hdfFile)
    };
  } // if DTYPE_STRING_VAR

  // General VLEN, not variable length strings
  else if (dtype == HdfGroup.DTYPE_VLEN) {
    if (dsubTypes == null) throwerr("dsubTypes == null");
    if (dsubTypes.length != 1)
      throwerr("invalid len for dsubTypes: " + dsubTypes.length);
    typeClass = TCLS_VLEN;

    // Flag bits: see doc above for DTYPE_STRING_VAR
    typeFlag = 0x0;               // variable length sequence

    int dsubType = dsubTypes[0];
    checkDsubType( dsubType);
    subMsgs = new MsgDataType[1];
    subMsgs[0] = new MsgDataType(
      dsubType,           // dtype: one of HdfGroup.DTYPE*
      null,               // subSubType
      null,               // member names for DTYPE_COMPOUND
      stgFieldLen,
      hdfGroup,           // the owning group
      hdfFile);
  } // if DTYPE_VLEN

  else if (dtype == HdfGroup.DTYPE_COMPOUND) {
    if (dsubTypes == null) throwerr("dsubTypes == null");
    if (dsubTypes.length < 1)
      throwerr("invalid len for dsubTypes: " + dsubTypes.length);
    typeClass = TCLS_COMPOUND;
    typeFlag = dsubTypes.length;
    subMsgs = new MsgDataType[ dsubTypes.length];
    for (int ii = 0; ii < dsubTypes.length; ii++) {
      int dsubType = dsubTypes[ii];
      checkDsubType( dsubType);
      subMsgs[ii] = new MsgDataType(
        dsubType,           // dtype: one of HdfGroup.DTYPE*
        null,               // subSubType
        null,               // names for DTYPE_COMPOUND
        stgFieldLen,
        hdfGroup,           // the owning group
        hdfFile);
    }
  } // if DTYPE_COMPOUND
  else throwerr("unknown dtype: " + dtype);
} // end constructor





/**
 * Throws HdfException if the dsubType is not allowed
 * for a member of DTYPE_VLEN or DTYPE_COMPOUND.
 */

void checkDsubType( int dsubType)
throws HdfException
{
  if (dsubType != HdfGroup.DTYPE_SFIXED08
    && dsubType != HdfGroup.DTYPE_UFIXED08
    && dsubType != HdfGroup.DTYPE_FIXED16
    && dsubType != HdfGroup.DTYPE_FIXED32
    && dsubType != HdfGroup.DTYPE_FIXED64
    && dsubType != HdfGroup.DTYPE_FLOAT32
    && dsubType != HdfGroup.DTYPE_FLOAT64
    && dsubType != HdfGroup.DTYPE_STRING_FIX
    && dsubType != HdfGroup.DTYPE_REFERENCE)
    throwerr("invalid dsubType: " + HdfGroup.dtypeNames[dsubType]);
}



public String toString() {
  String res = "dtype: " + HdfGroup.dtypeNames[dtype];
  if (dsubTypes != null) {
    res += "  dsubTypes: (";
    for (int isub : dsubTypes) {
      res += " " + HdfGroup.dtypeNames[isub];
    }
    res += ")";
  }
  if (subNames != null) {
    res += "  subNames: (";
    for (String nm : subNames) {
      res += " \"" + nm + "\"";
    }
    res += ")";
  }
  if (hdfFile.bugs >= 10) {
    res += "  " + super.toString();
    res += "  typeVersion: " + typeVersion;
    res += "  typeClass: " + typeClassNames[typeClass];
    res += "  elementLen: " + elementLen;
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
  if (typeVersion != 1)
    throwerr("msgType 3 wrong version: %d", typeVersion);

  // Build the message
  int versionClass = (typeVersion << 4) | (typeClass);
  fmtBuf.putBufByte( "MsgDataType: typeVersionClass", versionClass);
  int flaga = 0xff & typeFlag;
  int flagb = 0xff & (typeFlag >>> 8);
  int flagc = 0xff & (typeFlag >>> 16);
  fmtBuf.putBufByte("MsgDataType: typeFlaga", flaga);
  fmtBuf.putBufByte("MsgDataType: typeFlagb", flagb);
  fmtBuf.putBufByte("MsgDataType: typeFlagc", flagc);

  fmtBuf.putBufInt("MsgDataType: elementLen", elementLen);

  if (typeClass == TCLS_FIXED) {              // if fixed point
    fmtBuf.putBufShort("MsgDataType: fixptBitOffset", fixptBitOffset);
    fmtBuf.putBufShort("MsgDataType: fixptPrecision", fixptPrecision);
  }
  else if (typeClass == TCLS_FLOAT) {         // if floating point
    fmtBuf.putBufShort("MsgDataType: floatBitOffset", floatBitOffset);
    fmtBuf.putBufShort("MsgDataType: floatPrecision", floatPrecision);
    fmtBuf.putBufByte("MsgDataType: floatExponPos", floatExponPos);
    fmtBuf.putBufByte("MsgDataType: floatExponLen", floatExponLen);
    fmtBuf.putBufByte("MsgDataType: floatMantissaPos", floatMantissaPos);
    fmtBuf.putBufByte("MsgDataType: floatMantissaLen", floatMantissaLen);
    fmtBuf.putBufInt("MsgDataType: floatExponBias", floatExponBias);
  }
  else if (typeClass == TCLS_STRING) {
  }
  else if (typeClass == TCLS_REFERENCE) {
  }
  else if (typeClass == TCLS_VLEN) {          // if variable length
    if (subMsgs == null || subMsgs.length != 1)
      throwerr("invalid subType for DTYPE_VLEN or DTYPE_STRING_VAR");
    MsgDataType subtp = subMsgs[0];
    if (hdfFile.bugs >= 5) {
      prtIndent("format vlen subtp: msgType: %d  %s  size: %d  flag: 0x%x",
        subtp.hdrMsgType,
        hdrMsgTypeNames[subtp.hdrMsgType],
        subtp.hdrMsgSize,
        subtp.hdrMsgFlag);
    }
    subtp.formatNakedMsg( formatPass, fmtBuf);
  }
  else if (typeClass == TCLS_COMPOUND) {          // if compound
    if (subMsgs == null || subMsgs.length < 1)
      throwerr("invalid subType for DTYPE_COMPOUND");
    int memberOffset = 0;
    for (int isub = 0; isub < subMsgs.length; isub++) {
      MsgDataType subtp = subMsgs[isub];
      String subName = subNames[isub];
      if (hdfFile.bugs >= 5) {
        prtIndent(
          "format compound subtp: msgType: %d  %s  size: %d  flag: 0x%x" 
            + "  subName: \"%s\"",
          subtp.hdrMsgType,
          hdrMsgTypeNames[subtp.hdrMsgType],
          subtp.hdrMsgSize,
          subtp.hdrMsgFlag,
          subName);
      }

      byte[] bytes = HdfUtil.encodeString( subName, true, hdfGroup);
      int subFieldLen = bytes.length;       // includes null term
      fmtBuf.putBufBytes( "cmpnd member name", bytes);

      // Pad name field out to a multiple of 8.
      // Caution: we are not necessarily aligning to an 8-boundary.
      if (subFieldLen % 8 != 0) {
        int padLen = 8 - subFieldLen % 8;
        for (int ii = 0; ii < padLen; ii++) {
          fmtBuf.putBufByte("cmpnd name pad", 0);
        }
      }

      fmtBuf.putBufInt("cmpnd memberOffset", memberOffset);
      fmtBuf.putBufByte("cmpnd rank", 0);
      fmtBuf.putBufByte("reserved", 0);
      fmtBuf.putBufShort("reserved", 0);
      fmtBuf.putBufInt("dim perm", 0);
      fmtBuf.putBufInt("reserved", 0);
      fmtBuf.putBufInt("dim 1", 0);
      fmtBuf.putBufInt("dim 2", 0);
      fmtBuf.putBufInt("dim 3", 0);
      fmtBuf.putBufInt("dim 4", 0);
      subtp.formatNakedMsg( formatPass, fmtBuf);
      
      if (subtp.dtype == HdfGroup.DTYPE_FIXED32)
        memberOffset += 4;
      else if (subtp.dtype == HdfGroup.DTYPE_REFERENCE)
        memberOffset += HdfFileWriter.OFFSET_SIZE;
      else throwerr("unknown compound subtype: " + subtp.dtype);
    } // for isub
  } // if TCLS_COMPOUND
  else throwerr("unknown typeClass: %d", typeClass);
}




} // end class
