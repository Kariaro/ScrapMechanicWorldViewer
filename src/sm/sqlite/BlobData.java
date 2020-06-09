package sm.sqlite;

// TODO: Maybe implement this for some of the data inside the SQLiteObjects
public @interface BlobData {
	int offset() default 0;
	int size() default 0;
	boolean bigEndian() default true;
	BlobType type();
}
