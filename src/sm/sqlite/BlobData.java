package sm.sqlite;

public @interface BlobData {
	int offset() default 0;
	int size() default 0;
	boolean bigEndian() default true;
	BlobType type();
}
