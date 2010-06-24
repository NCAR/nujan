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
  echo "  nhType:    sbyte ubyte short int long float double char vstring"
  echo "  rank:      all/0/1/2/3/4/5"
  echo "  bugs       none / echo / update"
  echo ""
  echo "Examples:"
  echo "./testall.sh v1 0 short 1"
  exit 1
}

if [ $# -ne 4 -a $# -ne 5 ]; then badparms "wrong num parms"; fi

versionSpec=$1
compressSpec=$2
nhTypeSpec=$3
rankSpec=$4
bugs=none
if [ $# -eq 5 ]; then bugs=$5; fi


if [ "$versionSpec" == "all" ]; then versions="1 2"
elif [ "$versionSpec" == "v1" ]; then versions="1"
elif [ "$versionSpec" == "v2" ]; then versions="2"
else badparms "invalid ver"
fi

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



echo "versions: $versions"
echo "compressVals: $compressVals"
echo "nhTypes: $nhTypes"
echo "ranks: $ranks"

make all
if [ $? -ne 0 ]; then badparms "make failed"; fi








testOne() {
  if [ $# -ne 4 ]; then
    badparms "wrong num parms"
  fi
  fileVersion=$1
  compress=$2
  nhType=$3
  rank=$4
  if [ "$bugs" != "none" ]; then
    echo "testOne: vers: $fileVersion  compress: $compress"
    echo "  nhType: $nhType  rank: $rank"
  fi

  if [[ "$rank" == "0" ]]; then dims="0"
  elif [[ "$rank" == "1" ]]; then dims="3"
  elif [[ "$rank" == "2" ]]; then dims="3,4"
  elif [[ "$rank" == "3" ]]; then dims="3,4,5"
  elif [[ "$rank" == "4" ]]; then dims="3,4,5,2"
  elif [[ "$rank" == "5" ]]; then dims="3,4,5,2,3"
  elif [[ "$rank" == "6" ]]; then dims="3,4,5,2,3,2"
  elif [[ "$rank" == "7" ]]; then dims="3,4,5,2,3,2,3"
  else badparms "invalid rank: $rank"
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

    cmd="java -cp tdcls:../hdf5/tdcls \
      nhPkgTest.TestNetcdfa \
      -bugs 10 \
      -nhType $nhType \
      -dims $dims \
      -fileVersion $fileVersion \
      -compress $compress \
      -outFile tempa.nc"

    if [ "$bugs" != "none" ]; then echo "cmd: $cmd"; fi
    configMsg="./testall.sh v$fileVersion $compress $nhType $rank"

    $cmd > tempa.log
    if [ "$?" -ne "0" ]; then
      echo "Cmd failed for config: $configMsg"
      echo "  cmd: $cmd"
      exit 1
    fi

    echo "  test: $configMsg  size: $(wc -c tempa.nc | cut -f 1 -d ' ')"

    oldTxt=testVerif/test.$nhType.rank.$rank.out.gz
    checkOne $oldTxt tempa.nc

    # Copy to testVerif
    if [ "$bugs" == "update" ]; then
      gzip -c tempout.newe > $oldTxt
    fi

    # Test NhCopy
    useNhCopy=1
    if [[ "$useNhCopy" == "1" ]]; then
      /bin/rm -f tempb.nc
      copyCmd="java -cp tdcls:../hdf5/tdcls:${NCJAR} nhPkgTest.NhCopy -bugs 0 -compress $compress -inFile tempa.nc -outFile tempb.nc"
      if [ "$bugs" != "none" ]; then echo "copyCmd: $copyCmd"; fi
      $copyCmd > tempb.log
      if [ "$?" -ne "0" ]; then
        echo "NhCopy failed for config: $configMsg"
        echo "  cmd: $cmd"
        echo "  copyCmd: $copyCmd"
        exit 1
      fi
      checkOne $oldTxt tempb.nc
    fi

  fi

} # end testOne






checkOne() {
  if [ $# -ne 2 ]; then
    badparms "wrong num parms"
  fi
  oldTxt=$1
  newDs=$2

  dumpCmd="h5dump -p -w 10000 $newDs"
  $dumpCmd | sed '1s/HDF5 .*/HDF5 "someFile" {/' > tempout.newa
  if [ "$?" -ne "0" ]; then
    echo "h5dump failed for config: $configMsg"
    echo "  cmd: $cmd"
    echo "  copyCmd: $copyCmd"
    echo "  dumpCmd: $dumpCmd"
    exit 1
  fi

  echo '===== ncdump =====' >> tempout.newa
  dumpCmd="ncdump $newDs"
  $dumpCmd | sed '1s/netcdf .*{/netcdf someFile {/' >> tempout.newa
  if [ "$?" -ne "0" ]; then
    echo "ncdump failed for config: $configMsg"
    echo "  cmd: $cmd"
    echo "  copyCmd: $copyCmd"
    echo "  dumpCmd: $dumpCmd"
    exit 1
  fi

  #sed -e 's/OFFSET [0-9][0-9]*/OFFSET someOffset/g' \
  #    -e 's/GROUP [0-9][0-9]*/GROUP someOffset/g' \
  #    -e 's/DATASET [0-9][0-9]*/DATASET someOffset/g' \
  #  tempout.newa > tempout.newb
  /bin/egrep -v '^ *OFFSET' tempout.newa > tempout.newb

  # Filter out stuff that changes with contig vs chunked.
  # For netcdf, don't filter the dimension variables,
  # so the sed ends at the start of dimensions '^   DATASET "dim00"'.
  #
  # Here compress==0 <==> contiguous.

  if [ "$compress" == "0" ]; then
    /bin/sed -e '1,/^   DATASET "dim00"/s/^ *CONTIGUOUS.*/          contigOrChunked/' \
      tempout.newb > tempout.newc
  else
    /bin/sed -e '1,/^   DATASET "dim00"/s/^ *CHUNKED.*/          contigOrChunked/' \
      tempout.newb > tempout.newc
  fi

  # Filter out stuff that changes with compression level
  if [ "$compress" == "0" ]; then
    /bin/sed -e '1,/^   DATASET "dim00"/s/^ *NONE$/          compressType/' \
      -e '1,/^   DATASET "dim00"/s/^ *SIZE.*/          someSize/' \
      tempout.newc > tempout.newd
  else
    /bin/sed -e '1,/^   DATASET "dim00"/s/^ *COMPRESSION DEFLATE.*/          compressType/' \
      -e '1,/^   DATASET "dim00"/s/^ *SIZE.*/          someSize/' \
      tempout.newc > tempout.newd
  fi

  # Filter out group offsets for reference types
  /bin/sed -e 's/GROUP [0-9][0-9]*/GROUP someOffset/g' \
    -e 's/DATASET [0-9][0-9]*/DATASET someDataset/g' \
    tempout.newd > tempout.newe

  zcat $oldTxt > tempout.olde
  diffCmd="diff -w tempout.olde tempout.newe"
  if [ "$bugs" != "none" ]; then echo diffCmd: $diffCmd; fi
  $diffCmd
  diffOk=$?

  if [ "$diffOk" -ne "0" ]; then
    echo "Diff failed for config: $configMsg"
    echo "  cmd: $cmd"
    echo "  copyCmd: $copyCmd"
    echo "  diffCmd: $diffCmd"
    echo "wc:"
    wc tempout.olde tempout.newe
    #exit 1
  fi
} # end checkOne







for version in $versions; do
  for compress in $compressVals; do
    for nhType in $nhTypes; do
      for rank in $ranks; do

        testOne $version $compress $nhType $rank

      done
    done
  done
done

