package sm.sqlite.test;

import sm.sqlite.Sqlite;

@SqliteClass
public interface RigidBodyBoundsTest extends SqliteInterface {
	@SqliteCommand(command = "SELECT minX FROM RigidBodyBounds WHERE id = $id;")
	public double getMinX();
	
	@SqliteCommand(command = "SELECT maxX FROM RigidBodyBounds WHERE id = $id;")
	public double getMaxX();
	
	@SqliteCommand(command = "SELECT minY FROM RigidBodyBounds WHERE id = $id;")
	public double getMinY();
	
	@SqliteCommand(command = "SELECT maxX FROM RigidBodyBounds WHERE id = $id;")
	public double getMaxY();
	
	@SqliteCommand
	public default void getSomething() {
		System.out.println("AAAA: ");
		Sqlite sql = getSqlite();
		System.out.println("AAAA: " + sql);
	}
}
