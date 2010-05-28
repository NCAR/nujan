
package ncHdf;

import java.util.ArrayList;

import hdfnet.HdfException;
import hdfnet.HdfGroup;


public class NcDimension {

String dimName;
int dimLen;
NcGroup parentGroup;


HdfGroup hdfVar;

// Variables that use this NcDimension
ArrayList<NcVariable> refList = new ArrayList<NcVariable>();



NcDimension(
  String dimName,
  int dimLen,
  NcGroup parentGroup)
throws NcException
{
  this.dimName = dimName;
  this.dimLen = dimLen;
  this.parentGroup = parentGroup;

  try {
    hdfVar = parentGroup.hdfGroup.addVariable(
      dimName,                   // varName
      HdfGroup.DTYPE_FLOAT32,    // dtype
      0,                         // string length, incl null termination
      new int[] {dimLen},        // varDims
      null,                      // fillValue
      false,                     // isChunked
      0);                        // compressionLevel

    hdfVar.addAttribute(
      "CLASS",              // attrName
      "DIMENSION_SCALE",    // attrValue
      false,                // isVlen
      false);               // isCompoundRef


    // netcdf-4.0.1/libsrc4/nc4hdf.c:
    //   #define DIM_WITHOUT_VARIABLE "This is a netCDF dimension but not a netCDF variable."
    //   sprintf(dimscale_wo_var, "%s%10d", DIM_WITHOUT_VARIABLE, dim->len);

    String dimTitle = "This is a netCDF dimension but not a netCDF variable.";
    hdfVar.addAttribute(
      "NAME",               // attrName
      String.format("%s%10d", dimTitle, dimLen),      // attrValue
      false,                // isVlen
      false);               // isCompoundRef
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }
} // end constructor



static void throwerr( String msg, Object... args)
throws NcException
{
  throw new NcException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}



} // end class
