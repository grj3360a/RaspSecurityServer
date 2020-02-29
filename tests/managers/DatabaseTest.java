package managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.security.managers.DatabaseManager;
import me.security.managers.DatabaseManager.Log;

public class DatabaseTest {

	private DatabaseManager db;

	@Before
	public void setUp() throws Exception {
		this.db = DatabaseManager.generateFromFile();
	}

	@After
	public void tearDown() throws Exception {
		this.db.close();
		this.db = null;
	}

	@Test
	public void connectionValid() throws SQLException {
		Connection c = getConnection(this.db);
		assertNotNull(c);
		assertTrue(c.isValid(5));
	}

	@Test
	public void testLog() throws SQLException {
		this.db.log("JUnit Test");
		Statement stmt = getConnection(this.db).createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM `logs` ORDER BY `id_log` DESC LIMIT 1");
		while (rs.next()) {
			assertEquals("JUnit Test", rs.getString("log_info"));
			assertFalse(rs.getBoolean("relatedToSensor"));
			getConnection(this.db).createStatement()
					.execute("DELETE FROM `logs` WHERE `id_log` = " + rs.getInt("id_log"));
		}
		assertTrue(rs.first());
	}

	@Test
	public void testAlert() throws SQLException {
		this.db.alert("sensorName", "alertMessage");
		Statement stmt = getConnection(this.db).createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM `logs` ORDER BY `id_log` DESC LIMIT 1");
		while (rs.next()) {
			assertEquals("Detection sensorName (alertMessage)", rs.getString("log_info"));
			assertTrue(rs.getBoolean("relatedToSensor"));
			getConnection(this.db).createStatement()
					.execute("DELETE FROM `logs` WHERE `id_log` = " + rs.getInt("id_log"));
		}
		assertTrue(rs.first());
	}

	@Test
	public void testGetLast10Logs() throws SQLException {
		this.db.log("JUnit Test 1");
		this.db.log("JUnit Test 2");
		this.db.log("JUnit Test 3");
		this.db.log("JUnit Test 4");
		this.db.log("JUnit Test 5");
		this.db.log("JUnit Test 6");
		this.db.log("JUnit Test 7");
		this.db.log("JUnit Test 8");
		this.db.log("JUnit Test 9");
		this.db.log("JUnit Test 10");

		List<Log> logs = this.db.getLast10Logs();
		assertNotNull(logs);
		assertFalse(logs.isEmpty());

		int i = 10;
		for (Log l : logs) {
			assertNotNull(l);
			assertEquals("JUnit Test " + i, l.info);
			assertFalse(l.relatedToSensor);

			getConnection(this.db).createStatement().execute("DELETE FROM `logs` WHERE `id_log` = " + l.id);
			i--;
		}
	}

	/**
	 * We don't want Connection field to be available, but we must test things with
	 * a sql Connection object So we bypass runtime protection during tests with
	 * some reflection on DatabaseManager.
	 */
	public static Connection getConnection(DatabaseManager db) {
		for (Field f : DatabaseManager.class.getDeclaredFields()) {
			if (f.getType() == Connection.class) {
				f.setAccessible(true);
				try {
					return (Connection) f.get(db);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
