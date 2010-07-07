
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <hdf5.h>
#include <hdf5_hl.h>


// Simple test of references using the HDF5 C API.


void badparms( const char * msg) {
  printf("\nError: %s\n", msg);
  exit(1);
}




void printRes( const char * msg, int ires) {
  printf("%s: %d\n", msg, ires);
  if (ires < 0) {
    printf("bad ires: %d\n", ires);
    H5Eprint2( ires, stdout);
    exit(1);
  }
}



int main( int argc, char * argv[]) {
  int ires;

  if (argc != 2) badparms("must spec outFile");
  const char * fname = argv[1];

  // Create file
  hid_t fileId = H5Fcreate(        // Res is >= 0 if ok.
    fname,
    H5F_ACC_TRUNC,
    H5P_DEFAULT,       // file creation property list id
    H5P_DEFAULT);      // file access property list id
  printRes("create fileId", fileId);

  // Create data space
  hsize_t curDims[1] = {3};
  hid_t spaceId = H5Screate_simple(
    1,           // rank
    curDims,     // dimensions
    NULL);       // maxDims
  printRes("spaceId", spaceId);

  int useRegionRefs = 0;
  hid_t refType;
  if (useRegionRefs) refType = H5T_STD_REF_DSETREG;
  else refType = H5T_STD_REF_OBJ;

  // Create testDs dataset
  char * testDsName = "/testDs";
  hid_t dsId = H5Dcreate(     // macro calls H5Dcreate2
    fileId,           // id of the containing file or group
    testDsName,       // name of dataset within group
    refType,          // H5T_STD_REF_OBJ or H5T_STD_REF_DSETREG
    spaceId,          // dataspace id
    H5P_DEFAULT,      // Link creation property list
    H5P_DEFAULT,      // Dataset creation property list
    H5P_DEFAULT);     // Dataset access property list
  printRes("dsId", dsId);

  // Create dataset "testRefs" with references to itself
  hsize_t refDims[1] = {3};
  hid_t refSpaceId = H5Screate_simple( 1, refDims, NULL);
  printRes("refSpaceId", refSpaceId);


  // Create references
  if (useRegionRefs) {
    hdset_reg_ref_t refs[3];

    hsize_t starts[1];
    hsize_t counts[1];
    starts[0] = 0;
    counts[0] = 2;

    ires = H5Sselect_hyperslab(spaceId,H5S_SELECT_SET,starts,NULL,counts,NULL);
    printRes("hyperslab ires a", ires);

    ires = H5Rcreate( &refs[0], fileId, testDsName, H5R_DATASET_REGION,
      spaceId);
    printRes("ref create ires a", ires);
    ires = H5Rcreate( &refs[1], fileId, testDsName, H5R_DATASET_REGION,
      spaceId);
    printRes("ref create ires b", ires);
    ires = H5Rcreate( &refs[2], fileId, testDsName, H5R_DATASET_REGION,
      spaceId);
    printRes("ref create ires c", ires);

    ires = H5Dwrite( dsId, refType, H5S_ALL, H5S_ALL, H5P_DEFAULT, refs);
    printRes("ref write ires", ires);
  }
  else {
    hobj_ref_t refs[3];

    ires = H5Rcreate( &refs[0], fileId, testDsName, H5R_OBJECT, -1);
    printRes("ref create ires a", ires);
    ires = H5Rcreate( &refs[1], fileId, testDsName, H5R_OBJECT, -1);
    printRes("ref create ires b", ires);
    ires = H5Rcreate( &refs[2], fileId, testDsName, H5R_OBJECT, -1);
    printRes("ref create ires c", ires);

    ires = H5Dwrite( dsId, H5T_STD_REF_OBJ, H5S_ALL, H5S_ALL,
      H5P_DEFAULT, refs);
    printRes("ref write ires", ires);
  }


  ires = H5Dclose( dsId);
  printRes("close dsId ires", ires);

  ires = H5Fclose( fileId);
  printRes("file close ires", ires);

  return 0;
}



