
# OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
# OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
# OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
 
# Obsolete: Please use the maven build facilities instead.


SRC = $(shell ls *.java)

BUILDDIR = /home/steves/tech/hdf5/build
DOCDIR = /home/steves/tech/hdf5/build/docHdf
PKGBASE = edu.ucar.ral.nujan

all: testflagcls


testflagcls: ${SRC}
	rm -rf ${BUILDDIR}
	mkdir ${BUILDDIR}
	javac -d ${BUILDDIR} *.java
	touch testflagcls


clean:
	rm -rf ${BUILDDIR}/hdf
	rm -rf ${DOCDIR}
	/bin/rm -f testflagcls
	/bin/rm -f temp*



javadoc: testflagcls
	javadoc -public \
	  -sourcepath ../../../../.. \
	  -d ${DOCDIR} \
	  ${PKGBASE}.hdf ${PKGBASE}.hdfTest




test.fixed16.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed16 -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed16.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed16 -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed16.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed16 -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed16.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed16 -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed16.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed16 -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed16.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed16 -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5






test.fixed32.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed32 -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5


test.fixed32.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed32 -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5


test.fixed32.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed32 -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed32.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed32 -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed32.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed32 -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed32.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed32 -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5








test.fixed64.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed64 -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed64.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed64 -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed64.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed64 -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed64.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed64 -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed64.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed64 -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.fixed64.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype fixed64 -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5











test.float32.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float32 -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float32.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float32 -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float32.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float32 -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float32.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float32 -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float32.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float32 -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float32.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float32 -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5










test.float64.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float64 -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float64.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float64 -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float64.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float64 -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float64.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float64 -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float64.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float64 -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.float64.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype float64 -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5










test.string.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype string7 -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.string.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype string7 -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.string.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype string7 -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.string.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype string7 -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.string.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype string7 -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.string.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype string7 -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5





test.vstring.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype vstring -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.vstring.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype vstring -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.vstring.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype vstring -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.vstring.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype vstring -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.vstring.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype vstring -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.vstring.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype vstring -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5








test.reference.0: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype reference -dims 0 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.reference.1: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype reference -dims 3 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.reference.2: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype reference -dims 3,4 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.reference.3: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype reference -dims 3,4,5 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.reference.4: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype reference -dims 3,4,5,6 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5

test.reference.5: testflagcls
	/bin/rm -f tempa.h5
	java -cp ${BUILDDIR} hdfnetTest.Testa -bugs 10 -dtype reference -dims 3,4,5,6,7 \
      -fileVersion 1 -chunked false -compress 0 -outFile tempa.h5
	ls -l tempa.h5
	h5dump -p tempa.h5





testCustomNetcdf: testflagcls
	/bin/rm -f tempb.h5
	java -cp ${BUILDDIR} hdfnetTest.TestCustomNetcdf -bugs 10 -outFile tempb.h5
	ls -l tempb.h5
	h5dump -p tempb.h5
	java -cp /d1/steves/ftp/netcdfJava/tda/cdm/target/netcdf-4.1.jar:/d1/steves/ftp/netcdfJava/netcdfAll-4.1.jar \
	  ucar.nc2.NCdumpW tempb.h5 -vall -c


testTiny: testflagcls
	/bin/rm -f tempc.h5
	java -cp ${BUILDDIR} hdfnetTest.TestTiny -bugs 10 -outFile tempc.h5
	ls -l tempc.h5
	h5dump -p tempc.h5


testRefb: testflagcls
	/bin/rm -f tempb.h5
	java -cp ${BUILDDIR} hdfnetTest.TestRefb -bugs 10 -outFile tempb.h5
	ls -l tempb.h5
	h5dump -p tempb.h5





testcompile:
	rm -rf ${BUILDDIR}
	mkdir ${BUILDDIR}
	javac -d ${BUILDDIR} \
	    BaseBlk.java \
	    BtreeNode.java \
	    GlobalHeap.java \
	    HdfFileWriter.java \
	    HdfGroup.java \
	    LocalHeap.java \
	    MsgAttribute.java \
	    MsgBase.java \
	    MsgDataSpace.java \
	    MsgDataType.java \
	    MsgFillValue.java \
	    MsgLayout.java \
	    MsgModTime.java \
	    MsgNil.java \
	    MsgObjHdrContin.java \
	    MsgOldFillValue.java \
	    MsgSymbolTable.java \
	    SymbolTable.java \
	    SymTabEntry.java \
	    TestException.java

