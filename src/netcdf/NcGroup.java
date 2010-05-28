
package ncHdf;

import java.util.ArrayList;
import java.util.regex.Pattern;

import hdfnet.HdfException;
import hdfnet.HdfGroup;


public class NcGroup {


String groupName;
NcGroup parentGroup;
NcFile ncFile;

HdfGroup hdfGroup;

ArrayList<NcGroup> subGroupList = new ArrayList<NcGroup>();
ArrayList<NcDimension> dimensionList = new ArrayList<NcDimension>();
ArrayList<NcVariable> variableList = new ArrayList<NcVariable>();


NcGroup(
  String groupName,
  NcGroup parentGroup,
  NcFile ncFile)
throws HdfException
{
  this.groupName = groupName;
  this.parentGroup = parentGroup;
  this.ncFile = ncFile;
  if (parentGroup == null) hdfGroup = null;
  else hdfGroup = parentGroup.hdfGroup.addGroup( groupName);
}


public NcGroup addGroup(
  String subName)
throws NcException
{
  if (ncFile.hdfFile.getDebugLevel() >= 1) {
    prtf("addGroup: group: \"" + groupName + "\"\n"
      + "  subGroup: \"" + subName + "\"\n");
  }
  checkName("subGroup", subName);
  NcGroup subNc = null;
  try { subNc = new NcGroup( subName, this, ncFile); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
  subGroupList.add( subNc);
  return subNc;
}


public NcDimension addDimension(
  String dimName,
  int dimLen)
throws NcException
{
  if (ncFile.hdfFile.getDebugLevel() >= 1) {
    prtf("addDimension: group: \"" + groupName + "\"\n"
      + "  dimName: \"" + dimName + "\"\n"
      + "  dimLen: " + dimLen + "\n");
  }
  checkName("dimension", dimName);
  NcDimension ncDim = new NcDimension( dimName, dimLen, this);
  dimensionList.add( ncDim);
  return ncDim;
}



public NcVariable addVariable(
  String varName,
  int ncType,
  int stgFieldLen,           // max string len for STRING types,
                             // including null termination
  NcDimension[] ncDims,
  Object fillValue,
  int compressionLevel)
throws NcException
{
  if (ncFile.hdfFile.getDebugLevel() >= 1) {
    String msg = "addVariable: group: \"" + groupName + "\"\n"
      + "  var name: \"" + varName + "\"\n"
      + "  type: " + NcVariable.ncTypeNames[ ncType] + "\n"
      + "  slen: " + stgFieldLen + "\n"
      + "  dims: (";
    for (NcDimension dm : ncDims) {
      msg += " \"" + dm.dimName + "\"(" + dm.dimLen + "\"";
    }
    msg += ")\n";
    msg += "  fill: " + fillValue + "\n";
    msg += "  compressionLevel: " + compressionLevel + "\n";
    prtf( msg);
  }

  checkName("variable", varName);
  NcVariable ncVar = null;
  ncVar = new NcVariable(
    varName,
    ncType,
    stgFieldLen,
    ncDims,
    fillValue,
    compressionLevel,
    hdfGroup,
    ncFile);
  variableList.add( ncVar);
  return ncVar;
}





public NcVariable oldAddNumericVariable(
  String varName,
  int ncType,
  NcDimension[] ncDims,
  Object fillValue,
  int compressionLevel)
throws NcException
{
  checkName("variable", varName);
  NcVariable ncVar = null;
  ncVar = new NcVariable(
    varName,
    ncType,
    0,                    // max string len
    ncDims,
    fillValue,
    compressionLevel,
    hdfGroup,
    ncFile);
  variableList.add( ncVar);
  return ncVar;
}





public NcVariable oldAddStringVariable(
  String varName,
  int stgFieldLen,           // max string len for STRING types,
                             // including null termination
  NcDimension[] ncDims,
  Object fillValue,
  int compressionLevel)
throws NcException
{
  checkName("variable", varName);
  NcVariable ncVar = null;
  ncVar = new NcVariable(
    varName,
    NcVariable.TP_STRING_FIX,
    stgFieldLen,            // max stg len, including null term
    ncDims,
    fillValue,
    compressionLevel,
    hdfGroup,
    ncFile);
  variableList.add( ncVar);
  return ncVar;
}





public void addAttribute(
  String attrName,
  Object attrValue)
throws NcException
{
  if (ncFile.hdfFile.getDebugLevel() >= 1) {
    prtf("addAttribute: group: \"" + groupName + "\"\n"
      + "  attrName: \"" + attrName + "\"\n");
  }
  checkName("attribute", attrName);
  if (! (attrValue instanceof short[]
    || attrValue instanceof int[]
    || attrValue instanceof long[]
    || attrValue instanceof float[]
    || attrValue instanceof double[]
    || attrValue instanceof String[]))
    throwerr("Invalid type for the value of attribute \"%s\""
      + " in group \"%s\".  Type: %s",
      attrName, groupName, attrValue.getClass().toString());

  try {
    hdfGroup.addAttribute(
      attrName,
      attrValue,
      false,                 // isVlen
      false);                // isCompoundRef
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}






void checkName(
  String loc,
  String name)
throws NcException
{
  if (name == null || name.length() == 0)
    throwerr("Name for %s is empty in group %s",
      loc, groupName);
  if (! Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*$", name))
    throwerr("Invalid name for %s is empty in group %s.  Name: \"%s\"",
      loc, groupName, name);
}




static void throwerr( String msg, Object... args)
throws NcException
{
  throw new NcException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class

