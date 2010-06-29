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

import nhPkg.NhDimension;
import nhPkg.NhException;
import nhPkg.NhFileWriter;
import nhPkg.NhGroup;
import nhPkg.NhVariable;

import hdfnet.HdfException;
import hdfnet.HdfGroup;

import hdfnetTest.TestData;


// Simple test of rank 0 or 1 strings.


public class TestNetcdfString {


static void badparms( String msg) {
  prtf("Error: %s", msg);
  prtf("parms:");
  prtf("  -bugs         <int>");
  prtf("  -dims         <int,int,...>   or \"-\" if scalar");
  prtf("  -fileVersion  1 / 2");
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
throws NhException
{
  int bugs = -1;
  int[] dims = null;
  String fileVersionStg = null;
  int compressLevel = -1;
  String outFile = null;

  if (args.length % 2 != 0) badparms("parms must be key/value pairs");
  for (int iarg = 0; iarg < args.length; iarg += 2) {
    String key = args[iarg];
    String val = args[iarg+1];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
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
    else if (key.equals("-compress")) compressLevel = Integer.parseInt( val);
    else if (key.equals("-outFile")) outFile = val;
    else badparms("unkown parm: " + key);
  }

  if (bugs < 0) badparms("missing parm: -bugs");
  if (dims == null) badparms("missing parm: -dims");
  if (fileVersionStg == null) badparms("missing parm: -fileVersion");
  if (compressLevel < 0) badparms("missing parm: -compress");
  if (outFile == null) badparms("missing parm: -outFile");

  int fileVersion = 0;
  if (fileVersionStg.equals("1")) fileVersion = 1;
  else if (fileVersionStg.equals("2")) fileVersion = 2;
  else badparms("unknown fileVersion: " + fileVersionStg);

  prtf("TestNetcdfString: bugs: %d", bugs);
  prtf("TestNetcdfString: fileVersion: %s", fileVersion);
  prtf("TestNetcdfString: compress: %d", compressLevel);
  prtf("TestNetcdfString: outFile: \"%s\"", outFile);
  for (int ii = 0; ii < dims.length; ii++) {
    prtf("  dim %d: %d", ii, dims[ii]);
  }



  int rank = dims.length;

  // Create test data and fill value.
  Object vdata = null;
  Object fillValue = "sFill";
  if (rank == 0) {
    vdata = new String("alpha");
  }
  else if (rank == 1) {
    String[] amat = new String[ dims[0]];
    for (int ii = 0; ii < dims[0]; ii++) {
      amat[ii] = String.format("%d", 100 * ii);
    }
    vdata = amat;
  }
  else badparms("unknown rank");

  NhFileWriter hfile = NhFileWriter.mkTestWriter(
    "testingOnly", outFile, NhFileWriter.OPT_OVERWRITE, fileVersion);
  hfile.setDebugLevel( bugs);

  NhGroup rootGroup = hfile.getRootGroup();

  // Add dimensions
  NhDimension[] nhDims = new NhDimension[rank];
  for (int ii = 0; ii < rank; ii++) {
    nhDims[ii] = rootGroup.addDimension(
      String.format("dim%02d", ii),
      dims[ii]);
  }

  NhVariable vara = rootGroup.addVariable(
    "vara",              // varName
    NhVariable.TP_STRING_VAR,
    nhDims,              // varDims
    fillValue,
    compressLevel);

  hfile.endDefine();

  vara.writeData( vdata);

  hfile.close();
}








static void throwerr( String msg, Object... args)
throws NhException
{
  throw new NhException( String.format( msg, args));
}





static void prtf( String msg, Object... args) {
  System.out.printf( msg, args);
  System.out.printf("\n");
}

} // end class
