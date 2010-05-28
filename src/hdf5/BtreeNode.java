
package hdfnet;

import java.util.ArrayList;


// This is a version 1 Btree node, required for chunked data.

class BtreeNode extends BaseBlk {

static final int NT_GROUP = 0;  // this tree points to group nodes
                                //   (only used for fileVersion==1)
static final int NT_DATA  = 1;  // this tree points to raw data chunk nodes
static final String[] nodeTypeNames = {"GROUP", "DATA"};


// For fileVersion==1 only:
// If we're an internal node:
// Our children are BtreeNodes (numKid of them).
ArrayList<BtreeNode> subNodeList = null;

// For fileVersion==1 only:
// Else we're a so-called leaf node, although the
// true leaf nodes are SymbolTables.
// Our children are SymbolTables (numKid of them).
ArrayList<SymbolTable> subTableList = null;

// For fileVersion==1 only:
// This is the only member of subTableList.
SymbolTable symbolTable;

// For fileVersion==1 only:
LocalHeap localHeap;              // if NT_GROUP


HdfGroup hdfGroup;                // if NT_DATA or fileVersion==2

ArrayList<byte[]> keyList;

int compressionLevel;

final int signa = 'T';
final int signb = 'R';
final int signc = 'E';
final int signd = 'E';

int nodeType;              // NT_DATA:  1: this tree points to raw data chunks
int nodeLevel;
int numKid;                // num children of this node




final byte[] lowKey = new byte[5];

final byte[] highKey = new byte[] {
  (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
  (byte) 0};  // null termination




// For fileVersion==1 only:
// Constructor for NT_GROUP trees.
BtreeNode(
  LocalHeap localHeap,
  HdfFile hdfFile)
throws HdfException
{
  super("BtreeNode", hdfFile);
  this.localHeap = localHeap;
  nodeType = NT_GROUP;

  // We're always a leaf node, with 1 child: a SymbolTable.
  symbolTable = new SymbolTable( localHeap, hdfFile);
  subTableList = new ArrayList<SymbolTable>();
  subTableList.add( symbolTable);
  numKid = subTableList.size();

  keyList = new ArrayList<byte[]>();
  keyList.add( lowKey);
  keyList.add( highKey);
  localHeap.putHeapItem("lowKey", lowKey);
  localHeap.putHeapItem("highKey", highKey);
}






// For fileVersion==2 or (fileVersion==1 and NT_DATA):
// Constructor for NT_DATA trees.
BtreeNode(
  int compressionLevel,
  HdfGroup hdfGroup,
  HdfFile hdfFile)
{
  super("BtreeNode", hdfFile);
  this.compressionLevel = compressionLevel;
  this.hdfGroup = hdfGroup;
  nodeType = NT_DATA;
  numKid = 1;                // num Btree entries used
}






public String toString() {
  String res = super.toString();
  res += "  nodeType: " + nodeType + "(" + nodeTypeNames[nodeType] + ")";
  res += "  nodeLevel: " + nodeLevel;
  res += "  numKid: " + numKid;
  if (nodeType == NT_GROUP) {      // fileVersion==1 only
    if (subNodeList == null) res += "  subNodeList: null";
    else res += "  subNodeList len: " + subNodeList.size();
    if (subTableList == null) res += "  subTableList: null";
    else res += "  subTableList len: " + subTableList.size();
  }
  else if (nodeType == NT_DATA) {
    res += "  group: \"" + hdfGroup.groupName + "\"";
  }
  return res;
}


// For fileVersion==1 only:
// Called by HdfGroup.addSubGroup.
void addTreeName(
  HdfGroup subGroup)
throws HdfException
{
  symbolTable.addSymName( subGroup);
}



// The formatted output is always the same, the fileVersion==1 format.
// Oddly fileVersion==2 only uses Btrees for the chunked data,
// but requires fileVersion==1 Btrees for data chunks.

void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  if (hdfFile.fileVersion == 1) {
    // Check list lengths.
    if (nodeType == NT_GROUP) {       // if this tree points to group nodes
      if (keyList.size() != numKid + 1) throwerr("keyList len mismatch");
      if (nodeLevel > 0) {        // if internal node
        if (subNodeList.size() != numKid)
          throwerr("subNodeList len mismatch");
      }
      else {                      // else leaf node
        if (subTableList.size() != numKid)
          throwerr("subNodeList len mismatch");
      }
    }
  }

  fmtBuf.putBufByte("BtreeNode: signa", signa);
  fmtBuf.putBufByte("BtreeNode: signb", signb);
  fmtBuf.putBufByte("BtreeNode: signc", signc);
  fmtBuf.putBufByte("BtreeNode: signd", signd);

  fmtBuf.putBufByte("BtreeNode: nodeType", nodeType);
  fmtBuf.putBufByte("BtreeNode: nodeLevel", nodeLevel);
  fmtBuf.putBufShort("BtreeNode: numKid", numKid);

  fmtBuf.putBufLong("BtreeNode: leftSibling.pos", HdfFile.UNDEFINED_ADDR);

  fmtBuf.putBufLong("BtreeNode: rightSibling.pos", HdfFile.UNDEFINED_ADDR);

  // NT_GROUP is used only by fileVersion==1
  if (nodeType == NT_GROUP) {    // if this btree points to group nodes
    for (int isub = 0; isub < numKid + 1; isub++) {
      // Format the key
      fmtBuf.putBufLong(
        "BtreeNode: key", localHeap.getHeapOffset( keyList.get(isub)));

      // If not at last entry, format the child
      if (isub < numKid) {
        if (nodeLevel > 0) {
          // External block
          BtreeNode subNode = subNodeList.get( isub);
          fmtBuf.putBufLong(
            "BtreeNode: subNode.pos", subNode.blkPosition);
          hdfFile.addWork("BtreeNode", subNode);
        }
        else {
          // External block
          SymbolTable subTable = subTableList.get( isub);
          fmtBuf.putBufLong(
            "BtreeNode: subTable.pos", subTable.blkPosition);
          hdfFile.addWork("BtreeNode", subTable);
        }
      }
    }

    // We must format all the entries to fill out the btree node,
    // even if some are empty.
    // If the full table is not present, the HDF5 C software
    // may die when it tries to load the table and finds
    // the max table length extends beyond the end of file.
    //
    // An internal btree node should have:
    //   k_internal <= numUsedEntries <= 2 * k_internal
    //
    // So we must format a total of 2 * k_internal entries.

    for (int ii = 0; ii < 2 * hdfFile.k_internal - numKid; ii++) {
      fmtBuf.putBufLong("BtreeNode: fake child", 0);
      fmtBuf.putBufLong("BtreeNode: fake kid", 0);
    }

  } // if nodeType == NT_GROUP

  else if (nodeType == NT_DATA) {
    // Only one format, since fileVersion==2 uses fileVersion==1 format.

    // Format the initial key
    fmtBuf.putBufInt("BtreeNode: key chunkSize",
      (int) hdfGroup.rawDataSize);     // xxx ugh, convert long to int.

    // Turn on bit i to skip filter i.
    int mask = 0;                      // use all filters

    fmtBuf.putBufInt("BtreeNode: key mask", mask);
    for (int ii = 0; ii < hdfGroup.msgDataSpace.rank; ii++) {
      fmtBuf.putBufLong("BtreeNode: key dimOffset", 0);
    }
    fmtBuf.putBufLong("BtreeNode: key eleLenOffset", 0);

    // Format the child pointer
    fmtBuf.putBufLong("BtreeNode: chunk addr", hdfGroup.rawDataAddr);

    // Format the final key
    fmtBuf.putBufInt("BtreeNode: final key chunkSize", 0);
    fmtBuf.putBufInt("BtreeNode: final key mask", 0);
    for (int ii = 0; ii < hdfGroup.msgDataSpace.rank; ii++) {
      fmtBuf.putBufLong("BtreeNode: final key dimOffset",
        hdfGroup.msgDataSpace.varDims[ii]);
    }
    fmtBuf.putBufLong("BtreeNode: final key eleLenOffset",
      hdfGroup.msgDataType.elementLen);
  } // if nodeType == NT_DATA

  noteFormatExit( fmtBuf);         // BaseBlk: print debug
} // end formatBuf

} // end class
