package sm.sqlite;

import sm.objects.SQLiteObject;

public class BlobTest extends SQLiteObject implements BlobPattern {
	public BlobTest(Sqlite sqlite) {
		super(sqlite);
	}
	
	@BlobData(offset = 0, size = 4, type = BlobType.INT, bigEndian = false)
	public int getSeed() { return 0; }

	@Override
	public byte[] getBlob() {
		return null;
	}
}
