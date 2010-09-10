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


package edu.ucar.ral.nujan.hdf;


/**
 * Implements the samed checksum hack as used by the HDF5 C code.
 * There is so much well published, well established
 * theory for strong checksums.  Why use a hack like this?
 * <p>
 * For the hack details, see the HDF5 file: H5checksum.c
 */

class CheckSumHack {

int aa;
int bb;
int cc;

int rotate( int xx, int len) {
  return (xx << len) ^ (xx >>> (32 - len));
}



void mixStd() {
  aa -= cc;  aa ^= rotate( cc,  4);  cc += bb;
  bb -= aa;  bb ^= rotate( aa,  6);  aa += cc;
  cc -= bb;  cc ^= rotate( bb,  8);  bb += aa;
  aa -= cc;  aa ^= rotate( cc, 16);  cc += bb;
  bb -= aa;  bb ^= rotate( aa, 19);  aa += cc;
  cc -= bb;  cc ^= rotate( bb,  4);  bb += aa;
}


void mixFinal() {
  cc ^= bb;  cc -= rotate( bb, 14);
  aa ^= cc;  aa -= rotate( cc, 11);
  bb ^= aa;  bb -= rotate( aa, 25);
  cc ^= bb;  cc -= rotate( bb, 16);
  aa ^= cc;  aa -= rotate( cc,  4);
  bb ^= aa;  bb -= rotate( aa, 14);

  cc ^= bb;  cc -= rotate( bb, 24);
}


/**
 * Returns the checksum of bytes.
 * For the hack details, see the HDF5 file: H5checksum.c
 */

int calcHackSum(
  byte[] bytes)
{
  int bugs = 0;
  if (bugs >= 1) prtf("calcHackSum: len: %d", bytes.length);
  if (bugs >= 5) {
    for (int ii = 0; ii < bytes.length; ii++) {
      prtf("  ii: %d  byte: %d  '%c'", ii, 0xff & bytes[ii], 0xff & bytes[ii]);
    }
  }

  int initval = 0;
  aa = 0xdeadbeef + bytes.length + initval;
  bb = aa;
  cc = aa;

  int ix = 0;
  while (ix < bytes.length - 12) {
    aa += (0xff & bytes[ix + 0]);
    aa += (0xff & bytes[ix + 1]) <<  8;
    aa += (0xff & bytes[ix + 2]) << 16;
    aa += (0xff & bytes[ix + 3]) << 24;
    ix += 4;

    bb += (0xff & bytes[ix + 0]);
    bb += (0xff & bytes[ix + 1]) <<  8;
    bb += (0xff & bytes[ix + 2]) << 16;
    bb += (0xff & bytes[ix + 3]) << 24;
    ix += 4;

    cc += (0xff & bytes[ix + 0]);
    cc += (0xff & bytes[ix + 1]) <<  8;
    cc += (0xff & bytes[ix + 2]) << 16;
    cc += (0xff & bytes[ix + 3]) << 24;
    ix += 4;

    if (bugs >= 5) {
      prtf("    apremix: %d", aa);
      prtf("    bpremix: %d", bb);
      prtf("    cpremix: %d", cc);
    }

    mixStd();
    if (bugs >= 5) {
      prtf("    amix: %d", aa);
      prtf("    bmix: %d", bb);
      prtf("    cmix: %d", cc);
    }
  }


  if (bugs >= 5) prtf("    final rem length: %d", bytes.length - ix);
  switch (bytes.length - ix) {
    case 12: cc += (0xff & bytes[ ix + 11]) << 24;
    case 11: cc += (0xff & bytes[ ix + 10]) << 16;
    case 10: cc += (0xff & bytes[ ix +  9]) <<  8;
    case  9: cc += (0xff & bytes[ ix +  8]);

    case  8: bb += (0xff & bytes[ ix +  7]) << 24;
    case  7: bb += (0xff & bytes[ ix +  6]) << 16;
    case  6: bb += (0xff & bytes[ ix +  5]) <<  8;
    case  5: bb += (0xff & bytes[ ix +  4]);

    case  4: aa += (0xff & bytes[ ix +  3]) << 24;
    case  3: aa += (0xff & bytes[ ix +  2]) << 16;
    case  2: aa += (0xff & bytes[ ix +  1]) <<  8;
    case  1: aa += (0xff & bytes[ ix +  0]);
  }

  if (bugs >= 5) {
    prtf("    aprefinal: %d", aa);
    prtf("    bprefinal: %d", bb);
    prtf("    cprefinal: %d", cc);
  }

  mixFinal();

  if (bugs >= 5) {
    prtf("    afinal: %d", aa);
    prtf("    bfinal: %d", bb);
    prtf("    cfinal: %d", cc);
  }
  
  return cc;
} // end calcHackSum


static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class


