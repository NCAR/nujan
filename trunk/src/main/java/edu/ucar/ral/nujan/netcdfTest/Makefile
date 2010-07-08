
# OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
# OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
# OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
 
# Obsolete: Please use the maven build facilities instead.



SRC = $(shell ls *.java ../netcdf/*.java ../hdf/*.java)

BUILDDIR = /home/steves/tech/hdf5/build
NCJAR = /d1/steves/ftp/netcdfJava/netcdfAll-4.1.jar


all: testflagcls


testflagcls: ${SRC}
	(cd ../netcdf; ${MAKE} testflagcls)
	(cd ../hdfTest; ${MAKE} testflagcls)
	javac -cp ${BUILDDIR}:${NCJAR} -d ${BUILDDIR} *.java
	touch testflagcls


clean:
	rm -rf ${BUILDDIR}/netcdfTest
	/bin/rm -f testflagcls
	/bin/rm -f temp*


testExamplea: testflagcls
	java -cp ${BUILDDIR}:${NCJAR} \
	  edu.ucar.ral.nujan.netcdfTest.TestExamplea \
	  -outFile tempa.nc



testEmptya: testflagcls
	java -cp ${BUILDDIR}:${NCJAR} \
	  edu.ucar.ral.nujan.netcdfTest.TestEmptya \
	  -bugs 10 \
	  -outFile tempa.nc


