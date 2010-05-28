
package hdfnet;


abstract class BaseBlk {


long blkPosition;

String blkName;
HdfFile hdfFile;


BaseBlk(
  String blkName,
  HdfFile hdfFile)
{
  this.blkName = blkName;
  this.hdfFile = hdfFile;
}



public String toString() {
  String res = String.format("  blkName: %s  pos: 0x%x",
    blkName, blkPosition);
  return res;
}




abstract void formatBuf( int formatPass, HBuffer fmtBuf)
throws HdfException;








// Sets this.blkPosition and bbuf position
void setFormatEntry(
  int formatPass,
  boolean useAlign,
  HBuffer fmtBuf)
throws HdfException
{
  hdfFile.indent++;

  fmtBuf.alignPos( "setFormatEntry for " + blkName, 8);
  blkPosition = fmtBuf.getPos();
  if (hdfFile.bugs >= 5)
    prtf( hdfFile.formatName("setFormatEntry: " + blkName, fmtBuf.getPos()));
}




void noteFormatExit(
  HBuffer fmtBuf)
throws HdfException
{
  if (hdfFile.bugs >= 5)
    prtf( hdfFile.formatName("noteFormatExit: " + blkName, fmtBuf.getPos()));
  hdfFile.indent--;
}





static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}





void prtIndent( String msg, Object... args) {
  prtf( hdfFile.mkIndent() + msg, args);
}




static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}

} // end class
