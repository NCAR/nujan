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


//xxx redo example
/**
 * Represents an open NetCDF4 (HDF5) output file.
 *
 * Typical use:
 * <pre>
 * 
 *   int fileVersion = 2;        // either 1 or 2 (2 is recommended).
 *   NhFileWriter hfile = new NhFileWriter(
 *     outFile, NhFileWriter.OPT_OVERWRITE, fileVersion);
 * 
 *   NhGroup rootGroup = hfile.getRootGroup();
 * 
 *   int rank = 2;             // 2 dimensional data: x, y
 *   NhDimension[] nhDims = new NhDimension[ rank];
 *   nhDims[0] = rootGroup.addDimension( "xdim", 3);
 *   nhDims[1] = rootGroup.addDimension( "ydim", 4);
 * 
 *   rootGroup.addAttribute(
 *     "someName",
 *     NhVariable.TP_STRING_VAR,
 *     "some long comment");
 * 
 *   Double fillValue = new Double( -999999);
 *   int compressLevel = 0;        // compression level: 0==none, 1 - 9
 * 
 *   NhVariable humidityVar = rootGroup.addVariable(
 *     "humidity",                 // varName
 *     NhVariable.TP_DOUBLE,       // nhType
 *     nhDims,                     // varDims
 *     fillValue,
 *     compressLevel);
 * 
 *   humidityVar.addAttribute(
 *     "someUnits",
 *     NhVariable.TP_STRING_VAR,
 *     "milliKiloGrams per megaMicroGram");
 * 
 *   humidityVar.addAttribute(
 *     "someIndices",
 *     NhVariable.TP_INT,
 *     new int[] { 1, 2, 3, 5, 7, 13, 17});
 * 
 *   hfile.endDefine();
 * 
 *   // The data type must match that declared in addVariable above.
 *   // The data shape must correspond to xdim, ydim.
 *   double[][] testData = {
 *     { 11, 12, 13, 14},
 *     { 21, 22, 23, 24},
 *     { 31, 32, 33, 34}
 *   };
 * 
 *   humidityVar.writeData( testData);
 *   hfile.close();
 *
 * </pre>
 */


public class NhFileWriter {



/**
 * Specify allow overwrite of existing files for the
 * optFlag parameter in the constructor.
 */

public static final int OPT_OVERWRITE = 1;



// Define constants for fileStatus
/**
  * Before endDefine: the client may define groups, variables, and attributes.
  */
public static final int ST_DEFINING  = 1;
/**
  * After endDefine: the client may call writeData.
  */
public static final int ST_WRITEDATA = 2;
/**
  * After close: no further operations are possible.
  */
public static final int ST_CLOSED    = 3;
/**
  * Names of the ST_ status codes.
  */
public static final String[] statusNames = {
  "UNKNOWN", "DEFINING", "WRITEDATA", "CLOSED"};


String path;
int optFlag;                     // zero or more OPT_* bit options
int fileVersion;


int fileStatus;                 // one of ST_*
private HdfFileWriter hdfFile;
NhGroup rootGroup;
int bugs;




/**
 * Creates a new NetCDF4 (HDF5) output file.
 * Defaults are:
 * <ul>
 *   <li> optFlag = 0 &nbsp;&nbsp;&nbsp;  Don't overwrite existing files
 *   <li> fileVersion = 2    (Recommended)
 * </ul>
 *
 * @param path Name or path of the file to create.
 */

public NhFileWriter(
  String path)
throws NhException
{
  this( path, 0, 2);             // optFlag = 0, fileVersion = 2
}



/**
 * Creates a new NetCDF4 (HDF5) output file.
 * Defaults are:
 * <ul>
 *   <li> fileVersion = 2    (Recommended)
 * </ul>
 *
 * @param path Name or path of the file to create.
 * @param optFlag 0 or the bitwise "or" of OPT_* flags.
 */

public NhFileWriter(
  String path,
  int optFlag)                   // zero or more OPT_* bit options
throws NhException
{
  this( path, optFlag, 2);       // fileVersion = 2
}




/**
 * Creates a new NetCDF4 (HDF5) output file.
 *
 * @param path Name or path of the file to create.
 * @param optFlag 0 or the bitwise "or" of OPT_* flags.
 * @param fileVersion The output file HDF structure version, 1 or 2.
 *                   (2 is recommended).
 */

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



/**
 * Sets the verbosity of debug messages sent to stdout.
 * Recommended: 0, 1, or 2.
 */
public void setDebugLevel( int bugs) {
  this.bugs = bugs;
}



/**
 * Sets the verbosity of debug messages sent to stdout
 * by the underlying HDF5 library.
 * Recommended: 0.
 */

public void setHdfDebugLevel( int bugs) {
  hdfFile.setDebugLevel( bugs);
}


/**
 * Returns the full path name for this file.
 */

public String getPath() {
  return path;
}


/**
 * Returns the optFlag parameter specified in the constructor.
 */

public int getOptFlag() {
  return optFlag;
}


/**
 * Returns the fileVersion parameter specified in the constructor.
 */

public int getFileVersion() {
  return fileVersion;
}


/**
 * Returns the current file status, one of the ST_* constants.
 */

public int getStatus() {
  return fileStatus;
}


/**
 * Returns the root group, which is created in the constructor.
 */

public NhGroup getRootGroup() {
  return rootGroup;
}


/**
 * Ends the definition mode.  After calling endDefine,
 * the client may not create new groups, variables, or attributes.
 * After calling endDefine, the client may only call writeData
 * and close.
 */

public void endDefine()
throws NhException
{
  if (bugs >= 1) {
    prtf("NhFileWriter.endDefine: path: \"" + path + "\"\n");
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
        //
        // Oh, and skip the back refs if dimName == varName.

        NhVariable[] nhRefVars = new NhVariable[ dim.refList.size()];
        HdfGroup[] hdfRefVars = new HdfGroup[ dim.refList.size()];
        for (int ii = 0; ii < hdfRefVars.length; ii++) {
          nhRefVars[ii] = dim.refList.get(ii);
          hdfRefVars[ii] = nhRefVars[ii].hdfVar;
        }
        if (nhRefVars.length == 0
          || nhRefVars.length == 1
            && nhRefVars[0].varName.equals( dim.dimName))
        {
          prtf("xxx skip REFERENCE_LIST for NhDimension: %s", dim);
        }
        else {
          dim.hdfDimVar.addAttribute(
            "REFERENCE_LIST",           // attrName
            HdfGroup.DTYPE_COMPOUND,    // attrType
            0,                          // stgFieldLen
            hdfRefVars,                 // attrValue
            false);                     // isVlen
        }
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
      NhDimension coordDim = null;
      for (int ii = 0; ii < nhvar.rank; ii++) {
        NhDimension dim = nhvar.nhDims[ii];
        if (dim.coordVar != null) coordDim = dim;
        dimVarMat[ii][0] = dim.hdfDimVar;
      }

      // If only one dim, and it's the matching coord var, skip it.
      if (dimVarMat.length == 1
        && coordDim != null
        && coordDim.dimName.equals( nhvar.varName))
      {
        prtf("xxx skip DIMENSION_LIST for coord var: %s", nhvar);
      }
      else {       // else not coord var
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
      } // else not coord var
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




/**
 * Closes the file.  After calling close no further
 * operations are possible.
 */

public void close()
throws NhException
{
  if (bugs >= 1) {
    prtf("NhFileWriter.close: path: \"" + path + "\"\n");
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
