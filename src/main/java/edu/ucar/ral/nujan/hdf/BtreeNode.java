// The MIT License
// 
// Copyright (c) 2010 University Corporation for Atmospheric Research
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


package edu.ucar.ral.nujan.hdf;

import java.util.ArrayList;


/**
 * Represents a Btree node, required for chunked data.
 * <p>
 * Rather than have a full tree structure, we just use a single
 * leaf node that may be huge.  Performance tests have shown
 * this performs as well as the hierarchical tree structure.
 *
 * BtreeNodes are used in only one place:<ul>
 *   <li> MsgLayout uses a BtreeNode
 *     to point to the raw data chunks.
 *     Theoretically we could have a whole tree of chunks, but
 *     we always have exactly 1 chunk.  In general this is like
 *     the contiguous format, but we must use chunked since
 *     HDF5 compression requires chunked format.
 * </ul>
 *
 */

class BtreeNode extends BaseBlk {


/** The group containing the chunks to be represented by this BtreeNode */
HdfGroup hdfGroup;

ArrayList<byte[]> keyList;

int compressionLevel;

final int signa = 'T';
final int signb = 'R';
final int signc = 'E';
final int signd = 'E';

int nodeLevel;




final byte[] lowKey = new byte[5];

final byte[] highKey = new byte[] {
  (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};





/**
 * Constructor chunked data trees.
 *
 * @param compressionLevel Zip compression level:
 *        0 is uncompressed; 1 - 9 are increasing compression.
 * @param hdfGroup The owning group, containing the chunks to be
 *        represented by this BtreeNode.
 * @param hdfFile The global owning HdfFileWriter.
 */

BtreeNode(
  int compressionLevel,
  HdfGroup hdfGroup,
  HdfFileWriter hdfFile)
{
  super("BtreeNode", hdfFile);
  this.compressionLevel = compressionLevel;
  this.hdfGroup = hdfGroup;
}






public String toString() {
  String res = super.toString();
  res += "  nodeLevel: " + nodeLevel;
  res += "  group: \"" + hdfGroup.groupName + "\"";
  return res;
}






/**
 * Formats this individual BaseBlk to fmtBuf;
 * calls addWork to add any referenced BaseBlks (subNode, subTable)
 * to workList; extends abstract BaseBlk.
 *
 * <pre>
 * The formatted output is always the same, the fileVersion==1 format.
 * Oddly fileVersion==2 only uses Btrees for the chunked data
 * (not for group names), but still requires fileVersion==1 Btrees
 * for data chunks.
 *
 * We must format all the entries to fill out the btree node,
 * even if some are empty.
 * If the full table is not present, the HDF5 C software
 * may die when it tries to load the table and finds
 * the max table length extends beyond the end of file.
 *
 * A sample stack trace is appended to this file.
 *
 * ==========
 *
 * Analysis of Btree node size in HDF5 1.8.4 C API.
 *
 * See H5B.c:1784
 *     shared->sizeof_rnode = (H5B_SIZEOF_HDR(f) +    // node header
 *       shared->two_k * H5F_SIZEOF_ADDR(f) +         // child pointers
 *       (shared->two_k + 1) * shared->sizeof_rkey);  // keys
 * 
 * Expanding:
 *     shared->sizeof_rnode = (
 *       H5B_SIZEOF_HDR(f)
 *              # See H5B.c:118
 *              #   header: 4(magic) + 1(type) + 1(level) + 2(numUsed)
 *              #         + 8(leftSibling) + 8(rightSibling) = 24
 * 
 *     + shared->two_k
 *              # See H5B.c:1761,1780
 *              #     H5B_shared_new(...)
 *              #     two_k = 2 * H5F_KVALUE(f, type)
 *              # See H5Fprivate.h:267
 *              #     #define H5F_KVALUE(F,T)         (H5F_Kvalue(F,T))
 *              # See H5Fquery.c:289
 *              #     H5F_Kvalue(const H5F_t *f, const H5B_class_t *type)
 *              #     returns: f->shared->sblock->btree_k[type->id]
 *              #       = 2 * shared->sblock->btree_k[type(SNODE or CHUNK)]
 *              #
 *              # However, sometimes the btree_k we specify is
 *              # overridden by the default:
 *              # See H5Fsuper_cache.c:267
 *              #     btree_k[H5B_CHUNK_ID] = HDF5_BTREE_CHUNK_IK_DEF;
 *              # See H5Fprivate.h:396
 *              #     #define HDF5_BTREE_SNODE_IK_DEF         16
 *              #     #define HDF5_BTREE_CHUNK_IK_DEF         32
 *              #     /* Note! this value is assumed to be 32 for
 *              #     version 0 of the superblock and if it is
 *              #     changed, the code must compensate. -QAK
 * 
 *     * H5F_SIZEOF_ADDR(f)
 *              # See H5Fprivate.h:240
 *              #     #define H5F_SIZEOF_ADDR(F)   ((F)->shared->sizeof_addr)
 *               #     = 8   # len of ptr
 * 
 *     + (shared->two_k + 1) * shared->sizeof_rkey);
 *              # See H5Dbtree.c: H5D_btree_shared_create
 *              #   sizeof_rkey = 4 +         # chunk size
 *              #                 4 +         # filter mask
 *              #                 ndims * 8;  # dimension offsets
 * </pre>
 *
 * @param formatPass: <ul>
 *   <li> 1: Initial formatting to determine the formatted length.
 *          In HdfGroup we add msgs to hdrMsgList.
 *   <li> 2: Final formatting.
 * </ul>
 * @param fmtBuf  output buffer
 */







void formatBuf(
  int formatPass,
  HBuffer fmtBuf)
throws HdfException
{
  setFormatEntry( formatPass, true, fmtBuf); // BaseBlk: set blkPos, buf pos

  int numChunk = hdfGroup.hdfChunks.length;
  if (numChunk > hdfFile.maxNumBtreeKid) hdfFile.maxNumBtreeKid = numChunk;

  fmtBuf.putBufByte("BtreeNode: signa", signa);
  fmtBuf.putBufByte("BtreeNode: signb", signb);
  fmtBuf.putBufByte("BtreeNode: signc", signc);
  fmtBuf.putBufByte("BtreeNode: signd", signd);

  fmtBuf.putBufByte("BtreeNode: nodeType", 1);   // data node
  fmtBuf.putBufByte("BtreeNode: nodeLevel", nodeLevel);

  fmtBuf.putBufShort("BtreeNode: numChunk", numChunk);

  fmtBuf.putBufLong("BtreeNode: leftSibling.pos",
    HdfFileWriter.UNDEFINED_ADDR);

  fmtBuf.putBufLong("BtreeNode: rightSibling.pos",
    HdfFileWriter.UNDEFINED_ADDR);


  // Only one format, since fileVersion==2 uses fileVersion==1 format.

  // Turn on bit i to skip filter i.
  int filterMask = 0;                      // use all filters

  // For each chunk, format: chunkSize, key, diskAddr
  for (int ichunk = 0; ichunk < hdfGroup.hdfChunks.length; ichunk++) {
    HdfChunk chunk = hdfGroup.hdfChunks[ichunk];

    fmtBuf.putBufInt("BtreeNode: chunkSize",
      (int) chunk.chunkDataSize);     // xxx convert long to int
    fmtBuf.putBufInt("BtreeNode: key filterMask", filterMask);
    for (int ii = 0; ii < hdfGroup.varRank; ii++) {
      fmtBuf.putBufLong("BtreeNode: key startIx", chunk.chunkStartIxs[ii]);
    }
    fmtBuf.putBufLong("BtreeNode: key eleLen offset", 0);

    // Format the child pointer
    fmtBuf.putBufLong("BtreeNode: chunk addr", chunk.chunkDataAddr);
  }

  // Format the final key
  fmtBuf.putBufInt("BtreeNode: final key chunkSize", 0);
  fmtBuf.putBufInt("BtreeNode: final key filterMask", filterMask);
  for (int jj = 0; jj < hdfGroup.varRank; jj++) {
    fmtBuf.putBufLong("BtreeNode: final key dimOffset", hdfGroup.varDims[jj]);
  }
  fmtBuf.putBufLong("BtreeNode: final key eleLen offset",
    hdfGroup.msgDataType.elementLen);

  // We must format all the entries to fill out the btree node,
  // even if some are empty.
  // See doc at method start.
  int safe_k_value = 128;     // >= HDF5_BTREE_CHUNK_IK_DEF == 32
  for (int ii = 0; ii < 2 * safe_k_value - 1; ii++) {
    // Format the fake child pointer
    fmtBuf.putBufLong("BtreeNode: fill chunk addr", 0);

    // Format the fill key
    fmtBuf.putBufInt("BtreeNode: fill key chunkSize", 0);
    fmtBuf.putBufInt("BtreeNode: fill key mask", 0);
    for (int jj = 0; jj < hdfGroup.msgDataSpace.rank; jj++) {
      fmtBuf.putBufLong("BtreeNode: fill key dimOffset", 0);
    }
    fmtBuf.putBufLong("BtreeNode: fill key eleLen offset", 0);
  } // for ii


  noteFormatExit( fmtBuf);         // BaseBlk: print debug
} // end formatBuf

} // end class


// Sample stack trace from HDF5 1.8.4 when there aren't
// as many empty entries in the btree node as the C software
// expects, and the calculated table end falls beyond the eof.
//
// Why keep a stack trace?  It may be useful the next time
// someone has to use gdb with HDF5 to debug a btree problem.
// In particular the breakpoint at H5Bcache.c:200 was handy.
//
// #1  0x081148af in H5FD_read (file=0x84b9408, dxpl_id=167772168,
//     type=H5FD_MEM_BTREE, addr=13144, size=4696, buf=0x84bf608) at H5FDint.c:141
// #2  0x0810b2f3 in H5F_accum_read (f=0x84b9480, dxpl_id=167772168,
//     type=H5FD_MEM_BTREE, addr=13144, size=4696, buf=0x84bf608)
//     at H5Faccum.c:195
// #3  0x08129467 in H5F_block_read (f=0x84b9480, type=H5FD_MEM_BTREE,
//     addr=13144, size=4696, dxpl_id=167772168, buf=0x84bf608) at H5Fio.c:112
// #4  0x0808c260 in H5B_load (f=0x84b9480, dxpl_id=167772168, addr=13144,
//     _type=0x8493c20, udata=0xbfffcaf8) at H5Bcache.c:200
// #5  0x080a5331 in H5C_load_entry (f=0x84b9480, dxpl_id=167772168,
//     type=0x83d2fc8, addr=13144, udata1=0x8493c20, udata2=0xbfffcaf8,
//     skip_file_checks=0) at H5C.c:10988
// #6  0x0809b5de in H5C_protect (f=0x84b9480, primary_dxpl_id=167772168,
//     secondary_dxpl_id=167772168, cache_ptr=0xb754f008, type=0x83d2fc8,
//     addr=13144, udata1=0x8493c20, udata2=0xbfffcaf8, flags=1024) at H5C.c:6155
// #7  0x0805ba43 in H5AC_protect (f=0x84b9480, dxpl_id=167772168,
//     type=0x83d2fc8, addr=13144, udata1=0x8493c20, udata2=0xbfffcaf8,
//     rw=H5AC_READ) at H5AC.c:1819
// #8  0x08093953 in H5B_get_info_helper (f=0x84b9480, dxpl_id=167772168,
//     type=0x8493c20, addr=13144, info_udata=0xbfffcaa8) at H5B.c:1947
// #9  0x08093f90 in H5B_get_info (f=0x84b9480, dxpl_id=167772168,
//     type=0x8493c20, addr=13144, bt_info=0xbfffcae8, op=0, udata=0xbfffcaf8)
//     at H5B.c:2047
// #10 0x080a9ea5 in H5D_btree_idx_size (idx_info=0xbfffcb4c,
//     index_size=0xbfffd21c) at H5Dbtree.c:1335
// #11 0x080b8469 in H5D_chunk_bh_info (f=0x84b9480, dxpl_id=167772168,
//     layout=0xbfffcbd4, pline=0xbfffcb98, index_size=0xbfffd21c)
//     at H5Dchunk.c:4574
// #12 0x080f37f7 in H5O_dset_bh_info (f=0x84b9480, dxpl_id=167772168,
//     oh=0x84bbeb8, bh_info=0xbfffd21c) at H5Doh.c:396
// #13 0x0821dd4c in H5O_get_info (oloc=0xbfffd028, dxpl_id=167772168,
//     want_ih_info=1, oinfo=0xbfffd1b0) at H5O.c:2528
// #14 0x0816325e in H5G_loc_info_cb (grp_loc=0xbfffd038,
//     name=0x84ba938 "testVar0000", lnk=0xbfffcff0, obj_loc=0xbfffd014,
//     _udata=0xbfffd124, own_loc=0xbfffcfec) at H5Gloc.c:659
// #15 0x0817cd02 in H5G_traverse_real (_loc=0xbfffd168,
//     name=0x84bbddf "testVar0000", target=0, nlinks=0xbfffd0d8,
//     op=0x816315c <H5G_loc_info_cb>, op_data=0xbfffd124, lapl_id=167772160,
//     dxpl_id=167772168) at H5Gtraverse.c:702
// #16 0x0817d6ca in H5G_traverse (loc=0xbfffd168,
//     name=0x84bbdd8 "alpha2/testVar0000", target=0,
//     op=0x816315c <H5G_loc_info_cb>, op_data=0xbfffd124, lapl_id=167772160,
//     dxpl_id=167772168) at H5Gtraverse.c:876
// #17 0x0816340b in H5G_loc_info (loc=0xbfffd168,
//     name=0x84bbdd8 "alpha2/testVar0000", want_ih_info=1, oinfo=0xbfffd1b0,
//     lapl_id=167772160, dxpl_id=167772168) at H5Gloc.c:704
// #18 0x08217255 in H5Oget_info_by_name (loc_id=33554432,
//     name=0x84bbdd8 "alpha2/testVar0000", oinfo=0xbfffd1b0, lapl_id=167772160)
//     at H5O.c:615
// #19 0x0839a63b in traverse_cb (loc_id=33554432,
//     path=0x84bbdd8 "alpha2/testVar0000", linfo=0xbfffd304, _udata=0xbfffd7cc)
//     at h5trav.c:170
// #20 0x0816f8ce in H5G_visit_cb (lnk=0x84bbd10, _udata=0xbfffd710) at H5G.c:1627
// #21 0x08161470 in H5G_link_iterate_table (ltable=0xbfffd3d8, skip=0,
//     last_lnk=0x0, op=0x816f638 <H5G_visit_cb>, op_data=0xbfffd710)
//     at H5Glink.c:626
// #22 0x08154b0c in H5G_compact_iterate (oloc=0xbfffd500, dxpl_id=167772168,
//     linfo=0xbfffd43c, idx_type=H5_INDEX_NAME, order=H5_ITER_INC, skip=0,
//     last_lnk=0x0, op=0x816f638 <H5G_visit_cb>, op_data=0xbfffd710)
//     at H5Gcompact.c:428
// #23 0x081720ca in H5G_obj_iterate (grp_oloc=0xbfffd500,
//     idx_type=H5_INDEX_NAME, order=H5_ITER_INC, skip=0, last_lnk=0x0,
//     op=0x816f638 <H5G_visit_cb>, op_data=0xbfffd710, dxpl_id=167772168)
//     at H5Gobj.c:588
// #24 0x0816fd32 in H5G_visit_cb (lnk=0x84bb590, _udata=0xbfffd710) at H5G.c:1711
// #25 0x08161470 in H5G_link_iterate_table (ltable=0xbfffd5f8, skip=0,
//     last_lnk=0x0, op=0x816f638 <H5G_visit_cb>, op_data=0xbfffd710)
//     at H5Glink.c:626
// #26 0x08154b0c in H5G_compact_iterate (oloc=0x84bb4dc, dxpl_id=167772168,
//     linfo=0xbfffd65c, idx_type=H5_INDEX_NAME, order=H5_ITER_INC, skip=0,
//     last_lnk=0x0, op=0x816f638 <H5G_visit_cb>, op_data=0xbfffd710)
//     at H5Gcompact.c:428
// #27 0x081720ca in H5G_obj_iterate (grp_oloc=0x84bb4dc, idx_type=H5_INDEX_NAME,
//     order=H5_ITER_INC, skip=0, last_lnk=0x0, op=0x816f638 <H5G_visit_cb>,
//     op_data=0xbfffd710, dxpl_id=167772168) at H5Gobj.c:588
// #28 0x0817049d in H5G_visit (loc_id=16777216, group_name=0x8488f6c "/",
//     idx_type=H5_INDEX_NAME, order=H5_ITER_INC, op=0x839a507 <traverse_cb>,
//     op_data=0xbfffd7cc, lapl_id=167772160, dxpl_id=167772168) at H5G.c:1860
// #29 0x081ce0fd in H5Lvisit_by_name (loc_id=16777216, group_name=0x8488f6c "/",
//     idx_type=H5_INDEX_NAME, order=H5_ITER_INC, op=0x839a507 <traverse_cb>,
//     op_data=0xbfffd7cc, lapl_id=167772160) at H5L.c:1379
// #30 0x0839a8e3 in traverse (file_id=16777216, grp_name=0x8488f6c "/",
//     visit_start=1, recurse=1, visitor=0xbfffd89c) at h5trav.c:258
// #31 0x0839b87c in h5trav_visit (fid=16777216, grp_name=0x8488f6c "/",
//     visit_start=1, recurse=1, visit_obj=0x8390d61 <find_objs_cb>, visit_lnk=0,
//     udata=0xbfffd908) at h5trav.c:970
// #32 0x08391018 in init_objs (fid=16777216, info=0xbfffd908,
//     group_table=0x84bae90, dset_table=0x84bae94, type_table=0x84bae98)
//     at h5tools_utils.c:566
// #33 0x0804a415 in table_list_add (oid=16777216, file_no=1)
//     at /home/ss/ftp/hdf5/tda/hdf5-1.8.4-patch1/tools/h5dump/h5dump.c:755
// #34 0x080525a3 in main (argc=2, argv=0xbfffdab4)
//     at /home/ss/ftp/hdf5/tda/hdf5-1.8.4-patch1/tools/h5dump/h5dump.c:4403

