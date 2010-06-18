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


package nhPkgTest;

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

import nhPkg.NhDimension;
import nhPkg.NhException;
import nhPkg.NhFileWriter;
import nhPkg.NhGroup;
import nhPkg.NhVariable;

import hdfnet.Util;     /// rename this to HdfUtil  xxx


// Copy a general netcdf file.
//
// Uses the unidata netcdf library:
// http://www.unidata.ucar.edu/software/netcdf-java/


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
    outCdf = new NhFileWriter( outFile);
    outCdf.setDebugLevel( bugs);
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

  prtf("NhCopy.copyVarDef: nhType: " + NhVariable.nhTypeNames[nhType]);

  //xxxboolean isUnsigned = inVar.isUnsigned();
  //xxxif ((nhType == NhVariable.TP_CHAR || nhType == NhVariable.TP_STRING_VAR)
  //xxx  && ! isUnsigned)
  //xxx{
  //xxx  if (bugs >= 1) prtf("copyVarDef: var \"%s\" type is %s but is signed."
  //xxx    + "  Forcing unsigned.",
  //xxx    inVar.getName(),
  //xxx    NhVariable.nhTypeNames[nhType]);
  //xxx  isUnsigned = true;
  //xxx}

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
    // but returns a fill value as a Byte.
    // In this case, convert Byte to String.
    if (nhType == NhVariable.TP_CHAR) {
      if (! (fillValue instanceof Byte))
        throwerr("unexpected fillValue class: " + fillValue.getClass());
      byte[] bytes = new byte[] { ((Byte) fillValue).byteValue() };
      fillValue = new String( bytes);   // stg len = 1
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
  String outName,
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
  // We copy neither of these generated attributes.
  //
  // We use the _FillValue attribute, if specified, to set
  // the HDF5 variable's fillValue message in copyVarDef, above.
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
  if (atName.equals("_FillValue"))
    prtf("ignoring \"%s\" _FillValue attr: %s", outName, attr);
  else if (atName.equals("_lastModified"))
    prtf("ignoring \"%s\" _lastModified attr: %s", outName, attr);
  else if (atName.equals("_Unsigned"))
    prtf("ignoring \"%s\" _Unsigned attr: %s", outName, attr);
  else if (atName.equals("_Netcdf4Dimid"))
    prtf("ignoring \"%s\" _Netcdf4Dimid attr: %s", outName, attr);
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

    //xxxboolean isUnsigned = attr.isUnsigned();
    //xxxif (attr.isString() && ! isUnsigned) {
    //xxx  if (bugs >= 1)
    //xxx    prtf("copyAttribute: attr \"%s\" type is stg but is signed."
    //xxx      + "  Forcing unsigned.",
    //xxx      attr.getName());
    //xxx  isUnsigned = true;
    //xxx}

//xxx    if (attrValue instanceof Byte)
//xxx      attrValue = new byte[] { ((Byte) attrValue).byteValue() };
//xxx    else if (attrValue instanceof Short)
//xxx      attrValue = new short[] { ((Short) attrValue).shortValue() };
//xxx    else if (attrValue instanceof Integer)
//xxx      attrValue = new int[] { ((Integer) attrValue).intValue() };
//xxx    else if (attrValue instanceof Long)
//xxx      attrValue = new long[] { ((Long) attrValue).longValue() };
//xxx    else if (attrValue instanceof Float)
//xxx      attrValue = new float[] { ((Float) attrValue).floatValue() };
//xxx    else if (attrValue instanceof Double)
//xxx      attrValue = new double[] { ((Double) attrValue).doubleValue() };

    if (outObj instanceof NhGroup) {
      NhGroup outGrp = (NhGroup) outObj;
      outGrp.addAttribute(
        atName,
        nhType,
        attrValue,
        false);          // isVlen
    }
    else if (outObj instanceof NhVariable) {
      NhVariable outVar = (NhVariable) outObj;
      outVar.addAttribute(
        atName,
        nhType,
        attrValue,
        false);          // isVlen
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
  } // if special case for Strings

  // Special case for scalar data
  else if (arr.getRank() == 0
    || arr.getRank() == 1 && arr.getShape()[0] == 1)
  {
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

