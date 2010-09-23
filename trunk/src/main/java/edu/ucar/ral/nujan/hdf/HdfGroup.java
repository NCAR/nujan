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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.ArrayList;


/**
 * Represents a Group or Dataset in HDF5 (both have the same
 * representation).
 */

public class HdfGroup extends BaseBlk {



// Overall type summary
/** dtype value: unknown (should never happen) */
public static final int DTYPE_UNKNOWN       =  0;

/** dtype value: 8 bit signed integer */
public static final int DTYPE_SFIXED08      =  1;
/** dtype value: 8 bit unsigned integer */
public static final int DTYPE_UFIXED08      =  2;
/** dtype value: 16 bit signed integer */
public static final int DTYPE_FIXED16       =  3;
/** dtype value: 32 bit signed integer */
public static final int DTYPE_FIXED32       =  4;
/** dtype value: 64 bit signed integer */
public static final int DTYPE_FIXED64       =  5;
/** dtype value: 32 bit signed float */
public static final int DTYPE_FLOAT32       =  6;
/** dtype value: 64 bit signed float */
public static final int DTYPE_FLOAT64       =  7;

/**
 * Do not use: for internal testing only by
 * Thdfa, GenData, and Tnetcdfa to force character generation.
 */
public static final int DTYPE_TEST_CHAR     =  8;  // for internal test only

/** dtype value: fixed length string */
public static final int DTYPE_STRING_FIX    =  9;
/** dtype value: variable length string */
public static final int DTYPE_STRING_VAR    = 10;
/** dtype value: reference to a group */
public static final int DTYPE_REFERENCE     = 11;
/** dtype value: 1-dimensional variable length array of some other type,
  specified by dsubTypes.  */
public static final int DTYPE_VLEN          = 12;
/** dtype value: compound type composed of other types,
  specified by dsubTypes.  */
public static final int DTYPE_COMPOUND      = 13;

/** Names of the DTYPE_* constants */
public static final String[] dtypeNames = {
  "UNKNOWN",
  "SFIXED08", "UFIXED08", "FIXED16", "FIXED32", "FIXED64",
  "FLOAT32", "FLOAT64", "TEST_CHAR",
  "STRING_FIX", "STRING_VAR", "REFERENCE",
  "VLEN", "COMPOUND"};

/** HdfGroup signature byte 0 */
final int signa = 'O';
/** HdfGroup signature byte 1 */
final int signb = 'H';
/** HdfGroup signature byte 2 */
final int signc = 'D';
/** HdfGroup signature byte 3 */
final int signd = 'R';

// Referenced blocks
/**
 * List of sub-groups (HdfGroups having isVariable==false)
 * contained in this group.
 */
ArrayList<HdfGroup> subGroupList = null;

/**
 * List of sub-variables (HdfGroups having isVariable==true)
 * contained in this group.
 */
ArrayList<HdfGroup> subVariableList = null;



// Blocks contained in hdrMsgList
/** An HDF5 dataType message, contained in our hdrMsgList. */
MsgDataType msgDataType;

/** An HDF5 dataSpace message, contained in our hdrMsgList. */
MsgDataSpace msgDataSpace;

/** An HDF5 layout message, contained in our hdrMsgList. */
MsgLayout msgLayout;

/** An HDF5 fillValue message, contained in our hdrMsgList. */
MsgFillValue msgFillValue;

/** An HDF5 modification time message, contained in our hdrMsgList. */
MsgModTime msgModTime;

/** An HDF5 attribute message, contained in our hdrMsgList. */
MsgAttrInfo msgAttrInfo;

/** An HDF5 filter message, contained in our hdrMsgList. */
MsgFilter msgFilter;


/**
 * If false, this HdfGroup represents an HDF5 group; if true,
 * this HdfGroup represents a data variable.
 */
boolean isVariable;

/**
 * The local name of this group or variable -
 * does not include the complete path (see {@link #getPath} for that).
 */
String groupName;

/**
 * The parent group (null for the rootGroup).
 */
HdfGroup parentGroup;

/**
 * String length for a DTYPE_STRING_FIX variable, without null termination.
 * Should be 0 for all other types, including DTYPE_STRING_VAR.
 */
int stgFieldLen;

/**
 * Dimensions for this variable.  varDims may be null
 *        or may have length 0 in odd cases where a variable has
 *        no data.  For example a variable may have attributes
 *        without data.  If varDims is null or length 0, must have
 *        chunkDims==null and compressionLevel==0.
 */
int[] varDims;

/**
 * Len of each side of a chunk hyperslab.
 *        Must have chunkDims.length == varDims.length,
 *        or null if using contiguous storage.
 */
int[] chunkDims;

/**
 * rank == dimensionality == varDims.length == chunkDims.length
 */
int rank;

/**
 * total num elements, calculated from varDims
 */
long totNumEle;

/**
 * Element length in bytes.
 */
int elementLen;


/**
 * Zip compression level: 0==Uncompressed; 1 - 9 are increasing compression.
 */
int compressionLevel;

/**
 * List of header messages, like MsgDataType, MsgDataSpace,
 * MsgAttribute, etc., to be formatted for this group.
 */
ArrayList<MsgBase> hdrMsgList;

/**
 * When isVariable==true,
 * the data type of this variable.
 */
int dtype;                       // one of DTYPE*


/**
 * totChunkNums = number of chunks represented by any level
 * of the startIxs indices, so in calcChunkIxs we can do ...
 *   ichunk = sum( startIxs[ii] * totChunkNums[ii]);
 */
int[] totChunkNums;


/**
 * Description of each chunk, or if contiguous, the contiguous area.
 * For a multidimensional array the chunks are put in a linear
 * array, with the last dimension varying the fastest.
 */
HdfChunk[] hdfChunks;

int linkCreationOrder = 0;






/**
 * Creates an HDF5 group that is not a variable (isVariable == false).
 * @param groupName The local name of the new group -
 *   does not include the complete path (see {@link #getPath} for that).
 * @param parentGroup The parent group (null for the rootGroup).
 * @param hdfFile The global owning HdfFileWriter.
 */

HdfGroup(
  String groupName,
  HdfGroup parentGroup,        // is null when creating the rootGroup
  HdfFileWriter hdfFile)
throws HdfException
{
  super("HdfGroup: " + groupName, hdfFile);
  this.isVariable = false;
  this.groupName = groupName;
  this.parentGroup = parentGroup;

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup: new group at path: \"" + getPath() + "\"");
  }
  if (parentGroup != null) {     // if not creating root group
    HdfUtil.checkName( groupName,
      "subGroup in group \"" + parentGroup.groupName + "\"");
  }

  subGroupList = new ArrayList<HdfGroup>();
  subVariableList = new ArrayList<HdfGroup>();
  msgModTime = new MsgModTime( hdfFile.utcModTimeMilliSec, this, hdfFile);

  // Build message list
  hdrMsgList = new ArrayList<MsgBase>();
  hdrMsgList.add( msgModTime);

  msgAttrInfo = new MsgAttrInfo( this, hdfFile);
  hdrMsgList.add( msgAttrInfo);
}




/**
 * Creates an HDF5 variable (isVariable == true).
 * @param groupName The local name of the new variable.
 * @param parentGroup The parent group.
 * @param dtype The data type - one of DTYPE_*.
 * @param dsubTypes  The types of the members when dtype=DTYPE_COMPOUND.
 * @param subNames  The names of the members when dtype=DTYPE_COMPOUND.
 * @param stgFieldLen String length for a DTYPE_STRING_FIX variable,
 *        without null termination.
 *        Should be 0 for all other types, including DTYPE_STRING_VAR.
 * @param varDims Dimensions for this variable.  varDims may be null
 *        or may have length 0 in odd cases where a variable has
 *        no data.  For example a variable may have attributes
 *        without data.  If varDims is null or length 0, must have
 *        chunkDims==null and compressionLevel==0.
 * @param chunkDims len of each side of a chunk hyperslab.
 *        Must have chunkDims.length == varDims.length.
 *        If chunkDims == null use contiguous storage.
 *        If chunkDims == varDims, use chunked storage with just one chunk.
 *        If varDims == null or varDims.length == 0, chunkDims must be null.
 * @param fillValue Fill value of appropriate type for this variable.
 *        May be null.
 *        <p>
 * <table border="1" cellpadding="2">
 * <tr><th> dtype value      </th><th> Meaning                 </th><th> Java class of fillValue  </th></tr>
 * <tr><td> DTYPE_SFIXED08   </td><td> Signed byte             </td><td> Byte                     </td></tr>
 * <tr><td> DTYPE_UFIXED08   </td><td> Unsigned byte           </td><td> Byte                     </td></tr>
 * <tr><td> DTYPE_FIXED16    </td><td> Signed 16 bit int       </td><td> Short                    </td></tr>
 * <tr><td> DTYPE_FIXED32    </td><td> Signed 32 bit int       </td><td> Integer                  </td></tr>
 * <tr><td> DTYPE_FIXED64    </td><td> Signed 64 bit int       </td><td> Long                     </td></tr>
 * <tr><td> DTYPE_FLOAT32    </td><td> Signed 32 bit float     </td><td> Float                    </td></tr>
 * <tr><td> DTYPE_FLOAT64    </td><td> Signed 64 bit float     </td><td> Double                   </td></tr>
 * <tr><td> DTYPE_STRING_FIX </td><td> Fixed length string     </td><td> String                   </td></tr>
 * <tr><td> DTYPE_STRING_VAR </td><td> Variable length string  </td><td> String                   </td></tr>
 * </table>
 *        <p>
 *
 * @param compressionLevel Zip compression level:
 *        0 is uncompressed; 1 - 9 are increasing compression.
 * @param hdfFile The global owning HdfFileWriter.
 */

HdfGroup(
  String groupName,
  HdfGroup parentGroup,
  int dtype,                 // one of DTYPE*
  int[] dsubTypes,           // subType for DTYPE_VLEN
                             // or DTYPE_COMPOUND
  String[] subNames,         // member names for DTYPE_COMPOUND

  int stgFieldLen,           // String length for a DTYPE_STRING_FIX
                             // variable, without null termination.
                             // Should be 0 for all other types,
                             // including DTYPE_STRING_VAR.

  int[] varDims,
  int[] chunkDims,
  Object fillValue,          // null, Byte, Short, Int, Long,
                             // Float, Double, String, etc.
  int compressionLevel,      // Zip compression level: 0==Uncompressed;
                             // 1 - 9 are increasing compression.
  HdfFileWriter hdfFile)
throws HdfException
{
  super("HdfGroup: (var) " + groupName, hdfFile);
  this.isVariable = true;
  this.groupName = groupName;
  this.parentGroup = parentGroup;
  this.dtype = dtype;
  this.stgFieldLen = stgFieldLen;
  this.compressionLevel = compressionLevel;

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup: new dataset at path: \"" + getPath() + "\""
      + "  type: " + HdfUtil.formatDtypeDim( dtype, varDims)
      + "  chunkDims: " + HdfUtil.formatInts( chunkDims));
  }
  HdfUtil.checkName( groupName,
    "dataset in group \"" + parentGroup.groupName + "\"");


  if (varDims == null) this.varDims = null;
  else this.varDims = Arrays.copyOf( varDims, varDims.length);

  // Set up chunkDims.

  int layoutClass;
  if (chunkDims == null) {
    layoutClass = MsgLayout.LY_CONTIGUOUS;
    this.chunkDims = null;
  }
  else {
    layoutClass = MsgLayout.LY_CHUNKED;
    this.chunkDims = Arrays.copyOf( chunkDims, chunkDims.length);
  }


  if (varDims == null) {
    if (chunkDims != null)
      throwerr("varDims == null but chunkDims != null");
    if (compressionLevel > 0)
      throwerr("cannot use compression with null data");
  }
  else if (varDims.length == 0) {
    if (chunkDims != null)
      throwerr("varDims len == 0 but chunkDims != null");
    if (compressionLevel > 0)
      throwerr("cannot use compression with scalar data");
  }
  else {
    this.varDims = Arrays.copyOf( varDims, varDims.length);
  }

  // Set rank
  rank = 0;
  if (varDims != null) rank = varDims.length;

  // Set totNumEle
  if (varDims == null) {
    totNumEle = 0;
  }
  else {
    if (varDims.length == 0) totNumEle = 0;
    else {
      totNumEle = 1;
      for (int ii : varDims) {
        totNumEle *= ii;
      }
    }
  }
  if (hdfFile.bugs >= 1)
    prtf("HdfGroup: rank: %d  totNumEle: %d", rank, totNumEle);


  if (chunkDims != null) {
    if (chunkDims.length != varDims.length)
      throwerr("chunkDims len != varDims len");
    for (int ii = 0; ii < rank; ii++) {
      if (chunkDims[ii] <= 0) throwerr("invalid chunkDims");
      if (chunkDims[ii] > varDims[ii]) throwerr("chunkDims > varDims");
    }
  }

  if (compressionLevel > 0 && layoutClass != MsgLayout.LY_CHUNKED)
    throwerr("if compressed, must use chunked");



  // Calc numDimChunks = num chunks in each dimension,
  //   which is varDims[ii] / chunkDims[ii], round up.
  // Calc totNumChunks = total number of chunks = product of numDimChunks[*].
  // If contiguous, we have one chunk.

  int[] numDimChunks = new int[rank];
  int totNumChunks = 1;
  for (int ii = 0; ii < rank; ii++) {
    if (chunkDims == null) numDimChunks[ii] = 1;
    else {
      numDimChunks[ii] = varDims[ii] / chunkDims[ii];
      if (numDimChunks[ii] * chunkDims[ii] != varDims[ii])
        numDimChunks[ii]++;
    }
    totNumChunks *= numDimChunks[ii];
    if (hdfFile.bugs >= 1) prtf("HdfGroup: %s: numDimChunks[%d]: %d",
      getPath(), ii, numDimChunks[ii]);
  }
  if (hdfFile.bugs >= 1) prtf("HdfGroup: %s: totNumChunks: %d",
    getPath(), totNumChunks);

  // Calc totChunkNums = number of chunks represented by any level
  // of the startIxs indices, so in calcChunkIxs we can do ...
  //   ichunk = sum( (startIxs[ii]/chunkDims[ii]) * totChunkNums[ii]);

  totChunkNums = new int[rank];
  if (rank > 0) {
    totChunkNums[rank - 1] = 1;
    for (int ii = rank - 2; ii >= 0; ii--) {
      totChunkNums[ii] = numDimChunks[ii+1] * totChunkNums[ii+1];
      if (hdfFile.bugs >= 1) prtf("HdfGroup: %s: totChunkNums[%d]: %d",
        getPath(), ii, totChunkNums[ii]);
    }
  }

  // Initialize chunks.
  // We keep chunks in a LINEAR array for ease of use later.

  hdfChunks = new HdfChunk[ totNumChunks];
  int[] startIxs = new int[rank];
  if (chunkDims == null) {
    hdfChunks[0] = new HdfChunk( startIxs, varDims);
  }
  else {
    for (int ichunk = 0; ichunk < totNumChunks; ichunk++) {
      if (hdfFile.bugs >= 1)
        prtf("HdfGroup: %s: ichunk: %d  calcChunkIx: %d  startIxs: %s",
          getPath(), ichunk, calcChunkIx( startIxs),
          HdfUtil.formatInts( startIxs));
      if (calcChunkIx( startIxs) != ichunk) throwerr("calcChunkIx error");

      // Set userDims = chunkDims, but the edge chunks
      // in a dimension ii may be shorter if varDims[ii]
      // is not a multiple of chunkDims[ii].

      int[] userDims = new int[rank];
      for (int ii = 0; ii < rank; ii++) {
        if (chunkDims == null) userDims[ii] = varDims[ii];
        else {
          userDims[ii] = Math.min(
            chunkDims[ii],
            varDims[ii] - startIxs[ii]);
        }
      }
      if (hdfFile.bugs >= 1)
        prtf("HdfGroup: %s: ichunk: %d  userDims: %s",
          getPath(), ichunk, HdfUtil.formatInts( userDims));

      hdfChunks[ichunk] = new HdfChunk( startIxs, userDims);
      if (hdfFile.bugs >= 1) {
        prtf("HdfGroup: %s: hdfChunks[%d]: %s",
          getPath(), ichunk, hdfChunks[ichunk]);
      }

      // Increment startIxs
      for (int ii = rank - 1; ii >= 0; ii--) {
        startIxs[ii] += chunkDims[ii];
        if (startIxs[ii] < varDims[ii]) break;
        startIxs[ii] = 0;
      }
    }
  } // else chunkDims != null

  // Initialize various messages
  msgDataType = new MsgDataType(
    dtype, dsubTypes, subNames, stgFieldLen, this, hdfFile);
  elementLen = msgDataType.elementLen;

  msgDataSpace = new MsgDataSpace( rank, totNumEle, varDims, this, hdfFile);


  msgLayout = new MsgLayout( layoutClass, compressionLevel, this, hdfFile);

  boolean isFillExtant = false;
  if (fillValue != null) {
    isFillExtant = true;
  }
  msgFillValue = new MsgFillValue(
    dtype, isFillExtant, fillValue, this, hdfFile);
  msgModTime = new MsgModTime(
    hdfFile.utcModTimeMilliSec, this, hdfFile);

  // Build message list
  hdrMsgList = new ArrayList<MsgBase>();
  hdrMsgList.add( msgDataType);
  hdrMsgList.add( msgDataSpace);
  hdrMsgList.add( msgLayout);
  hdrMsgList.add( msgFillValue);
  hdrMsgList.add( msgModTime);
  if (compressionLevel > 0) {
    msgFilter = new MsgFilter(
      MsgFilter.FILT_DEFLATE,
      compressionLevel,
      this, hdfFile);
    hdrMsgList.add( msgFilter);
  }

  msgAttrInfo = new MsgAttrInfo( this, hdfFile);
  hdrMsgList.add( msgAttrInfo);
} // end constructor for variables








/**
 * Creates a sub-group of the current group.
 * @param subName The local name of the new subGroup.
 */

public HdfGroup addGroup(
  String subName)                   // name of new subGroup
throws HdfException
{
  if (hdfFile.fileStatus != HdfFileWriter.ST_DEFINING)
    throwerr("cannot define after calling endDefine");
  if (isVariable) throwerr("cannot add a group to a variable");
  if (findSubItem( subName) != null)
    throwerr("Duplicate subgroup.  The group \"%s\" already contains"
      + "  a subgroup or variable named \"%s\"",
      groupName, subName);

  HdfGroup subGroup = new HdfGroup( subName, this, hdfFile);

  addSubGroup( subGroup);
  return subGroup;
}




/**
 * Creates a variable in the current group.
 *
 * @param varName The local name of the new variable.
 * @param dtype The data type - one of DTYPE_*.
 * @param stgFieldLen String length for a DTYPE_STRING_FIX variable,
 *        without null termination.
 *        Should be 0 for all other types, including DTYPE_STRING_VAR.
 * @param varDims Dimensions for this variable.  varDims may be null
 *        or may have length 0 in odd cases where a variable has
 *        no data.  For example a variable may have attributes
 *        without data.  If varDims is null or length 0, must have
 *        chunkDims==null and compressionLevel==0.
 * @param chunkDims len of each side of a chunk hyperslab.
 *        Must have chunkDims.length == varDims.length.
 *        If chunkDims == null use contiguous storage.
 *        If chunkDims == varDims, use chunked storage with just one chunk.
 *        If varDims == null or varDims.length == 0, chunkDims must be null.
 * @param fillValue Fill value of appropriate type for this variable.
 *        May be null.
 *        <p>
 * <table border="1" cellpadding="2">
 * <tr><th> dtype value      </th><th> Meaning                 </th><th> Java class of fillValue  </th></tr>
 * <tr><td> DTYPE_SFIXED08   </td><td> Signed byte             </td><td> Byte                     </td></tr>
 * <tr><td> DTYPE_UFIXED08   </td><td> Unsigned byte           </td><td> Byte                     </td></tr>
 * <tr><td> DTYPE_FIXED16    </td><td> Signed 16 bit int       </td><td> Short                    </td></tr>
 * <tr><td> DTYPE_FIXED32    </td><td> Signed 32 bit int       </td><td> Integer                  </td></tr>
 * <tr><td> DTYPE_FIXED64    </td><td> Signed 64 bit int       </td><td> Long                     </td></tr>
 * <tr><td> DTYPE_FLOAT32    </td><td> Signed 32 bit float     </td><td> Float                    </td></tr>
 * <tr><td> DTYPE_FLOAT64    </td><td> Signed 64 bit float     </td><td> Double                   </td></tr>
 * <tr><td> DTYPE_STRING_FIX </td><td> Fixed length string     </td><td> String                   </td></tr>
 * <tr><td> DTYPE_STRING_VAR </td><td> Variable length string  </td><td> String                   </td></tr>
 * </table>
 *        <p>
 *
 * @param compressionLevel Zip compression level:
 *        0==Uncompressed; 1 - 9 are increasing compression.
 */

public HdfGroup addVariable(
  String varName,
  int dtype,                 // one of DTYPE*
  int stgFieldLen,           // string length for DTYPE_STRING_FIX.
                             // Without null termination.
                             // Should be 0 for all other types,
                             // including DTYPE_STRING_VAR.

  int[] varDims,             // dimension lengths
  int[] chunkDims,
  Object fillValue,          // fill value or null
  int compressionLevel)
throws HdfException
{
  if (hdfFile.fileStatus != HdfFileWriter.ST_DEFINING)
    throwerr("cannot define after calling endDefine");
  if (isVariable) throwerr("cannot add a variable to a variable");
  if (findSubItem( varName) != null)
    throwerr("Duplicate subgroup.  The group \"%s\" already contains"
      + "  a subgroup or variable named \"%s\"",
      groupName, varName);

  int[] dsubTypes = null;
  String[] subNames = null;
  if (dtype == HdfGroup.DTYPE_COMPOUND) {
    dsubTypes = new int[] { HdfGroup.DTYPE_REFERENCE, HdfGroup.DTYPE_FIXED32};
    subNames = new String[] {"dataset", "dimension"};
  }

  HdfGroup var = new HdfGroup(
    varName,
    this,
    dtype,
    dsubTypes,
    subNames,
    stgFieldLen,
    varDims,
    chunkDims,
    fillValue,
    compressionLevel,
    hdfFile);

  addSubGroup( var);
  return var;
} // end addVariable









/**
 * Creates an attribute in the current group or variable.
 *
 * @param attrName The local name of the new attribute.
 * @param attrType The data type - one of DTYPE_*.
 * @param stgFieldLen String length for a DTYPE_STRING_FIX variable,
 *        without null termination.
 *        Should be 0 for all other types, including DTYPE_STRING_VAR.
 * @param attrValue  The value of this attribute.
 *        <p>
 * <table border="1" cellpadding="2">
 * <tr><th> Java class of attrValue                       </th><th> dtype              </th></tr>
 * <tr><td> Byte (scalar) or byte[] or byte[][] or ...             </td><td> DTYPE_UFIXED08     </td></tr>
 * <tr><td> Short (scalar) or short[] or short[][] or ...          </td><td> DTYPE_FIXED16      </td></tr>
 * <tr><td> Integer (scalar) or int[] or int[][] or ...            </td><td> DTYPE_FIXED32      </td></tr>
 * <tr><td> Long (scalar) or long[] or long[][] or ...             </td><td> DTYPE_FIXED64      </td></tr>
 * <tr><td> Float (scalar) or float[] or float[][] or ...          </td><td> DTYPE_FLOAT32      </td></tr>
 * <tr><td> Double (scalar) or double[] or double[][] or ...       </td><td> DTYPE_FLOAT64      </td></tr>
 * <tr><td> Character (scalar) or char[] or char[][] or ...        </td><td> DTYPE_STRING_FIX   </td></tr>
 * <tr><td> String (scalar) or String[] or String[][] or ...       </td><td> DTYPE_STRING_VAR   </td></tr>
 * <tr><td> HdfGroup (scalar) or HdfGroup[] or HdfGroup[][] or ... </td><td> DTYPE_REFERENCE    </td></tr>
 * </table>
 *        <p>
 *
 * @param isVlen if true this array is a 2-dimensional array
 *        in which rows may have differing lengths (ragged right edge).
 */

public void addAttribute(
  String attrName,
  int attrType,              // one of DTYPE_*
  int stgFieldLen,
  Object attrValue,
  boolean isVlen)
throws HdfException
{
  if (hdfFile.bugs >= 1) {
    String tmsg = "HdfGroup.addAttribute: \"" + getPath()
      + "/" + attrName + "\"" + "  cls: ";
    if (attrValue == null) tmsg += "(null)";
    else tmsg += attrValue.getClass().getName();
    prtf( tmsg);
  }
  if (hdfFile.bugs >= 5) {
    prtf("  attr isVlen: " + isVlen);
    prtf("  attrValue: " + HdfUtil.formatObject( attrValue));
  }
  HdfUtil.checkName( attrName,
    "attribute in group \"" + groupName + "\"");

  if (findAttribute( attrName) != null)
    throwerr("Duplicate attribute.  The group \"%s\" already contains"
      + "  an attribute named \"%s\"",
      getPath(), attrName);

  // If attrType==DTYPE_STRING_FIX and stgFieldLen==0,
  // MsgAttribute will find the max stg len in attrValue.
  MsgAttribute msgAttr = new MsgAttribute(
    attrName,
    attrType,
    stgFieldLen,
    attrValue,
    isVlen,
    this,
    hdfFile);
  hdrMsgList.add( msgAttr);
  if (hdfFile.bugs >= 5) {
    prtf("HdfGroup.addAttribute: added name: \"" + attrName + "\"\n"
      + "  at path: \"" + getPath() + "\"\n"
      + "  dtype: " + dtypeNames[attrType] + "\n"
      + "  type: " + HdfUtil.formatDtypeDim(
        attrType, msgAttr.dataVarDims) + "\n"
      + "  isVlen: " + isVlen);
  }
} // end addAttribute








public String toString() {
  String res = "path: \"" + getPath() + "\""
    + "  isVariable: " + isVariable;
  if (isVariable) {
    res += "  dtype: " + dtypeNames[ dtype]
      + "  dataSpace: " + msgDataSpace;
  }
  else {
    res += "  subGroupList: (";
    for (HdfGroup grp : subGroupList) {
      res += " " + grp.groupName;
    }
    res += ")";
    res += "  subVariableList: (";
    for (HdfGroup grp : subVariableList) {
      res += " " + grp.groupName;
    }
    res += ")";
  }
  return res;
}



/**
 * Returns the full path name within the HDF5 file, starting at
 * the root group.  Example: "/forecast/temperature".
 */

String getPath()
{
  String res = "";
  HdfGroup grp = this;
  while (grp != null) {
    if (res.length() != 0) res = "/" + res;
    res = grp.groupName + res;
    grp = grp.parentGroup;
  }
  if (res.length() == 0) res = "/";
  return res;
}



/**
 * Adds a sub-HdfGroup, either for a true group or a variable,
 * to this group.
 * @param subGroup The group to be added.
 */

void addSubGroup(
  HdfGroup subGroup)
throws HdfException
{
  if (subGroup.isVariable) subVariableList.add( subGroup);
  else subGroupList.add( subGroup);
}









/**
 * Searches our subGroupList and subVariableList (not recursively)
 * for a matching local name; returns null if not found.
 * @param subName The name for which to search.
 */

HdfGroup findSubItem( String subName)
{
  HdfGroup resGroup = null;
  for (HdfGroup subGroup : subGroupList) {
    if (subGroup.groupName.equals( subName)) {
      resGroup = subGroup;
      break;
    }
  }
  if (resGroup == null) {
    for (HdfGroup subGroup : subVariableList) {
      if (subGroup.groupName.equals( subName)) {
        resGroup = subGroup;
        break;
      }
    }
  }
  return resGroup;
}





/**
 * Searches our hdrMsgList (not recursively)
 * for a matching attribute name; returns null if not found.
 * @param attrName The name for which to search.
 */

public MsgAttribute findAttribute( String attrName)
{
  MsgAttribute resMsg = null;
  for (MsgBase baseMsg : hdrMsgList) {
    if (baseMsg instanceof MsgAttribute) {
      MsgAttribute attrMsg = (MsgAttribute) baseMsg;
      if (attrMsg.attrName.equals( attrName)) {
        resMsg = attrMsg;
        break;
      }
    }
  }
  return resMsg;
}






/**
 * Returns the number of attributes.
 */

int getNumAttribute()
{
  int ires = 0;
  for (MsgBase baseMsg : hdrMsgList) {
    if (baseMsg instanceof MsgAttribute) ires++;
  }
  return ires;
}







/**
 * Writes all data for a single variable to disk.
 *
 *        <p>
 * <table border="1" cellpadding="2">
 * <tr><th> dtype              </th><th> Java class of vdata                           </th></tr>
 * <tr><td> DTYPE_UFIXED08     </td><td> Byte (scalar) or byte[] or byte[][] or ...             </td></tr>
 * <tr><td> DTYPE_FIXED16      </td><td> Short (scalar) or short[] or short[][] or ...          </td></tr>
 * <tr><td> DTYPE_FIXED32      </td><td> Integer (scalar) or int[] or int[][] or ...            </td></tr>
 * <tr><td> DTYPE_FIXED64      </td><td> Long (scalar) or long[] or long[][] or ...             </td></tr>
 * <tr><td> DTYPE_FLOAT32      </td><td> Float (scalar) or float[] or float[][] or ...          </td></tr>
 * <tr><td> DTYPE_FLOAT64      </td><td> Double (scalar) or double[] or double[][] or ...       </td></tr>
 * <tr><td> DTYPE_STRING_FIX   </td><td> Character (scalar) or char[] or char[][] or ...        </td></tr>
 * <tr><td> DTYPE_STRING_VAR   </td><td> String (scalar) or String[] or String[][] or ...       </td></tr>
 * <tr><td> DTYPE_REFERENCE    </td><td> HdfGroup (scalar) or HdfGroup[] or HdfGroup[][] or ... </td></tr>
 * </table>
 *        <p>
 *
 * @param startIxs  The indices of the starting point (lower left corner)
 *    of the hyperslab to be written.  For contiguous storage,
 *    startIxs should be all zeros.
 *    Must have startIxs.length == varDims.length.
 * @param vdata  The data to be written.
 */

public void writeData(
  int[] startIxs,
  Object vdata)
throws HdfException
{
  try { writeDataSub( startIxs, vdata); }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: %s", exc);
  }
}









/**
 * Implements writeData: for doc, see {@link #writeData}.
 */

void writeDataSub(
  int[] startIxs,
  Object vdata)
throws HdfException, IOException
{
  hdfFile.outChannel.position( HdfUtil.alignLong( 8, hdfFile.eofAddr));

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup.writeData entry: path: " + getPath() + "\n"
      + "  specified type: "
      + HdfUtil.formatDtypeDim( dtype, varDims) + "\n"
      + "  eofAddr: " + hdfFile.eofAddr
      + "  new pos: " + hdfFile.outChannel.position()
      + "  startIxs: " + HdfUtil.formatInts( startIxs));
  }

  if (hdfFile.fileStatus != HdfFileWriter.ST_WRITEDATA)
    throwerr("must call endDefine first");
  if (! isVariable) throwerr("cannot write data to a group");

  // Find dtype and varDims of vdata
  // Use isVlen==false: variable length data arrays are not supported,
  // although variable length attributes are.
  int[] dataInfo = HdfUtil.getDimLen( vdata, false);
  int dataDtype = dataInfo[0];
  int dataTotNumEle = dataInfo[1];
  int dataElementLen = dataInfo[2];
  int[] dataDims = Arrays.copyOfRange( dataInfo, 3, dataInfo.length);

  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup.writeData: actual data:" + "\n"
      + "  vdata object: " + vdata + "\n"
      + "  vdata class: " + vdata.getClass() + "\n"
      + "  dtype:  declared: " + dtypeNames[ dtype]
        + "  actual: " + dtypeNames[ dataDtype] + "\n"
      + "  totNumEle:  declared: " + totNumEle
        + "  actual: " + dataTotNumEle + "\n"
      + "  elementLen:  declared: " + elementLen
        + "  actual: " + dataElementLen + "\n"
      + "  rank:  declared: " + rank
        + "  actual: " + dataDims.length + "\n"
      + "  vdata type and dims: "
      + HdfUtil.formatDtypeDim( dataDtype, dataDims));
  }

  // Find the chunk
  int ichunk = 0;
  if (startIxs == null) {
    if (chunkDims != null) throwerr("startIxs == null but chunkDims != null");
    ichunk = 0;
  }
  else {
    if (chunkDims == null) throwerr("startIxs != null but chunkDims == null");
    ichunk = calcChunkIx( startIxs);
  }

  HdfChunk chunk = hdfChunks[ichunk];
  if (hdfFile.bugs >= 1) {
    prtf("HdfGroup.writeData: ichunk: %d  chunk: %s", ichunk, chunk);
  }

  // Check that dtype and dataDims match what the user
  // declared in the earlier addVariable call.

  HdfUtil.checkTypeMatch( getPath(), dtype, dataDtype,
    chunk.chunkUserDims, dataDims);

  if (chunk.chunkDataAddr != 0)
    throwerr("chunk has already been written.  path: %s  startIxs: %s",
      getPath(), HdfUtil.formatInts( chunk.chunkStartIxs));

  // As outbuf fills, it gets written to outChannel.
  HBuffer outbuf = new HBuffer( hdfFile.outChannel, compressionLevel, hdfFile);

  if (dtype == HdfGroup.DTYPE_VLEN)
    throwerr("DTYPE_VLEN datasets are not supported");

  // Format and the data to outbuf and write to outChannel.

  // Special case for DTYPE_STRING_VAR
  //
  // We don't allow compression of DTYPE_STRING_VAR -
  // deliberately not implemented.
  // It turns out that HDF5 compresses the references to
  // variable length strings, but not the strings themselves.
  // The strings remain in the global heap GCOL, uncompressed.

  if (dtype == DTYPE_STRING_VAR) {

    if (compressionLevel > 0)
      throwerr("compression not supported for DTYPE_STRING_VAR");

    // Write two areas into fmtBuf:
    //   1.  A GCOL (global heap) area.
    //       There is a separate gcol for each DTYPE_STRING_VAR variable.
    //       The term "global heap" is a misnomer.
    //   2.  A list of references to the GCOL entries.
    //       The variables chunkDataAddr, chunkDataSize refer to this
    //       list of references.

    GlobalHeap gcol = new GlobalHeap( hdfFile);
    HBuffer refBuf = new HBuffer( null, compressionLevel, hdfFile);
    long gcolAddr = hdfFile.outChannel.position();

    formatRawData(
      dtype,
      0,               // stgFieldLen for DTYPE_STRING_FIX
      vdata,
      null,            // cntr for DTYPE_COMPOUND
      gcolAddr,        // addr where we will write this gcol
      gcol,            // output: holds strings
      refBuf);         // output: holds references to strings

    if (hdfFile.bugs >= 5) {
      prtf("  writeDataSub.STRING_VAR: gcol: %s", gcol);
      prtf("  writeDataSub.STRING_VAR: refBuf: %s", refBuf);
    }

    // Write gcol to outChannel
    gcol.formatBuf( 0, outbuf);       // formatPass = 0
    outbuf.flush();                   // write remaining data to outChannel

    // Save addr; write refBuf to outChannel
    chunk.chunkDataAddr = HdfUtil.alignLong( 8, hdfFile.outChannel.position());
    hdfFile.outChannel.position( chunk.chunkDataAddr);

    refBuf.writeChannel( hdfFile.outChannel);
  }

  else {                   // else not DTYPE_STRING_VAR
    chunk.chunkDataAddr = HdfUtil.alignLong( 8, hdfFile.eofAddr);
    hdfFile.outChannel.position( chunk.chunkDataAddr);

    formatRawData(
      dtype,
      stgFieldLen,
      vdata,
      new HdfModInt(0),
      -1,                  // gcolAddr for DTYPE_STRING_VAR
      null,                // gcol for DTYPE_STRING_VAR
      outbuf);

    outbuf.flush();        // write remaining data to outChannel
  }

  // Set chunk.chunkDataSize.
  // For non-compressed numeric data we could use something like ...
  //   chunkDataSize = elementLen;
  //   for (int ii = 0; ii < rank; ii++) {
  //     chunkDataSize *= chunk.chunkUserDims[ii];
  //   }
  //
  // However compressed data can be any length,
  // so we just use the output length.

  long endPos = hdfFile.outChannel.position();
  chunk.chunkDataSize = endPos - chunk.chunkDataAddr;

  if (hdfFile.bugs >= 2) {
    prtf("HdfGroup.writeData exit: path: " + getPath());
    prtf("  chunkDataAddr: %d  endPos: %d  chunkDataSize: %d",
      chunk.chunkDataAddr, endPos, chunk.chunkDataSize);
    prtf("  old eofAddr: %d", hdfFile.eofAddr);
  }

  hdfFile.eofAddr = hdfFile.outChannel.position();

  if (hdfFile.bugs >= 2) {
    prtf("  new eofAddr: %d", hdfFile.eofAddr);
  }

} // end writeDataSub






/**
 * Given starting indices, returns the index of the
 * appropriate chunk in hdfChunks.
 * Also checks for startIxs validity.
 */

int calcChunkIx( int[] startIxs)
throws HdfException
{
  int ichunk = 0;
  for (int ii = 0; ii < rank; ii++) {
    if (startIxs[ii] < 0)
      throwerr("startIxs[%d] == %d is < 0: %d", ii, startIxs[ii]);
    if (startIxs[ii] >= varDims[ii])
      throwerr("startIxs[%d] == %d is >= varDims[%d] == %d",
        ii, startIxs[ii], ii, varDims[ii]);
    if (chunkDims == null) {
      if (startIxs[ii] != 0) throwerr("startIxs != 0 for chunkDims == null");
    }
    else {
      if (startIxs[ii] % chunkDims[ii] != 0)
        throwerr("startIxs[%d] == %d is not a multiple of chunkDims[%d] == %d",
          ii, startIxs[ii], ii, chunkDims[ii]);
      ichunk += (startIxs[ii] / chunkDims[ii]) * totChunkNums[ii];
    }
  }
  if (ichunk < 0 || ichunk >= hdfChunks.length) throwerr("invalid ichunk");
  return ichunk;
}







/**
 * Formats this individual BaseBlk to fmtBuf;
 * calls addWork to add any referenced BaseBlks (btreeNode, localHeap)
 * to workList; extends abstract BaseBlk.
 * <p>
 * Calls layoutVersion2 twice: to get the length, then
 * to format to fmtBuf.
 *
 * @param formatPass: <ul>
 *   <li> 1: Initial formatting to determine the formatted length.
 *          In HdfGroup we add msgs to hdrMsgList.
 *   <li> 2: Final formatting.
 * </ul>
 * @param fmtBuf  output buffer
 */

void formatBuf(
  int formatPass,
  HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  // We need to use version 2 to support the
  // messages: link, link info, group info

  // Write it out


  if (formatPass == 1) {
    if (! isVariable) {
      hdrMsgList.add( new MsgGroupInfo( this, hdfFile));
      hdrMsgList.add( new MsgLinkInfo( this, hdfFile));
    }
    if (subGroupList != null) {
      for (HdfGroup subGroup : subGroupList) {
        hdrMsgList.add( new MsgLinkit(
          linkCreationOrder++,
          subGroup,
          this,
          hdfFile));
      }
    }
    if (subVariableList != null) {
      for (HdfGroup subGroup : subVariableList) {
        hdrMsgList.add( new MsgLinkit(
          linkCreationOrder++,
          subGroup,
          this,
          hdfFile));
      }
    }
  }

  // There is some needlessly twisted code
  // in the HDF5 H5Odbg.c.
  // It requires that the length of the chunklen field
  // be "appropriate" for the chunk0Len value, meaning
  // if  0 <= chunk0Len <= 255 the chunklen field must be
  // exactly 1 byte, if chunk0Len <= 65535 the field must be
  // exactly 2 bytes, etc.
  //
  // So we cannot always specify that the chunklen field size = 8.
  // This implies we must know chunk0Len before laying out
  // chunk0 ... as I said, a bit twisted.
  //
  // So we lay out the HdfGroup twice on each formatPass.
  // The first layout uses a temp HBuffer and just gets
  // us the chunk0Len.
  // The second layout is for real and uses fmtBuf.

  HBuffer tempHbuf = new HBuffer(
    null,                 // outChannel
    0,                    // compressionLevel
    hdfFile);

  int svIndent = hdfFile.indent;
  hdfFile.indent += 6;
  if (hdfFile.bugs >= 5) prtIndent("Start HdfGroup temp layout");

  // Find chunk0Len
  // Use formatPass = 0 so MsgLayout and MsgLinkit don't call addWork.
  long chunk0Len = 0;
  chunk0Len = layoutVersion2(
    0, chunk0Len, tempHbuf);  // formatPass = 0
  if (hdfFile.bugs >= 5) prtIndent("End HdfGroup temp layout");
  hdfFile.indent = svIndent;

  // Layout for real
  layoutVersion2( formatPass, chunk0Len, fmtBuf);

  noteFormatExit( fmtBuf);         // BaseBlk: print debug
} // end formatBuf





/**
 * Called by formatBuf when fileVersion==2: formats this individual BaseBlk
 * to fmtBuf.
 */


long layoutVersion2(
  int formatPass,
  long prevChunk0Len,
  HBuffer fmtBuf)
throws HdfException
{

  long startAllPos = fmtBuf.getPos();
  fmtBuf.putBufByte("HdfGroup: signa", signa);
  fmtBuf.putBufByte("HdfGroup: signb", signb);
  fmtBuf.putBufByte("HdfGroup: signc", signc);
  fmtBuf.putBufByte("HdfGroup: signd", signd);

  fmtBuf.putBufByte("HdfGroup: groupVersion", 2);

  // Flags:
  //   bits  mask  desc
  //   0-1    3  len of chunk#0 field: 0: 1 byte, 1: 2 bytes, 2: 4, 3: 8
  //   2      4  1: track attr creation order
  //   3      8  1: index attr creation order
  //   4     16  1: store non-default attr storage phase values
  //   5     32  1: store set, access, mod, change, birth times
  //   6-7: reserved

  int lenMask;
  if (prevChunk0Len <= 255) lenMask = 0;
  else if (prevChunk0Len <= 65535) lenMask = 1;
  else if (prevChunk0Len <= 4294967295L) lenMask = 2;
  else lenMask = 3;

  int flag = lenMask | 4 | 8 | 32;
    // chunklen=8, track attrs, index attrs, store all times

  fmtBuf.putBufByte("HdfGroup: flags", flag);

  // Caution: HDF5 has a possible year 2038 problem.
  // It uses a 4 byte date, which if handled as a signed int,
  // wraps in year 2038.
  if ((flag & 32) != 0) {
    fmtBuf.putBufInt(
      "HdfGroup: accessTime", (int) hdfFile.utcModTimeSec);
    fmtBuf.putBufInt(
      "HdfGroup: modTime", (int) hdfFile.utcModTimeSec);
    fmtBuf.putBufInt(
      "HdfGroup: changeTime", (int) hdfFile.utcModTimeSec);
    fmtBuf.putBufInt(
      "HdfGroup: birthTime", (int) hdfFile.utcModTimeSec);
  }

  if ((flag & 16) != 0) {
    fmtBuf.putBufShort("HdfGroup: maxNumCompact", 30000);
    fmtBuf.putBufShort("HdfGroup: minNumDense", 0);
  }

  if (lenMask == 0)
    fmtBuf.putBufByte("HdfGroup: chunk0Len", (int) prevChunk0Len);
  else if (lenMask == 1)
    fmtBuf.putBufShort("HdfGroup: chunk0Len", (int) prevChunk0Len);
  else if (lenMask == 2)
    fmtBuf.putBufInt("HdfGroup: chunk0Len", (int) prevChunk0Len);
  else if (lenMask == 3)
    fmtBuf.putBufLong("HdfGroup: chunk0Len", prevChunk0Len);

  // Write out all the messages
  long startMsgPos = fmtBuf.getPos();
  for (MsgBase hmsg : hdrMsgList) {
    // Internal block
    hmsg.formatFullMsg( formatPass, fmtBuf);
    if (hdfFile.bugs >= 5) {
      prtIndent(
        "Group write: above hmsg type: 0x%x == %d  size: 0x%x == %d",
        hmsg.hdrMsgType, hmsg.hdrMsgType, hmsg.hdrMsgSize, hmsg.hdrMsgSize);
    }
  }

  long endPos = fmtBuf.getPos();

  byte[] chkBytes = fmtBuf.getBufBytes( startAllPos, endPos);
  int checkSumHack = new CheckSumHack().calcHackSum( chkBytes);
  fmtBuf.putBufInt("HdfGroup: checkSumHack", checkSumHack);

  long chunk0Len = endPos - startMsgPos;
  return chunk0Len;
} // end layoutVersion2









/**
 * Formats a regular array of raw data.  Not a ragged (VLEN) array.
 * Called by writeDataSub and MsgAttribute.formatMsgCore.
 * <p>
 * If vdata is strings, they are all fixed len,
 * and are padded to elementLen.
 * <p>
 * vdata may be one of:<ul>
 *   <li> Byte (scalar),      byte[],      [][],  [][][],  etc.
 *   <li> Short (scalar),     short[],     [][],  [][][],  etc.
 *   <li> Integer (scalar),   int[],       [][],  [][][],  etc.
 *   <li> Long (scalar),      long[],      [][],  [][][],  etc.
 *   <li> Float (scalar),     float[],     [][],  [][][],  etc.
 *   <li> Double (scalar),    double[],    [][],  [][][],  etc.
 *   <li> Character (scalar), char[],      [][],  [][][],  etc.
 *   <li> String (scalar),    String[],    [][],  [][][],  etc.
 *   <li> HdfGroup (scalar),  HdfGroup[],  [][],  [][][],  etc.  (reference)
 * </ul>
 * <p>
 * The scalar types (Short, Integer, Float, etc)
 * and the 1 dimensional types (short[], int[], float[], etc)
 * are handled explicitly below.
 * <p>
 * The higher dimension types, [][], [][][], etc, are handled
 * by recursive calls in the test: if vdata instanceof Object[].
 * <p>
 * String[] is handled recursively as Object[], then as scalar String.
 * Similarly for HdfGroup[].
 */

void formatRawData(
  int dtp,             // one of DTYPE_*
  int stgFieldLen,     // used for DTYPE_STRING_FIX
  Object vdata,
  HdfModInt cntr,      // used for the index of compound types
  long gcolAddr,       // used for DTYPE_STRING_VAR
  GlobalHeap gcol,     // used for DTYPE_STRING_VAR
  HBuffer fmtBuf)      // output buffer
throws HdfException
{
  if (vdata == null) throwerr("vdata is null");

  if (vdata instanceof Object[]) {
    Object[] objVec = (Object[]) vdata;
    for (int ii = 0; ii < objVec.length; ii++) {
      formatRawData(                               // recursion
        dtp,
        stgFieldLen,
        objVec[ii],
        cntr,
        gcolAddr,
        gcol,
        fmtBuf);
    }
  }
  else if (vdata instanceof Byte) {
    checkDtype( DTYPE_SFIXED08, DTYPE_UFIXED08, dtp);
    byte aval = ((Byte) vdata).byteValue();
    fmtBuf.putBufByte("formatRawData", aval);
  }
  else if (vdata instanceof Short) {
    checkDtype( DTYPE_FIXED16, dtp);
    short aval = ((Short) vdata).shortValue();
    fmtBuf.putBufShort("formatRawData", aval);
  }
  else if (vdata instanceof Integer) {
    checkDtype( DTYPE_FIXED32, dtp);
    int aval = ((Integer) vdata).intValue();
    fmtBuf.putBufInt("formatRawData", aval);
  }
  else if (vdata instanceof Long) {
    checkDtype( DTYPE_FIXED64, dtp);
    long aval = ((Long) vdata).longValue();
    fmtBuf.putBufLong("formatRawData", aval);
  }
  else if (vdata instanceof Float) {
    checkDtype( DTYPE_FLOAT32, dtp);
    float aval = ((Float) vdata).floatValue();
    fmtBuf.putBufFloat("formatRawData", aval);
  }
  else if (vdata instanceof Double) {
    checkDtype( DTYPE_FLOAT64, dtp);
    double aval = ((Double) vdata).doubleValue();
    fmtBuf.putBufDouble("formatRawData", aval);
  }

  else if (vdata instanceof Character) {
    // Normally we would handle character encoding by
    // calling HdfUtil.encodeString.
    // However the final length is fixed, so we must
    // do 1 char -> 1 byte encoding.
    checkDtype( DTYPE_STRING_FIX, dtp);
    fmtBuf.putBufByte("formatRawData", ((Character) vdata).charValue());
  }

  else if (vdata instanceof String) {
    checkDtype( DTYPE_STRING_FIX, DTYPE_STRING_VAR, dtp);
    String aval = (String) vdata;
    if (dtp == DTYPE_STRING_FIX) {
      byte[] bytes = HdfUtil.encodeString( aval, false, this);
      fmtBuf.putBufBytes(
        "formatRawData", HdfUtil.truncPadNull( bytes, stgFieldLen));
    }
    else if (dtp == DTYPE_STRING_VAR) {
      byte[] bytes = HdfUtil.encodeString(
        (String) vdata, false, this);     // addNull = false
      int gcolIx = gcol.putHeapItem("vlen string data", bytes);
      fmtBuf.putBufInt("vlen len", bytes.length);
      fmtBuf.putBufLong("vlen gcol addr", gcolAddr);
      fmtBuf.putBufInt("vlen gcol ix", gcolIx);
    }
    else throwerr("dtp mismatch");
  }

  else if (vdata instanceof HdfGroup) {
    // For object refs, data is:
    //   addr of group header, 8 bytes.
    // For region refs, data is:
    //   globalHeapReference:
    //     addr of globalHeap, 8 bytes
    //     global heap index, 4 bytes
    //   The object on the global heap is:
    //     addr of group header, 8 bytes
    //     some sort of info on type, dimension, start and end indices
    long aval = ((HdfGroup) vdata).blkPosition;
    fmtBuf.putBufLong("formatRawData", aval);
    if (dtp == DTYPE_COMPOUND) {
      fmtBuf.putBufInt("formatRawData ref ix", cntr.getValue());
      cntr.increment();
    }
  }

  else if (vdata instanceof byte[]) {
    checkDtype( DTYPE_SFIXED08, DTYPE_UFIXED08, dtp);
    byte[] avec = (byte[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufByte("formatRawData", 0xff & avec[ii]);
    }
  }
  else if (vdata instanceof short[]) {
    checkDtype( DTYPE_FIXED16, dtp);
    short[] avec = (short[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufShort("formatRawData", 0xffff & avec[ii]);
    }
  }
  else if (vdata instanceof int[]) {
    checkDtype( DTYPE_FIXED32, dtp);
    int[] avec = (int[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufInt("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof long[]) {
    checkDtype( DTYPE_FIXED64, dtp);
    long[] avec = (long[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufLong("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof float[]) {
    checkDtype( DTYPE_FLOAT32, dtp);
    float[] avec = (float[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufFloat("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof double[]) {
    checkDtype( DTYPE_FLOAT64, dtp);
    double[] avec = (double[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufDouble("formatRawData", avec[ii]);
    }
  }
  else if (vdata instanceof char[]) {
    // Normally we would handle character encoding by
    // calling HdfUtil.encodeString.
    // However the final length is fixed, so we must
    // do 1 char -> 1 byte encoding.
    checkDtype( DTYPE_STRING_FIX, dtp);
    char[] avec = (char[]) vdata;
    for (int ii = 0; ii < avec.length; ii++) {
      fmtBuf.putBufByte("formatRawData", 0xff & avec[ii]);
    }
  }
  else throwerr("unknown raw data type.  class: " + vdata.getClass());
} // end formatRawData






void checkDtype(
  int expecta,
  int actual)
throws HdfException
{
  if (actual != expecta)
    throwerr("data type mismatch.  Expected: %s  Actual: %s",
      dtypeNames[expecta],
      dtypeNames[actual]);
}


void checkDtype(
  int expecta,
  int expectb,
  int actual)
throws HdfException
{
  if (actual != expecta && actual != expectb)
    throwerr("data type mismatch.  Expected: %s or %s  Actual: %s",
      dtypeNames[expecta],
      dtypeNames[expectb],
      dtypeNames[actual]);
}






/**
 * Formats the GlobalHeap indices of ragged (VLEN) array vdata
 * to fmtBuf.  The data itself must have been formatted previously
 * to the GlobalHeap by calling GlobalHeap.putHeapVlenObject.
 * <p>
 * <pre>
 * For irow = 0, < len(vdata):
 *   format to fmtBuf:
 *     len of ele == ncol
 *     blkPosition of mainGlobalHeap
 *     heapIxs[irow]
 * </pre>
 *
 * Called by MsgAttribute.formatMsgCore.
 */

void formatVlenRawData(
  int[] heapIxs,
  Object vdata,
  HBuffer fmtBuf)      // output buffer
throws HdfException
{
  Object[] vdataVec = (Object[]) vdata;

  int nrow = vdataVec.length;
  for (int irow = 0; irow < nrow; irow++) {
    Object vrow = vdataVec[irow];
    int ncol = -1;
    if (vrow instanceof Object[]) ncol = ((Object[]) vrow).length;
    else if (vrow instanceof byte[]) ncol = ((byte[]) vrow).length;
    else if (vrow instanceof short[]) ncol = ((short[]) vrow).length;
    else if (vrow instanceof int[]) ncol = ((int[]) vrow).length;
    else if (vrow instanceof long[]) ncol = ((long[]) vrow).length;
    else if (vrow instanceof float[]) ncol = ((float[]) vrow).length;
    else if (vrow instanceof double[]) ncol = ((double[]) vrow).length;
    else if (vrow instanceof char[]) ncol = ((char[]) vrow).length;
    else throwerr("unknown vlen type");

    // Format numVal, global heap ID
    fmtBuf.putBufInt("vlen.ncol", ncol);
    fmtBuf.putBufLong("globalHeap.pos", hdfFile.mainGlobalHeap.blkPosition);
    fmtBuf.putBufInt("outputGlobalHeap.ix", heapIxs[irow]);
  } // for irow
} // end formatVlenRawData





} // end class
