package sm.sqlite;

public enum BlobType {
	BOOL,
	BYTE,
	SHORT,
	INT_3BYTE,
	INT,
	@Deprecated LONG_5BYTE,
	@Deprecated LONG_6BYTE,
	@Deprecated LONG_7BYTE,
	LONG,
	UUID,
}
