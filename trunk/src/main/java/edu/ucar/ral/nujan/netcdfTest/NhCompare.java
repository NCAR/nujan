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


// Compare two general netcdf files.
//
// Uses the unidata netcdf library:
// http://www.unidata.ucar.edu/software/netcdf-java/


public class NhCompare {


final static String ATAG = "A";
final static String BTAG = "B";


static void badparms( String msg) {
  prtf("Error: " + msg);
  prtf("Parms:");
  prtf("  -bugs       debug level.  Default is 0.");
  prtf("  -order      y/n.  Check for identical ordering.");
  prtf("  -skipUnder  y/n.  Skip groups, attrs and vars starting with underbar _");
  prtf("  -verbose    y/n.  Print every data pair difference.");
  prtf("  -ina        input file name.");
  prtf("  -inb        input file name.");
  prtf("");
  prtf("Example:");
  prtf("java -cp x/classes:x/netcdfAll-4.1.jar edu.ucar.ral.nujan.netcdfTest.NhCompare -bugs 0 -order y -skipUnder y -verbose y -inFilea tempa.nc -inFileb tempb.nc");
  System.exit(1);
}





public static void main( String[] args) {
  int numDiff = 0;
  try {
    numDiff = runIt( args);
  }
  catch( Exception exc) {
    exc.printStackTrace();
    prtf("main: caught: %s", exc);
    System.exit(1);
  }
  System.exit( numDiff);
}





// Returns numDiff

static int runIt( String[] args)
throws Exception
{
  int bugs = 0;
  boolean orderFlag = false;
  boolean skipUnder = false;
  boolean useVerbose = false;
  String inFilea = null;
  String inFileb = null;
  int iarg = 0;
  while (iarg < args.length) {
    String key = args[iarg++];
    if (iarg >= args.length) badparms("no value for the last key");
    String val = args[iarg++];
    if (key.equals("-bugs")) bugs = Integer.parseInt( val);
    else if (key.equals("-order")) orderFlag = parseBoolean( key, val);
    else if (key.equals("-skipUnder")) skipUnder = parseBoolean( key, val);
    else if (key.equals("-verbose")) useVerbose = parseBoolean( key, val);
    else if (key.equals("-inFilea")) inFilea = val;
    else if (key.equals("-inFileb")) inFileb = val;
    else badparms("unknown parm: \"" + key + "\"");
  }
  if (inFilea == null) badparms("parm not specified: -inFilea");
  if (inFileb == null) badparms("parm not specified: -inFileb");

  prtf("inFilea: \"%s\"", inFilea);
  prtf("inFileb: \"%s\"", inFileb);

  int numDiff = 0;
  NetcdfFile inCdfa = null;
  NetcdfFile inCdfb = null;
  try {
    inCdfa = NetcdfFile.open( inFilea);
    inCdfb = NetcdfFile.open( inFileb);
    Group inGroupa = inCdfa.getRootGroup();
    Group inGroupb = inCdfb.getRootGroup();

    numDiff += compareGroups(
      "",                // tag
      inGroupa,
      inGroupb,
      orderFlag,
      skipUnder,
      useVerbose,
      bugs,
      0);                // indent
    inCdfa.close();
    inCdfb.close();
    inCdfa = null;
    inCdfb = null;
  }
  catch( Exception exc) {
    exc.printStackTrace();
    badparms("const: caught: " + exc);
  }
  prtf("numDiff: %d", numDiff);
  return numDiff;
} // end runIt






static int compareGroups(
  String tag,
  Group inGroupa,
  Group inGroupb,
  boolean orderFlag,
  boolean skipUnder,
  boolean useVerbose,
  int bugs,
  int indent)
throws Exception
{
  int numDiff = 0;
  String grpName = inGroupa.getName();
  if (! inGroupb.getName().equals( grpName))
    throwerr("name mismatch: internal error");
  prtIndent( indent, grpName, "Start comparison");
  if (grpName.equals("")) grpName = "/";      // easier for error msgs

  Dimension[] dimsa = inGroupa.getDimensions().toArray( new Dimension[0]);
  Dimension[] dimsb = inGroupb.getDimensions().toArray( new Dimension[0]);

  if (skipUnder) {
    Dimension[] tmpDimsa = new Dimension[ dimsa.length];
    int kka = 0;
    for (Dimension dima : dimsa) {
      if (dima.getName().startsWith("_"))
        prtIndent( indent, inGroupa.getName(),
        "skipping dimension: %s", dima.getName());
      else tmpDimsa[kka++] = dima;
    }
    dimsa = Arrays.copyOf( tmpDimsa, kka);

    Dimension[] tmpDimsb = new Dimension[ dimsb.length];
    int kkb = 0;
    for (Dimension dimb : dimsb) {
      if (dimb.getName().startsWith("_"))
        prtIndent( indent, inGroupb.getName(),
        "skipping dimension: %s", dimb.getName());
      else tmpDimsb[kkb++] = dimb;
    }
    dimsb = Arrays.copyOf( tmpDimsb, kkb);
  }

  for (Dimension dima : dimsa) {
    numDiff += findDimension( grpName, ATAG, dima, dimsb, bugs, indent);
  }
  for (Dimension dimb : dimsb) {
    numDiff += findDimension( grpName, BTAG, dimb, dimsa, bugs, indent);
  }
  if (orderFlag && dimsa.length == dimsb.length) {
    for (int ii = 0; ii < dimsa.length; ii++) {
      if (! dimsa[ii].getName().equals( dimsb[ii].getName())) {
        numDiff += 1;
        prtIndent(
          indent, grpName,
          "Diff: dimensions out of order: %s  %s",
          dimsa[ii].getName(), dimsb[ii].getName());
      }
    }
  }


  Attribute[] attrsa = inGroupa.getAttributes().toArray( new Attribute[0]);
  Attribute[] attrsb = inGroupb.getAttributes().toArray( new Attribute[0]);

  if (skipUnder) {
    Attribute[] tmpAttrsa = new Attribute[ attrsa.length];
    int kka = 0;
    for (Attribute attra : attrsa) {
      if (attra.getName().startsWith("_"))
        prtIndent( indent, inGroupa.getName(),
        "skipping attribute: %s", attra.getName());
      else tmpAttrsa[kka++] = attra;
    }
    attrsa = Arrays.copyOf( tmpAttrsa, kka);

    Attribute[] tmpAttrsb = new Attribute[ attrsb.length];
    int kkb = 0;
    for (Attribute attrb : attrsb) {
      if (attrb.getName().startsWith("_"))
        prtIndent( indent, inGroupb.getName(),
        "skipping attribute: %s", attrb.getName());
      else tmpAttrsb[kkb++] = attrb;
    }
    attrsb = Arrays.copyOf( tmpAttrsb, kkb);
  }

  for (Attribute attra : attrsa) {
    numDiff += findAttr(
      grpName, ATAG, attra, attrsb, useVerbose, bugs, indent);
  }
  for (Attribute attrb : attrsb) {
    numDiff += findAttr(
      grpName, BTAG, attrb, attrsa, useVerbose, bugs, indent);
  }
  if (orderFlag && attrsa.length == attrsb.length) {
    for (int ii = 0; ii < attrsa.length; ii++) {
      if (! attrsa[ii].getName().equals( attrsb[ii].getName())) {
        numDiff += 1;
        prtIndent(
          indent, grpName,
          "Diff: attributes out of order: %s  %s",
          attrsa[ii].getName(), attrsb[ii].getName());
      }
    }
  }


  Variable[] varsa = inGroupa.getVariables().toArray( new Variable[0]);
  Variable[] varsb = inGroupb.getVariables().toArray( new Variable[0]);

  if (skipUnder) {
    Variable[] tmpVarsa = new Variable[ varsa.length];
    int kka = 0;
    for (Variable vara : varsa) {
      if (vara.getName().startsWith("_"))
        prtIndent( indent, inGroupa.getName(),
        "skipping variable: %s", vara.getName());
      else tmpVarsa[kka++] = vara;
    }
    varsa = Arrays.copyOf( tmpVarsa, kka);

    Variable[] tmpVarsb = new Variable[ varsb.length];
    int kkb = 0;
    for (Variable varb : varsb) {
      if (varb.getName().startsWith("_"))
        prtIndent( indent, inGroupb.getName(),
        "skipping variable: %s", varb.getName());
      else tmpVarsb[kkb++] = varb;
    }
    varsb = Arrays.copyOf( tmpVarsb, kkb);
  }

  for (Variable vara : varsa) {
    numDiff += findVariable(
      grpName, ATAG, vara, varsb, useVerbose, bugs, indent);
  }
  for (Variable varb : varsb) {
    numDiff += findVariable(
      grpName, BTAG, varb, varsa, useVerbose, bugs, indent);
  }
  if (orderFlag && varsa.length == varsb.length) {
    for (int ii = 0; ii < varsa.length; ii++) {
      if (! varsa[ii].getName().equals( varsb[ii].getName())) {
        numDiff += 1;
        prtIndent(
          indent, grpName,
          "Diff: variables out of order: %s  %s",
          varsa[ii].getName(), varsb[ii].getName());
      }
    }
  }


  Group[] subGroupsa = inGroupa.getGroups().toArray( new Group[0]);
  Group[] subGroupsb = inGroupb.getGroups().toArray( new Group[0]);

  if (skipUnder) {
    Group[] tmpSubGroupsa = new Group[ subGroupsa.length];
    int kka = 0;
    for (Group subGroupa : subGroupsa) {
      if (subGroupa.getName().startsWith("_"))
        prtIndent( indent, inGroupa.getName(),
        "skipping group: %s", subGroupa.getName());
      else tmpSubGroupsa[kka++] = subGroupa;
    }
    subGroupsa = Arrays.copyOf( tmpSubGroupsa, kka);

    Group[] tmpSubGroupsb = new Group[ subGroupsb.length];
    int kkb = 0;
    for (Group subGroupb : subGroupsb) {
      if (subGroupb.getName().startsWith("_"))
        prtIndent( indent, inGroupb.getName(),
        "skipping group: %s", subGroupb.getName());
      else tmpSubGroupsb[kkb++] = subGroupb;
    }
    subGroupsb = Arrays.copyOf( tmpSubGroupsb, kkb);
  }

  for (Group grpa : subGroupsa) {
    numDiff += findGroup( grpName, ATAG, grpa, subGroupsb, bugs, indent);
  }
  for (Group grpb : subGroupsb) {
    numDiff += findGroup( grpName, BTAG, grpb, subGroupsa, bugs, indent);
  }
  if (orderFlag && subGroupsa.length == subGroupsb.length) {
    for (int ii = 0; ii < subGroupsa.length; ii++) {
      if (! subGroupsa[ii].getName().equals( subGroupsb[ii].getName())) {
        numDiff += 1;
        prtIndent(
          indent, grpName,
          "Diff: subGroups out of order: %s  %s",
          subGroupsa[ii].getName(), subGroupsb[ii].getName());
      }
    }
  }

  // Recursion
  for (Group grpa : subGroupsa) {
    for (Group grpb : subGroupsb) {
      if (grpb.getName().equals( grpa.getName())) {
        numDiff += compareGroups(
          tag + "/" + grpa.getName(),     // tag
          grpa,
          grpb,
          orderFlag,
          skipUnder,
          useVerbose,
          bugs,
          indent + 1);
      }
    }
  }

  return numDiff;
} // end compareGroups










static int findDimension(
  String grpName,
  String tag,
  Dimension dima,
  Dimension[] dimsb,
  int bugs,
  int indent)
throws Exception
{
  int numDiff = 0;
  boolean foundIt = false;
  for (Dimension dimb : dimsb) {
    if (dimb.getName().equals( dima.getName())) {
      foundIt = true;
      if (dima.getLength() != dimb.getLength()) {
        prtIndent(
          indent, grpName,
          "Diff: dim \"%s\" has different lengths:  %d  %d",
          dima.getName(), dima.getLength(), dimb.getLength());
      }
    }
  }
  if (! foundIt) {
    numDiff += 1;
    prtIndent( indent, grpName,
      "Diff: file %s  has extra dim: \"%s\"",
      tag, dima.getName());
  }
  return numDiff;
} // end findDimension







static int findAttr(
  String grpName,
  String tag,                // ATAG or BTAG
  Attribute attra,
  Attribute[] attrsb,
  boolean useVerbose,
  int bugs,
  int indent)
throws Exception
{
  int numDiff = 0;
  boolean foundIt = false;
  for (Attribute attrb : attrsb) {
    if (attrb.getName().equals( attra.getName())) {
      foundIt = true;
      if (tag.equals( ATAG)) {
        int numEleDiff = compareArrays(
          grpName,
          "attribute " + attra.getName(),    // tag
          attra.getValues(),
          attrb.getValues(),
          useVerbose,
          bugs,
          indent);
        if (numEleDiff != 0) numDiff += 1;
      }
    }
  }
  if (! foundIt) {
    numDiff += 1;
    prtIndent(
      indent, grpName,
      "Diff: file %s  has extra attr: \"%s\"",
      tag,
      attra.getName());
  }
  return numDiff;
} // end findAttr











static int findVariable(
  String grpName,
  String tag,                // ATAG or BTAG
  Variable vara,
  Variable[] varsb,
  boolean useVerbose,
  int bugs,
  int indent)
throws Exception
{
  int numDiff = 0;
  boolean foundIt = false;
  for (Variable varb : varsb) {
    if (varb.getName().equals( vara.getName())) {
      foundIt = true;
      if (tag.equals( ATAG)) {
        numDiff += compareVariables(
          grpName, vara, varb, useVerbose, bugs, indent);
      }
    }
  }
  if (! foundIt) {
    numDiff += 1;
    prtIndent(
      indent, grpName,
      "Diff: file %s  has extra variable: \"%s\"",
      tag,
      vara.getName());
  }
  return numDiff;
} // end findAttr









static int findGroup(
  String grpName,
  String tag,
  Group grpa,
  Group[] grpsb,
  int bugs,
  int indent)
throws Exception
{
  int numDiff = 0;
  boolean foundIt = false;
  for (Group grpb : grpsb) {
    if (grpb.getName().equals( grpa.getName())) {
      foundIt = true;
    }
  }
  if (! foundIt) {
    numDiff += 1;
    prtIndent(
      indent, grpName,
      "Diff: file %s  has extra group: \"%s\"",
      tag,
      grpa.getName());
  }
  return numDiff;
} // end findAttr






// Returns 0 if identical; 1 otherwise.

static int compareVariables(
  String grpName,
  Variable vara,
  Variable varb,
  boolean useVerbose,
  int bugs,
  int indent)
throws Exception
{
  int numDiff = 0;
  if (! vara.getName().equals( varb.getName()))
    throwerr("name mismatch: internal error");
  DataType tpa = vara.getDataType();
  DataType tpb = varb.getDataType();
  if (tpa != tpb) {
    numDiff += 1;
    prtIndent( indent, grpName,
      "Diff: variable %s  types differ:  %s  %s",
      vara.getName(), tpa, tpb);
  }
  else {     // else types match
    if (vara.getRank() != varb.getRank()) {
      numDiff += 1;
      prtIndent( indent, grpName,
        "Diff: variable %s  ranks differ:  %d  %d",
        grpName, vara.getName(), vara.getRank(), varb.getRank());
    }
    else {        // else ranks match
      boolean isOk = true;
      for (int ii = 0; ii < vara.getRank(); ii++) {
        if (vara.getShape()[ii] != varb.getShape()[ii]) isOk = false;
      }
      if (! isOk) {
        numDiff += 1;
        prtIndent( indent, grpName,
          "Diff: variable %s  shapes differ:  [%s]  [%s]",
          vara.getName(),
          formatInts( vara.getShape()),
          formatInts( varb.getShape()));
      }
      else {
        Array arra = vara.read();
        Array arrb = varb.read();
        int numEleDiff = compareArrays(
          grpName,
          "variable " + vara.getName(),   // tag
          arra,
          arrb,
          useVerbose,
          bugs,
          indent);
        if (numEleDiff != 0) numDiff += 1;
      }
    } // else ranks match
  } // else types match
  return numDiff;                // 0 or 1
}









// Returns 0 if identical; 1 if rank shape or type mismatch;
// num of data mismatches otherwise.

static int compareArrays(
  String grpName,
  String tag,         // like "attribute alpha" or "variable beta"
  Array arra,
  Array arrb,
  boolean useVerbose,
  int bugs,
  int indent)
throws Exception
{
  int numEleDiff = 0;

  // The check for ranks and shapes is redundant with
  // compareVariables, above, but no problem.

  if (arra.getRank() != arrb.getRank()) {
    numEleDiff += 1;
    prtIndent( indent, grpName,
      "Diff: %s  ranks differ:  %d  %d",
      tag,
      arra.getRank(),
      arrb.getRank());
  }

  else {        // else ranks match
    boolean isOk = true;
    for (int ii = 0; ii < arra.getRank(); ii++) {
      if (arra.getShape()[ii] != arrb.getShape()[ii]) isOk = false;
    }
    if (! isOk) {
      numEleDiff += 1;
      prtIndent( indent, grpName,
        "Diff: %s  shapes differ:  [%s]  [%s]",
        tag,
        formatInts( arra.getShape()),
        formatInts( arrb.getShape()));
    }

    else {          // else shapes match
      Class tpa = arra.getElementType();
      Class tpb = arrb.getElementType();
      if (tpa != tpb) {
        numEleDiff += 1;
        prtIndent( indent, grpName,
          "Diff: %s  element types differ:  %s  %s",
          tag, tpa, tpb);
      }
      else {      // else types match

        long totSize = arra.getSize();
        int iFirstDiff = -1;
        if (tpa == Byte.TYPE
          || tpa == Character.TYPE
          || tpa == Short.TYPE
          || tpa == Integer.TYPE
          || tpa == Long.TYPE)
        {
          for (int ii = 0; ii < totSize; ii++) {
            long vala = arra.getLong(ii);
            long valb = arrb.getLong(ii);
            if (vala != valb) {
              if (iFirstDiff < 0) iFirstDiff = ii;
              numEleDiff += 1;
              if (useVerbose) {
                prtIndent( indent + 4, grpName,
                  "Diff detail: %s  value differs:  %s  %s",
                  tag, vala, valb);
              }
            }
          }
        } // if Byte or Char or ...

        else if (tpa == Float.TYPE || tpa == Double.TYPE)
        {
          for (int ii = 0; ii < totSize; ii++) {
            double vala = arra.getDouble(ii);
            double valb = arrb.getDouble(ii);
            if (vala != valb) {
              if (iFirstDiff < 0) iFirstDiff = ii;
              numEleDiff += 1;
              if (useVerbose) {
                prtIndent( indent + 4, grpName,
                  "Diff detail: %s  value differs:  %s  %s",
                  tag, vala, valb);
              }
            }
          }
        } // if Float or Double

        else if (tpa == "".getClass()) {       // if String
          for (int ii = 0; ii < totSize; ii++) {
            String vala = (String) arra.getObject(ii);
            String valb = (String) arrb.getObject(ii);
            if (! vala.equals( valb)) {
              if (iFirstDiff < 0) iFirstDiff = ii;
              numEleDiff += 1;
              if (useVerbose) {
                prtIndent( indent + 4, grpName,
                  "Diff detail: %s  value differs:  \"%s\"  \"%s\"",
                  tag, vala, valb);
              }
            }
          }
        } // if String
        else throwerr("unknown type: " + tpa);

        if (iFirstDiff >= 0) {
          prtIndent( indent, grpName,
            "Diff: %s  data differ.  Num diffs: %d  First diff at index %d",
            tag, numEleDiff, iFirstDiff);
        }
      } // else types match
    } // else shapes match
  } // else ranks match
  return numEleDiff;
} // end compareArrays





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
  for (int ii = 0; ii < vals.length; ii++) {
    if (ii > 0) res += " ";
    res += vals[ii];
  }
  return res;
}




static void throwerr( String msg, Object... args)
throws Exception
{
  throw new Exception( String.format( msg, args));
}




static void prtIndent(
  int indent,
  String grpName,
  String msg,
  Object... args)
{
  String indentStg = "";
  for (int ii = 0; ii < indent; ii++) {
    indentStg += "  ";
  }
  System.out.printf(
    indentStg + "Group " + grpName + ": " + msg + "\n",
    args);
}


static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}



} // end class

