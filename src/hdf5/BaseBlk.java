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


abstract class BaseBlk {


long blkPosition;

String blkName;
HdfFileWriter hdfFile;


BaseBlk(
  String blkName,
  HdfFileWriter hdfFile)
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
