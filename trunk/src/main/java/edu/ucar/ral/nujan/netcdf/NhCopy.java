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



// Future:
// To implement chunkLens with useLinear:
//   In copyData, in each of the sections like
//     else if (dimLens.length == 2 or 3 or ...)
//   Use something like:
//     float[] linVec = new float[ product of dimLens];
//     copy chunkVals[*][*] to linVec
//     writeData( LinVec)

class FieldSpec {
  String name;
  int iscale;
  double mfact;
  boolean isFound;
  int[] chunkLens;

  FieldSpec( String name, int iscale, double mfact) {
    this.name = name;
    this.iscale = iscale;
    this.mfact = mfact;
    this.isFound = false;
    this.chunkLens = null;
  }
} // end class FieldSpec




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


static boolean useLinear = false;        // for performance testing only
static int copyNd = 0;                   // for performance testing only

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




static void badparms( int bugs, String msg) {
  prtf("Error: " + msg);
  prtf("Parms:");
  prtf("  -bugs       debug level.  Default is 0.");
  prtf("  -compress   compressionLevel for outFile.  0: none,  9: max");
  prtf("  -inFile     input file name.");
  prtf("  -outFile    output file name.");
  prtf("");
  prtf("  -field      name:rounding.");
  prtf("              or");
  prtf("              name:rounding:chunkLen,chunkLen,... (one len per dim).");
  prtf("              The -field arg may be repeated.");
  prtf("                name: fully qualified field name.");
  prtf("                rounding: \"none\" or use rounding, like grib2:");
  prtf("                  mfact = 10^rounding");
  prtf("                  val = ((int) (mfact * val)) / mfact");
  prtf("                chunkLens: The chunkLen for each dimension,");
  prtf("                  or omitted.");
  prtf("                Note: this feature is for testing only,");
  prtf("                works on float vars having 1 <= rank <= 4.");
  if (bugs > 1) {
    prtf("");
    prtf("  -useLinear  use linear mode (not with chunks)");
  }
  prtf("");
  prtf("Example:");
  prtf("java -cp tdcls:netcdfAll-4.2.jar testpk.NhCopy"
    + " -compress 0 -inFile ta.nc -outFile tb.nc");
  System.exit(1);
}







static void copyIt( String[] args)
throws NhException
{
  int bugs = 0;
  String logDir = null;
  String statTag = null;
  int compressionLevel = -1;
  String inFile = null;
  String outFile = null;
  ArrayList<FieldSpec> fieldList = new ArrayList<FieldSpec>();
  int iarg = 0;
  while (iarg < args.length) {
    String key = args[iarg++];
    if (iarg >= args.length) badparms( bugs, "no value for the last key");
    String val = args[iarg++];
    if (key.equals("-bugs")) bugs = parseInt( "-bugs", val);
    else if (key.equals("-compress")) {
      compressionLevel = parseInt( "-compress", val);
      if (compressionLevel < 0 || compressionLevel > 9)
        badparms( bugs, "invalid compress: " + compressionLevel);
    }
    else if (key.equals("-inFile")) inFile = val;
    else if (key.equals("-outFile")) outFile = val;

    else if (key.equals("-field")) {
      String[] toks = val.split(":");
      String name = toks[0];
      int iscale = -9999;
      double mfact = 0;
      if (toks.length == 0) badparms( bugs, "invalid -field spec");
      else if (toks.length == 1
        || toks.length == 2 && toks[1].equals("none"))
      {
        iscale = -9999;
        mfact = 0;
      }
      else {
        if (toks.length < 2) badparms( bugs, "invalid -field spec");
        iscale = parseInt( "scale", toks[1]);
        mfact = 1;
        if (iscale < 0) {
          for (int ii = 0; ii < -iscale; ii++) mfact *= 0.1;
        }
        else if (iscale > 0) {
          for (int ii = 0; ii < iscale; ii++) mfact *= 10;
        }
      }
      FieldSpec fspec = new FieldSpec( name, iscale, mfact);
      fieldList.add( fspec);
      if (toks.length > 2) {      // get chunkLens
        fspec.chunkLens = new int[ toks.length - 2];
        for (int ii = 0; ii < fspec.chunkLens.length; ii++) {
          fspec.chunkLens[ii] = parseInt( "chunkLen", toks[2+ii]);
        }
      }
    } // if key == "-field"

    else if (key.equals("-testValLog")) {
      logDir = val;
      statTag = "stats";
    }
    else if (key.equals("-useLinear"))
      useLinear = parseBoolean("-useBoolean", val);
    else if (key.equals("-testValNdArray")) {
      copyNd = parseInt( "testValNdArray", val);
    }

    else badparms( bugs, "unknown parm: \"" + key + "\"");
  }
  if (compressionLevel < 0) badparms( bugs, "parm not specified: -compress");
  if (inFile == null) badparms( bugs, "parm not specified: -inFile");
  if (outFile == null) badparms( bugs, "parm not specified: -outFile");

  FieldSpec[] fieldSpecs = fieldList.toArray( new FieldSpec[0]);
  for (FieldSpec fspec : fieldSpecs) {
    if (useLinear && fspec.chunkLens != null)
      throwerr("Sorry, useLinear with chunkLens not yet implemented.");
      // To implement useLinear with chunkLens:  See doc at top.
  }

  if (bugs >= 1) {
    prtf("copyIt: compress: %d", compressionLevel);
    prtf("copyIt: inFile: \"%s\"", inFile);
    prtf("copyIt: outFile: \"%s\"", outFile);
    if (fieldSpecs.length == 0) prtf("copyIt: fields: (all)");
    else {
      for (FieldSpec fspec : fieldSpecs) {
        String tmsg = "none";
        if (fspec.iscale != -9999) tmsg = "" + fspec.iscale;
        prtf("copyIt: field: \"" + fspec.name + "\"  iscale: " + tmsg);
        if (fspec.chunkLens != null)
          prtf("  chunkLens: " + formatInts( fspec.chunkLens));
      }
    }
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
      0,             // utcModTime: use current time.
      logDir,
      statTag);

    Group inGroup = inCdf.getRootGroup();
    NhGroup outGroup = outCdf.getRootGroup();

    // pass 1: define vars
    copyGroup( 1, fieldSpecs, compressionLevel, inGroup, outGroup, bugs);
    outCdf.endDefine();

    // pass 2: copy data
    copyGroup( 2, fieldSpecs, compressionLevel, inGroup, outGroup, bugs);
    inCdf.close();
    inCdf = null;
    outCdf.close();
    outCdf = null;
  }
  catch( Exception exc) {
    exc.printStackTrace();
    badparms( bugs, "const: caught: " + exc);
  }

  boolean allOk = true;
  for (FieldSpec fspec : fieldSpecs) {
    if (! fspec.isFound) {
      allOk = false;
      prtf("Error: field not found: \"" + fspec.name + "\"");
    }
  }
  if (! allOk) throwerr("at least one field not found");
} // end const






static void copyGroup(
  int pass,                  // 1: define vars;  2: copy data
  FieldSpec[] fieldSpecs,    // if len==0, use all fields
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
    if (bugs >= 2) prtf("NhCopy.copyGroup: pass: "
      + pass + "  var: " + var.getName());

    FieldSpec fspec = findFieldSpec( fieldSpecs, var.getName());
    if (fspec != null) fspec.isFound = true;
    // Only process the field if it is in fieldSpecs,
    // or if no fieldSpecs were given.
    if (fieldSpecs.length == 0 || fspec != null) {
      if (var.getDataType() == DataType.STRUCTURE) {
        if (pass == 1) prtf("Warning: skipping structure: " + var);
      }
      else {
        if (pass == 1)
          copyVarDef( fspec, compressionLevel, var, outGroup, bugs);
        else copyData( fspec, var, outGroup, bugs);
      }
    }
  }

  for (Group inSub : inGroup.getGroups()) {
    NhGroup outSub;
    if (pass == 1) outSub = outGroup.addGroup( inSub.getShortName());
    else outSub = outGroup.findSubGroup( inSub.getShortName());

    copyGroup( pass, fieldSpecs, compressionLevel, inSub, outSub, bugs);
  }
  
} // end copyGroup





static FieldSpec findFieldSpec(
  FieldSpec[] fieldSpecs,
  String name)
{
  FieldSpec fspec = null;
  for (FieldSpec tspec : fieldSpecs) {
    if (tspec.name.equals( name)) {
      fspec = tspec;
      break;
    }
  }
  return fspec;
}







static void copyVarDef(
  FieldSpec fspec,
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

  ///String tmsg = "NhCopy.copyVarDef: " + inVar.getNameAndDimensions();
  ///tmsg += " [";
  ///for (int ii = 0; ii < inVar.getRank(); ii++) {
  ///  if (ii > 0) tmsg += ",";
  ///  tmsg += inVar.getShape(ii);
  ///}
  ///tmsg += "]";
  ///tmsg += "  size: " + inVar.getSize();
  ///tmsg += "  type: " + inVar.getDataType();
  ///prtf( tmsg);

  if (bugs >= 1) {
    prtf("copyVarDef: nhType: %s", NhVariable.nhTypeNames[nhType]);
  }

  NhDimension[] nhDims = getNhDims( inVar, outGroup);

  int[] dimLens = new int[nhDims.length];
  for (int ii = 0; ii < dimLens.length; ii++) {
    dimLens[ii] = nhDims[ii].getLength();
  }

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


  int[] useChunkLens = null;
  if (nhDims.length > 0) {
    if (fspec != null && fspec.chunkLens != null) {
      if (nhDims.length == 0)
        throwerr("cannot spec chunkLens for scaler.  inVar: " + inVar);
      if (fspec.chunkLens.length != nhDims.length)
        throwerr("chunkLens rank mismatch for inVar: " + inVar);
      for (int ii = 0; ii < nhDims.length; ii++) {
        if (fspec.chunkLens[ii] > dimLens[ii])
          throwerr("chunkLen exceeds dim for var: " + inVar);
      }
      useChunkLens = fspec.chunkLens;
    }
    else {
      useChunkLens = dimLens;
    }
  }

  int compress = compressionLevel;
  if (nhDims.length == 0) compress = 0;        // cannot compress a scalar
  NhVariable outVar = outGroup.addVariable(
    inVar.getShortName(),
    nhType,
    nhDims,
    useChunkLens,
    fillValue,
    compress);

  // Copy attributes
  for (Attribute attr : inVar.getAttributes()) {
    copyAttribute( attr, outVar.getName(), outVar, bugs);
  }

} // end copyVarDef






static NhDimension[] getNhDims(
  Variable inVar,
  NhGroup outGroup)
throws NhException
{

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
  return nhDims;
}






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
  FieldSpec fspec,               // may be null
  Variable inVar,
  NhGroup outGroup,
  int bugs)
throws NhException
{
  if (bugs >= 1) {
    prtf("copyData: inVar: %s", inVar.getName());
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
  Object[] ndarrs = null;                  // for performance testing only
  if (copyNd > 0) {
    Runtime.getRuntime().gc();
    long mema = Runtime.getRuntime().freeMemory();
    ndarrs = new Object[copyNd];
    for (int ii = 0; ii < copyNd; ii++) {
      ndarrs[ii] = arr.copyToNDJavaArray();
    }
    long memb = Runtime.getRuntime().freeMemory();
    prtf("copyNd: %d  deltaFreeMem: %d", copyNd, memb - mema);
  }

  // Print helpful info: min, max
  if (bugs >= 0) {
    String tmsg = "  variable: \"" + inVar.getName() + "\"";

    tmsg += " (";
    for (int ii = 0; ii < inVar.getRank(); ii++) {
      if (ii > 0) tmsg += ",";
      tmsg += inVar.getDimension(ii).getName();
    }
    tmsg += ")";

    tmsg += " (";
    for (int ii = 0; ii < inVar.getRank(); ii++) {
      if (ii > 0) tmsg += ",";
      tmsg += inVar.getDimension(ii).getLength();
    }
    tmsg += ")";

    tmsg += "  size: " + inVar.getSize();
    DataType tp = inVar.getDataType();
    tmsg += "  type: " + tp;

    if (fspec == null || fspec.iscale == -9999) tmsg += "  iscale: none";
    else tmsg += "  iscale: " + fspec.iscale;

    if (tp == DataType.FLOAT || tp == DataType.DOUBLE) {
      double minVal = Double.MAX_VALUE;
      double maxVal = Double.MIN_VALUE;
      double total = 0;
      for (int ii = 0; ii < arr.getSize(); ii++) {
        double vv = arr.getDouble(ii);
        if (vv < minVal) minVal = vv;
        if (vv > maxVal) maxVal = vv;
        total += vv;
        if (fspec != null && fspec.iscale != -9999) {
          double roundVal = ((int) (fspec.mfact * vv)) / fspec.mfact;
          arr.setDouble( ii, roundVal);
        }
      }
      prtf( tmsg + "  min: %g  max: %g  avg: %g",
        minVal, maxVal, total / (double) arr.getSize());
    } // if some type of float

    else if (tp == DataType.BYTE || tp == DataType.INT
     || tp == DataType.LONG || tp == DataType.SHORT)
    {
      long minVal = Long.MAX_VALUE;
      long maxVal = Long.MIN_VALUE;
      long total = 0;
      for (int ii = 0; ii < arr.getSize(); ii++) {
        long vv = arr.getLong(ii);
        if (vv < minVal) minVal = vv;
        if (vv > maxVal) maxVal = vv;
        total += vv;
        if (fspec != null && fspec.iscale != -9999) {
          double roundVal = ((int) (fspec.mfact * vv)) / fspec.mfact;
          arr.setLong( ii, (long) roundVal);
        }
      }
      prtf( tmsg + "  min: %d  max: %d  avg: %g",
        minVal, maxVal, total / (double) arr.getSize());
    } // if some type of integer

    else {
      prtf(  tmsg);
    }
  } // if bugs >= 0

  Object rawData = decodeArray( arr, bugs);

  int[] strts = null;      // startIxs for chunks
  if (inVar.getRank() > 0) strts = new int[ inVar.getRank()];
  if (fspec != null && fspec.chunkLens != null) {

    // Get dimLens so we can check chunkLens
    NhDimension[] nhDims = getNhDims( inVar, outGroup);
    int[] dimLens = new int[nhDims.length];
    for (int ii = 0; ii < dimLens.length; ii++) {
      dimLens[ii] = nhDims[ii].getLength();
    }

    int[] clens = fspec.chunkLens;
    if (dimLens.length == 1) {
      if (! (rawData instanceof float[]))
        throwerr("wrong type for rawData: " + rawData.getClass());
      float[] fvals = (float[]) rawData;
      float[] chunkVals = new float[clens[0]];
      for (strts[0] = 0; strts[0] < dimLens[0]; strts[0] += clens[0]) {

        int lima = Math.min( clens[0], dimLens[0] - strts[0]);
        for (int ia = 0; ia < lima; ia++) {
          chunkVals[ia] = fvals[strts[0]+ia];
        }
        nhVar.writeData( strts, chunkVals, false);  // useLinear = false
      }
    } // if rank == 1

    else if (dimLens.length == 2) {
      if (! (rawData instanceof float[][]))
        throwerr("wrong type for rawData: " + rawData.getClass());
      float[][] fvals = (float[][]) rawData;
      float[][] chunkVals = new float[clens[0]][clens[1]];
      for (strts[0] = 0; strts[0] < dimLens[0]; strts[0] += clens[0]) {
        for (strts[1] = 0; strts[1] < dimLens[1]; strts[1] += clens[1]) {

          int lima = Math.min( clens[0], dimLens[0] - strts[0]);
          int limb = Math.min( clens[1], dimLens[1] - strts[1]);
          for (int ia = 0; ia < lima; ia++) {
            for (int ib = 0; ib < limb; ib++) {
              chunkVals[ia][ib] = fvals[strts[0]+ia] [strts[1]+ib];
            }
          } // for ia
          nhVar.writeData( strts, chunkVals, false);  // useLinear = false
        } // for strts[1]
      } // for strts[0]
    } // if rank == 2

    else if (dimLens.length == 3) {
      if (! (rawData instanceof float[][][]))
        throwerr("wrong type for rawData: " + rawData.getClass());
      float[][][] fvals = (float[][][]) rawData;
      float[][][] chunkVals = new float[clens[0]][clens[1]][clens[2]];
      for (strts[0] = 0; strts[0] < dimLens[0]; strts[0] += clens[0]) {
        for (strts[1] = 0; strts[1] < dimLens[1]; strts[1] += clens[1]) {
          for (strts[2] = 0; strts[2] < dimLens[2]; strts[2] += clens[2]) {

            int lima = Math.min( clens[0], dimLens[0] - strts[0]);
            int limb = Math.min( clens[1], dimLens[1] - strts[1]);
            int limc = Math.min( clens[2], dimLens[2] - strts[2]);
            for (int ia = 0; ia < lima; ia++) {
              for (int ib = 0; ib < limb; ib++) {
                for (int ic = 0; ic < limc; ic++) {
                  chunkVals[ia][ib][ic]
                    = fvals[strts[0]+ia]
                      [strts[1]+ib]
                      [strts[2]+ic];
                }
              }
            } // for ia
            nhVar.writeData( strts, chunkVals, false);  // useLinear = false
          } // for strts[2]
        } // for strts[1]
      } // for strts[0]
    } // if rank == 3

    else if (dimLens.length == 4) {
      if (! (rawData instanceof float[][][][]))
        throwerr("wrong type for rawData: " + rawData.getClass());
      float[][][][] fvals = (float[][][][]) rawData;
      float[][][][] chunkVals
        = new float[clens[0]][clens[1]][clens[2]][clens[3]];
      for (strts[0] = 0; strts[0] < dimLens[0]; strts[0] += clens[0]) {
        for (strts[1] = 0; strts[1] < dimLens[1]; strts[1] += clens[1]) {
          for (strts[2] = 0; strts[2] < dimLens[2]; strts[2] += clens[2]) {
            for (strts[3] = 0; strts[3] < dimLens[3]; strts[3] += clens[3]) {

              int lima = Math.min( clens[0], dimLens[0] - strts[0]);
              int limb = Math.min( clens[1], dimLens[1] - strts[1]);
              int limc = Math.min( clens[2], dimLens[2] - strts[2]);
              int limd = Math.min( clens[3], dimLens[3] - strts[3]);
              for (int ia = 0; ia < lima; ia++) {
                for (int ib = 0; ib < limb; ib++) {
                  for (int ic = 0; ic < limc; ic++) {
                    for (int id = 0; id < limd; id++) {
                      chunkVals[ia][ib][ic][id]
                        = fvals[strts[0]+ia]
                          [strts[1]+ib]
                          [strts[2]+ic]
                          [strts[3]+id];
                    }
                  }
                }
              } // for ia
              nhVar.writeData( strts, chunkVals, false);  // useLinear = false
            } // for strts[3]
          } // for strts[2]
        } // for strts[1]
      } // for strts[0]
    } // if rank == 4

    else if (dimLens.length == 5) {
      if (! (rawData instanceof float[][][][][]))
        throwerr("wrong type for rawData: " + rawData.getClass());
      float[][][][][] fvals = (float[][][][][]) rawData;
      float[][][][][] chunkVals
        = new float[clens[0]][clens[1]][clens[2]][clens[3]][clens[4]];
      for (strts[0] = 0; strts[0] < dimLens[0]; strts[0] += clens[0]) {
        for (strts[1] = 0; strts[1] < dimLens[1]; strts[1] += clens[1]) {
          for (strts[2] = 0; strts[2] < dimLens[2]; strts[2] += clens[2]) {
            for (strts[3] = 0; strts[3] < dimLens[3]; strts[3] += clens[3]) {
              for (strts[4] = 0; strts[4] < dimLens[4]; strts[4] += clens[4]) {

                int lima = Math.min( clens[0], dimLens[0] - strts[0]);
                int limb = Math.min( clens[1], dimLens[1] - strts[1]);
                int limc = Math.min( clens[2], dimLens[2] - strts[2]);
                int limd = Math.min( clens[3], dimLens[3] - strts[3]);
                int lime = Math.min( clens[4], dimLens[4] - strts[4]);
                for (int ia = 0; ia < lima; ia++) {
                  for (int ib = 0; ib < limb; ib++) {
                    for (int ic = 0; ic < limc; ic++) {
                      for (int id = 0; id < limd; id++) {
                        for (int ie = 0; ie < lime; ie++) {
                          chunkVals[ia][ib][ic][id][ie]
                            = fvals[strts[0]+ia]
                              [strts[1]+ib]
                              [strts[2]+ic]
                              [strts[3]+id]
                              [strts[4]+ie];
                        }
                      }
                    }
                  }
                } // for ia
                nhVar.writeData( strts, chunkVals, false); // useLinear = false
              } // for strts[4]
            } // for strts[3]
          } // for strts[2]
        } // for strts[1]
      } // for strts[0]
    } // if rank == 5
    else throwerr("higher dims not supported yet");

  } // if chunkLens were specified

  else {   // else no chunkLens
    nhVar.writeData( strts, rawData, useLinear);
  }
} // end copyData






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




// Do special handling of scalars, string arrays.

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
    prtf("    rank: %d", rank);
    prtf("    shape: %s", formatInts( shape));
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
    if (useLinear) resObj = arr.getStorage();
    else resObj = arr.copyToNDJavaArray();
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
    if (useLinear) resObj = arr.getStorage();
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
    if (useLinear) resObj = arr.getStorage();
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



static int parseInt( String msg, String stg)
throws NhException
{
  int ival = 0;
  try { ival = Integer.parseInt( stg); }
  catch( NumberFormatException exc) {
    throwerr("bad format for parm %s.  value: \"%s\"", msg, stg);
  }
  return ival;
}




static boolean parseBoolean( String msg, String stg)
throws NhException
{
  boolean bval = false;
  if (stg.equals("false") || stg.equals("no")) bval = false;
  else if (stg.equals("true") || stg.equals("yes")) bval = true;
  else throwerr("bad format for parm %s.  value: \"%s\"", msg, stg);
  return bval;
}



static String formatInts( int[] ivals) {
  String res = "";
  for (int ival : ivals) {
    res += " " + ival;
  }
  return res;
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

