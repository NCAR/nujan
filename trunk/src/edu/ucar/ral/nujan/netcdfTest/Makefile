
SRC = $(shell ls *.java)

BUILDDIR = /home/steves/tech/hdf5/build
NCJAR = /d1/steves/ftp/netcdfJava/netcdfAll-4.1.jar


all: testflagcls


testflagcls: ${SRC}
	javac -cp ${BUILDDIR}:${NCJAR} -d ${BUILDDIR} *.java
	touch testflagcls


clean:
	rm -rf ${BUILDDIR}/netcdfTest
	/bin/rm -f testflagcls
	/bin/rm -f temp*



