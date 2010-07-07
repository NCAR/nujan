
SRC = $(shell ls *.java ../hdf/*.java)

BUILDDIR = /home/steves/tech/hdf5/build
DOCDIR = /home/steves/tech/hdf5/build/docNetcdf
PKGBASE = edu.ucar.ral.nujan

all: testflagcls


testflagcls: ${SRC}
	(cd ../hdf; ${MAKE} testflagcls)
	javac -d ${BUILDDIR} -cp ${BUILDDIR}:${NCJAR} *.java
	touch testflagcls


clean:
	rm -rf ${BUILDDIR}/netcdf
	/bin/rm -f testflagcls
	/bin/rm -f temp*

dist: testflagcls javadoc jar
	rm -rf /scratch/steves/nujan
	mkdir -p /scratch/steves/nujan
	cp ${BUILDDIR}/*.jar /scratch/steves/nujan
	cp -r ${DOCDIR} /scratch/steves/nujan/doc
	cp ../netcdfTest/TestExamplea.java /scratch/steves/nujan
	ls -al /scratch/steves/nujan


javadoc:
	rm -rf ${DOCDIR}
	javadoc -public \
	  -overview overview.html \
	  -classpath ${BUILDDIR}:${NCJAR} \
	  -sourcepath ../../../../.. \
	  -d ${DOCDIR} \
	  edu.ucar.ral.nujan.netcdf edu.ucar.ral.nujan.netcdfTest


jar: testflagcls
	/bin/rm -f ${BUILDDIR}/nujan.*.jar
	(cd ${BUILDDIR}; jar cvf nujan.2010.07.07.b.jar edu)



testExamplea: testflagcls
	/bin/rm -rf td.example
	mkdir td.example
	javac -d td.example -cp tdjar/nujan.2010.07.02.a.jar TestExamplea.java
	/bin/rm -f tempa.nc
	java -cp td.example:tdjar/nujan.2010.07.02.a.jar  nhPkgTest.TestExamplea -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc



testbyte0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType byte -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testbyte1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType byte -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	h5dump -p tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testbyte2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType byte -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testbyte3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType byte -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testbyte4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType byte -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testbyte5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType byte -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc




testshort0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType short -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType short -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	h5dump -p tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType short -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType short -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType short -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType short -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc






testint0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType int -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc


testint1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType int -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc


testint2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType int -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testint3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType int -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testint4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType int -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testint5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType int -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc








testlong0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType long -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType long -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType long -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType long -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType long -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType long -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc











testfloat0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType float -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType float -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType float -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType float -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType float -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType float -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc










testdouble0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType double -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType double -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType double -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType double -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType double -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType double -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc












testchar0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType char -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testchar1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType char -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testchar2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType char -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testchar3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType char -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testchar4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType char -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testchar5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType char -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc










teststring0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType vstring -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType vstring -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType vstring -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType vstring -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType vstring -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType vstring -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc










testreference0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType reference -dims 0 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType reference -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType reference -dims 3,4 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType reference -dims 3,4,5 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType reference -dims 3,4,5,6 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfa -bugs 10 -nhType reference -dims 3,4,5,6,7 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc




test.copya: testflagcls
	/bin/rm -f tempb.nc
	java -cp tdcls:${HDFDIR}/tdcls:${NCJAR} nhPkgTest.NhCopy \
        -bugs 0 -compress 0 -fileVersion 2 \
        -inFile tempa.nc -outFile tempb.nc

test.copyb: testflagcls
	/bin/rm -f tempc.nc
	java -cp tdcls:${HDFDIR}/tdcls:${NCJAR} nhPkgTest.NhCopy \
	    -bugs 0 -compress 0 -fileVersion 2 \
        -inFile tempb.nc -outFile tempc.nc

test.copyc: testflagcls
	/bin/rm -f tempc.nc
	java -cp tdcls:${HDFDIR}/tdcls:${NCJAR} nhPkgTest.NhCopy \
        -bugs 0 -compress 0 -fileVersion 2 \
        -inFile ../../../data/margolis/gfs-grb2.nc -outFile tempc.nc




test.printa: testflagcls
	java -cp tdcls:${NCJAR} nhPkgTest.NhPrint -bugs 0 -inFile tempa.nc

test.printb: testflagcls
	java -cp tdcls:${NCJAR} nhPkgTest.NhPrint -bugs 0 -inFile tempb.nc

test.printc: testflagcls
	java -cp tdcls:${NCJAR} nhPkgTest.NhPrint -bugs 0 -inFile /home/steves/tda/ruc-mdvConvert-nc4_vlevel-3.01km.nc




test.newstring1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfString -bugs 10 -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

