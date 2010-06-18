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


package nhPkg;

import java.util.ArrayList;
import java.util.regex.Pattern;

import hdfnet.HdfException;
import hdfnet.HdfGroup;
import hdfnet.Util;


public class NhGroup {

// xxx
//   Test:
//     dim lat = 5
//     var lat(3)
//     or var lat( lat, lon)  ???
// 
// addDim:
//   insure no ancestor var with same name: err "define dims before vars"
// 
// xxx










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
  this.groupName = groupName;
  this.parentGroup = parentGroup;
  this.nhFile = nhFile;
  if (parentGroup == null) hdfGroup = null;
  else hdfGroup = parentGroup.hdfGroup.addGroup( groupName);
}






public String toString() {
  String res = String.format(
    "name: \"%s\"  path: \"%s\"  nSubGrp: %d  nDim: %d  nVar: %d",
    groupName, getPath(),
    subGroupList.size(),
    dimensionList.size(),
    variableList.size());
  return res;
}



public String getName() { return groupName; }

public NhGroup getParentGroup() { return parentGroup; }

public NhFileWriter getFileWriter() { return nhFile; }





public String getPath() {
  String res = "";
  NhGroup grp = this;
  while (grp != null) {
    res = groupName + "/" + res;
    grp = grp.parentGroup;
  }
  return res;
}




public NhGroup[] getSubGroups() {
  return subGroupList.toArray( new NhGroup[0]);
}



public NhDimension[] getDimensions() {
  return dimensionList.toArray( new NhDimension[0]);
}



public NhVariable[] getVariables() {
  return variableList.toArray( new NhVariable[0]);
}



// Returns null if not found.

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



// Returns null if not found.

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


// Find Dimension, searching within this group only.
// Returns null if not found.

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




// Find Dimension, searching all ancestors.
// Returns null if not found.

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






public NhGroup addGroup(
  String subName)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addGroup: group: \"" + groupName + "\""
      + "  subGroup: \"" + subName + "\"");
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


public NhDimension addDimension(
  String dimName,
  int dimLen)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addDimension: group: \"" + groupName + "\""
      + "  dimName: \"" + dimName + "\""
      + "  dimLen: " + dimLen + "");
  }
  checkName( dimName, "dimension in group \"" + groupName + "\"");
  NhDimension nhDim = new NhDimension( dimName, dimLen, this);
  dimensionList.add( nhDim);
  return nhDim;
}






public NhVariable addVariable(
  String varName,
  int nhType,
  NhDimension[] nhDims,
  Object fillValue,
  int compressionLevel)      // 0: no compression;  9: max compression
throws NhException
{
  if (nhFile.bugs >= 1) {
    String msg = "NhGroup.addVariable: group: \"" + groupName + "\""
      + "  var name: \"" + varName + "\"\n"
      + "  type: " + NhVariable.nhTypeNames[ nhType] + "\n"
      + "  dims: ";
    for (NhDimension dm : nhDims) {
      msg += "  \"" + dm.dimName + "\"(" + dm.dimLen + ")";
    }
    msg += "\n";
    msg += "  fill: " + fillValue + "\n";
    msg += "  compressionLevel: " + compressionLevel;
    prtf( msg);
  }

  checkName( varName, "variable in group \"" + groupName + "\"");

  // Netcdf doesn't support fill values for Strings or scalars.
  if (fillValue != null) {
    if (nhType == NhVariable.TP_STRING_VAR)
      throwerr("TP_STRING_* variables must have fillValue == null");
    if (nhDims.length == 0)
      throwerr("scalar variables must have fillValue == null");
  }

  NhVariable nhVar = null;
  nhVar = new NhVariable(
    varName,
    nhType,
    nhDims,
    fillValue,
    compressionLevel,
    this,
    nhFile);
  variableList.add( nhVar);
  return nhVar;
}






// Although HDF5 supports attributes of any dimsionality, 0, 1, 2, ...,
// the NetCDF data model only supports 1 dimensional arrays and strings.

public void addAttribute(
  String attrName,
  int atType,                // one of NhVariable.TP_*
  Object attrValue,
  boolean isVlen)
throws NhException
{
  if (nhFile.bugs >= 1) {
    prtf("NhGroup.addAttribute: grp: \"" + groupName + "\""
      + "  attrName: \"" + attrName + "\""
      + "  atType: " + NhVariable.nhTypeNames[atType]
      + "  value: " + Util.formatObject( attrValue));
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

  int stgFieldLen = 0;     // max string len for STRING_FIX, without null term
  if (atType == NhVariable.TP_CHAR) stgFieldLen = 1;

  try {
    hdfGroup.addAttribute(
      attrName,
      dtype,
      stgFieldLen,
      attrValue,
      isVlen);
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}






static void checkName(
  String name,
  String loc)
throws NhException
{
  if (name == null || name.length() == 0)
    throwerr("Name for %s is empty", loc);
  if (! Pattern.matches("^[_a-zA-Z][_a-zA-Z0-9]*$", name))
    throwerr("Invalid name for %s.  Name: \"%s\"", loc, name);
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

