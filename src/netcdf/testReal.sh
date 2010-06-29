#!/bin/sh


# Validation.
#
# Run TestNetcdfa.java on various configurations and compare the
# output with saved known good files.
#
# For usage, see badparms below.
#
# To compare a new netcdf type (int and double, for example):
#   cd ../netcdf
#   for ii in 0 1 2 3 4 5; do diff <(sed -e '1,/ncdump/ d'  testVerif/test.int.rank.$ii.out) <(sed -e '1,/ncdump/ d'  testVerif/test.double.rank.$ii.out) | less; done


NCJAR=/home/ss/ftp/netcdfJava/netcdfAll-4.1.jar
NCJAR=/d1/steves/ftp/netcdfJava/netcdfAll-4.1.jar


badparms() {
  echo Error: $1
  echo Parms:
  echo "  version:   all / v1 / v2"
  echo "  compress:  all / compressLevel (0==none, 1 - 9)"
  echo "  inDir"
  echo "  bugs       optional: none / echo / update"
  echo ""
  echo "Examples:"
  echo "./testall.sh v1 0 short 1"
  exit 1
}



if [ $# -ne 3 -a $# -ne 4 ]; then badparms "wrong num parms"; fi

versionSpec=$1
compressSpec=$2
inDir=$3
bugs=none
if [ $# -eq 4 ]; then bugs=$4; fi


if [ "$versionSpec" == "all" ]; then versions="1 2"
elif [ "$versionSpec" == "v1" ]; then versions="1"
elif [ "$versionSpec" == "v2" ]; then versions="2"
else badparms "invalid ver"
fi

if [ "$compressSpec" == "all" ]; then compressVals="0 5"
else compressVals="$compressSpec"
fi



echo "versions: $versions"
echo "compressVals: $compressVals"
echo "inDir: $inDir"

make all
if [ $? -ne 0 ]; then badparms "make failed"; fi



for version in $versions; do
  for compress in $compressVals; do
    for ifile in $inDir/*; do

      /bin/rm -f tempb.nc
      copyCmd="java -cp tdcls:../hdf5/tdcls:${NCJAR} nhPkgTest.NhCopy -bugs 0 -fileVersion version -compress $compress -inFile tempa.nc -outFile tempb.nc"
      if [ "$bugs" != "none" ]; then echo "copyCmd: $copyCmd"; fi
      $copyCmd > tempb.log
      if [ "$?" -ne "0" ]; then
        echo "NhCopy failed for config: $configMsg"
        echo "  copyCmd: $copyCmd"
        exit 1
      fi

      oldTxt=testRealOut/test.$nhType.rank.$rank.out.gz
      testComparePair.sh std $oldTxt tempb.nc

    done
  done
done



xxx spec version to NhCOpy
