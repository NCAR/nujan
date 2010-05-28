
package hdfnetTest;

import hdfnet.HdfFile;
import hdfnet.HdfGroup;
import hdfnet.HdfException;


// Create a NetCDF4 compatible dataset by creating
// the variables and attributes to describe the NetCDF4 dimensions.


public class TestCustomNetcdf {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs     <int>");
  prtf("  -outFile  <fname>");
  System.exit(1);
}



public static void main( String[] args) {
  try { runit( args); }
  catch( Exception exc) {
    exc.printStackTrace();
    prtf("main: caught: %s", exc);
    System.exit(1);
  }
}


static void runit( String[] args)
throws HdfException
{
  int bugs = -1;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }

  if (bugs < 0) badparms("missing parm: -bugs");
  if (outFile == null) badparms("missing parm: -outFile");

  prtf("TestCustomNetcdf: bugs: %d", bugs);
  prtf("TestCustomNetcdf: outFile: \"%s\"", outFile);

  int fileVersion = 1;
  HdfFile hfile = new HdfFile(
    outFile, fileVersion, HdfFile.OPT_ALLOW_OVERWRITE);
  HdfGroup rootGroup = hfile.getRootGroup();


  // Create dimensions
  int numDim = 2;
  String[] dimNames = new String[ numDim];
  int[] dimLens = new int[ numDim];
  HdfGroup[] dimVars = new HdfGroup[ numDim];

  for (int ii = 0; ii < numDim; ii++) {
    dimNames[ii] = String.format("dimName%02d", ii);
    dimLens[ii] = 3 + ii;
    dimVars[ii] = rootGroup.addVariable(
      dimNames[ii],              // varName
      HdfGroup.DTYPE_FLOAT32,    // dtype
      0,                         // string length, incl null termination
      new int[] {dimLens[ii]},   // varDims
      null,                      // fillValue
      false,                     // isChunked
      0);                        // compressionLevel

    dimVars[ii].addAttribute(
      "CLASS",              // attrName
      "DIMENSION_SCALE",    // attrValue
      false,                // isVlen
      false);               // isCompoundRef


    // netcdf-4.0.1/libsrc4/nc4hdf.c:
    //   #define DIM_WITHOUT_VARIABLE "This is a netCDF dimension but not a netCDF variable."
    //   sprintf(dimscale_wo_var, "%s%10d", DIM_WITHOUT_VARIABLE, dim->len);

    String dimTitle = "This is a netCDF dimension but not a netCDF variable.";
    dimVars[ii].addAttribute(
      "NAME",               // attrName
      String.format("%s%10d", dimTitle, dimLens[ii]),      // attrValue
      false,                // isVlen
      false);               // isCompoundRef

  } // for ii


  // Create matrix of dimension variables, one dimVar per row,
  // so we can make vlen DIMENSION_LIST.
  HdfGroup[][] dimVarMat = new HdfGroup[numDim][1];
  for (int ii = 0; ii < numDim; ii++) {
    dimVarMat[ii][0] = dimVars[ii];
  }


  // Create variables with references to dimensions
  int numVar = 2;
  String[] varNames = new String[ numVar];
  HdfGroup[] dataVars = new HdfGroup[ numVar];

  for (int ii = 0; ii < numVar; ii++) {
    varNames[ii] = String.format( "dataVar%02d", ii);

    dataVars[ii] = rootGroup.addVariable(
      varNames[ii],                   // varName
      HdfGroup.DTYPE_FLOAT64,    // dtype
      0,                         // string length, incl null termination
      dimLens,                   // varDims
      new Double(999),           // fillValue
      false,                     // isChunked
      0);                        // compressionLevel

    dataVars[ii].addAttribute(
      "DIMENSION_LIST",     // attrName
      dimVarMat,            // attrValue
      true,                 // isVlen
      false);               // isCompoundRef
  } // for ii

  // Finally add the needless refs from the dimensions
  // back to the variables.
  for (int ii = 0; ii < numDim; ii++) {
    dimVars[ii].addAttribute(
      "REFERENCE_LIST",        // attrName
      dataVars,                // attrValue
      false,                   // isVlen
      true);                   // isCompoundRef
  } // for ii


  hfile.endDefine();

  // Not needed: write the data for the dimension variables
  for (int ii = 0; ii < numDim; ii++) {
    float[] dimData = new float[ 3 + ii];
    for (int jj = 0; jj < dimData.length; jj++) {
      dimData[jj] = 100 + 100 * ii + jj;
    }
    dimVars[ii].writeData( dimData);
  }


  // Create and write the data for the main variables
  for (int ii = 0; ii < numVar; ii++) {
    if (dimLens.length != 2) badparms("wrong rank");
    double[][] vdata = new double[ dimLens[0]] [dimLens[1]];
    for (int ia = 0; ia < dimLens[0]; ia++) {
      for (int ib = 0; ib < dimLens[1]; ib++) {
        vdata[ia][ib] = 1000 + 1000 * ii + 10 * ia + ib;
      }
    }
    dataVars[ii].writeData( vdata);
  }

  hfile.close();
}















static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class
