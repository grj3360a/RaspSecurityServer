package me.security.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * @author Geraldes Jocelyn
 * @since 24/11/2019
 */
public class DatabaseManager implements AutoCloseable {

	/**
	 * Generate a DatabaseManager object from the 'database.password' file<br>
	 * The file <b>database.password</b> must be in the same directory composed of 4
	 * lines in the following order :<br>
	 * <blockquote> domain.com<br>
	 * database<br>
	 * username<br>
	 * password </blockquote>
	 * 
	 * @return The created database manager
	 * @throws IOException           If 'database.password' file can't be readed
	 * @throws IllegalStateException If the file doesn't respect the format
	 */
	public static DatabaseManager generateFromFile() throws IOException {
		File dbPassword = new File("database.password");
		System.out.println("Parsing " + dbPassword.getCanonicalPath() + " file");

		if (!dbPassword.exists() || !dbPassword.canRead()) {
			throw new IllegalStateException("Database password file doesn't exist, unable to launch.");
		}

		if (Files.readAllLines(dbPassword.toPath()).size() != 4) {
			throw new IllegalStateException(
					"Database password file doesn't respect defined format, impossible to launch.");
		}

		List<String> dbInfo = Files.readAllLines(dbPassword.toPath());
		System.out.println("Database file readed successfuly, creating DatabaseManager");
		return new DatabaseManager(dbInfo.get(0), dbInfo.get(1), dbInfo.get(2), dbInfo.get(3));
	}

	private Connection connection;

	/**
	 * Create a DatabaseManager to handle db connection
	 * 
	 * @param domain   The domain or ip adress to connect to
	 * @param db       The database name
	 * @param user     The username
	 * @param password The corresponding password
	 * @throws IllegalArgumentException If any argument is null
	 */
	public DatabaseManager(String domain, String db, String user, String password) throws IllegalArgumentException {
		if (domain == null)
			throw new IllegalArgumentException("Domain can't be null");
		if (db == null)
			throw new IllegalArgumentException("Database can't be null");
		if (user == null)
			throw new IllegalArgumentException("Username can't be null");
		if (password == null)
			throw new IllegalArgumentException("Password can't be null");

		initializeConnection(domain, db, user, password);
	}

	/**
	 * Define a connection in this DatabaseManager<br>
	 * <b>Note:</b><br>
	 * This method is protected and separated from the constructor as it is
	 * Overrided by DummyDatabaseManager
	 * 
	 * @param domain   The domain or ip address to connect to
	 * @param db       The database name
	 * @param user     The username
	 * @param password The corresponding password
	 * @throws SQLException If the connection can't be initiated
	 */
	protected void initializeConnection(String domain, String db, String user, String password) {
		System.out.println("Establishing database connection (" + domain + ")...");
		DriverManager.setLoginTimeout(3);
		try {
			this.connection = DriverManager
					.getConnection("jdbc:mysql://" + domain + ":3306/" + db + "?autoReconnect=true", user, password);
		} catch (SQLException e) {
			e.printStackTrace();
			// We can still run security without database
			// autoReconnect option will do the rest for us
		}
	}

	/**
	 * Log a message and if it is related to a sensor
	 * 
	 * @param relatedToSensor If it is related to a sensor
	 * @param info            The related information
	 * @throws IllegalArgumentException The text information must not be null or empty
	 */
	public void rawLog(boolean relatedToSensor, String info) throws IllegalArgumentException {
		if (info == null)
			throw new IllegalArgumentException("Information must not be null");
		if (info.length() == 0)
			throw new IllegalArgumentException("Information must not be empty");
		System.out.println(info);

		try {
			if (!isAlive())
				return;

			PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO `logs`(`relatedToSensor`,`log_info`) VALUES (?,?)");
			stmt.setBoolean(1, relatedToSensor);
			stmt.setString(2, info);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			// Ignore the error as error on logging will not be damageable for our code
			// Security system need to continue running even if we have errors
		}
	}

	/**
	 * @return A list of the 10 last logs in an List of Log object
	 */
	public List<Log> getLast10Logs() {
		try {
			if (!isAlive())
				throw new IllegalStateException("Database connection seems dead.");

			List<Log> logs = new ArrayList<DatabaseManager.Log>();
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM `logs` ORDER BY `id_log` DESC LIMIT 10");

			while (rs.next()) {
				logs.add(new Log(rs.getInt(1), rs.getTimestamp(2), rs.getBoolean(3), rs.getString(4)));
			}

			return logs;
		} catch (SQLException e) {
			e.printStackTrace();
			return Arrays.asList(
					new Log(0, new Timestamp(System.currentTimeMillis()), false, "Error on DB connection, unable to retrieve logs.")
					);
		} catch (Error e) {
			e.printStackTrace();
			return Arrays.asList(new Log(0, new Timestamp(System.currentTimeMillis()), false, "Error " + e.getMessage()));
		}
	}

	private boolean isAlive() {
		try {
			return this.connection != null && !this.connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public void close() {
		try {
			System.out.println("Closing connection...");
			if (!this.isAlive())
				this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.connection = null;
	}

	/**
	 * Only for serialized reading by RestAPIManager
	 * @see RestAPIManager
	 */
	public static class Log {
		@Expose public final int id;
		@Expose public final long time;
		@Expose public final boolean relatedToSensor;
		@Expose public final String info;

		public Log(int id, Timestamp time, boolean relatedToSensor, String info) {
			this.id = id;
			this.time = time.getTime();
			this.relatedToSensor = relatedToSensor;
			this.info = info;
		}
	}

}
