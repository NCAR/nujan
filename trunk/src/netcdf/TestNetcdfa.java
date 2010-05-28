
package ncHdfTest;

import ncHdf.NcDimension;
import ncHdf.NcException;
import ncHdf.NcFile;
import ncHdf.NcGroup;
import ncHdf.NcVariable;

import hdfnet.HdfException;
import hdfnet.HdfGroup;

import hdfnetTest.TestData;


// Test short / int / long / float / double / string<n>,
// with any number of dimensions.


public class TestNetcdfa {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs         <int>");
  prtf("  -nctype       short / int / long / float / double / string<n>");
  prtf("                  where n is max string len,");
  prtf("                  including null termination");
  prtf("  -dims         <int,int,...>   or \"0\" if a scalar");
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


static void runit( String[] args)
throws NcException
{
  int bugs = -1;
  String typeStg = null;
  int nctype = -1;
  int stgFieldLen = 0;
  int[] dims = null;
  String fileVersionStg = null;
  String chunkedStg = null;      // xxx chunked not used
  int compressLevel = -1;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-nctype")) {
      typeStg = val;
      if (val.equals("byte")) nctype = NcVariable.TP_BYTE;
      else if (val.equals("short")) nctype = NcVariable.TP_SHORT;
      else if (val.equals("int")) nctype = NcVariable.TP_INT;
      else if (val.equals("long")) nctype = NcVariable.TP_LONG;
      else if (val.equals("float")) nctype = NcVariable.TP_FLOAT;
      else if (val.equals("double")) nctype = NcVariable.TP_DOUBLE;
      else if (val.startsWith("string")) {
        if (val.length() <= 6)
          badparms("must spec stgFieldLen after \"string\"");
        nctype = NcVariable.TP_STRING_FIX;
        stgFieldLen = Integer.parseInt( val.substring(6));
        typeStg = val.substring(0,6);
      }
      else if (val.equals("vstring")) nctype = NcVariable.TP_STRING_VAR;
      else badparms("unknown nctype A: " + val);
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
  if (typeStg == null || nctype < 0) badparms("missing parm: -nctype");
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

  prtf("TestNetcdfa: bugs: %d", bugs);
  prtf("TestNetcdfa: typeStg: \"%s\"", typeStg);
  prtf("TestNetcdfa: nctype: \"%s\"", NcVariable.ncTypeNames[nctype]);
  prtf("TestNetcdfa: stgFieldLen: %d", stgFieldLen);
  prtf("TestNetcdfa: rank: %d", dims.length);
  for (int idim : dims) {
    prtf("  TestNetcdfa: dim: %d", idim);
  }
  prtf("TestNetcdfa: fileVersion: %s", fileVersion);
  prtf("TestNetcdfa: chunked: %s", useChunked);
  prtf("TestNetcdfa: compress: %d", compressLevel);
  prtf("TestNetcdfa: outFile: \"%s\"", outFile);

  NcFile hfile = new NcFile( outFile, NcFile.OPT_OVERWRITE, fileVersion);
  hfile.setDebugLevel( bugs);

  NcGroup rootGroup = hfile.getRootGroup();

  // Create test data and fill value.
  Object[] testData = TestData.genNcData( typeStg, dims);
  Object vdata = testData[0];
  Object fillValue = testData[1];
  prtf("  ######### vdata: " + vdata);
  prtf("  ######### fillValue: " + fillValue);

  NcGroup alpha1 = rootGroup.addGroup("alpha1");
  NcGroup alpha2 = rootGroup.addGroup("alpha2");
  NcGroup alpha3 = rootGroup.addGroup("alpha3");

  int rank = dims.length;
  NcDimension[] ncDims = new NcDimension[rank];
  for (int ii = 0; ii < rank; ii++) {
    ncDims[ii] = rootGroup.addDimension(
      String.format("dim%02d", ii),
      dims[ii]);
  }

  int numAttr = 3;
  // NetCDF attributes all have rank == 1.
  if (rank == 1) {
    for (int ii = 0; ii < numAttr; ii++) {
      rootGroup.addAttribute(
        String.format("globAttr%04d", ii),   // attrName
        vdata);
    }
  }


  int numVar = 2;
  NcVariable[] testVars = new NcVariable[ numVar];
  for (int ii = 0; ii < numVar; ii++) {
    testVars[ii] = testDefineVariable(
      numAttr,
      alpha2,                            // parentGroup
      String.format("testVar%04d", ii),  // varName
      nctype,
      stgFieldLen,
      ncDims,
      compressLevel,
      vdata,
      fillValue);
  }

  hfile.endDefine();

  for (int ii = 0; ii < numVar; ii++) {
    testVars[ii].writeData( vdata);
  }

  hfile.close();
}









static NcVariable testDefineVariable(
  int numAttr,
  NcGroup parentGroup,
  String varName,
  int nctype,
  int stgFieldLen,        // string length, incl null termination
  NcDimension[] ncDims,   // varDims
  int compressLevel,      // compression level: 0==none, 1 - 9
  Object vdata,
  Object fillValue)
throws NcException
{
  int rank = ncDims.length;

  NcVariable vara = parentGroup.addVariable(
    varName,             // varName
    nctype,              // nctype
    stgFieldLen,
    ncDims,              // varDims
    fillValue,
    compressLevel);

  //xxx del if (nctype == NcVariable.TP_STRING) {
  //xxx del   vara = parentGroup.addStringVariable(
  //xxx del     varName,             // varName
  //xxx del     stgFieldLen,         // string length, incl null termination
  //xxx del     ncDims,              // varDims
  //xxx del     fillValue,
  //xxx del     compressLevel);
  //xxx del }
  //xxx del else {
  //xxx del   vara = parentGroup.addNumericVariable(
  //xxx del     varName,             // varName
  //xxx del     nctype,              // nctype
  //xxx del     ncDims,              // varDims
  //xxx del     fillValue,
  //xxx del     compressLevel);
  //xxx del }

  // NetCDF attributes all have rank == 1.
  if (rank == 1) {
    for (int ii = 0; ii < numAttr; ii++) {
      vara.addAttribute(
        String.format("varAttr%04d", ii),   // attrName
        vdata);
    }
  }

  prtf("TestNetcdfa: parentGroup: %s", parentGroup);
  prtf("TestNetcdfa: vara: %s", vara);
  return vara;

} // end testDefineVariable




static void throwerr( String msg, Object... args)
throws NcException
{
  throw new NcException( String.format( msg, args));
}





static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class
