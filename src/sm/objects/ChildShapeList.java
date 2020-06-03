package sm.objects;

import sm.sqlite.Sqlite;

public class ChildShapeList extends SQLiteObject {
	public ChildShapeList(Sqlite sqlite) {
		super(sqlite, "ChildShape");
	}
	
	
	public class ChildShape extends SQLiteObject {
		private ChildShape(Sqlite sqlite) {
			super(sqlite, "ChildShape");
		}
		
	}
}
