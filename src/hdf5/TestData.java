
package hdfnetTest;

import hdfnet.HdfFile;
import hdfnet.HdfGroup;
import hdfnet.HdfException;


// Create test data.
//   genHdfData is used by Testa.java and netcdf/TestNetcdfa.java.
//   genNcData is a thin wrapper to interpret type strings like "short",
//     and is used by netcdf/TestNetcdfa.java.


public class TestData {



// Used by TestNetcdfa.
// Throws no exceptions.

public static Object[] genNcData(
  String typeStg,
  int[] dims)         // if len==0, make a scalar.
{
  Object[] testData = null;
  int dtype = 0;
  try {
    if (typeStg.equals("byte")) dtype = HdfGroup.DTYPE_FIXED08;
    else if (typeStg.equals("short")) dtype = HdfGroup.DTYPE_FIXED16;
    else if (typeStg.equals("int")) dtype = HdfGroup.DTYPE_FIXED32;
    else if (typeStg.equals("long")) dtype = HdfGroup.DTYPE_FIXED64;
    else if (typeStg.equals("float")) dtype = HdfGroup.DTYPE_FLOAT32;
    else if (typeStg.equals("double")) dtype = HdfGroup.DTYPE_FLOAT64;
    else if (typeStg.equals("string")) dtype = HdfGroup.DTYPE_STRING_FIX;
    else throwerr("genNcData: unknown typeStg: \"%s\"", typeStg);

    testData = genHdfData( dtype, dims, null);   // refGroup = null
  }
  catch( HdfException exc) {
    exc.printStackTrace();
    System.exit(1);                // *********** quit now ***************
  }
  return testData;
}





public static Object[] genHdfData(
  int dtype,
  int[] dims,
  HdfGroup refGroup)              // group to use for references
throws HdfException
{
  int rank = dims.length;
  Object vdata = null;
  Object fillValue = null;

  if (dtype == HdfGroup.DTYPE_FIXED08) {
    fillValue = new Byte( (byte) 99);
    if (rank == 0) vdata = new Byte( mkByte0( dims, 0, 0));
    else if (rank == 1) vdata = mkByte1( dims, 0, 0);
    else if (rank == 2) vdata = mkByte2( dims, 0, 0);
    else if (rank == 3) vdata = mkByte3( dims, 0, 0);
    else if (rank == 4) vdata = mkByte4( dims, 0, 0);
    else if (rank == 5) vdata = mkByte5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_FIXED16) {
    fillValue = new Short( (short) 999);
    if (rank == 0) vdata = new Short( mkShort0( dims, 0, 0));
    else if (rank == 1) vdata = mkShort1( dims, 0, 0);
    else if (rank == 2) vdata = mkShort2( dims, 0, 0);
    else if (rank == 3) vdata = mkShort3( dims, 0, 0);
    else if (rank == 4) vdata = mkShort4( dims, 0, 0);
    else if (rank == 5) vdata = mkShort5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_FIXED32) {
    fillValue = new Integer(999);
    if (rank == 0) vdata = new Integer( mkInt0( dims, 0, 0));
    else if (rank == 1) vdata = mkInt1( dims, 0, 0);
    else if (rank == 2) vdata = mkInt2( dims, 0, 0);
    else if (rank == 3) vdata = mkInt3( dims, 0, 0);
    else if (rank == 4) vdata = mkInt4( dims, 0, 0);
    else if (rank == 5) vdata = mkInt5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_FIXED64) {
    fillValue = new Long(999);
    if (rank == 0) vdata = new Long( mkLong0( dims, 0, 0));
    else if (rank == 1) vdata = mkLong1( dims, 0, 0);
    else if (rank == 2) vdata = mkLong2( dims, 0, 0);
    else if (rank == 3) vdata = mkLong3( dims, 0, 0);
    else if (rank == 4) vdata = mkLong4( dims, 0, 0);
    else if (rank == 5) vdata = mkLong5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_FLOAT32) {
    fillValue = new Float(999);
    if (rank == 0) vdata = new Float( mkFloat0( dims, 0, 0));
    else if (rank == 1) vdata = mkFloat1( dims, 0, 0);
    else if (rank == 2) vdata = mkFloat2( dims, 0, 0);
    else if (rank == 3) vdata = mkFloat3( dims, 0, 0);
    else if (rank == 4) vdata = mkFloat4( dims, 0, 0);
    else if (rank == 5) vdata = mkFloat5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_FLOAT64) {
    fillValue = new Double(999);
    if (rank == 0) vdata = new Double( mkDouble0( dims, 0, 0));
    else if (rank == 1) vdata = mkDouble1( dims, 0, 0);
    else if (rank == 2) vdata = mkDouble2( dims, 0, 0);
    else if (rank == 3) vdata = mkDouble3( dims, 0, 0);
    else if (rank == 4) vdata = mkDouble4( dims, 0, 0);
    else if (rank == 5) vdata = mkDouble5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_STRING_FIX
    || dtype == HdfGroup.DTYPE_STRING_VAR)
  {
    fillValue = new String("999");
    if (rank == 0) vdata = mkString0( dims, 0, 0);
    else if (rank == 1) vdata = mkString1( dims, 0, 0);
    else if (rank == 2) vdata = mkString2( dims, 0, 0);
    else if (rank == 3) vdata = mkString3( dims, 0, 0);
    else if (rank == 4) vdata = mkString4( dims, 0, 0);
    else if (rank == 5) vdata = mkString5( dims, 0, 0);
    else throwerr("unknown rank: " + rank);
  }
  else if (dtype == HdfGroup.DTYPE_REFERENCE) {
    fillValue = null;
    if (rank == 0) vdata = mkReference0( dims, 0, 0, refGroup);
    else if (rank == 1) vdata = mkReference1( dims, 0, 0, refGroup);
    else if (rank == 2) vdata = mkReference2( dims, 0, 0, refGroup);
    else if (rank == 3) vdata = mkReference3( dims, 0, 0, refGroup);
    else if (rank == 4) vdata = mkReference4( dims, 0, 0, refGroup);
    else if (rank == 5) vdata = mkReference5( dims, 0, 0, refGroup);
    else throwerr("unknown rank: " + rank);
  }
  else throwerr("unknown dtype B: " + dtype);
  return new Object[] { vdata, fillValue};
} // end genHdfData








static byte mkByte0( int[] dims, int idim, int ix) {
  return (byte) (ix+1);
}

static byte[] mkByte1( int[] dims, int idim, int ix) {
  byte[] vv = new byte[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkByte0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static byte[][] mkByte2( int[] dims, int idim, int ix) {
  byte[][] vv = new byte[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkByte1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static byte[][][] mkByte3( int[] dims, int idim, int ix) {
  byte[][][] vv = new byte[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkByte2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static byte[][][][] mkByte4( int[] dims, int idim, int ix) {
  byte[][][][] vv = new byte[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkByte3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static byte[][][][][] mkByte5( int[] dims, int idim, int ix) {
  byte[][][][][] vv = new byte[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    ///vv[jj] = mkByte4( dims, idim+1, 10*(ix+1)+jj);
    vv[jj] = mkByte4( dims, idim+1, 0);
  }
  return vv;
}









static short mkShort0( int[] dims, int idim, int ix) {
  return (short) (ix+1);
}

static short[] mkShort1( int[] dims, int idim, int ix) {
  short[] vv = new short[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkShort0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static short[][] mkShort2( int[] dims, int idim, int ix) {
  short[][] vv = new short[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkShort1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static short[][][] mkShort3( int[] dims, int idim, int ix) {
  short[][][] vv = new short[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkShort2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static short[][][][] mkShort4( int[] dims, int idim, int ix) {
  short[][][][] vv = new short[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkShort3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static short[][][][][] mkShort5( int[] dims, int idim, int ix) {
  short[][][][][] vv = new short[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    ///vv[jj] = mkShort4( dims, idim+1, 10*(ix+1)+jj);
    vv[jj] = mkShort4( dims, idim+1, 0);
  }
  return vv;
}










static int mkInt0( int[] dims, int idim, int ix) {
  return (int) (ix+1);
}

static int[] mkInt1( int[] dims, int idim, int ix) {
  int[] vv = new int[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkInt0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static int[][] mkInt2( int[] dims, int idim, int ix) {
  int[][] vv = new int[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkInt1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static int[][][] mkInt3( int[] dims, int idim, int ix) {
  int[][][] vv = new int[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkInt2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static int[][][][] mkInt4( int[] dims, int idim, int ix) {
  int[][][][] vv = new int[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkInt3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static int[][][][][] mkInt5( int[] dims, int idim, int ix) {
  int[][][][][] vv = new int[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkInt4( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}








static long mkLong0( int[] dims, int idim, int ix) {
  return (long) (ix+1);
}

static long[] mkLong1( int[] dims, int idim, int ix) {
  long[] vv = new long[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkLong0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static long[][] mkLong2( int[] dims, int idim, int ix) {
  long[][] vv = new long[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkLong1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static long[][][] mkLong3( int[] dims, int idim, int ix) {
  long[][][] vv = new long[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkLong2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static long[][][][] mkLong4( int[] dims, int idim, int ix) {
  long[][][][] vv = new long[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkLong3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static long[][][][][] mkLong5( int[] dims, int idim, int ix) {
  long[][][][][] vv = new long[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkLong4( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}











static float mkFloat0( int[] dims, int idim, int ix) {
  return (float) (ix+1);
}

static float[] mkFloat1( int[] dims, int idim, int ix) {
  float[] vv = new float[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkFloat0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static float[][] mkFloat2( int[] dims, int idim, int ix) {
  float[][] vv = new float[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkFloat1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static float[][][] mkFloat3( int[] dims, int idim, int ix) {
  float[][][] vv = new float[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkFloat2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static float[][][][] mkFloat4( int[] dims, int idim, int ix) {
  float[][][][] vv = new float[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkFloat3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static float[][][][][] mkFloat5( int[] dims, int idim, int ix) {
  float[][][][][] vv = new float[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkFloat4( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}








static double mkDouble0( int[] dims, int idim, int ix) {
  return (double) (ix+1);
}

static double[] mkDouble1( int[] dims, int idim, int ix) {
  double[] vv = new double[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkDouble0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static double[][] mkDouble2( int[] dims, int idim, int ix) {
  double[][] vv = new double[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkDouble1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static double[][][] mkDouble3( int[] dims, int idim, int ix) {
  double[][][] vv = new double[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkDouble2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static double[][][][] mkDouble4( int[] dims, int idim, int ix) {
  double[][][][] vv = new double[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkDouble3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static double[][][][][] mkDouble5( int[] dims, int idim, int ix) {
  double[][][][][] vv = new double[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkDouble4( dims, idim+1, 0);
  }
  return vv;
}










static String mkString0( int[] dims, int idim, int ix) {
  // Make string like  aaa37
  String res = "";
  for (int ii = 0; ii < ix % 4; ii++) {
    res += "a";
  }
  res += String.format("%d", ix + 1);
  return res;
}

static String[] mkString1( int[] dims, int idim, int ix) {
  String[] vv = new String[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkString0( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static String[][] mkString2( int[] dims, int idim, int ix) {
  String[][] vv = new String[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkString1( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static String[][][] mkString3( int[] dims, int idim, int ix) {
  String[][][] vv = new String[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkString2( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static String[][][][] mkString4( int[] dims, int idim, int ix) {
  String[][][][] vv = new String[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkString3( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}

static String[][][][][] mkString5( int[] dims, int idim, int ix) {
  String[][][][][] vv = new String[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkString4( dims, idim+1, 10*(ix+1)+jj);
  }
  return vv;
}




static HdfGroup mkReference0(
  int[] dims, int idim, int ix, HdfGroup grp)
{
  return grp;
}

static HdfGroup[] mkReference1(
  int[] dims, int idim, int ix, HdfGroup grp)
{
  HdfGroup[] vv = new HdfGroup[dims[idim]];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkReference0( dims, idim+1, 10*(ix+1)+jj, grp);
  }
  return vv;
}

static HdfGroup[][] mkReference2(
  int[] dims, int idim, int ix, HdfGroup grp)
{
  HdfGroup[][] vv = new HdfGroup[dims[idim]][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkReference1( dims, idim+1, 10*(ix+1)+jj, grp);
  }
  return vv;
}

static HdfGroup[][][] mkReference3(
  int[] dims, int idim, int ix, HdfGroup grp)
{
  HdfGroup[][][] vv = new HdfGroup[dims[idim]][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkReference2( dims, idim+1, 10*(ix+1)+jj, grp);
  }
  return vv;
}

static HdfGroup[][][][] mkReference4(
  int[] dims, int idim, int ix, HdfGroup grp)
{
  HdfGroup[][][][] vv = new HdfGroup[dims[idim]][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkReference3( dims, idim+1, 10*(ix+1)+jj, grp);
  }
  return vv;
}

static HdfGroup[][][][][] mkReference5(
  int[] dims, int idim, int ix, HdfGroup grp)
{
  HdfGroup[][][][][] vv = new HdfGroup[dims[idim]][][][][];
  for (int jj = 0; jj < dims[idim]; jj++) {
    vv[jj] = mkReference4( dims, idim+1, 10*(ix+1)+jj, grp);
  }
  return vv;
}






static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}



static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}


} // end class
