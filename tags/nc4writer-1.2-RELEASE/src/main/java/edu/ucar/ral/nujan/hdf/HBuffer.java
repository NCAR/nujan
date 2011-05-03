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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.zip.Deflater;


/**
 * A write-only buffer either in memory or on top of an
 * open output FileChannel.
 */

class HBuffer {


/**
 * Approx len of write to channel
 */
static final int BLEN = 10000;


/**
 * Open output FileChannel, or null in the case of a memory-only buffer.
 */
private FileChannel outChannel;

/**
 * Deflater compression level (0==none) for use with outChannel.
 */
int compressionLevel;

/**
 * The global owning HdfFileWriter.
 */
HdfFileWriter hdfFile;


/**
 * The in-memory buffer, or the front end to the outChannel.
 */
private ByteBuffer bbuf;

/**
 * Used to compress when compressionLevel > 0.
 */
Deflater deflater;



/**
 * Creates a write-only buffer on top of an open output FileChannel,
 * or, if outChannel==null, in main memory.
 *
 * @param outChannel Either an open output FileChannel, or null.
 * @param compressionLevel The Deflater compression level used with
 *     an open output FileChannel.  0 == no compression.
 * @param hdfFile The global owning HdfFileWriter.
 */

HBuffer(
  FileChannel outChannel,         // if null, just builds internal bbuf.
  int compressionLevel,
  HdfFileWriter hdfFile)
throws HdfException
{
  this.outChannel = outChannel;
  this.compressionLevel = compressionLevel;
  this.hdfFile = hdfFile;

  bbuf = ByteBuffer.allocate( BLEN);
  bbuf.order( ByteOrder.LITTLE_ENDIAN);
  if (compressionLevel > 0) {
    if (outChannel == null)
      throwerr("cannot have compressionLevel > 0 with no outChannel");
    deflater = new Deflater( compressionLevel);
  }
  if (hdfFile.bugs >= 5) {
    prtf("HBuffer: outChannel: %s  compressionLevel: %d",
      outChannel == null ? "no" : "yes",
      compressionLevel);
  }
}



public String toString() {
  String res = "outChannel:";
  if (outChannel == null) res += " null";
  else {
    try {
      res += " pos: " + outChannel.position();
    }
    catch( IOException exc) {
      exc.printStackTrace();
      res += " pos: " + exc + "  caught: " + exc;
    }
  }
  res += "  bbuf: " + bbuf;
  return res;
}




/**
 * Clears the in-memory buffer, but doesn't change the outChannel.
 */

void clear() {
  bbuf.clear();
}



/**
 * Returns the current position of the in-memory buffer.
 */

int getPos() {
  return bbuf.position();
}



/**
 * Sets the current position of the in-memory buffer.
 */

void setPos( long pos)
throws HdfException
{
  if (pos < 0 || pos >= bbuf.capacity()) throwerr("invalid setPos");
  bbuf.position( (int) pos);
}






/**
 * Returns a subset of the bytes in the in-memory buffer:
 *    for startPos &lt;= pos &lt; limPos.
 */

byte[] getBufBytes(
  long startPos,
  long limPos)
throws HdfException
{
  // Allow the case of an empty buffer: startPos == limPos == 0.
  if (startPos < 0 || startPos > getPos()) throwerr("invalid startPos");
  if (limPos < startPos || limPos > getPos()) throwerr("invalid limPos");
  int blen = (int) (limPos - startPos);
  byte[] bytes = new byte[blen];
  for (int ii = 0; ii < blen; ii++) {
    bytes[ii] = bbuf.get( (int) startPos + ii);
  }
  return bytes;
}





/**
 * Writes the in-memory buffer to <b><tt>chan</tt><b>
 * - a <b>different</b> FileChannel than our outChannel.
 * Must have compressionLevel == 0.
 */

void writeChannel( FileChannel chan)
throws HdfException
{
  if (hdfFile.bugs >= 5) {
    prtf("writeChannel: bbuf: pos: %d  limit: %d  capacity: %d",
      getPos(), bbuf.limit(), bbuf.capacity());
  }
  if (outChannel != null) throwerr("two channels specified");
  if (compressionLevel > 0) throwerr("compression not supported here");
  bbuf.flip();

  try { chan.write( bbuf); }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: %s", exc);
  }
  bbuf.clear();
}



/**
 * Insure bbuf has at least idelta free space - if bbuf is too full,
 * write bbuf to outChannel (if outChanel != null) or expand bbuf.
 */

private void expandBuf( int idelta)
throws HdfException
{
  //if (hdfFile.bugs >= 10) {
  //  prtf("expandBuf: idelta: %d  bbuf: pos: %d  limit: %d  capacity: %d",
  //    idelta, getPos(), bbuf.limit(), bbuf.capacity());
  //}

  if (getPos() + idelta > bbuf.capacity()) {
    if (outChannel == null) {
      // Expand bbuf
      int newLen = 100 + 2 * (getPos() + idelta);
      if (hdfFile.bugs >= 10) {
        prtf("expandBuf: expand A: getPos: %d  idelta: %d  newLen: %d",
          getPos(), idelta, newLen);
      }
      ByteBuffer newbuf = ByteBuffer.allocateDirect( newLen);
      newbuf.order( ByteOrder.LITTLE_ENDIAN);

      byte[] oldVals = new byte[ getPos()];
      bbuf.flip();
      bbuf.get( oldVals);

      newbuf.put( oldVals);
      bbuf = newbuf;
    }
    else {        // else we have outChannel: write it
      // Write bbuf to outChannel
      if (hdfFile.bugs >= 10) {
        prtf("expandBuf: write: getPos: %d  idelta: %d  compressionLevel: %d",
          getPos(), idelta, compressionLevel);
      }

      try {
        if (compressionLevel > 0) writeCompressedOutput();
        else {
          bbuf.flip();
          outChannel.write( bbuf);
        }
      }
      catch( IOException exc) {
        exc.printStackTrace();
        throwerr("caught: %s", exc);
      }

      bbuf.clear();
      // If idelta > bbuf.capacity, we still need to reallocate
      if (idelta > bbuf.capacity()) {
        // Expand bbuf
        int newLen = 100 + 2 * idelta;
        if (hdfFile.bugs >= 10) {
          prtf("expandBuf: expand with outChannel: idelta: %d  newLen: %d",
            idelta, newLen);
        }
        bbuf = ByteBuffer.allocateDirect( newLen);
        bbuf.order( ByteOrder.LITTLE_ENDIAN);
      }
    }
  }
} // end expandBuf




/**
 * Compresses bbuf contents and writes to outChannel.
 */

private void writeCompressedOutput()
throws IOException, HdfException
{
  if (hdfFile.bugs >= 5) {
    prtf("writeCompressedOutput.entry:");
    prtf("  bbuf: pos: %d  limit: %d  capacity: %d",
      getPos(), bbuf.limit(), bbuf.capacity());
    prtf("  outChannel: pos: %d", outChannel.position());
  }
  byte[] bytes = new byte[ getPos()];
  bbuf.flip();
  bbuf.get( bytes);

  deflater.setInput( bytes, 0, bbuf.position());
  byte[] compBytes = new byte[ BLEN];
  ByteBuffer cbuf = ByteBuffer.wrap( compBytes);
  cbuf.order( ByteOrder.LITTLE_ENDIAN);

  while (true) {
    int compLen = deflater.deflate( compBytes);
    if (compLen == 0) break;

    cbuf.position( 0);
    cbuf.limit( compLen);
    outChannel.write( cbuf);
  }
  if (hdfFile.bugs >= 5) {
    prtf("writeCompressedOutput.exit:");
    prtf("  outChannel: pos: %d", outChannel.position());
  }
}




/**
 * Writes bbuf to outChannel, compressing if need be.
 */

void flush()
throws IOException, HdfException
{
  if (outChannel == null) throwerr("cannot flush null channel");
  if (hdfFile.bugs >= 5)
    prtf("flush.entry: outChannel.pos: %d", outChannel.position());
  try {
    if (compressionLevel > 0) {
      deflater.finish();
      writeCompressedOutput();
    }
    else {
      bbuf.flip();
      outChannel.write( bbuf);
    }
  }
  catch( IOException exc) {
    exc.printStackTrace();
    throwerr("caught: %s", exc);
  }
  bbuf.clear();
  if (hdfFile.bugs >= 5)
    prtf("flush.exit: outChannel.pos: %d", outChannel.position());
}





/**
 * Advances bbuf's position to the next multiple of bound.
 * @param msg Unused
 * @param bound  The desired multiple, such as 8.
 */

long alignPos(
  String msg,
  long bound)
throws HdfException
{
  byte fillByte = 0x77;
  int oldPos = getPos();

  while (getPos() % bound != 0) {
    putBufByte( "align fill", fillByte);
  }
  return getPos();
}




/**
 * Puts a single byte to the internal buffer.
 * @param name  debug name
 * @param value contains the value in the low order byte.
 */

void putBufByte(
  String name,
  int value)
throws HdfException
{
  expandBuf( 1);
  if (value < 0 || value > 255)
    throwerr("putBufByte: invalid value: " + value);
  if (hdfFile.bugs >= 5)
    printValue( 1, name, new Byte( (byte) (0xff & value)));
  bbuf.put( (byte) value);
}


/**
 * Puts an array of bytes to the internal buffer.
 * @param name  debug name
 * @param values the bytes to be copied.
 */

void putBufBytes(
  String name,
  byte[] values)
throws HdfException
{
  expandBuf( values.length);
  if (hdfFile.bugs >= 5)
    printValue( values.length, name, values);
  bbuf.put( values);
}


/**
 * Puts a single short value to the internal buffer.
 * @param name  debug name
 * @param value contains the value in the low order two bytes.
 */

void putBufShort(
  String name,
  int value)
throws HdfException
{
  expandBuf( 2);
  if (value < Short.MIN_VALUE || value > Short.MAX_VALUE)
    throwerr("putBufShort: invalid value: " + value);
  if (hdfFile.bugs >= 5) printValue( 2, name, new Short( (short) value));
  bbuf.putShort( (short) value);
}


/**
 * Puts a single int value to the internal buffer.
 * @param name  debug name
 * @param value contains the value to be copied.
 */

void putBufInt(
  String name,
  int value)
throws HdfException
{
  expandBuf( 4);
  if (hdfFile.bugs >= 5) printValue( 4, name, new Integer( value));
  bbuf.putInt( value);
}


/**
 * Puts a single long value to the internal buffer.
 * @param name  debug name
 * @param value contains the value to be copied.
 */

void putBufLong(
  String name,
  long value)
throws HdfException
{
  expandBuf( 8);
  if (hdfFile.bugs >= 5) printValue( 8, name, new Long( value));
  bbuf.putLong( value);
}


/**
 * Puts a single float value to the internal buffer.
 * @param name  debug name
 * @param value contains the value to be copied.
 */

void putBufFloat(
  String name,
  float value)
throws HdfException
{
  expandBuf( 4);
  if (hdfFile.bugs >= 5) printValue( 4, name, new Float(value));
  bbuf.putFloat( value);
}


/**
 * Puts a single double value to the internal buffer.
 * @param name  debug name
 * @param value contains the value to be copied.
 */

void putBufDouble(
  String name,
  double value)
throws HdfException
{
  expandBuf( 8);
  if (hdfFile.bugs >= 5) printValue( 8, name, new Double(value));
  bbuf.putDouble( value);
}



/**
 * Appends the contents of inBuf to our internal buffer.
 * @param name  debug name
 * @param inBuf  The buffer to be copied.
 */

void putBufBuf(
  String name,
  HBuffer inBuf)
throws HdfException
{
  int inLen = inBuf.getPos();
  expandBuf( inLen);
  byte[] inBytes = inBuf.getBufBytes( 0, inLen);
  if (hdfFile.bugs >= 5)
    printValue( inLen, name, inBytes);
  bbuf.put( inBytes);
}



/**
 * Debug: prints name, valueLength, hexValue.
 * Called by putBuf*.
 */

private void printValue(
  int len,
  String name,
  Object value)
throws HdfException
{
  if (name != null) {
    String decStg = null;
    StringBuilder hexBuf = new StringBuilder();
    if (value instanceof Byte) {
      hexBuf.append( String.format("%02x", 0xff & ((Byte) value).byteValue()));
      decStg = "" + (0xff & ((Byte) value).byteValue());
    }
    else if (value instanceof Short) {
      hexBuf.append( String.format("%04x",
        0xffff & ((Short) value).shortValue()));
      decStg = "" + (0xffff & ((Short) value).shortValue());
    }
    else if (value instanceof Integer) {
      hexBuf.append( String.format("%08x", ((Integer) value).intValue()));
      decStg = "" + value;
    }
    else if (value instanceof Long) {
      hexBuf.append( String.format("%016x", ((Long) value).longValue()));
      decStg = "" + value;
    }
    else if (value instanceof Float) {
      decStg = "" + value;
    }
    else if (value instanceof Double) {
      decStg = "" + value;
    }
    else if (value instanceof byte[]) {
      for (byte bb : (byte[]) value) {
        hexBuf.append( String.format("%02x", 0xff & bb));
      }
    }
    else throwerr("unknown type: " + value.getClass());

    String msg = String.format("%s  len: %d",
      hdfFile.formatName( name, bbuf.position()),
      len);
    if (hexBuf.length() > 0) msg += "  hex: " + hexBuf.toString();
    if (decStg != null) msg += "  dec: " + decStg;
    prtf( msg);
  }
} // end printValue





static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}




static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}



} // end class
