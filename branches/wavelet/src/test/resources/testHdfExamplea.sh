#!/bin/sh


# Tests the simple example hdf/ExampleSimple.java


BUILDDIR=../../../target/test-classes:../../../target/classes
PKGBASE=edu.ucar.ral.nujan
TESTDIR=.
H5CHECK=/d1/steves/ftp/hdf5/tdi/bin/h5check

cmd="java -cp ${BUILDDIR} \
  ${PKGBASE}.hdf.ExampleSimple \
  -outFile tempa.h5"

if [ "$bugs" != "none" ]; then echo "cmd: $cmd"; fi
configMsg="./testHdfSyn.sh $chunk $compress $dtype $rank"

$cmd > tempa.log
if [ "$?" -ne "0" ]; then
  echo "Cmd failed"
  exit 1
fi

$H5CHECK tempa.h5 > /dev/null
if [ "$?" -ne "0" ]; then
  echo "$H5CHECK failed"
  exit 1
fi

echo "all ok"
