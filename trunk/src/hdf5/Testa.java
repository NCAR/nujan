
package hdfnetTest;

import hdfnet.HdfFile;
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
  prtf("  -dtype        one of: fixed08/16/32/64");
  prtf("                        float32/64");
  prtf("                        stringN vstring");
  prtf("                           where N is the max string len including");
  prtf("                           null termination");
  prtf("                        reference");
  prtf("  -dims         <int,int,...>   or \"0\" if scalar");
  prtf("  -fileVersion  1 / 2");
  prtf("  -chunked      false / true");
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


//xxx: testall.sh: also test fixed08

//xxx also test vstring

//xxx array might not fit in memory

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
      if (val.equals("fixed08")) dtype = HdfGroup.DTYPE_FIXED08;
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
      else badparms("unknown dtype A: " + val);
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
  if (chunkedStg.equals("false")) useChunked = false;
  else if (chunkedStg.equals("true")) useChunked = true;
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

  HdfFile hfile = new HdfFile(
    outFile, fileVersion, HdfFile.OPT_ALLOW_OVERWRITE);
  hfile.setDebugLevel( bugs);
  HdfGroup rootGroup = hfile.getRootGroup();

  Object[] testData = TestData.genHdfData( dtype, dims, rootGroup);
  Object vdata = testData[0];
  Object fillValue = testData[1];

  if (bugs >= 1) {
    if (fillValue instanceof String)
      prtf("Testa: fillValue: class: %s  value: \"%s\"",
        fillValue.getClass().getName(), fillValue);
    else
      prtf("Testa: fillValue: class: %s  value: %s",
        fillValue.getClass().getName(), fillValue);
  }

  HdfGroup alpha1 = rootGroup.addGroup("alpha1");
  HdfGroup alpha2 = rootGroup.addGroup("alpha2");
  HdfGroup alpha3 = rootGroup.addGroup("alpha3");

  int numVar = 2;
  HdfGroup[] testVars = new HdfGroup[ numVar];
  for (int ii = 0; ii < numVar; ii++) {
    testVars[ii] = testDefineVariable(
      useChunked,
      compressLevel,
      alpha2,                            // parentGroup
      String.format("testVar%04d", ii),  // varName
      dtype,
      stgFieldLen,
      dims,
      vdata,
      fillValue);
  }

  hfile.endDefine();

  for (int ii = 0; ii < numVar; ii++) {
    testVars[ii].writeData( vdata);
  }

  hfile.close();
}










static HdfGroup testDefineVariable(
  boolean useChunked,
  int compressLevel,
  HdfGroup parentGroup,
  String varName,
  int dtype,
  int stgFieldLen,        // string length, incl null termination
  int[] dims,             // varDims
  Object vdata,
  Object fillValue)
throws HdfException
{
  int rank = dims.length;

  HdfGroup vara = parentGroup.addVariable(
    varName,                   // varName
    dtype,                     // dtype
    stgFieldLen,               // string length, incl null termination
    dims,                      // varDims
    fillValue,
    useChunked,                // isChunked
    compressLevel);            // compressionLevel

  int numAttr = 2;
  for (int ii = 0; ii < numAttr; ii++) {
    vara.addAttribute(
      String.format("testAttr%04d", ii),   // attrName
      vdata,
      false,         // isVlen
      false);        // isCompoundRef
  }

  // If 2 dimensions, add VLEN attribute.
  if (rank == 2) {
    Object vlenData = null;
    if (dtype == HdfGroup.DTYPE_FIXED08) {
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
    else if (dtype == HdfGroup.DTYPE_STRING_FIX
      || dtype == HdfGroup.DTYPE_STRING_VAR)
    {
      vlenData = new String[][] {
        { "111uuu", "112", "113" },
        { "111", "112uuu" },
        { "111", "112", "113", "114uuu" }};
    }
    else if (dtype == HdfGroup.DTYPE_REFERENCE) {
      vlenData = new HdfGroup[][] {
        { vara, vara, vara },
        { vara, vara },
        { vara, vara, vara, vara }};
    }
    else badparms("unknown dtype C: " + dtype);

    vara.addAttribute(
      "vlenAttr",
      vlenData,
      true,            // isVlen
      false);          // isCompoundRef
  }

  // If rank 1 reference, test COMPOUND.
  if (rank == 1 && dtype == HdfGroup.DTYPE_REFERENCE) {
    vara.addAttribute(
      "compoundAttr",
      new HdfGroup[] {
        vara,
        vara,
        vara},
      false,           // isVlen
      true);           // isCompoundRef
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
