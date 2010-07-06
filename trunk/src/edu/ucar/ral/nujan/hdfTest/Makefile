
SRC = $(shell ls *.java)

BUILDDIR = /home/steves/tech/hdf5/build

all: testflagcls


testflagcls: ${SRC}
	javac -cp ${BUILDDIR} -d ${BUILDDIR} *.java
	touch testflagcls


clean:
	rm -rf ${BUILDDIR}/hdfTest
	/bin/rm -f testflagcls
	/bin/rm -f temp*



