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
import hdfnet.HdfGroup;


public class NhDimension {

String dimName;
int dimLen;
NhGroup parentGroup;

// If this dimension is represented by a coordinate variable,
// note it.
NhVariable coordVar = null;


HdfGroup hdfDimVar;     // used by NhFile.endDefine().

// Variables that use this NhDimension
ArrayList<NhVariable> refList = new ArrayList<NhVariable>();



NhDimension(
  String dimName,
  int dimLen,
  NhGroup parentGroup)
throws NhException
{
  this.dimName = dimName;
  this.dimLen = dimLen;
  this.parentGroup = parentGroup;

//xxx del:
///  try {
///    hdfVar = parentGroup.hdfGroup.addVariable(
///      dimName,                   // varName
///      HdfGroup.DTYPE_FLOAT32,    // dtype
///      0,                         // string length, incl null termination
///      new int[] {dimLen},        // varDims
///      null,                      // fillValue
///      false,                     // isChunked
///      0);                        // compressionLevel
///
///    hdfVar.addAttribute(
///      "CLASS",              // attrName
///      "DIMENSION_SCALE",    // attrValue
///      false,                // isVlen
///      false);               // isCompoundRef
///
///
///    // netcdf-4.0.1/libsrc4/nc4hdf.c:
///    //   #define DIM_WITHOUT_VARIABLE "This is a netCDF dimension but not a netCDF variable."
///    //   sprintf(dimscale_wo_var, "%s%10d", DIM_WITHOUT_VARIABLE, dim->len);
///
///    String dimTitle = "This is a netCDF dimension but not a netCDF variable.";
///    hdfVar.addAttribute(
///      "NAME",               // attrName
///      String.format("%s%10d", dimTitle, dimLen),      // attrValue
///      false,                // isVlen
///      false);               // isCompoundRef
///  }
///  catch( HdfException exc) {
///    exc.printStackTrace();
///    throwerr("caught: " + exc);
///  }

} // end constructor





public String toString() {
  String res = String.format("name: \"%s\"  length: %d", dimName, dimLen);
  return res;
}



public String getName() {
  return dimName;
}


public int getLength() {
  return dimLen;
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
