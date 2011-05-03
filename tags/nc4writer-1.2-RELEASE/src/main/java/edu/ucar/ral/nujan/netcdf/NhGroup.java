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


package edu.ucar.ral.nujan.netcdf;

import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.ucar.ral.nujan.hdf.HdfException;
import edu.ucar.ral.nujan.hdf.HdfGroup;



/**
 * Represents a NetCDF4 (HDF5) group.
 * Groups may be arbitrarily nested.
 * For typical use see {@link NhFileWriter}.
 * @see NhFileWriter
 */

public class NhGroup {


String groupName;
NhGroup parentGroup;
NhFileWriter nhFile;

HdfGroup hdfGroup;

ArrayList<NhGroup> subGroupList = new ArrayList<NhGroup>();
ArrayList<NhDimension> dimensionList = new ArrayList<NhDimension>();
ArrayList<NhVariable> variableList = new ArrayList<NhVariable>();







NhGroup(
  String groupName,
  NhGroup parentGroup,
  NhFileWriter nhFile)
throws HdfException
{
  if (nhFile.bugs >= 1) {
    String parentName = "(null)";
    if (parentGroup != null) parentName = parentGroup.getPath();
    prtf("NhGroup.const: groupName: \"%s\"  parent: \"%s\"  file: \"%s\"",
      groupName, parentName, nhFile.getPath());
  }
  this.groupName = groupName;
  this.parentGroup = parentGroup;
  this.nhFile = nhFile;
  if (parentGroup == null) hdfGroup = null;
  else hdfGroup = parentGroup.hdfGroup.addGroup( groupName);
}






public String toString() {
  String res = String.format(
    "path: \"%s\"  numSubGrp: %d  numDim: %d  numVar: %d",
    getPath(),
    subGroupList.size(),
    dimensionList.size(),
    variableList.size());
  return res;
}



/**
 * Returns the parent of this NhGroup.
 */
public NhGroup getParentGroup() { return parentGroup; }

/**
 * Returns the open file containing this NhGroup.
 */
public NhFileWriter getFileWriter() { return nhFile; }



/**
 * Returns the name of this NhGroup.
 */

public String getName() { return groupName; }


/**
 * Returns the full path, from the root group, of this NhGroup.
 */

public String getPath() {
  String res = "";
  NhGroup grp = this;
  while (grp != null) {
    if (res.length() != 0) res = "/" + res;
    res = grp.groupName + res;
    grp = grp.parentGroup;
  }
  if (res.length() == 0) res = "/";
  return res;
}



/**
 * Returns the direct children of this NhGroup.
 */

public NhGroup[] getSubGroups() {
  return subGroupList.toArray( new NhGroup[0]);
}



/**
 * Returns the dimensions defined in this group.
 * Does not return dimensions defined in other ancestor
 * or descendent groups.
 */

public NhDimension[] getDimensions() {
  return dimensionList.toArray( new NhDimension[0]);
}



/**
 * Returns the variables defined in this group.
 * Does not return variables defined in other ancestor
 * or descendent groups.
 */

public NhVariable[] getVariables() {
  return variableList.toArray( new NhVariable[0]);
}



/**
 * Returns the child subGroup having the specified name.
 * Does not search descendents other than direct children.
 *
 * @param  nm   The name of the child group.
 * @return The matching child group, or null if no match found.
 */

public NhGroup findSubGroup( String nm) {
  NhGroup res = null;
  for (NhGroup tgrp : subGroupList) {
    if (tgrp.groupName.equals( nm)) {
      res = tgrp;
      break;
    }
  }
  return res;
}



/**
 * Returns the variable having the specified name.
 * Does not search children or ancestors.
 *
 * @param  nm   The name of the variable.
 * @return The matching variable, or null if no match found.
 */

public NhVariable findVariable( String nm) {
  NhVariable res = null;
  for (NhVariable tvar : variableList) {
    if (tvar.varName.equals( nm)) {
      res = tvar;
      break;
    }
  }
  return res;
}


/**
 * Returns the dimension having the specified name.
 * Does not search children or ancestors.
 *
 * @param  nm   The name of the dimension.
 * @return The matching dimension, or null if no match found.
 */

public NhDimension findLocalDimension( String nm) {
  NhDimension res = null;
  for (NhDimension tdim : dimensionList) {
    if (tdim.dimName.equals( nm)) {
      res = tdim;
      break;
    }
  }
  return res;
}




/**
 * Returns the dimension having the specified name.
 * Searches first this group, then each ancestor in turn.
 * Does not search children.
 *
 * @param  nm   The name of the dimension.
 * @return The matching dimension, or null if no match found.
 */

public NhDimension findAncestorDimension( String nm) {
  NhDimension res = null;
  NhGroup tgrp = this;
  while (tgrp != null && res == null) {
    for (NhDimension tdim : tgrp.dimensionList) {
      if (tdim.dimName.equals( nm)) {
        res = tdim;
        break;
      }
    }
    tgrp = tgrp.parentGroup;
  }
  return res;
}



/**
 * Adds a subGroup to this group.
 * @param subName The name of the new subGroup.
 * @return The newly created subGroup.
 */

public NhGroup addGroup(
  String subName)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addGroup: this: \"%s\"  subName: \"%s\"  file: \"%s\"",
      getPath(), subName, nhFile.getPath());
  }
  checkName( subName, "subGroup in group \"" + groupName + "\"");
  NhGroup subGrp = null;
  try { subGrp = new NhGroup( subName, this, nhFile); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
  subGroupList.add( subGrp);
  return subGrp;
}




/**
 * Adds a dimension to this group.
 * @param dimName The name of the new dimension.
 * @param dimLen The length of the new dimension.
 * @return The newly created dimension.
 */

public NhDimension addDimension(
  String dimName,
  int dimLen)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addDimension: this: \"%s\"  dimName: \"%s\"  dimLen: %d",
      getPath(), dimName, dimLen);
  }
  checkName( dimName, "dimension in group \"" + groupName + "\"");
  NhDimension nhDim = new NhDimension( dimName, dimLen, this);
  dimensionList.add( nhDim);
  return nhDim;
}






/**
 * Adds a variable to this group (HDF5 calls variables "datasets").
 * <p>
 * Legal types of the fill value and the rawData parameter passed to
 * {@link NhVariable#writeData NhVariable.writeData} are:
 * <table border="1" align="center">
 *   <tr><th> nhType          </th><th> Java class of fillValue </th>
 *      <th> Java class of writeData rawData</th></tr>
 *   <tr><td> TP_SBYTE        </td><td> Byte      </td>
 *      <td>rank=0: Byte, rank=1: byte[], rank=2: byte[][], etc. </td></tr>
 *   <tr><td> TP_UBYTE        </td><td> Byte      </td>
 *      <td>rank=0: Byte, rank=1: byte[], rank=2: byte[][], etc. </td></tr>
 *   <tr><td> TP_SHORT        </td><td> Short     </td>
 *      <td>rank=0: Short, rank=1: short[], rank=2: short[][], etc. </td></tr>
 *   <tr><td> TP_INT          </td><td> Integer   </td>
 *      <td>rank=0: Integer, rank=1: int[], rank=2: int[][], etc. </td></tr>
 *   <tr><td> TP_LONG         </td><td> Long      </td>
 *      <td>rank=0: Long, rank=1: long[], rank=2: long[][], etc. </td></tr>
 *   <tr><td> TP_FLOAT        </td><td> Float     </td>
 *      <td>rank=0: Float, rank=1: float[], rank=2: float[][], etc. </td></tr>
 *   <tr><td> TP_DOUBLE       </td><td> Double    </td>
 *      <td>rank=0: Double, rank=1: double[], rank=2: double[][], etc. </td></tr>
 *   <tr><td> TP_CHAR         </td><td> Character </td>
 *      <td>rank=0: Character, rank=1: char[], rank=2: char[][], etc. </td></tr>
 *   <tr><td> TP_STRING_VAR   </td><td> String    </td>
 *      <td>rank=0: String, rank=1: String[], rank=2: String[][], etc. </td></tr>
 * </table>
 *
 * @param varName The name of the new variable.
 * @param nhType The type of the new variable: one of NhVariable.TP_*.
 * @param nhDims The dimensions of the data array for the variable.
 *    A scalar variable is represented by nhDims = new int[0].
 * @param chunkLens len of each side of a chunk hyperslab.
 *        Must have chunkLens.length == nhDims.length.
 *        If chunkLens == null use contiguous storage.
 *        If chunkLens == nhDims values,
 *          use chunked storage with just one chunk.
 *        If nhDims == null or nhDims.length == 0, chunkLens must be null.
 * @param fillValue The fill value.  The type must agree with nhType
 *    as shown in the table above.
 *    May be null.
 *    Must be null for scalar or TP_STRING.
 * @param compressionLevel  Desired level of compression.  0 is no
 *    compression; 1 through 9 are increasing compression.
 *    Scalar data (nhDims == new int[0]) must have compressionLevel = 0.
 * @return The newly created dimension.
 */

public NhVariable addVariable(
  String varName,
  int nhType,
  NhDimension[] nhDims,
  int[] chunkLens,
  Object fillValue,
  int compressionLevel)      // 0: no compression;  9: max compression
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addVariable: this: \"" + getPath() + "\""
      + "  var name: \"" + varName + "\"\n"
      + "  nhType: " + NhVariable.nhTypeNames[ nhType]);

    if (nhDims == null) prtf("  dims: (null)");
    else {
      String msg = "  nhDims (len " + nhDims.length + "): ";
      for (int ii = 0; ii < nhDims.length; ii++) {
        NhDimension dm = nhDims[ii];
        if (dm == null) throwerr("nhDims element " + ii + " is null");
        msg += "  \"" + dm.dimName + "\"(" + dm.dimLen + ")";
      }
      prtf( msg);
    }

    prtf("  chunkLens: " + formatInts( chunkLens));
    prtf("  fill: " + fillValue);
    prtf("  compressionLevel: " + compressionLevel);
  }

  checkName( varName, "variable in group \"" + groupName + "\"");

  // Netcdf doesn't support fill values for Strings or scalars.
  if (fillValue != null) {
    if (nhType == NhVariable.TP_STRING_VAR)
      throwerr("TP_STRING_* variables must have fillValue == null");
    // Allow fillValue even with scalars, since some datasets have that.
    //if (nhDims == null || nhDims.length == 0)
    //  throwerr("scalar variables must have fillValue == null");
  }

  NhVariable nhVar = null;
  nhVar = new NhVariable(
    varName,
    nhType,
    nhDims,
    chunkLens,
    fillValue,
    compressionLevel,
    this,
    nhFile);
  variableList.add( nhVar);
  return nhVar;
}




/**
 * Returns true if an attribute with the given names exists
 * in this group; false otherwise.
 */

public boolean attributeExists(
  String attrName)
{
  boolean bres = false;
  if (hdfGroup.findAttribute( attrName) != null) bres = true;
  return bres;
}




/**
 * Adds an attribute to this group.
 * Although the underlying HDF5 library supports attributes
 * of any dimsionality, 0, 1, 2, ...,
 * the NetCDF data model only supports attributes that
 * are a String or a 1 dimensional
 * array of: String, byte, short, int, long, float, or double.
 * <p>
 * Legal types of attrValue:
 * <table border="1" align="center">
 *   <tr><th> atType          </th><th> Java class of attrValue   </th></tr>
 *   <tr><td> TP_SBYTE        </td><td> byte[]                    </td></tr>
 *   <tr><td> TP_UBYTE        </td><td> byte[]                    </td></tr>
 *   <tr><td> TP_SHORT        </td><td> short[]                   </td></tr>
 *   <tr><td> TP_INT          </td><td> int[]                     </td></tr>
 *   <tr><td> TP_LONG         </td><td> long[]                    </td></tr>
 *   <tr><td> TP_FLOAT        </td><td> float[]                   </td></tr>
 *   <tr><td> TP_DOUBLE       </td><td> double[]                  </td></tr>
 *   <tr><td> TP_CHAR         </td><td> char[]                    </td></tr>
 *   <tr><td> TP_STRING_VAR   </td><td> String or String[]        </td></tr>
 * </table>
 *
 * @param attrName The name of the new attribute.
 * @param atType The type of the new variable: one of NhVariable.TP_*.
 * @param attrValue The value of the new attribute.
 */

// Although HDF5 supports attributes of any dimsionality, 0, 1, 2, ...,
// the NetCDF data model only supports 1 dimensional arrays and strings.

public void addAttribute(
  String attrName,
  int atType,                // one of NhVariable.TP_*
  Object attrValue)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addAttribute: this: \"" + getPath() + "\""
      + "  attrName: \"" + attrName + "\""
      + "  type: " + NhVariable.nhTypeNames[atType]);
  }
  if (nhFile.bugs >= 10) {
    prtf("  attrValue: " + attrValue);
  }
  checkName( attrName, "attribute in group \"" + groupName + "\"");

  attrValue = NhVariable.getAttrValue(
    attrName,
    attrValue, 
    "group \"" + groupName + "\"",
    nhFile.bugs);

  int dtype = NhVariable.findDtype( attrName, atType);

  // Netcdf cannot read HDF5 attributes that are Scalar STRING_VAR.
  // They must be encoded as STRING_FIX.
  // However datasets can be a scalar STRING_VAR.
  if (dtype == HdfGroup.DTYPE_STRING_VAR
    && NhVariable.testScalar( attrValue))
    dtype = HdfGroup.DTYPE_STRING_FIX;

  // If attrType==DTYPE_STRING_FIX and stgFieldLen==0,
  // MsgAttribute will find the max stg len in attrValue.
  int stgFieldLen = 0;     // max string len for STRING_FIX, without null term

  try {
    hdfGroup.addAttribute(
      attrName,
      dtype,
      stgFieldLen,
      attrValue,
      false);              // isVlen
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}



/**
 * Checks that a name (for a group or attribute) is legal in HDF5;
 * else throws an NhException.
 * Coord with hdf/HdfUtil.checkName.
 */

public static void checkName(
  String name,
  String loc)
throws NhException
{
  if (name == null || name.length() == 0)
    throwerr("Name for %s is empty", loc);
  if (! Pattern.matches("^[_a-zA-Z][-_: a-zA-Z0-9]*$", name))
    throwerr("Invalid name for %s.  Name: \"%s\"", loc, name);
}






/**
 * Formats an array of ints.
 */

static String formatInts(
  int[] vals)
{
  String res = "";
  if (vals == null) res = "(null)";
  else {
    for (int ii = 0; ii < vals.length; ii++) {
      if (ii > 0) res += " ";
      res += vals[ii];
    }
  }
  return res;
}







static void throwerr( String msg, Object... args)
throws NhException
{
  throw new NhException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class

