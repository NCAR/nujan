
package ncHdf;

import java.util.ArrayList;

import hdfnet.HdfException;
import hdfnet.HdfFile;
import hdfnet.HdfGroup;


public class NcFile {


// Bit flags for optFlag
public static final int OPT_OVERWRITE = 1;


String path;
int optFlag;                   // zero or more OPT_* bit options
int fileVersion;


HdfFile hdfFile;
NcGroup rootGroup;


public NcFile(
  String path,
  int optFlag)                   // zero or more OPT_* bit options
throws NcException
{
  this( path, optFlag, 2);       // fileVersion = 2
}



public NcFile(
  String path,
  int optFlag,                   // zero or more OPT_* bit options
  int fileVersion)
throws NcException
{
  this.path = path;
  this.optFlag = optFlag;
  this.fileVersion = fileVersion;

  try {
    int hdfOptFlag = 0;
    if ((optFlag & OPT_OVERWRITE) != 0)
      hdfOptFlag |= HdfFile.OPT_ALLOW_OVERWRITE;
    hdfFile = new HdfFile( path, fileVersion, hdfOptFlag);
    rootGroup = new NcGroup( "", null, this);   // rootName, parent, ncFile
    rootGroup.hdfGroup = hdfFile.getRootGroup();
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}



public void setDebugLevel( int level) {
  hdfFile.setDebugLevel( level);
}



public NcGroup getRootGroup() {
  return rootGroup;
}



public void endDefine()
throws NcException
{
  if (hdfFile.getDebugLevel() >= 1) {
    prtf("endDefine: path: \"" + path + "\"\n");
  }
  ArrayList<NcGroup> groupList = new ArrayList<NcGroup>();
  ArrayList<NcVariable> variableList = new ArrayList<NcVariable>();
  findGroups( rootGroup, groupList, variableList);

  //xxx this is done in NcVariable now
  //xxx omit: // For each var, for each dimension,
  //xxx omit: // find where it was defined.
  //xxx omit: for (NcVariable var : variableList) {
  //xxx omit:   for (NcDimension dim : var:ncDims) {
  //xxx omit:     // Go up tree to find where dim is defined
  //xxx omit:     HdfGroup grp = var.parentGroup;
  //xxx omit:     while (grp != null) {
  //xxx omit:       for (NcDimension dimDef : grp.dimsensionList) {
  //xxx omit:         if (dimDef == dim) {
  //xxx omit:           foundit = true;
  //xxx omit:           dimDef.refList.add( var);
  //xxx omit:           break;
  //xxx omit:         }
  //xxx omit:       }
  //xxx omit:       if (foundit) break;
  //xxx omit:       grp = grp.parentGroup;
  //xxx omit:     } // while grp != null
  //xxx omit:     if (! foundit) throwerr("dimension def not found for:\n"
  //xxx omit:       + "  dim: " + dim + "\n"
  //xxx omit:       + "  in var: " + var + "\n");
  //xxx omit:   } // for each dim in var
  //xxx omit: } // for var

  // For each Nc group, for each dimension, add the back-reference attrs.
  // My, what silly architecture you have, Hdf!
  for (NcGroup grp : groupList) {
    for (NcDimension dim : grp.dimensionList) {
      HdfGroup[] hdfVars = new HdfGroup[ dim.refList.size()];
      for (int ii = 0; ii < hdfVars.length; ii++) {
        hdfVars[ii] = dim.refList.get(ii).hdfVar;
      }
      try {
        dim.hdfVar.addAttribute(
          "REFERENCE_LIST",        // attrName
          hdfVars,                 // attrValue
          false,                   // isVlen
          true);                   // isCompoundRef
      }
      catch( HdfException exc) {
        exc.printStackTrace();
        throwerr("caught: " + exc);
      }
    }
  }

  try { hdfFile.endDefine(); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  // Write the data for all dimension variables in the entire tree
  writeTreeDimData( rootGroup);

} // end endDefine




// Find all groups and data variables, and fill groupList, variableList.
// Ignores dimensions.

void findGroups(
  NcGroup grp,
  ArrayList<NcGroup> groupList,
  ArrayList<NcVariable> variableList)
{
  groupList.add( grp);
  variableList.addAll( grp.variableList);
  for (NcGroup subGrp : grp.subGroupList) {
    findGroups( subGrp, groupList, variableList);
  }
}




void writeTreeDimData( NcGroup ncGroup)
throws NcException
{
  for (NcDimension ncdim : ncGroup.dimensionList) {
    float[] dimData = new float[ ncdim.dimLen];
    try { ncdim.hdfVar.writeData( dimData); }
    catch( HdfException exc) {
      exc.printStackTrace();
      throwerr("caught: " + exc);
    }

    for (NcGroup subGroup : ncGroup.subGroupList) {
      writeTreeDimData( subGroup);
    }
  }
}




public void close()
throws NcException
{
  if (hdfFile.getDebugLevel() >= 1) {
    prtf("close: path: \"" + path + "\"\n");
  }
  try { hdfFile.close(); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
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
