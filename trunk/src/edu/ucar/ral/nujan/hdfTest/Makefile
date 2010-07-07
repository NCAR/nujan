
SRC = $(shell ls *.java ../hdf/*.java)

BUILDDIR = /home/steves/tech/hdf5/build

all: testflagcls


testflagcls: ${SRC}
	(cd ../hdf; ${MAKE} testflagcls)
	javac -cp ${BUILDDIR} -d ${BUILDDIR} *.java
	touch testflagcls


clean:
	rm -rf ${BUILDDIR}/hdfTest
	/bin/rm -f testflagcls
	/bin/rm -f temp*



testTiny: testflagcls
	/bin/rm -f tempc.h5
	java -cp ${BUILDDIR} \
	  edu.ucar.ral.nujan.hdfTest.TestTiny \
	  -bugs 10 \
	  -outFile tempc.h5
	ls -l tempc.h5
	h5dump -p tempc.h5

testEmptya: testflagcls
	/bin/rm -f tempc.h5
	java -cp ${BUILDDIR} \
	  edu.ucar.ral.nujan.hdfTest.TestEmptya \
	  -bugs 10 \
	  -outFile tempc.h5
	ls -l tempc.h5
	h5dump -p tempc.h5

