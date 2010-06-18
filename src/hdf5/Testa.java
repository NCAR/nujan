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


package hdfnetTest;

import hdfnet.HdfFileWriter;
import hdfnet.HdfGroup;
import hdfnet.HdfException;


// Test fixed08, fixed16, fixed32, fixed64, float32, float64,
// string, or reference type,
// with any number of dimensions.


public class Testa {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs         <int>");
  prtf("  -dtype        one of: fixed08 ufixed08 fixed16/32/64");
  prtf("                        float32/64");
  prtf("                        stringN vstring");
  prtf("                           where N is the max string len without");
  prtf("                           null termination");
  prtf("                        reference compound");
  prtf("  -dims         <int,int,...>   or \"0\" if scalar");
  prtf("  -fileVersion  1 / 2");
  prtf("  -chunked      contig / chunked");
  prtf("  -compress     compression level: 0==none, 1 - 9");
  prtf("  -outFile      <fname>");
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
  int dtype = -1;
  int stgFieldLen = 0;
  int[] dims = null;
  String fileVersionStg = null;
  String chunkedStg = null;
  int compressLevel = -1;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-dtype")) {
      if (val.equals("sfixed08")) dtype = HdfGroup.DTYPE_SFIXED08;
      else if (val.equals("ufixed08")) dtype = HdfGroup.DTYPE_UFIXED08;
      else if (val.equals("fixed16")) dtype = HdfGroup.DTYPE_FIXED16;
      else if (val.equals("fixed32")) dtype = HdfGroup.DTYPE_FIXED32;
      else if (val.equals("fixed64")) dtype = HdfGroup.DTYPE_FIXED64;
      else if (val.equals("float32")) dtype = HdfGroup.DTYPE_FLOAT32;
      else if (val.equals("float64")) dtype = HdfGroup.DTYPE_FLOAT64;
      else if (val.startsWith("string")) {
        dtype = HdfGroup.DTYPE_STRING_FIX;
        if (val.length() <= 6)
          badparms("must spec stgFieldLen after \"string\"");
        stgFieldLen = Integer.parseInt( val.substring(6));
      }
      else if (val.equals("vstring")) dtype = HdfGroup.DTYPE_STRING_VAR;
      else if (val.equals("reference")) dtype = HdfGroup.DTYPE_REFERENCE;
      else if (val.equals("compound")) dtype = HdfGroup.DTYPE_COMPOUND;
      else badparms("unknown dtype: " + val);
    }
    else if (key.equals("-dims")) {
      if (val.equals("0")) dims = new int[0];
      else {
        String[] stgs = val.split(",");
        dims = new int[ stgs.length];
        for (int ii = 0; ii < stgs.length; ii++) {
          dims[ii] = Integer.parseInt( stgs[ii]);
          if (dims[ii] < 1) badparms("invalid dimension: " + dims[ii]);
        }
      }
    }
    else if (key.equals("-fileVersion")) fileVersionStg = val;
    else if (key.equals("-chunked")) chunkedStg = val;
    else if (key.equals("-compress")) compressLevel = Integer.parseInt( val);
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }

  if (bugs < 0) badparms("missing parm: -bugs");
  if (dtype < 0) badparms("missing parm: -dtype");
  if (dims == null) badparms("missing parm: -dims");
  if (fileVersionStg == null) badparms("missing parm: -fileVersion");
  if (chunkedStg == null) badparms("missing parm: -chunked");
  if (compressLevel < 0) badparms("missing parm: -compress");
  if (outFile == null) badparms("missing parm: -outFile");

  int fileVersion = 0;
  if (fileVersionStg.equals("1")) fileVersion = 1;
  else if (fileVersionStg.equals("2")) fileVersion = 2;
  else badparms("unknown fileVersion: " + fileVersionStg);

  boolean useChunked = false;
  if (chunkedStg.equals("contig")) useChunked = false;
  else if (chunkedStg.equals("chunked")) useChunked = true;
  else badparms("unknown chunked: " + chunkedStg);

  prtf("Testa: bugs: %d", bugs);
  prtf("Testa: dtype: %s", HdfGroup.dtypeNames[dtype]);
  prtf("Testa: stgFieldLen: %d", stgFieldLen);
  prtf("Testa: rank: %d", dims.length);
  for (int idim : dims) {
    prtf("  Testa: dim: %d", idim);
  }
  prtf("Testa: fileVersion: %s", fileVersion);
  prtf("Testa: chunked: %s", useChunked);
  prtf("Testa: compress: %d", compressLevel);
  prtf("Testa: outFile: \"%s\"", outFile);

  HdfFileWriter hfile = new HdfFileWriter(
    outFile, fileVersion, HdfFileWriter.OPT_ALLOW_OVERWRITE);
  hfile.setDebugLevel( bugs);
  HdfGroup rootGroup = hfile.getRootGroup();

  Object vdata = TestData.genHdfData( dtype, stgFieldLen, rootGroup, dims, 0);
  //xxxvdata = new String[] {"aaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbb"}; //xxx
  //xxxvdata = "aaaaaaaaaa"; //xxx

  Object fillValue = TestData.genFillValue( dtype, stgFieldLen);

  if (bugs >= 1) {
    String msg = "Testa: fillValue: ";
    if (fillValue == null) msg += "null";
    else if (fillValue instanceof String)
      msg += String.format("\"%s\"  class: String", fillValue);
    else
      msg += String.format("%s  class: %s",
        fillValue,
        fillValue.getClass().getName());
    prtf( msg);
  }

  HdfGroup alpha1 = rootGroup.addGroup("alpha1");
  HdfGroup alpha2 = rootGroup.addGroup("alpha2");
  HdfGroup alpha3 = rootGroup.addGroup("alpha3");

  int numVar = 2;
  HdfGroup[] testVars = new HdfGroup[ numVar];
  for (int ivar = 0; ivar < numVar; ivar++) {
    testVars[ivar] = testDefineVariable(
      useChunked,
      compressLevel,
      alpha2,                              // parentGroup
      ivar,
      String.format("testVar%04d", ivar),  // varName
      dtype,
      stgFieldLen,
      dims,
      vdata,
      fillValue);
  }

  hfile.endDefine();

  for (int ivar = 0; ivar < numVar; ivar++) {
    testVars[ivar].writeData( vdata);
  }

  hfile.close();
}










static HdfGroup testDefineVariable(
  boolean useChunked,
  int compressLevel,
  HdfGroup parentGroup,
  int ivar,
  String varName,
  int dtype,
  int stgFieldLen,        // string length, without null termination
  int[] dims,             // varDims
  Object vdata,
  Object fillValue)
throws HdfException
{
  int rank = dims.length;

  HdfGroup vara = parentGroup.addVariable(
    varName,                   // varName
    dtype,                     // dtype
    stgFieldLen,               // string length, without null termination
    dims,                      // varDims
    fillValue,
    useChunked,                // isChunked
    compressLevel);            // compressionLevel

  int numAttr = 2;
  if (ivar == 0) {
    for (int ii = 0; ii < numAttr; ii++) {
      vara.addAttribute(
        String.format("testAttr%04d", ii),   // attrName
        dtype,
        stgFieldLen,
        vdata,
        false);          // isVlen
    }
  }

  // If 2 dimensions, add VLEN attribute.
  // But vlen not allowed for DTYPE_STRING_VAR.
  // We cannot use either STRING_VAR or STRING_FIX for a
  // VLEN attr since the MsgAttribute call to
  //     Util.getDtypeAndDims( isVlen, attrValue);
  // will always return STRING_VAR for a string attrValue.

  if (rank == 2
    && dtype != HdfGroup.DTYPE_STRING_FIX
    && dtype != HdfGroup.DTYPE_STRING_VAR
    && dtype != HdfGroup.DTYPE_COMPOUND)
  {
    Object vlenData = null;
    if (dtype == HdfGroup.DTYPE_SFIXED08
      || dtype == HdfGroup.DTYPE_UFIXED08)
    {
      vlenData = new byte[][] {
        { (byte) 111, (byte) 112, (byte) 113 },
        { (byte) 111, (byte) 112 },
        { (byte) 111, (byte) 112, (byte) 113, (byte) 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FIXED16) {
      vlenData = new short[][] {
        { (short) 111, (short) 112, (short) 113 },
        { (short) 111, (short) 112 },
        { (short) 111, (short) 112, (short) 113, (short) 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FIXED32) {
      vlenData = new int[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FIXED64) {
      vlenData = new long[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT32) {
      vlenData = new float[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }
    else if (dtype == HdfGroup.DTYPE_FLOAT64) {
      vlenData = new double[][] {
        { 111, 112, 113 },
        { 111, 112 },
        { 111, 112, 113, 114 }};
    }

    //xxx else if (dtype == HdfGroup.DTYPE_STRING_FIX) {
    //xxx   vlenData = new String[][] {
    //xxx     { "111uuu", "112", "113" },
    //xxx     { "111", "112uuu" },
    //xxx     { "111", "112", "113", "114uuu" }};
    //xxx }

    else if (dtype == HdfGroup.DTYPE_REFERENCE) {
      vlenData = new HdfGroup[][] {
        { vara, vara, vara },
        { vara, vara },
        { vara, vara, vara, vara }};
    }
    else badparms("unknown dtype: " + dtype);

    vara.addAttribute(
      "vlenAttr",
      dtype,
      stgFieldLen,
      vlenData,
      true);           // isVlen
  } // if rank == 2 and not STRING_*

  // If rank 1 reference, test COMPOUND.
  if (rank == 1 && dtype == HdfGroup.DTYPE_COMPOUND) {
    vara.addAttribute(
      "compoundAttr",
      dtype,
      stgFieldLen,
      new HdfGroup[] {
        vara,
        vara,
        vara},
      false);          // isVlen
  }

  prtf("Testa: parentGroup: %s", parentGroup);
  prtf("Testa: vara: %s", vara);
  return vara;

} // end testDefineVariable





static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class
