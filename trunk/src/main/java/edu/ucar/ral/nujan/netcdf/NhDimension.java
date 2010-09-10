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

import edu.ucar.ral.nujan.hdf.HdfException;
import edu.ucar.ral.nujan.hdf.HdfGroup;


/**
 * Represents one dimension for an NhVariable.
 */

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
} // end constructor





public String toString() {
  String res = String.format("name: \"%s\"  length: %d", dimName, dimLen);
  return res;
}



/**
 * Returns the name of this dimension.
 */

public String getName() {
  return dimName;
}


/**
 * Returns the numeric length of this dimension.
 */

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
