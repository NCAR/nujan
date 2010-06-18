// The MIT License
// 
// Copyright (c) 2009 University Corporation for Atmospheric
// Research and Massachusetts Institute of Technology Lincoln
// Laboratory.
// 
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.


package hdfnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


class SymbolTable extends BaseBlk {


// Referenced blocks
ArrayList<SymTabEntry> entryList = null;


LocalHeap localHeap;

final int signa = 'S';
final int signb = 'N';
final int signc = 'O';
final int signd = 'D';

final int tableVersion = 1;



SymbolTable(
  LocalHeap localHeap,
  HdfFileWriter hdfFile)
{
  super( "SymbolTable", hdfFile);
  this.localHeap = localHeap;
  entryList = new ArrayList<SymTabEntry>();
}



public String toString() {
  String res = super.toString();
  return res;
}




void addSymName(
  HdfGroup subGroup)
throws HdfException
{
  long heapOffset = localHeap.putHeapItem(
    "subGroupName",
    Util.encodeString( subGroup.groupName, true, subGroup));
  SymTabEntry entry = new SymTabEntry( heapOffset, subGroup, hdfFile);
  entryList.add( entry);

  // A leaf btree node (a symbol table) should have:
  //   k_leaf <= numUsedEntries <= 2 * k_leaf
  int minKLeaf = entryList.size() / 2 + 1;
  if (hdfFile.k_leaf < minKLeaf) {
    hdfFile.k_leaf = minKLeaf;
    prtf("addSymName: set new k_leaf.  entryList len: %d  k_leaf: %d",
      entryList.size(), hdfFile.k_leaf);
  }
}





void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException
{
  // Sort entryList by entry.hdfGroup.groupName
  SymTabEntry[] symEntries = entryList.toArray( new SymTabEntry[0]);
  Arrays.sort( symEntries, new Comparator<SymTabEntry>() {
    public int compare( SymTabEntry enta, SymTabEntry entb) {
      return enta.hdfGroup.groupName.compareTo( entb.hdfGroup.groupName);
    }
  });

  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  fmtBuf.putBufByte("MsgSymbolTable: signa", signa);
  fmtBuf.putBufByte("MsgSymbolTable: signb", signb);
  fmtBuf.putBufByte("MsgSymbolTable: signc", signc);
  fmtBuf.putBufByte("MsgSymbolTable: signd", signd);

  fmtBuf.putBufByte("MsgSymbolTable: tableVersion", tableVersion);
  fmtBuf.putBufByte("MsgSymbolTable: reserved", 0);
  fmtBuf.putBufShort("MsgSymbolTable: numSymbol", symEntries.length);

  for (SymTabEntry entry : symEntries) {
    // Internal block
    entry.formatBuf( formatPass, fmtBuf);
  }

  // We must format all the entries to fill out the symbol
  // table, even if some are empty.
  // If the full table is not present, the HDF5 C software
  // will die near either ...
  //    H5Gcache.c line 169
  //    H5FDsec2.c line 739
  //
  // H5Gcache.c line 161    size = H5G_node_size_real(f);
  //
  // H5Gnode.c line 274  res = H5G_NODE_SIZEOF_HDR(f) +
  //                 (2 * H5F_SYM_LEAF_K(f)) * H5G_SIZEOF_ENTRY(f));
  //
  // H5Gnode.c line 56  #define H5G_NODE_SIZEOF_HDR(F) (H5_SIZEOF_MAGIC + 4)
  //                 == 4 + 4 == 8
  //
  // H5Gprivate.h line 50   #define H5G_SIZEOF_ENTRY(F)
  //     (H5F_SIZEOF_SIZE(F) +        // offset of name into heap
  //     H5F_SIZEOF_ADDR(F) +        // address of object header
  //     4 +                         // entry type
  //     4 +             // reserved
  //     H5G_SIZEOF_SCRATCH)   // scratch pad space
  //   == 8 + 8 + 4 + 4 + 16 == 40
  //
  // 
  // So for H5Gcache.c, the total needed size = 8 + 2 * k_leaf * 40
  //
  //
  // This is in method H5G_node_load, when it reads
  // the serialized symbol table node by calling
  //    H5F_block_read(f, H5FD_MEM_BTREE, addr, size, dxpl_id, node)
  //
  // A leaf btree node (a symbol table) should have:
  //   k_leaf <= numUsedEntries <= 2 * k_leaf
  //
  // So we must format a total of 2 * k_leaf entries.

  for (int ii = 0; ii < 2 * hdfFile.k_leaf - symEntries.length; ii++) {
    // Format a fake SymTabEntry.
    // Use cacheType = 1, which includes scratchPad,
    // even though we may not need it.
    fmtBuf.putBufLong("MsgSymbolTable: fake SymTabEntry: name offset", 0);
    fmtBuf.putBufLong("MsgSymbolTable: fake SymTabEntry: obj hdr addr", 0);
    fmtBuf.putBufInt("MsgSymbolTable: fake SymTabEntry: cache type", 0);
    fmtBuf.putBufInt("MsgSymbolTable: fake SymTabEntry: reserved", 0);
    fmtBuf.putBufLong("MsgSymbolTable: fake SymTabEntry: scratch a", 0);
    fmtBuf.putBufLong("MsgSymbolTable: fake SymTabEntry: scratch b", 0);
  }

  noteFormatExit( fmtBuf);         // BaseBlk: print debug
}


} // end class
