
package hdfnet;


class SymTabEntry extends BaseBlk {


// Referenced blocks
// None.  (groups are traversed via HdfGroup.subGroupList)


long heapOffset;

HdfGroup hdfGroup;

int cacheType;           // Cache type.  Default is 0.
                         //  0: no data is cached
                         //  1: group object header metadata is cached in
                         //     scratch, which implies the symbol table
                         //     refers to another group.
                         //  2: entry is a sym link....

BtreeNode scratchBtree;   // scratchPad contents
LocalHeap scratchHeap;    // scratchPad contents


SymTabEntry(
  long heapOffset,
  HdfGroup hdfGroup,
  HdfFile hdfFile)
{
  super("SymTabEntry", hdfFile);
  this.heapOffset = heapOffset;
  this.hdfGroup = hdfGroup;
  cacheType = 0;
  scratchBtree = null;
  scratchHeap = null;
}




public String toString() {
  String res = super.toString();
  res += "  heapOffset: " + heapOffset;
  res += "  hdfGroup: " + hdfGroup.groupName;
  return res;
}






void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  fmtBuf.putBufLong("SymTabEntry: heapOffset", heapOffset);

  // External block
  fmtBuf.putBufLong("SymTabEntry: hdfGroup.pos", hdfGroup.blkPosition);
  hdfFile.addWork("SymTabEntry", hdfGroup);

  fmtBuf.putBufInt("SymTabEntry: cacheType", cacheType);
  fmtBuf.putBufInt("SymTabEntry: reserved", 0);

  if (cacheType == 0) {
    fmtBuf.putBufLong("SymTabEntry: scratchPad", 0);
    fmtBuf.putBufLong("SymTabEntry: scratchPad", 0);
  }
  else if (cacheType == 1) {        // if root
    fmtBuf.putBufLong("SymTabEntry: scratchBtreeAddr",
      scratchBtree.blkPosition);
    fmtBuf.putBufLong("SymTabEntry: scratchHeapAddr",
      scratchHeap.blkPosition);
  }
  else throwerr("unknown cacheType: %d", cacheType);

  noteFormatExit( fmtBuf);             // BaseBlk: print debug
}



} // end class
