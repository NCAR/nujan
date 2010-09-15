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


package edu.ucar.ral.nujan.netcdfTest;

import java.io.IOException;
import java.util.ArrayList;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import edu.ucar.ral.nujan.netcdf.NhDimension;
import edu.ucar.ral.nujan.netcdf.NhException;
import edu.ucar.ral.nujan.netcdf.NhFileWriter;
import edu.ucar.ral.nujan.netcdf.NhGroup;
import edu.ucar.ral.nujan.netcdf.NhVariable;


/**
 * Command line tool to copy a NetCDF4 file (which uses HDF5 format).
 * For usage info, invoke NhCopy with no parameters.
 *
 * Internally NhCopy uses
 * <ul>
 *   <li> the Unidata NetCDF4 Java reader from
 *        http://www.unidata.ucar.edu/software/netcdf-java/
 *   <li> our Java writer, {@link edu.ucar.ral.nujan.netcdf.NhFileWriter}
 * <ul>
 */

public class NhCopy {


public static void main( String[] args) {
  try {
    copyIt( args);
  }
  catch( Exception exc) {
    exc.printStackTrace();
    prtf("main: caught: %s", exc);
    System.exit(1);
  }
}




static void badparms( String msg) {
  prtf("Error: " + msg);
  prtf("Parms:");
  prtf("  -bugs       debug level.  Default is 0.");
  prtf("  -compress   compressionLevel for outFile.  0: none,  9: max");
  prtf("  -inFile     input file name.");
  prtf("  -outFile    output file name.");
  prtf("");
  prtf("Example:");
  prtf("java -cp tdcls:netcdfAll-4.0.jar testpk.NhCopy"
    + " -compress 0 -inFile ta.nc -outFile tb.nc");
  System.exit(1);
}







static void copyIt( String[] args)
throws NhException
{
  int bugs = 0;
  int compressionLevel = -1;
  String inFile = null;
  String outFile = null;
  int iarg = 0;
  while (iarg < args.length) {
    String key = args[iarg++];
    if (iarg >= args.length) badparms("no value for the last key");
    String val = args[iarg++];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-compress")) {
      compressionLevel = Integer.parseInt( val);
      if (compressionLevel < 0 || compressionLevel > 9)
        badparms("invalid compress: " + compressionLevel);
    }
    else if (key.equals("-inFile")) inFile = val;
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unknown parm: \"" + key + "\"");
  }
  if (compressionLevel < 0) badparms("parm not specified: -compress");
  if (inFile == null) badparms("parm not specified: -inFile");
  if (outFile == null) badparms("parm not specified: -outFile");

  if (bugs >= 1) {
    prtf("copyIt: compress: %d", compressionLevel);
    prtf("copyIt: inFile: \"%s\"", inFile);
    prtf("copyIt: outFile: \"%s\"", outFile);
  }

  NetcdfFile inCdf = null;
  NhFileWriter outCdf = null;
  try {
    inCdf = NetcdfFile.open( inFile);
    outCdf = new NhFileWriter(
      outFile,
      NhFileWriter.OPT_OVERWRITE,
      bugs,          // Netcdf debug level
      bugs,          // HDF5 debug level
      0);            // utcModTime: use current time.

    Group inGroup = inCdf.getRootGroup();
    NhGroup outGroup = outCdf.getRootGroup();

    // pass 1: define vars
    copyGroup( 1, compressionLevel, inGroup, outGroup, bugs);
    outCdf.endDefine();

    // pass 2: copy data
    copyGroup( 2, compressionLevel, inGroup, outGroup, bugs);
    inCdf.close();
    inCdf = null;
    outCdf.close();
    outCdf = null;
  }
  catch( Exception exc) {
    exc.printStackTrace();
    badparms("const: caught: " + exc);
  }
} // end const






static void copyGroup(
  int pass,                  // 1: define vars;  2: copy data
  int compressionLevel,      // 0: no compression;  9: max compression
  Group inGroup,
  NhGroup outGroup,
  int bugs)
throws NhException
{
  if (bugs >= 1) prtf("copyGroup: pass: %d  inGroup: %s", pass, inGroup);
  if (pass == 1) {
    for (Attribute attr : inGroup.getAttributes()) {
      copyAttribute( attr, outGroup.getName(), outGroup, bugs);
    }
    for (Dimension dim : inGroup.getDimensions()) {
      outGroup.addDimension(
        dim.getName(),
        dim.getLength());
    }
  } // if pass == 1

  for (Variable var : inGroup.getVariables()) {
    if (bugs >= 2) prtf("NhCopy testaa 1: pass: "
      + pass + "  var: " + var.getName());
    if (pass == 1) copyVarDef( compressionLevel, var, outGroup, bugs);
    else copyData( var, outGroup, bugs);
  }

  for (Group inSub : inGroup.getGroups()) {
    NhGroup outSub;
    if (pass == 1) outSub = outGroup.addGroup( inSub.getShortName());
    else outSub = outGroup.findSubGroup( inSub.getShortName());

    copyGroup( pass, compressionLevel, inSub, outSub, bugs);
  }
  
} // end copyGroup








static void copyVarDef(
  int compressionLevel,      // 0: no compression;  9: max compression
  Variable inVar,
  NhGroup outGroup,
  int bugs)
throws NhException
{
  if (bugs >= 1) {
    prtf("copyVarDef: inVar: %s", inVar);
    prtf("  name: \"%s\"", inVar.getName());
    prtf("  type: %s", inVar.getDataType());
    prtf("  isUnsigned: %s", inVar.isUnsigned());
    prtf("  isScalar: %s", inVar.isScalar());
    prtf("  rank: %d", inVar.getRank());
    String tmsg = "";
    for (int ii : inVar.getShape()) {
      tmsg += "  " + ii;
    }
    prtf("  shape: %s", tmsg);
  }
  // Translate their type to our type
  DataType tp = inVar.getDataType();
  int nhType = 0;
  if (tp == DataType.BYTE) {
    if (inVar.isUnsigned()) nhType = NhVariable.TP_UBYTE;
    else nhType = NhVariable.TP_SBYTE;
  }
  else if (tp == DataType.SHORT) nhType = NhVariable.TP_SHORT;
  else if (tp == DataType.INT) nhType = NhVariable.TP_INT;
  else if (tp == DataType.LONG) nhType = NhVariable.TP_LONG;
  else if (tp == DataType.FLOAT) nhType = NhVariable.TP_FLOAT;
  else if (tp == DataType.DOUBLE) nhType = NhVariable.TP_DOUBLE;
  else if (tp == DataType.CHAR) nhType = NhVariable.TP_CHAR;
  else if (tp == DataType.STRING) nhType = NhVariable.TP_STRING_VAR;
  else throwerr("unknown type \"%s\" for variable \"%s\"",
    tp, inVar.getName());

  prtf("NhCopy.copyVarDef: name: \"%s\"  type: %s  nhType: %s",
    inVar.getName(), inVar.getDataType(), NhVariable.nhTypeNames[nhType]);

  // Make a list of our matching dimensions.
  ArrayList<NhDimension> nhDimList = new ArrayList<NhDimension>();
  for (Dimension dim : inVar.getDimensions()) {
    // Search outGroup and it's ancestors for a NhDimension
    // having the same name as dim.
    NhDimension nhDim = null;
    NhGroup grp = outGroup;
    while (grp != null) {
      for (NhDimension nhd : grp.getDimensions()) {
        if (nhd.getName().equals( dim.getName())) {
          nhDim = nhd;
          break;
        }
      }
      if (nhDim != null) break;
      grp = grp.getParentGroup();
    }
    if (nhDim == null)
      throwerr("cannot find dimension \"%s\" for variable \"%s\"",
        dim.getName(), inVar.getName());
    nhDimList.add( nhDim);
  } // for each dim

  NhDimension[] nhDims = nhDimList.toArray( new NhDimension[0]);

  // Get the fill value, if specified
  Object fillValue = null;
  Attribute fillAttr = inVar.findAttribute("_FillValue");
  if (fillAttr != null) {
    fillValue = getAttrValue( fillAttr, bugs);

    // Unfortunately NetCDF stores type "char" as HDF5 strings of length 1,
    // but returns a fill value as a Byte or byte[].
    // In this case, convert Byte to String.
    if (nhType == NhVariable.TP_CHAR) {
      if (fillValue instanceof Byte) {
        byte[] bytes = new byte[] { ((Byte) fillValue).byteValue() };
        fillValue = new String( bytes);   // stg len = 1
      }
      else if (fillValue instanceof byte[]) {
        byte[] bytes = (byte[]) fillValue;
        if (bytes.length != 1)
          throwerr("unexpected fillValue class: " + fillValue.getClass());
        fillValue = new String( bytes);   // stg len = 1
      }
      else throwerr("unexpected fillValue class: " + fillValue.getClass());
    }

    // Sometimes NetCDF returns a 1-dim array for fillValue,
    // but we need a scalar
    if (fillValue instanceof byte[]) {
      byte[] vals = (byte[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Byte( vals[0]);
    }
    else if (fillValue instanceof short[]) {
      short[] vals = (short[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Short( vals[0]);
    }
    else if (fillValue instanceof int[]) {
      int[] vals = (int[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Integer( vals[0]);
    }
    else if (fillValue instanceof long[]) {
      long[] vals = (long[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Long( vals[0]);
    }
    else if (fillValue instanceof float[]) {
      float[] vals = (float[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Float( vals[0]);
    }
    else if (fillValue instanceof double[]) {
      double[] vals = (double[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Double( vals[0]);
    }
    else if (fillValue instanceof char[]) {
      char[] vals = (char[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = new Character( vals[0]);
    }
    else if (fillValue instanceof Object[]) {
      Object[] vals = (Object[]) fillValue;
      if (vals.length != 1)
        throwerr("invalid fillValue for inVar: " + inVar);
      fillValue = vals[0];
      if (fillValue instanceof Object[])
        throwerr("invalid fillValue for inVar: " + inVar);
    }
  }

  NhVariable outVar = outGroup.addVariable(
    inVar.getShortName(),
    nhType,
    nhDims,
    fillValue,
    compressionLevel);

  // Copy attributes
  for (Attribute attr : inVar.getAttributes()) {
    copyAttribute( attr, outVar.getName(), outVar, bugs);
  }

} // end copyVarDef






static void copyAttribute(
  Attribute attr,
  String outName,    // name of outObj, for error msgs
  Object outObj,     // outObj is either NhGroup or NhVariable
  int bugs)
throws NhException
{
  if (bugs >= 1) {
    prtf("copyAttribute: attr: %s", attr);
    prtf("  name: \"%s\"", attr.getName());
    prtf("  type: %s", attr.getDataType());
    prtf("  getLength: %s", attr.getLength());
    if (attr.getLength() == 1) prtf("  len is 1: SCALAR");
    prtf("  isString: %s", attr.isString());
  }
  // Unidata's Netcdf-java reader can add some extra attributes:
  //   "_FillValue", based on the HDF5 fillValue message.
  //      The _FillValue attribute is generated only if:
  //         The dataspace is not a scalar, and
  //         The datatype is not String.
  //
  //   "_lastModified", based on the HDF5 modTime message.
  //
  //   "_Unsigned", for unsigned byte or other unsigned data.
  //
  // Others:
  //   "_CoordinateAxes"
  //   "_CoordinateAxisType"
  //   "_CoordinateModelRunDate"
  //   "_CoordinateZisLayer"
  //   "_CoordinateZisPositive"
  //   "_Netcdf4Dimid"

  // We omit all of these artificial attributes.
  // According to Unidata:
  //    Subject: [netCDFJava #BYC-698785]: synthetic attributes
  //    Date: Mon, 21 Jun 2010 16:03:54 -0600
  //    Attributes beginning with  underscore are supposed to
  //    be reserved for Unidata use only and can be recognized
  //    by that underscore.
  //
  // We use the _FillValue attribute, if specified, to set
  // the HDF5 variable's fillValue message in copyVarDef, above.
  // But we don't create a new attribute for it.
  //
  // We ignore the _lastModified attribute, and the HDF5 software
  // sets all variable's modTime message to the current time.
  //
  //
  // See: nc2/iosp/hdf5/H5header.java
  //    private void processSystemAttributes(...) {
  //      for (HeaderMessage mess : messages) {
  //        if (mess.mtype == MessageType.LastModified) {
  //          MessageLastModified m = (MessageLastModified) mess.messData;
  //          Date d = new Date((long) m.secs * 1000);
  //          attributes.add(new Attribute(
  //            "_lastModified", formatter.toDateTimeStringISO(d)));
  //    } ... } ... }

  String atName = attr.getName();

  if (atName.equals("_lastModified")
    || atName.equals("_Netcdf4Dimid"))
  {
    prtf("Ignoring internal attribute for variable or group \"%s\"."
      + "  Attribute: %s", outName, attr);
  }

  else {

    DataType tp = attr.getDataType();
    int nhType = 0;
    if (tp == DataType.BYTE) {
      if (attr.isUnsigned()) nhType = NhVariable.TP_UBYTE;
      else nhType = NhVariable.TP_SBYTE;
    }
    else if (tp == DataType.SHORT) nhType = NhVariable.TP_SHORT;
    else if (tp == DataType.INT) nhType = NhVariable.TP_INT;
    else if (tp == DataType.LONG) nhType = NhVariable.TP_LONG;
    else if (tp == DataType.FLOAT) nhType = NhVariable.TP_FLOAT;
    else if (tp == DataType.DOUBLE) nhType = NhVariable.TP_DOUBLE;
    else if (tp == DataType.CHAR) nhType = NhVariable.TP_CHAR;
    else if (tp == DataType.STRING) nhType = NhVariable.TP_STRING_VAR;
    else throwerr("unknown type for attribute \"%s\".  Type: %s",
      atName, tp);

    Object attrValue = getAttrValue( attr, bugs);

    if (outObj instanceof NhGroup) {
      NhGroup outGrp = (NhGroup) outObj;
      outGrp.addAttribute(
        atName,
        nhType,
        attrValue);
    }
    else if (outObj instanceof NhVariable) {
      NhVariable outVar = (NhVariable) outObj;
      outVar.addAttribute(
        atName,
        nhType,
        attrValue);
    }
    else throwerr("invalid outObj: " + outObj);
  } // if not "_lastModified"
} // end copyAttribute







static void copyData(
  Variable inVar,
  NhGroup outGroup,
  int bugs)
throws NhException
{
  if (bugs >= 1) {
    prtf("copyData: inVar: %s", inVar);
  }
  // Find our corresponding variable
  NhVariable nhVar = null;
  for (NhVariable nhv : outGroup.getVariables()) {
    if (nhv.getName().equals( inVar.getShortName())) {
      nhVar = nhv;
      break;
    }
  }
  if (nhVar == null) throwerr("nhVar not found");

  Array arr = null;
  try { arr = inVar.read(); }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: " + exc);
  }

  Object rawData = decodeArray( arr, bugs);

  nhVar.writeData( rawData);
}






static Object getAttrValue( Attribute attr, int bugs)
throws NhException
{
  if (bugs >= 1) prtf("getAttrValue: attr: %s", attr);
  Object attrValue = null;
  // Special case for Strings.
  DataType atType = attr.getDataType();
  if (attr.isString()) {
    int alen = attr.getLength();
    if (alen == 1) {
      attrValue = attr.getStringValue();
    }
    else {
      String[] stgs = new String[ alen];
      for (int ii = 0; ii < alen; ii++) {
        stgs[ii] = attr.getStringValue(ii);
      }
      attrValue = stgs;
    }
  }
  else {                    // else not String
    Array arr = attr.getValues();
    attrValue = decodeArray( arr, bugs);
  }
  if (bugs >= 1) prtf("  getAttrValue: return attrValue: %s  cls: %s",
    attrValue, attrValue.getClass());
  return attrValue;
}





static Object decodeArray( Array arr, int bugs)
throws NhException
{
  if (bugs >= 1) {
    int rank = arr.getRank();
    int[] shape = arr.getShape();

    // The following prtf causes the code in ucar.ma2.Array.toString
    // to fail with ArrayIndexOutOfBoundsException on large datasets.
    ///prtf("decodeArray: arr: %s", arr);

    prtf("  decodeArray: getElementType: %s", arr.getElementType());
    prtf("    getRank: %d", rank);
    for (int ii = 0; ii < rank; ii++) {
      prtf("    shape[%d]: %s", ii, shape[ii]);
    }
    if (rank == 1) {
      for (int ii = 0; ii < shape[0]; ii++) {
        Object obj = arr.getObject( ii);
        prtf("    ele %d: %s  class: %s", ii, obj, obj.getClass().getName());
      }
    }
    else if (rank == 2) {
      Index index = arr.getIndex();
      for (int ii = 0; ii < shape[0]; ii++) {
        for (int jj = 0; jj < shape[1]; jj++) {
          index.set( ii, jj);
          Object obj = arr.getObject( index);
          prtf("    ele %d %d: %s  class: %s",
            ii, jj, obj, obj.getClass().getName());
        }
      }
    }
  }
  
  Class eleType = arr.getElementType();
  Object resObj = null;

  // Special case for Strings.
  if (eleType == "".getClass()) {        // if Strings
    resObj = decodeStringArray( arr, bugs);
  }

  // Special case for scalar data of primitives.
  // Netcdf Array.copyToNDJavaArray dies on primitives of rank 0.
  else if (arr.getRank() == 0 && arr.getElementType().isPrimitive()) {
    Class cls = arr.getElementType();
    if (cls == Byte.TYPE) resObj = new Byte( arr.getByte( 0));
    else if (cls == Short.TYPE) resObj = new Short( arr.getShort( 0));
    else if (cls == Integer.TYPE) resObj = new Integer( arr.getInt( 0));
    else if (cls == Long.TYPE) resObj = new Long( arr.getLong( 0));
    else if (cls == Float.TYPE) resObj = new Float( arr.getFloat( 0));
    else if (cls == Double.TYPE) resObj = new Double( arr.getDouble( 0));
    else if (cls == Character.TYPE) resObj = new Character( arr.getChar( 0));
    else if (cls == "".getClass()) resObj = arr.getObject( 0); // String
    else throwerr("unknown cls: " + cls);
  }

  else {
    resObj = arr.copyToNDJavaArray();
  }
  return resObj;
} // end decodeArray






static Object decodeStringArray( Array arr, int bugs)
throws NhException
{
  Class eleType = arr.getElementType();
  if (eleType != "".getClass())        // if not Strings
    throwerr("bad type for decodeStringArray");

  Object resObj = null;
  int rank = arr.getRank();
  int[] shape = arr.getShape();

  Index index = arr.getIndex();
  if (rank == 0) resObj = arr.getObject( 0);
  else if (rank == 1) {
    String[] stgs = new String[shape[0]];
    for (int ia = 0; ia < shape[0]; ia++) {
      index.set( ia);
      stgs[ia] = (String) arr.getObject( index);
    }
    resObj = stgs;
  }
  else if (rank == 2) {
    String[][] stgs = new String[shape[0]][shape[1]];
    for (int ia = 0; ia < shape[0]; ia++) {
      for (int ib = 0; ib < shape[1]; ib++) {
        index.set( ia, ib);
        stgs[ia][ib] = (String) arr.getObject( index);
        //stgs[ia][ib] = (String) arr.getObject( ia * shape[1] + ib);
      }
    }
    resObj = stgs;
  }
  else if (rank == 3) {
    String[][][] stgs = new String[shape[0]][shape[1]][shape[2]];
    for (int ia = 0; ia < shape[0]; ia++) {
      for (int ib = 0; ib < shape[1]; ib++) {
        for (int ic = 0; ic < shape[2]; ic++) {
          index.set( ia, ib, ic);
          stgs[ia][ib][ic] = (String) arr.getObject( index);
        }
      }
    }
    resObj = stgs;
  }
  else if (rank == 4) {
    String[][][][] stgs = new String[shape[0]][shape[1]][shape[2]][shape[3]];
    for (int ia = 0; ia < shape[0]; ia++) {
      for (int ib = 0; ib < shape[1]; ib++) {
        for (int ic = 0; ic < shape[2]; ic++) {
          for (int id = 0; id < shape[3]; id++) {
            index.set( ia, ib, ic, id);
            stgs[ia][ib][ic][id] = (String) arr.getObject( index);
          }
        }
      }
    }
    resObj = stgs;
  }
  else if (rank == 5) {
    String[][][][][] stgs = new String[shape[0]][shape[1]][shape[2]]
      [shape[3]][shape[4]];
    for (int ia = 0; ia < shape[0]; ia++) {
      for (int ib = 0; ib < shape[1]; ib++) {
        for (int ic = 0; ic < shape[2]; ic++) {
          for (int id = 0; id < shape[3]; id++) {
            for (int ie = 0; ie < shape[4]; ie++) {
              index.set( ia, ib, ic, id, ie);
              stgs[ia][ib][ic][id][ie] = (String) arr.getObject( index);
            }
          }
        }
      }
    }
    resObj = stgs;
  }
  else if (rank == 6) {
    String[][][][][][] stgs = new String[shape[0]][shape[1]][shape[2]]
      [shape[3]][shape[4]][shape[5]];
    for (int ia = 0; ia < shape[0]; ia++) {
      for (int ib = 0; ib < shape[1]; ib++) {
        for (int ic = 0; ic < shape[2]; ic++) {
          for (int id = 0; id < shape[3]; id++) {
            for (int ie = 0; ie < shape[4]; ie++) {
              for (int ig = 0; ig < shape[5]; ig++) {
                index.set( ia, ib, ic, id, ie, ig);
                stgs[ia][ib][ic][id][ie][ig]
                  = (String) arr.getObject( index);
              }
            }
          }
        }
      }
    }
    resObj = stgs;
  }
  else if (rank == 7) {
    String[][][][][][][] stgs = new String[shape[0]][shape[1]][shape[2]]
      [shape[3]][shape[4]][shape[5]][shape[6]];
    for (int ia = 0; ia < shape[0]; ia++) {
      for (int ib = 0; ib < shape[1]; ib++) {
        for (int ic = 0; ic < shape[2]; ic++) {
          for (int id = 0; id < shape[3]; id++) {
            for (int ie = 0; ie < shape[4]; ie++) {
              for (int ig = 0; ig < shape[5]; ig++) {
                for (int ih = 0; ih < shape[6]; ih++) {
                  index.set( ia, ib, ic, id, ie, ig, ih);
                  stgs[ia][ib][ic][id][ie][ig][ih]
                    = (String) arr.getObject( index);
                }
              }
            }
          }
        }
      }
    }
    resObj = stgs;
  }
  else throwerr("unknown rank");

  return resObj;
} // end decodeStringArray






static void throwerr( String msg, Object... args)
throws NhException
{
  throw new NhException( String.format( msg, args));
}





static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class

