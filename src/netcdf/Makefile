
HDFDIR = ../java
HDFSRC = $(shell ls ${HDFDIR}/*.java)
SRC = $(shell ls *.java)


all: testflagcls


testflagcls: ${SRC} ${HDFSRC}
	(cd ${HDFDIR}; ${MAKE} testflagcls)
	rm -rf tdcls
	mkdir tdcls
	javac -d tdcls -cp ${HDFDIR}/tdcls *.java
	touch testflagcls
	@echo Try make targets: testshort0/1/... testint0/1/2 teststring0/1 testref1


clean:
	rm -rf tdcls
	/bin/rm -f testflagcls



testshort0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype short -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype short -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	h5dump -p tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype short -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype short -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype short -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testshort5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype short -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc






testint0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype int -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc


testint1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype int -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc


testint2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype int -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testint3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype int -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testint4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype int -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testint5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype int -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc








testlong0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype long -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype long -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype long -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype long -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype long -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testlong5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype long -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc











testfloat0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype float -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype float -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype float -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype float -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype float -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testfloat5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype float -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc










testdouble0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype double -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype double -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype double -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype double -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype double -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testdouble5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype double -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc










teststring0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype string7 -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype string7 -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype string7 -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype string7 -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype string7 -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

teststring5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype string7 -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc










testreference0: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype reference -dims 0 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype reference -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference2: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype reference -dims 3,4 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference3: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype reference -dims 3,4,5 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference4: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype reference -dims 3,4,5,6 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

testreference5: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfa -bugs 10 -nctype reference -dims 3,4,5,6,7 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc





test.newstring1: testflagcls
	/bin/rm -f tempa.nc
	java -cp tdcls:${HDFDIR}/tdcls ncHdfTest.TestNetcdfString -bugs 10 -dims 3 -fileVersion 1 -chunked false -compress 0 -outFile tempa.nc
	ls -l tempa.nc
	@echo ===== start h5dump =====
	h5dump -p tempa.nc
	@echo ===== start ncdump =====
	ncdump tempa.nc

