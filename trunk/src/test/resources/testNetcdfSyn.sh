#!/bin/sh


# Validates the NetCDF4 layer using synthetic data.
#
# Runs TestNetcdfa.java on various configurations and compares the
# output with saved known good files.
#
# For usage, see badparms below.
#
# To compare a new netcdf type (int and double, for example):
#   cd ../netcdf
#   for ii in 0 1 2 3 4 5; do diff <(sed -e '1,/ncdump/ d'  testVerif/test.int.rank.$ii.out) <(sed -e '1,/ncdump/ d'  testVerif/test.double.rank.$ii.out) | less; done


NCJAR=/d1/steves/ftp/netcdfJava/netcdfAll-4.2.jar
BUILDDIR=../../../target/test-classes:../../../target/classes
PKGBASE=edu.ucar.ral.nujan
TESTDIR=.


badparms() {
  echo Error: $1
  echo Parms:
  echo "  compress:  all / compressLevel (0==none, 1 - 9)"
  echo "  nhType:    sbyte ubyte short int long float double char vstring"
  echo "  rank:      all/0/1/2/3/4/5"
  echo "  bugs       optional: none / echo / continue / update"
  echo "               none: no debug msgs"
  echo "               echo: some debug msgs"
  echo "               continue: continue even if diff errors"
  echo "               update: update verification results - Caution"
  echo ""
  echo "Examples:"
  echo "./testNetcdfSyn.sh 0 short 1"
  echo "./testNetcdfSyn.sh 5 all 2"
  echo "./testNetcdfSyn.sh all all all"
  exit 1
}



if [ $# -ne 3 -a $# -ne 4 ]; then badparms "main: wrong num parms"; fi

compressSpec=$1
nhTypeSpec=$2
rankSpec=$3
bugs=none
if [ $# -eq 4 ]; then bugs=$4; fi


if [ "$compressSpec" == "all" ]; then compressVals="0 5"
else compressVals="$compressSpec"
fi

if [ "$nhTypeSpec" == "all" ]; then
  nhTypes="sbyte ubyte short int long float double char vstring"
else nhTypes=$nhTypeSpec
fi

if [ "$rankSpec" == "all" ]; then ranks="0 1 2 3 4 5 6 7"
else ranks=$rankSpec
fi



echo "compressVals: $compressVals"
echo "nhTypes: $nhTypes"
echo "ranks: $ranks"



exitUnlessContinue() {
  if [ $# -ne 1 ]; then
    badparms "exitUnlessContinue: wrong num parms"
  fi
  bugs=$1
  if [ "$bugs" != "continue" -a "$bugs" != "update" ]; then
    exit 1
  fi
}



testOne() {
  if [ $# -ne 3 ]; then
    badparms "testOne: wrong num parms"
  fi
  compress=$1
  nhType=$2
  rank=$3

  if [ "$compress" == "0" ]; then chunk="contiguous"
  else chunk="chunked";
  fi
    
  if [ "$bugs" != "none" ]; then
    echo "testOne: compress: $compress  chunk: $chunk"
    echo "  nhType: $nhType  rank: $rank"
  fi

  if [[ "$rank" == "0" ]]; then
    dims="scalar"
    chunkLens="contiguous"
  elif [[ "$rank" == "1" ]]; then
    dims="3"
    if [[ "$nhType" == "float" ]]; then chunkLens="1"
    else chunkLens="3"
    fi
  elif [[ "$rank" == "2" ]]; then
    dims="3,4"
    if [[ "$nhType" == "float" ]]; then
      # Both chunk lens do not evenly divide the variable's dimensions.
      chunkLens="2,3"
    else chunkLens="3,4"
    fi
  elif [[ "$rank" == "3" ]]; then
    dims="3,4,5"
    chunkLens="3,4,5"
  elif [[ "$rank" == "4" ]]; then
    dims="3,4,5,2"
    chunkLens="3,4,5,2"
  elif [[ "$rank" == "5" ]]; then
    dims="3,4,5,2,3"
    chunkLens="3,4,5,2,3"
  elif [[ "$rank" == "6" ]]; then
    dims="3,4,5,2,3,2"
    chunkLens="3,4,5,2,3,2"
  elif [[ "$rank" == "7" ]]; then
    dims="3,4,5,2,3,2,3"
    chunkLens="3,4,5,2,3,2,3"
  else badparms "invalid rank: $rank"
  fi

  if [[ "$chunk" == "contiguous" ]]; then
    chunkLens="contiguous"
  fi

  if [ "$compress" != "0" -a "$nhType" == "vstring" ]; then
    # Cannot compress vstring because vstrings are kept on
    # the global heap, and hdf5 compresses only the references
    # to the strings, not the strings themselves.
    # So we don't allow compression of vstrings.
    echo "Cannot compress vstring (rank $rank) ... ignoring"
  elif [ "$compress" != "0" -a "$rank" == "0" ]; then
    echo "Cannot compress scalar data ... ignoring"
  ##elif [ "$nhType" == "vstring" -a "$rank" != "0" ]; then
  ##  echo "String arrays foobared by unidata: see [netCDFJava #TKT-143293]: 16 June 2010 ... ignoring"
  else

    /bin/rm -f tempa.nc

    cmd="java -cp ${BUILDDIR}:${NCJAR} \
      ${PKGBASE}.netcdfTest.Tnetcdfa \
      -bugs 10 \
      -nhType $nhType \
      -dims $dims \
      -chunks $chunkLens \
      -compress $compress \
      -utcModTime 0 \
      -numThread 1 \
      -outFile tempa.nc"

    if [ "$bugs" != "none" ]; then echo "cmd: $cmd"; fi
    configMsg="./testNetcdfSyn.sh $compress $nhType $rank"

    $cmd > tempa.log
    if [ "$?" -ne "0" ]; then
      echo "Cmd failed for"
      echo "  config: $configMsg"
      echo "  cmd: $cmd"
      exit 1
    fi

    echo "  test: $configMsg  size: $(wc -c tempa.nc | cut -f 1 -d ' ')"

    oldTxt=${TESTDIR}/testNetcdfSynOut/test.$nhType.rank.$rank.out.gz
    ###xxx replace with CompareNh.java
    testComparePair.sh $bugs $chunk $compress $oldTxt tempa.nc


    ###compareCmd="java -cp ${BUILDDIR}:${NCJAR} \
    ###  ${PKGBASE}.netcdfTest.NhCompare \
    ###  -bugs 0 \
    ###  -order y \
    ###  -skipUnder y \
    ###  -verbose n \
    ###  -inFilea tempa.nc \
    ###  -inFileb testNetcdfSynOut/test.v2.$nhType.compress.$compress.rank.$rank.nc"
    #xxx rename above to del v2; get rid of all v1 files

    ###if [ "$bugs" != "none" ]; then echo "compareCmd: $compareCmd"; fi
    ###$compareCmd > tempb.log
    diffOk=$?

    # If update, copy to testSynOut
    if [ "$bugs" == "update" ]; then
      gzip -c tempout.newf > $oldTxt
      echo '*** updated ***'
    fi

    if [ "$diffOk" -ne "0" ]; then
      echo "testComparePair on original failed for:"
      echo "  config: $configMsg"
      echo "  cmd: $cmd"
      echo "  compareCmd: $compareCmd"
      echo "  copyCmd: $copyCmd"
      exitUnlessContinue $bugs
    fi
    ############xxxxx /bin/cp tempa.nc testNetcdfSynOut/test.v2.$nhType.compress.$compress.rank.$rank.nc
    #xxx rename above to del v2; get rid of all v1 files

    # Test NhCopy
    useNhCopy=0
    if [[ "$useNhCopy" == "1" ]]; then
      /bin/rm -f tempb.nc
      copyCmd="java -cp ${BUILDDIR}:${NCJAR} \
        ${PKGBASE}.netcdfTest.NhCopy \
        -bugs 0 \
        -compress $compress \
        -inFile tempa.nc \
        -outFile tempb.nc"
      if [ "$bugs" != "none" ]; then echo "copyCmd: $copyCmd"; fi
      $copyCmd > tempc.log
      if [ "$?" -ne "0" ]; then
        echo "NhCopy failed for:"
        echo "  config: $configMsg"
        echo "  cmd: $cmd"
        echo "  copyCmd: $copyCmd"
        exit 1
      fi
      ###xxx replace with CompareNh.java
      ###xxx testComparePair.sh $bugs $compress $oldTxt tempb.nc
      if [ "$?" -ne "0" ]; then
        echo "testComparePair on copy failed for:"
        echo "  config: $configMsg"
        echo "  cmd: $cmd"
        echo "  copyCmd: $copyCmd"
        exitUnlessContinue $bugs
      fi
    fi

  fi

} # end testOne







for compress in $compressVals; do
  for nhType in $nhTypes; do
    for rank in $ranks; do

      testOne $version $compress $nhType $rank

    done
  done
done

