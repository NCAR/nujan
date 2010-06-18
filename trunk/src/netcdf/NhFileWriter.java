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

import hdfnet.HdfException;
import hdfnet.HdfFileWriter;
import hdfnet.HdfGroup;


public class NhFileWriter {



// Bit flags for optFlag
public static final int OPT_OVERWRITE = 1;



// Define constants for fileStatus
static final int ST_DEFINING  = 1;
static final int ST_WRITEDATA = 2;
static final int ST_CLOSED    = 3;
static final String[] statusNames = {
  "UNKNOWN", "DEFINING", "WRITEDATA", "CLOSED"};


String path;
int optFlag;                     // zero or more OPT_* bit options
int fileVersion;


int fileStatus;                 // one of ST_*
private HdfFileWriter hdfFile;
NhGroup rootGroup;
int bugs;






public NhFileWriter(
  String path)
throws NhException
{
  this( path, 0, 2);             // optFlag = 0, fileVersion = 2
}




public NhFileWriter(
  String path,
  int optFlag)                   // zero or more OPT_* bit options
throws NhException
{
  this( path, optFlag, 2);       // fileVersion = 2
}




public NhFileWriter(
  String path,
  int optFlag,                   // zero or more OPT_* bit options
  int fileVersion)
throws NhException
{
  this.path = path;
  this.optFlag = optFlag;
  this.fileVersion = fileVersion;

  fileStatus = ST_DEFINING;
  try {
    int hdfOptFlag = 0;
    if ((optFlag & OPT_OVERWRITE) != 0)
      hdfOptFlag |= HdfFileWriter.OPT_ALLOW_OVERWRITE;
    hdfFile = new HdfFileWriter( path, fileVersion, hdfOptFlag);
    rootGroup = new NhGroup( "", null, this);
      // rootName, parent, nhFileWriter
    rootGroup.hdfGroup = hdfFile.getRootGroup();
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
}




public String toString() {
  String res = String.format("path: \"%s\"  fileVersion: %d  status: %s",
    path, fileVersion, fileStatus);
  return res;
}



public void setDebugLevel( int bugs) {
  this.bugs = bugs;
  //xxx hdfFile.setDebugLevel( level);
}



public String getPath() {
  return path;
}


public int getOptFlag() {
  return optFlag;
}


public int getFileVersion() {
  return fileVersion;
}


public int getStatus() {
  return fileStatus;
}


public NhGroup getRootGroup() {
  return rootGroup;
}



public void endDefine()
throws NhException
{
  if (bugs >= 1) {
    prtf("endDefine: path: \"" + path + "\"\n");
  }
  if (fileStatus != ST_DEFINING) throwerr("already called endDefine");
  fileStatus = ST_WRITEDATA;

  ArrayList<NhGroup> groupList = new ArrayList<NhGroup>();
  ArrayList<NhVariable> variableList = new ArrayList<NhVariable>();
  findGroupsAndVars( rootGroup, groupList, variableList);

  // For each dimension:
  //   If the dimension is represented by a coordinate variable,
  //     add attrs to that hdf5 coordVar.
  //   Else create an hdf5 variable to represent the dimension.
  //
  //   In either case:
  //   Add the back-reference attrs to the hdf5 variable.
  //   My, what silly architecture you have, Hdf!

  for (NhGroup grp : groupList) {
    for (NhDimension dim : grp.dimensionList) {

      String nameAttrValue;          // value for "NAME" attribute

      try {
        if (dim.coordVar == null) {    // If not a coordinate variable
          dim.hdfDimVar = dim.parentGroup.hdfGroup.addVariable(
            dim.dimName,               // varName
            HdfGroup.DTYPE_FLOAT32,    // dtype
            0,                         // string length, incl null termination
            new int[] {dim.dimLen},    // varDims
            new Float(0),              // fillValue
            false,                     // isChunked
            0);                        // compressionLevel

          // netcdf-4.0.1/libsrc4/nc4hdf.c:
          //   #define DIM_WITHOUT_VARIABLE \
          //     "This is a netCDF dimension but not a netCDF variable."
          //   sprintf(dimscale_wo_var, "%s%10d",
          //     DIM_WITHOUT_VARIABLE, dim->len);
          nameAttrValue = String.format(
            "%s%10d\0",
            "This is a netCDF dimension but not a netCDF variable.",
            dim.dimLen);
        }

        else {     // else dim has a matching coordinate variable.
          dim.hdfDimVar = dim.coordVar.hdfVar;
          nameAttrValue = dim.dimName;
        }

        dim.hdfDimVar.addAttribute(
          "CLASS",                    // attrName
          HdfGroup.DTYPE_STRING_FIX,  // attrType
          0,                          // stgFieldLen
          "DIMENSION_SCALE\0",        // attrValue
          false);                     // isVlen

        dim.hdfDimVar.addAttribute(
          "NAME",                     // attrName
          HdfGroup.DTYPE_STRING_FIX,  // attrType
          0,                          // stgFieldLen
          nameAttrValue,              // attrValue
          false);                     // isVlen

        // Add the back-reference attrs to the hdf5 variable.
        // My, what silly architecture you have, Hdf!

        HdfGroup[] hdfRefVars = new HdfGroup[ dim.refList.size()];
        for (int ii = 0; ii < hdfRefVars.length; ii++) {
          hdfRefVars[ii] = dim.refList.get(ii).hdfVar;
        }
        dim.hdfDimVar.addAttribute(
          "REFERENCE_LIST",           // attrName
          HdfGroup.DTYPE_COMPOUND,    // attrType
          0,                          // stgFieldLen
          hdfRefVars,                 // attrValue
          false);                     // isVlen
      }
      catch( HdfException exc) {
        exc.printStackTrace();
        throwerr("caught: " + exc);
      }

    } // for each dim
  } // for each grp


  // For each variable:
  //   add DIMENSION_LIST attr
  for (NhVariable nhvar : variableList) {
    if (nhvar.rank > 0) {
      // Create matrix of dimension variables, one dimVar per row,
      // so we can make vlen DIMENSION_LIST.
      HdfGroup[][] dimVarMat = new HdfGroup[nhvar.rank][1];
      for (int ii = 0; ii < nhvar.rank; ii++) {
        dimVarMat[ii][0] = nhvar.nhDims[ii].hdfDimVar;
      }

      try {
        nhvar.hdfVar.addAttribute(
          "DIMENSION_LIST",           // attrName
          HdfGroup.DTYPE_REFERENCE,   // attrType
          0,                          // stgFieldLen
          dimVarMat,                  // attrValue
          true);                      // isVlen
      }
      catch( HdfException exc) {
        exc.printStackTrace();
        throwerr("caught: " + exc);
      }
    }
  } // for each nhvar



  try { hdfFile.endDefine(); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  // Write the data for all dimension variables in the entire tree,
  // except coordinate variables.
  writeTreeDimData( rootGroup);

} // end endDefine




// Find all groups and data variables, and fill groupList, variableList.
// Ignores dimensions.

void findGroupsAndVars(
  NhGroup grp,
  ArrayList<NhGroup> groupList,
  ArrayList<NhVariable> variableList)
{
  groupList.add( grp);
  variableList.addAll( grp.variableList);
  for (NhGroup subGrp : grp.subGroupList) {
    findGroupsAndVars( subGrp, groupList, variableList);
  }
}




// Write the data for all dimension variables in the entire tree,
// except coordinate variables.

void writeTreeDimData( NhGroup nhGroup)
throws NhException
{
  for (NhDimension nhdim : nhGroup.dimensionList) {
    if (nhdim.coordVar == null) {
      float[] dimData = new float[ nhdim.dimLen];
      try { nhdim.hdfDimVar.writeData( dimData); }
      catch( HdfException exc) {
        exc.printStackTrace();
        throwerr("caught: " + exc);
      }
    }

    for (NhGroup subGroup : nhGroup.subGroupList) {
      writeTreeDimData( subGroup);
    }
  }
} // end writeTreeDimData




public void close()
throws NhException
{
  if (bugs >= 1) {
    prtf("close: path: \"" + path + "\"\n");
  }

  if (fileStatus == ST_DEFINING)
    throwerr("must call endDefine before calling close");
  else if (fileStatus == ST_CLOSED) throwerr("file is already closed");
  else if (fileStatus != ST_WRITEDATA) throwerr("invalid fileStatus");
  fileStatus = ST_CLOSED;

  try { hdfFile.close(); }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
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
