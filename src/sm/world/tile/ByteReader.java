package sm.world.tile;

import java.util.UUID;

public class ByteReader {
	private byte[] bytes;
	private int index;
	private int mark;
	
	public ByteReader(int capacity) {
		this(new byte[capacity]);
	}
	
	public ByteReader(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public ByteReader(ByteReader reader) {
		index = reader.index;
		mark = reader.mark;
		bytes = new byte[reader.bytes.length];
		System.arraycopy(reader.bytes, 0, bytes, 0, bytes.length);
	}
	
	public ByteReader markIndex() {
		mark = index;
		return this;
	}
	
	public ByteReader resetIndex() {
		index = mark;
		return this;
	}
	
	public ByteReader skip(int length) {
		index += length;
		return this;
	}
	
	public int remaining() {
		return bytes.length - index;
	}
	
	public int index() {
		return index;
	}
	
	public int setIndex(int index) {
		int old = this.index;
		this.index = index;
		return old;
	}
	
	public byte[] data() {
		return bytes;
	}
	
	public String readString(int length, boolean bigEndian) {
		StringBuilder sb = new StringBuilder();
		sb.append(new String(bytes, index, length));
		index += length;
		
		if(bigEndian) {
			return sb.toString();
		}
		
		return sb.reverse().toString();
	}
	
	public byte readByte() {
		return bytes[index++];
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

	public ByteReader readBytes(byte[] dest, int offset, int length) {
		System.arraycopy(bytes, index, dest, offset, length);
		index += length;
		return this;
	}
	
	public byte[] readBytes(int length) {
		byte[] dest = new byte[length];
		readBytes(dest, 0, length);
		return dest;
	}
	
	public byte readByteOffset(int offset) {
		int a = index;
		index += offset;
		byte result = readByte();
		index = a;
		return result;
	}
	
	public int readUnsignedByteOffset(int offset) {
		return Byte.toUnsignedInt(readByteOffset(offset));
	}
	
	public short readShortOffset(int offset, boolean bigEndian) {
		int a = index;
		index += offset;
		short result = readShort(bigEndian);
		index = a;
		return result;
	}
	
	public int readUnsignedShortOffset(int offset, boolean bigEndian) {
		return Short.toUnsignedInt(readShortOffset(offset, bigEndian));
	}
	
	public int readIntOffset(int offset, boolean bigEndian) {
		int a = index;
		index += offset;
		int result = readInt(bigEndian);
		index = a;
		return result;
	}
	
	public long readUnsignedIntOffset(int offset, boolean bigEndian) {
		return Integer.toUnsignedLong(readIntOffset(offset, bigEndian));
	}
	
	public long readLongOffset(int offset, boolean bigEndian) {
		int a = index;
		index += offset;
		long result = readLong(bigEndian);
		index = a;
		return result;
	}
}
