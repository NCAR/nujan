
package hdfnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.zip.Deflater;


class HBuffer {


// xxx use bigger buffer:
static final int BLEN = 1000;     // approx len of write to channel


int bugs;
private FileChannel outChannel;   // if null, just builds internal bbuf.
int compressionLevel;
HdfFile hdfFile;

private ByteBuffer bbuf;
Deflater deflater;



//xxx don't pass in bugs; use hdfFile.bugs

HBuffer(
  int bugs,
  FileChannel outChannel,         // if null, just builds internal bbuf.
  int compressionLevel,
  HdfFile hdfFile)
{
  this.bugs = bugs;
  this.outChannel = outChannel;
  this.compressionLevel = compressionLevel;
  this.hdfFile = hdfFile;

  bbuf = ByteBuffer.allocate( BLEN);
  bbuf.order( ByteOrder.LITTLE_ENDIAN);
  if (compressionLevel > 0) {
    deflater = new Deflater( compressionLevel);
  }
  if (bugs >= 2) {
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
      res += " pos: " + exc;
      /// xxx quit?
    }
  }
  res += "  bbuf: " + bbuf;
  return res;
}





void clear() {
  bbuf.clear();
}




int getPos() {
  return bbuf.position();
}



void setPos( long pos)
throws HdfException
{
  if (pos < 0 || pos >= bbuf.capacity()) throwerr("invalid setPos");
  bbuf.position( (int) pos);
}





void writeChannel( FileChannel chan)
throws HdfException
{
  if (bugs >= 2) {
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



// If bbuf is too short, either expand it or write it to outChannel.

private void expandBuf( int idelta)
throws HdfException
{
  //if (bugs >= 10) {
  //  prtf("expandBuf: idelta: %d  bbuf: pos: %d  limit: %d  capacity: %d",
  //    idelta, getPos(), bbuf.limit(), bbuf.capacity());
  //}

  if (getPos() + idelta > bbuf.capacity()) {
    if (outChannel == null) {
      // Expand bbuf
      int newLen = 100 + 2 * (getPos() + idelta);
      if (bugs >= 10) {
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
      if (bugs >= 10) {
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
        if (bugs >= 10) {
          prtf("expandBuf: expand with outChannel: idelta: %d  newLen: %d",
            idelta, newLen);
        }
        bbuf = ByteBuffer.allocateDirect( newLen);
        bbuf.order( ByteOrder.LITTLE_ENDIAN);
      }
    }
  }
} // end expandBuf





void writeCompressedOutput()
throws IOException, HdfException
{
  if (bugs >= 2) {
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
  if (bugs >= 2) {
    prtf("writeCompressedOutput.exit:");
    prtf("  outChannel: pos: %d", outChannel.position());
  }
}




void flush()
throws IOException, HdfException
{
  if (outChannel == null) throwerr("cannot flush null channel");
  if (bugs >= 2)
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
  if (bugs >= 2)
    prtf("flush.exit: outChannel.pos: %d", outChannel.position());
}





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


void putBufBytes(
  String name,
  byte[] values)
throws HdfException
{
  expandBuf( values.length);
  if (hdfFile.bugs >= 5)
    printValue( values.length, name, new Byte( (byte) (0xff & values[0])));
  bbuf.put( values);
}


void putBufShort(
  String name,
  int value)
throws HdfException
{
  expandBuf( 2);
  if (value < 0 || value > 65535)
    throwerr("putBufShort: invalid value: " + value);
  if (hdfFile.bugs >= 5) printValue( 2, name, new Short( (short) value));
  bbuf.putShort( (short) value);
}


void putBufInt(
  String name,
  int value)
throws HdfException
{
  expandBuf( 4);
  if (hdfFile.bugs >= 5) printValue( 4, name, new Integer( value));
  bbuf.putInt( value);
}


void putBufLong(
  String name,
  long value)
throws HdfException
{
  expandBuf( 8);
  if (hdfFile.bugs >= 5) printValue( 8, name, new Long( value));
  bbuf.putLong( value);
}


void putBufFloat(
  String name,
  float value)
throws HdfException
{
  expandBuf( 4);
  if (hdfFile.bugs >= 5) printValue( 4, name, new Float(value));
  bbuf.putFloat( value);
}


void putBufDouble(
  String name,
  double value)
throws HdfException
{
  expandBuf( 8);
  if (hdfFile.bugs >= 5) printValue( 8, name, new Double(value));
  bbuf.putDouble( value);
}




byte[] getBufBytes(
  long startPos,
  long limPos)
throws HdfException
{
  if (startPos < 0 || startPos >= getPos()) throwerr("invalid startPos");
  if (limPos <= startPos || limPos > getPos()) throwerr("invalid limPos");
  int blen = (int) (limPos - startPos);
  byte[] bytes = new byte[blen];
  for (int ii = 0; ii < blen; ii++) {
    bytes[ii] = bbuf.get( (int) startPos + ii);
  }
  return bytes;
}





// Called by putBuf*

void printValue(
  int len,
  String name,
  Object value)
{
  if (name != null) {
    String hexStg = "";
    if (value instanceof Byte)
      hexStg = String.format("  hex: 0x%02x", ((Byte) value).byteValue());
    if (value instanceof Short)
      hexStg = String.format("  hex: 0x%04x", ((Short) value).shortValue());
    if (value instanceof Integer)
      hexStg = String.format("  hex: 0x%08x", ((Integer) value).intValue());
    if (value instanceof Long)
      hexStg = String.format("  hex: 0x%016x", ((Long) value).longValue());

    prtf("%s  len: %d%s  dec: %s",
      hdfFile.formatName( name, bbuf.position()),
      len,
      hexStg,
      value);
  }
}





static void prtf( String msg, Object... args) {
  System.out.printf( msg + "\n", args);
}




static void throwerr( String msg, Object... args)
throws HdfException
{
  throw new HdfException( String.format( msg, args));
}



} // end class
