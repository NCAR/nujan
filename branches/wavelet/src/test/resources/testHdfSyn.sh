#!/bin/sh


# Validates the HDF5 layer using synthetic data.
#
# Runs Testa.java on various configurations and compares the
# output with saved known good files.
#
# For usage, see badparms below.
#
# To compare a new netcdf type (int and double, for example):
#   cd ../netcdf
#   for ii in 0 1 2 3 4 5; do diff <(sed -e '1,/ncdump/ d'  testSynOut/test.int.rank.$ii.out) <(sed -e '1,/ncdump/ d'  testSynOut/test.double.rank.$ii.out) | less; done


BUILDDIR=../../../target/test-classes:../../../target/classes
PKGBASE=edu.ucar.ral.nujan
TESTDIR=.
H5CHECK=/d1/steves/ftp/hdf5/tdi/bin/h5check


badparms() {
  echo Error: $1
  echo Parms:
  echo "  chunkSpec: all / contiguous / chunked"
  echo "  compress:  all / compressLevel (0==none, 1 - 9)"
  echo "  dtype:     all / sfixed08 / ufixed08 /fixed16,32,64"
  echo "               float32,64 string14 vstring reference compound"
  echo "  rank:      all/0/1/2/3/4/5"
  echo "  bugs       optional: none / echo / continue / update"
  echo "               none: no debug msgs"
  echo "               echo: some debug msgs"
  echo "               continue: continue even if diff errors"
  echo "               update: update verification results - Caution"
  echo ""
  echo "Examples:"
  echo "./testHdfSyn.sh  contiguous  0 fixed16  1"
  echo "./testHdfSyn.sh  chunked     0 string14 2"
  echo "./testHdfSyn.sh  contiguous  0 vstring  all"
  echo "./testHdfSyn.sh  all         0 all      all"
  exit 1
}

if [ $# -eq 1 ]; then
  if [ "$1" != "all" ]; then badparms "wrong num parms"; fi
  ./testHdfSyn.sh all all 0 all all
  if [ "$?" -ne 0 ]; then echo "exiting"; exit 1; fi
  ./testHdfSyn.sh all chunked 5 all all
  if [ "$?" -ne 0 ]; then echo "exiting"; exit 1; fi
  exit 0
fi


if [ $# -ne 4 -a $# -ne 5 ]; then badparms "wrong num parms"; fi

chunkSpec=$1
compressSpec=$2
dtypeSpec=$3
rankSpec=$4
bugs=none
if [ $# -eq 5 ]; then bugs=$5; fi

if [ "$chunkSpec" == "all" ]; then chunks="contiguous chunked"
else chunks="$chunkSpec"
fi

if [ "$compressSpec" == "all" ]; then compressVals="0 5"
else compressVals="$compressSpec"
fi

if [ "$dtypeSpec" == "all" ]; then
  dtypes="sfixed08 ufixed08 fixed16 fixed32 fixed64 float32 float64 string14 vstring reference"
else dtypes=$dtypeSpec
fi

if [ "$rankSpec" == "all" ]; then ranks="0 1 2 3 4 5 6 7"
else ranks=$rankSpec
fi



echo "chunks: $chunks"
echo "compressVals: $compressVals"
echo "dtypes: $dtypes"
echo "ranks: $ranks"





testOne() {
  if [ $# -ne 4 ]; then
    badparms "wrong num parms"
  fi
  chunk=$1
  compress=$2
  dtype=$3
  rank=$4
  if [ "$bugs" != "none" ]; then
    echo "testOne: chunk: $chunk  compress: $compress"
    echo "  dtype: $dtype  rank: $rank"
  fi

  if [[ "$rank" == "0" ]]; then
    dims="scalar"
    chunkLens="contiguous"
  elif [[ "$rank" == "1" ]]; then
    dims="3"
    if [[ "$dtype" == "float32" ]]; then chunkLens="1"
    else chunkLens="3"
    fi
  elif [[ "$rank" == "2" ]]; then
    dims="3,4"
    if [[ "$dtype" == "float32" ]]; then
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

  if [ "$compress" != "0" -a "$dtype" == "vstring" ]; then
    # Cannot compress vstring because vstrings are kept on
    # the global heap, and hdf5 compresses only the references
    # to the strings, not the strings themselves.
    # So we don't allow compression of vstrings.
    echo "Cannot compress vstring (rank $rank) ... ignoring"
  elif [ "$chunk" == "chunked" -a "$rank" == "0" ]; then
    echo "Cannot use chunked with scalar data ... ignoring"
  elif [ "$compress" != "0" -a "$rank" == "0" ]; then
    echo "Cannot compress scalar data ... ignoring"
  elif [ "$compress" != "0" -a "$chunk" == "contiguous" ]; then
    echo "Cannot compress contiguous data ... ignoring"
  else

    /bin/rm -f tempa.h5

    cmd="java -cp ${BUILDDIR} \
      ${PKGBASE}.hdfTest.Thdfa \
      -bugs 10 \
      -dtype $dtype \
      -dims $dims \
      -chunks $chunkLens \
      -compress $compress \
      -utcModTime 0 \
      -outFile tempa.h5"

    if [ "$bugs" != "none" ]; then echo "cmd: $cmd"; fi
    configMsg="./testHdfSyn.sh $chunk $compress $dtype $rank"

    $cmd > tempa.log
    if [ "$?" -ne "0" ]; then
      echo "Cmd failed for:"
      echo "  config: $configMsg"
      echo "  cmd: $cmd"
      exit 1
    fi

    echo "  test: $configMsg  size: $(wc -c tempa.h5 | cut -f 1 -d ' ')"

    $H5CHECK tempa.h5 > /dev/null
    if [ "$?" -ne "0" ]; then
      echo "$H5CHECK failed for:"
      echo "  config: $configMsg"
      echo "  cmd: $cmd"
      exit 1
    fi

    oldTxt=${TESTDIR}/testHdfSynOut.v2/test.$dtype.rank.$rank.out.gz
    #xxx rename above to del v2; get rid of all v1 files

    dumpCmd="h5dump -p -w 10000 tempa.h5"
    $dumpCmd > tempout.newa
    if [ "$?" -ne "0" ]; then
      echo "h5dump failed for:"
      echo "  config: $configMsg"
      echo "  cmd: $cmd"
      echo "  dumpCmd: $dumpCmd"
      exit 1
    fi

    /bin/egrep -v '^ *OFFSET' tempout.newa > tempout.newb

    # Filter out stuff that changes with contiguous vs chunked.
    if [ "$chunk" == "contiguous" ]; then
      /bin/sed -e 's/^ *CONTIGUOUS.*/          contigOrChunked/' \
        tempout.newb > tempout.newc
    else
      /bin/sed -e 's/^ *CHUNKED.*/          contigOrChunked/' \
        tempout.newb > tempout.newc
    fi

    # Filter out stuff that changes with compression level
    if [ "$compress" == "0" ]; then
      /bin/sed -e 's/^ *NONE$/          compressType/' \
        -e '1,/^   DATASET "dim00"/s/^ *SIZE.*/          someSize/' \
        tempout.newc > tempout.newd
    else
      /bin/sed -e 's/^ *COMPRESSION DEFLATE.*/          compressType/' \
        -e '1,/^   DATASET "dim00"/s/^ *SIZE.*/          someSize/' \
        tempout.newc > tempout.newd
    fi

    # Filter out group offsets for reference types
    if [ "$dtype" == "reference" ]; then
      /bin/sed -e 's/GROUP [0-9][0-9]*/GROUP someOffset/g' \
        -e 's/DATASET [0-9][0-9]*/DATASET someDataset/g' \
        tempout.newd > tempout.newe
    else
      cp tempout.newd tempout.newe
    fi

    zcat $oldTxt > tempout.olda
    diffCmd="diff -w tempout.olda tempout.newe"
    if [ "$bugs" != "none" ]; then echo diffCmd: $diffCmd; fi
    $diffCmd
    diffOk=$?

    # If update, copy to testSynOut
    if [ "$bugs" == "update" ]; then
      gzip -c tempout.newe > $oldTxt
      echo '*** updated ***'
    fi

    if [ "$diffOk" -ne "0" \
      -a "$bugs" != "continue" \
      -a "$bugs" != "update" ]; then
      echo "Diff failed for:"
      echo "  config: $configMsg"
      echo "  cmd: $cmd"
      echo "  diffCmd: $diffCmd"
      echo "wc:"
      wc tempout.olda tempout.newe
      exit 1
    fi

  fi
} # end testOne




for chunk in $chunks; do
  for compress in $compressVals; do
    for dtype in $dtypes; do
      for rank in $ranks; do

        testOne $chunk $compress $dtype $rank

      done
    done
  done
done

