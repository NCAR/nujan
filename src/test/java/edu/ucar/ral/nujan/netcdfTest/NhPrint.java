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


package edu.ucar.ral.nujan.netcdfTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


// Print a general netcdf file.
//
// Uses the unidata netcdf library:
// http://www.unidata.ucar.edu/software/netcdf-java/


public class NhPrint {


static void badparms( String msg) {
  prtf("Error: " + msg);
  prtf("Parms:");
  prtf("  -bugs       debug level.  Default is 0.");
  prtf("  -sort       y/n.  Sort groups, attributes, and variables.");
  prtf("  -inFile     input file name.");
  prtf("");
  prtf("Example:");
  prtf("java -cp x/classes:x/netcdfAll-4.1.jar edu.ucar.ral.nujan.netcdfTest.NhPrint -bugs 0 -sort y -inFile tempa.nc");
  System.exit(1);
}




public static void main( String[] args) {
  try {
    runIt( args);
  }
  catch( Exception exc) {
    exc.printStackTrace();
    prtf("main: caught: %s", exc);
    System.exit(1);
  }
}




static void runIt( String[] args)
throws Exception
{
  int bugs = 0;
  boolean sortFlag = false;
  String inFile = null;
  int iarg = 0;
  while (iarg < args.length) {
    String key = args[iarg++];
    if (iarg >= args.length) badparms("no value for the last key");
    String val = args[iarg++];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-sort")) sortFlag = parseBoolean( key, val);
    else if (key.equals("-inFile")) inFile = val;
    else badparms("unknown parm: \"" + key + "\"");
  }
  if (inFile == null) badparms("parm not specified: -inFile");

  prtf("inFile: \"%s\"", inFile);

  NetcdfFile inCdf = null;
  try {
    inCdf = NetcdfFile.open( inFile);
    Group inGroup = inCdf.getRootGroup();

    printGroup( inGroup, sortFlag, bugs, 0);
    inCdf.close();
    inCdf = null;
  }
  catch( Exception exc) {
    exc.printStackTrace();
    badparms("const: caught: " + exc);
  }
} // end runIt






static void printGroup(
  Group inGroup,
  boolean sortFlag,
  int bugs,
  int indent)
throws Exception
{
  prtIndent( indent, "");
  prtIndent( indent, "Group: name: " + inGroup.getName());
  prtIndent( indent, "Group: shortName: " + inGroup.getShortName());

  Attribute[] attrs = inGroup.getAttributes().toArray( new Attribute[0]);
  if (sortFlag) {
    Arrays.sort( attrs, new Comparator<Attribute>() {
      public int compare( Attribute attra, Attribute attrb) {
        return attra.getName().compareTo( attrb.getName());
      }
    });
  }
  for (Attribute attr : attrs) {
    printAttr( "global", attr, indent);
  }

  Dimension[] dims = inGroup.getDimensions().toArray( new Dimension[0]);
  if (sortFlag) {
    Arrays.sort( dims, new Comparator<Dimension>() {
      public int compare( Dimension dima, Dimension dimb) {
        return dima.getName().compareTo( dimb.getName());
      }
    });
  }
  for (Dimension dim : dims) {
    prtIndent( indent, "dim: %s", dim);
  }

  Variable[] vars = inGroup.getVariables().toArray( new Variable[0]);
  if (sortFlag) {
    Arrays.sort( vars, new Comparator<Variable>() {
      public int compare( Variable vara, Variable varb) {
        return vara.getName().compareTo( varb.getName());
      }
    });
  }
  for (Variable var : vars) {
    prtIndent( indent, "");
    prtIndent( indent, "var: %s", var);
    prtIndent( indent, "getName: %s", var.getName());
    prtIndent( indent, "getShortName: %s", var.getShortName());
    prtIndent( indent, "DataType: %s", var.getDataType());
    prtIndent( indent, "isUnsigned: %s", var.isUnsigned());
    for (Dimension dim : var.getDimensions()) {
      prtIndent( indent, "dim: %s", dim);
    }
    prtIndent( indent, "isScalar: %s", var.isScalar());
    prtIndent( indent, "isVariableLength: %s", var.isVariableLength());
    for (Attribute attr : var.getAttributes()) {
      printAttr( "var " + var.getShortName(), attr, indent);
    }
    Array arr = var.read();
    prtIndent( indent, "data:");
    printArray( indent + 1, arr);
  }

  Group[] subGroups = inGroup.getGroups().toArray( new Group[0]);
  if (sortFlag) {
    Arrays.sort( subGroups, new Comparator<Group>() {
      public int compare( Group suba, Group subb) {
        return suba.getName().compareTo( subb.getName());
      }
    });
  }
  for (Group inSub : subGroups) {
    printGroup( inSub, sortFlag, bugs, indent + 1);
  }
  
} // end printGroup




static void printAttr(
  String tag,
  Attribute attr,
  int indent)
throws Exception
{
  prtIndent( indent, "Attr for: %s  getName: \"%s\"",
    tag, attr.getName());
  prtIndent( indent + 1, "attr.getDataType(): %s", attr.getDataType());
  prtIndent( indent + 1, "attr.isUnsigned(): %s", attr.isUnsigned());
  prtIndent( indent + 1, "attr.isString(): %s", attr.isString());
  prtIndent( indent + 1, "attr.getLength(): %d", attr.getLength());
  prtIndent( indent + 1, "attr.getStringValue(): %s",
    attr.getStringValue());
  prtIndent( indent + 1, "attr value:");
  Array arr = attr.getValues();
  printArray( indent + 2, arr);
  prtIndent( indent + 1, "attr.toString: %s", attr);
}







static void printArray( int indent, Array arr)
{
  prtIndent( indent, "arr: %s", arr);
  prtIndent( indent, "arr.getRank: %s", arr.getRank());
  prtIndent( indent, "arr.getShape: %s", formatInts( arr.getShape()));
  prtIndent( indent, "arr.getElementType: %s", arr.getElementType());
  prtIndent( indent, "arr.getSize: %s", arr.getSize());
  for (int ii = 0; ii < arr.getSize(); ii++) {
    Object obj = arr.getObject( ii);
    prtIndent( indent, "arr.getObject(%d): %s  cls: %s",
      ii, obj, obj.getClass().getName());
  }
  Object storage = arr.getStorage();
  prtIndent( indent, "arr.getStorage() type: %s", storage.getClass());
  if (storage instanceof Object[]) {
    Object[] vals = (Object[]) storage;
    prtIndent( indent, "arr.getStorage() len: %d", vals.length);
    for (int ii = 0; ii < vals.length; ii++) {
      Object val = vals[ii];
      prtIndent( indent, "arr.getStorage()[%d] type: %s  value: %s",
        ii, val.getClass(), val);
    }
  }
  else if (storage instanceof char[]) {
    char[] vals = (char[]) storage;
    prtIndent( indent, "arr.getStorage() len: %d", vals.length);
    for (int ii = 0; ii < vals.length; ii++) {
      char val = vals[ii];
      prtIndent( indent, "arr.getStorage()[%d] value: %d  '%c'",
        ii, (int) val, val);
    }
  }
  else if (storage instanceof short[]) {
    short[] vals = (short[]) storage;
    prtIndent( indent, "arr.getStorage() len: %d", vals.length);
    for (int ii = 0; ii < vals.length; ii++) {
      short val = vals[ii];
      prtIndent( indent, "arr.getStorage()[%d] value: %d",
        ii, val);
    }
  }

  Class eleType = arr.getElementType();
  if (eleType != "".getClass()) {      // if not String
    prtIndent( indent, "arr.copyTo1DJavaArray() type: %s",
      arr.copyTo1DJavaArray().getClass());
    // Netcdf Array.copyToNDJavaArray dies on primitives of rank 0.
    if (! (arr.getRank() == 0 && eleType.isPrimitive())) {
      prtIndent( indent, "arr.copyToNDJavaArray() type: %s",
        arr.copyToNDJavaArray().getClass());
    }
  }
}





static boolean parseBoolean(
  String key,
  String val)
{
  boolean bres = false;
  if (val.equals("n")) bres = false;
  else if (val.equals("y")) bres = true;
  else badparms("invalid value for parm " + key + ": \"" + val + "\"");
  return bres;
}






static String formatInts( int[] vals) {
  String res = "";
  for (int ii : vals) {
    res += "  " + ii;
  }
  return res;
}




static void throwerr( String msg, Object... args)
throws Exception
{
  throw new Exception( String.format( msg, args));
}




static void prtIndent( int indent, String msg, Object... args) {
  String indentStg = "";
  for (int ii = 0; ii < indent; ii++) {
    indentStg += "  ";
  }
  System.out.printf( indentStg + msg + "\n", args);
}


static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}



} // end class

