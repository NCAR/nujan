
HDFDIR = ../hdf5
HDFSRC = $(shell ls ${HDFDIR}/*.java)
SRC = $(shell ls *.java)

NCJAR = /home/ss/ftp/netcdfJava/netcdfAll-4.1.jar
NCJAR = /d1/steves/ftp/netcdfJava/netcdfAll-4.1.jar


all: testflagcls


testflagcls: ${SRC} ${HDFSRC}
	(cd ${HDFDIR}; ${MAKE} testflagcls)
	rm -rf tdcls
	mkdir tdcls
	javac -d tdcls -cp ${HDFDIR}/tdcls:${NCJAR} *.java
	touch testflagcls
	@echo Try make targets: testshort0/1/... testint0/1/2 teststring0/1 testref1


clean:
	rm -rf tdcls
	rm -rf tddoc
	/bin/rm -f testflagcls
	/bin/rm -f temp*



javadoc:
	rm -rf tddoc
	mkdir -p tddoc/tda/nhPkg
	mkdir -p tddoc/tda/nhPkgTest
	cp *.java tddoc/tda/nhPkg
	mv tddoc/tda/nhPkg/Test*.java tddoc/tda/nhPkgTest
	javadoc -public \
	  -overview overview.html \
	  -classpath ${HDFDIR}/tdcls:${NCJAR} \
	  -sourcepath tddoc/tda \
	  -d tddoc \
	  nhPkg nhPkgTest
	rm -rf tddoc/tda



testExamplea: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestExamplea -outFile tempa.nc
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
	java -cp tdcls:${HDFDIR}/tdcls:${NCJAR} nhPkgTest.NhCopy -bugs 0 -compress 0 -inFile tempa.nc -outFile tempb.nc



test.copyb: testflagcls
	/bin/rm -f tempc.nc
	java -cp tdcls:${HDFDIR}/tdcls:${NCJAR} nhPkgTest.NhCopy -bugs 0 -compress 0 -inFile tempb.nc -outFile tempc.nc


test.copyc: testflagcls
	/bin/rm -f tempc.nc
	java -cp tdcls:${HDFDIR}/tdcls:${NCJAR} nhPkgTest.NhCopy -bugs 0 -compress 0 -inFile ../../../data/margolis/gfs-grb2.nc -outFile tempc.nc




test.printa: testflagcls
	java -cp tdcls:${NCJAR} nhPkgTest.NhPrint -bugs 0 -inFile tempa.nc


test.printc: testflagcls
	java -cp tdcls:${NCJAR} nhPkgTest.NhPrint -bugs 0 -inFile /home/ss/consult/ucar/updn/src/hdflib/netcdfc/tempa.nc




test.newstring1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls nhPkgTest.TestNetcdfString -bugs 10 -dims 3 -fileVersion 1 -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

