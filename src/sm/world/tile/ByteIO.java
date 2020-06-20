package sm.world.tile;

import java.util.UUID;

// TODO - Make this class more readable
public class ByteIO {
	private byte[] bytes;
	
	private int writerIndex;
	private int readerIndex;
	
	private int writerMark;
	private int readerMark;
	
	private boolean lockWriter;
	private boolean lockReader;
	
	public ByteIO(int capacity) {
		this(new byte[capacity]);
	}
	
	public ByteIO(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public ByteIO(ByteIO object) {
		bytes = new byte[object.bytes.length];
		writerIndex = object.writerIndex;
		readerIndex = object.readerIndex;
		writerMark = object.writerMark;
		readerMark = object.readerMark;
		System.arraycopy(object.bytes, 0, bytes, 0, bytes.length);
	}
	
	public ByteIO(ByteReader object) {
		byte[] data = object.data();
		bytes = new byte[data.length];
		writerIndex = 0;
		readerIndex = object.index();
		System.arraycopy(data, 0, bytes, 0, bytes.length);
	}
	
	public int writerIndex() {
		return writerIndex;
	}
	
	public int writerIndex(int index) {
		int result = writerIndex;
		writerIndex = index;
		return result;
	}
	
	public void markWriter() {
		writerMark = writerIndex;
	}
	
	public void resetWriter() {
		writerIndex = writerMark;
	}
	
	public void moveWriterIndex(int index) {
		writerIndex += index;
	}
	
	public int readerIndex() {
		return readerIndex;
	}
	
	public int readerIndex(int index) {
		int result = readerIndex;
		readerIndex = index;
		return result;
	}
	
	public void markReader() {
		readerMark = readerIndex;
	}
	
	public void resetReader() {
		readerIndex = readerMark;
	}
	
	public void moveReaderIndex(int index) {
		readerIndex += index;
	}
	
	public void moveIndex(int index) {
		writerIndex += index;
		readerIndex += index;
	}
	
	public int remaining() {
		return bytes.length - readerIndex;
	}
	
	public byte[] data() {
		return bytes;
	}
	
	public void lockWriter(boolean lock) {
		lockWriter = lock;
	}
	
	public void lockReader(boolean lock) {
		lockReader = lock;
	}
	
	// ====================== READING FROM BYTES ====================== //
	
	public byte readByte() {
		return bytes[readerIndex++];
	}
	
	public int readUnsignedByte() {
		return Byte.toUnsignedInt(readByte());
	}
	
	public short readShort(boolean bigEndian) {
		if(bigEndian) {
			return (short)((readUnsignedByte() << 8) |
						   (readUnsignedByte()));
		} else {
			return (short)((readUnsignedByte()) |
						   (readUnsignedByte() << 8));
		}
	}
	
	public int readUnsignedShort(boolean bigEndian) {
		return Short.toUnsignedInt(readShort(bigEndian));
	}
	
	public int readInt(boolean bigEndian) {
		if(bigEndian) {
			return (readUnsignedShort(bigEndian) << 16) |
				   (readUnsignedShort(bigEndian));
		} else {
			return (readUnsignedShort(bigEndian)) |
				   (readUnsignedShort(bigEndian) << 16);
		}
	}
	
	public long readUnsignedInt(boolean bigEndian) {
		return Integer.toUnsignedLong(readInt(bigEndian));
	}
	
	public float readFloat(boolean bigEndian) {
		return Float.intBitsToFloat(readInt(bigEndian));
	}
	
	public double readDouble(boolean bigEndian) {
		return Double.longBitsToDouble(readLong(bigEndian));
	}
	
	public long readLong(boolean bigEndian) {
		long a = readUnsignedInt(bigEndian);
		long b = readUnsignedInt(bigEndian);
		
		if(bigEndian) {
			return (a << 32L) | b;
		} else {
			return (b << 32L) | a;
		}
	}
	
	public UUID readUuid(boolean bigEndian) {
		long low = readLong(bigEndian);
		long high = readLong(bigEndian);
		
		if(bigEndian) {
			return new UUID(low, high);
		} else {
			return new UUID(high, low);
		}
	}
	
	public byte[] readBytes(int length) {
		byte[] dest = new byte[length];
		System.arraycopy(bytes, readerIndex, dest, 0, length);
		readerIndex += length;
		return dest;
	}
	
	// ====================== READING FROM BYTES OFFSET ====================== //
	
	public byte readByteOffset(int offset) {
		int a = readerIndex;
		readerIndex += offset;
		byte result = readByte();
		readerIndex = a;
		return result;
	}
	
	public int readUnsignedByteOffset(int offset) {
		return Byte.toUnsignedInt(readByteOffset(offset));
	}
	
	public short readShortOffset(int offset, boolean bigEndian) {
		int a = readerIndex;
		readerIndex += offset;
		short result = readShort(bigEndian);
		readerIndex = a;
		return result;
	}
	
	public int readUnsignedShortOffset(int offset, boolean bigEndian) {
		return Short.toUnsignedInt(readShortOffset(offset, bigEndian));
	}
	
	public byte[] readBytesOffset(int offset, int length) {
		int a = readerIndex;
		readerIndex += offset;
		byte[] result = readBytes(length);
		readerIndex = a;
		return result;
	}
	
	// ====================== WRITING TO BYTES ====================== //
	
	public ByteIO writeByte(int value) {
		if(lockWriter) throw new NullPointerException("Writer is locked");
		bytes[writerIndex++] = (byte)(value & 0xff);
		return this;
	}
	
	public ByteIO writeShort(int value, boolean bigEndian) {
		int a = (value >> 8) & 0xff;
		int b = value & 0xff;
		
		if(bigEndian) {
			return writeByte(a).writeByte(b);
		} else {
			return writeByte(b).writeByte(a);
		}
	}
	
	public ByteIO writeInt(int value, boolean bigEndian) {
		int a = (value >> 24) & 0xff;
		int b = (value >> 16) & 0xff;
		int c = (value >> 8) & 0xff;
		int d = value & 0xff;
		
		if(bigEndian) {
			return writeByte(a).writeByte(b).writeByte(c).writeByte(d);
		} else {
			return writeByte(d).writeByte(c).writeByte(b).writeByte(a);
		}
	}
	
	public ByteIO writeBytes(byte[] src, int srcPos, int length) {
		if(lockWriter) throw new NullPointerException("Writer is locked");
		System.arraycopy(src, srcPos, bytes, writerIndex, length);
		writerIndex += length;
		return this;
	}
	
	// ====================== WRITING TO BYTES OFFSET ====================== //
	
	public ByteIO writeByteOffset(int value, int offset) {
		int a = writerIndex;
		writerIndex += offset;
		writeByte(value);
		writerIndex = a;
		return this;
	}
	
	public ByteIO writeIntOffset(int value, int offset, boolean bigEndian) {
		int a = writerIndex;
		writerIndex += offset;
		writeInt(value, bigEndian);
		writerIndex = a;
		return this;
	}
	
	public ByteIO writeBytesOffset(byte[] src, int srcPos, int offset, int length) {
		int a = writerIndex;
		writerIndex += offset;
		writeBytes(src, srcPos, length);
		writerIndex = a;
		return this;
	}
	
}
