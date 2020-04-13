package Project_2_4800;
import java.awt.Desktop;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * This class implements the CRUD features that are necessary for a relational
 * database.
 * <p>
 * CREATE: This class implements methods which create a database and methods
 * which create tables. <br>
 * READ: This class implements methods which run SELECT queries. <br>
 * UPDATE: This class implements methods which ALTER user password and ALTERs
 * columns in a table. It also copies rows of data from a csv file into a table. <br>
 * DELETE: This class implements methods which delete records from a table. It
 * also implements a DROP TABLE method.
 * 
 * @author Rachel Friedman
 * @version 1.0
 */
public class Database {
	
	String[] credentials = new String[3];
	
//	public Database(String[] credentials) {
//		create database object with initial postgres string.
	// then use that string for createDatabaseAndUSer
//	}

	/**
	 * Prompts user for a database name, username and password. Creates database
	 * with name specified. Creates user with user and password as specified. Grants
	 * user all privileges for newly created database.
	 * 
	 * @return an array with 3 Strings: database name, username, password
	 */
	public String[] createDatabaseAndUser() {
		String[] credentials = new String[3];
		System.out.println("\nDATABASE SETUP");
		Scanner console2 = new Scanner(System.in);
		System.out.print("Select a username: ");
		credentials[0] = console2.next();
		System.out.print("Select a password: ");
		credentials[1] = console2.next();
		System.out.print("Select a name for your database: ");
		credentials[2] = console2.next();
		String username = credentials[0];
		String password = credentials[1];
		String dbName = credentials[2];
		try {
			Connection Conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "postgres");
			Statement Stmt = Conn.createStatement();
			try {

				Stmt.execute("DROP DATABASE IF EXISTS " + dbName + ";");
				Stmt.execute("CREATE DATABASE " + dbName + ";");
				Stmt.execute("CREATE USER " + username + " PASSWORD \'" + password + "\';");
				Stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + username + ";");
				Conn.close();
				// console2.close();
				System.out.println("Database " + dbName + " created for user " + username);
			}
			catch (SQLException e) {
				System.out.println("User already exists. Changing password to new password instead");
				Stmt.execute("ALTER USER " + username + " PASSWORD \'" + password + "\';");
			}
		}
		catch (SQLException e) {
			System.err.println("createDatabaseAndUser" + e.getClass().getName() + ": " + e.getMessage());
		}
		return credentials;
	}

	/**
	 * Connects to database db with specified username and password
	 * 
	 * @param db       the database to connect to
	 * @param username the username to connect with
	 * @param password the password to connect with
	 * @return the database connection
	 */
	public Connection connectToDatabase(String db, String username, String password) {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + db, username, password);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("\nConnected to database " + db + " successfully");
		return connection;
	}

	/**
	 * Drops the specified table.
	 * 
	 * @param connection the connection to the current database
	 * @param tableName  the table to be dropped
	 */
	public static void dropTable(Connection connection, String tableName) {
		try {
			Statement stmt = connection.createStatement();
			String sql = "DROP TABLE IF EXISTS " + tableName + ";";
			stmt.executeUpdate(sql);
			stmt.close();
			// System.out.println("Dropped table " + tableName);
		}
		catch (SQLException e) {
			System.err.println("Error dropping table " + e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Creates a table specified by tableName, using allFields from a csv file as
	 * columns
	 * 
	 * @param connection the connection to the current database
	 * @param tableName  the name of the table to be created
	 * @param allFields  the column names and data types
	 */
	public void convertToTable(Connection connection, String tableName, String allFields) {
		try {
			Statement stmt = connection.createStatement();
			String sql = "DROP TABLE IF EXISTS " + tableName + ";";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + allFields + "); ";
			stmt.executeUpdate(sql);
			stmt.close();
			// System.out.println("Created table: " + tableName);
		}
		catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Creates a table in this database with tableName as table and columns as
	 * specified. <br>
	 * This method also adds an ID column as the primary key. <br>
	 * This method also alters the column names.
	 * 
	 * @param connection the connection to the current database
	 * @param tableName  the name of the table to be created
	 * @param columns    the column names and data types
	 */
	public void createTable(Connection connection, String tableName, String columns) {
		try {
			Statement stmt = connection.createStatement();
			String sql = "DROP TABLE IF EXISTS " + tableName + ";";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS " + tableName + " AS(SELECT " + columns + " FROM covidData); ";
			stmt.executeUpdate(sql);
			sql = "ALTER TABLE  " + tableName + " ADD COLUMN ID serial PRIMARY KEY;";
			stmt.executeUpdate(sql);
			sql = "ALTER TABLE  " + tableName + " RENAME state to ST;";
			stmt.executeUpdate(sql);
			System.out.println("Created table: " + tableName);
			stmt.close();
		}
		catch (Exception e) {
			System.err.println("CREATE TABLE " + e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Adds record to specified table by copying data from specified file.
	 * 
	 * @param connection the database connection
	 * @param filename   the csv file from which to copy the data
	 * @param tableName  the name of the table to add the records to
	 */
	public void addRecords(Connection connection, String filename, String tableName) {
		String path = "data\\";
		try {
			String sql = "copy " + tableName + " FROM stdin DELIMITER ',' CSV header";
			BaseConnection con = (BaseConnection) connection;
			CopyManager mgr = new CopyManager(con);
			try {
				Reader in = new BufferedReader(new FileReader(new File(path + filename)));
				long rowsaffected = mgr.copyIn(sql, in);
				System.out.println("Records imported for " + tableName + ": " + rowsaffected);
			}
			catch (FileNotFoundException e) {
				System.err.println(e.getClass().getName() + ": " + "File " + filename + " not found");
			}
		}
		catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * Deletes rows from table tableName
	 * 
	 * @param connection the connection to the database
	 * @param tableName  the table from which to delete the rows
	 */
	public void deleteRecords(Connection connection, String tableName) {
		try {
			Statement stmt = connection.createStatement();
			String sql = "DELETE FROM " + tableName + " WHERE date < \'2020-03-15\'; ";
			stmt.executeUpdate(sql);
			stmt.close();
			System.out.println("Deleted all data prior to March 15");
		}
		catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Deletes this session's database and user. This method is typically used to
	 * perform cleanup when exiting a program.
	 * 
	 * @param conn   the connection to the database
	 * @param dbName the database to be deleted
	 * @param user   the user to be deleted
	 */
	public void deleteDatabaseAndUser(Connection conn, String dbName, String user) {
		try {
			conn.close();
			Connection Conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "postgres");
			Statement Stmt = Conn.createStatement();
			Stmt.execute("DROP DATABASE IF EXISTS " + dbName + ";");
			Stmt.execute("DROP USER " + user + ";");
			System.out.println("\nDeleting database and user for current session... Done.");
		}
		catch (SQLException e) {
			// System.err.println("dropDatabase " + e.getClass().getName() + ": " +
			// e.getMessage());
			System.out.println("Unable to delete current session user and database. Delete manually.");
		}
	}

	/**
	 * Performs a "select by state" query on table specified
	 * 
	 * @param connection the connection to the database
	 * @param tableName  the table to be queried
	 * @param state      the specific state to fetch data for
	 * @return the results of the query formatted in an html table
	 */
	public String selectByState(Connection connection, String tableName, String state) {
		String htmlResults = "";
		String pattern = "MMMM d";
		DateFormat df = new SimpleDateFormat(pattern);
		DecimalFormat decF = new DecimalFormat("#,###");

		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT state, date, positive, hospitalizedCumulative, death FROM " + tableName
					+ " WHERE state = \'" + state.toUpperCase() + "\' ORDER BY date desc;";
			System.out.println("Retrieving records from " + tableName + " for state=" + state.toUpperCase());
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String st = rs.getString("state");
				Date date = rs.getDate("date");
				String dateF = df.format(date);
				int pos = rs.getInt("positive");
				String poss = decF.format(pos);
				int hosp = rs.getInt("hospitalizedCumulative");
				String hosps = decF.format(hosp);
				if (hosp == 0)
					hosps = "NA";
				int death = rs.getInt("death");
				String deaths = decF.format(death);
				htmlResults = htmlResults + "\n\t\t\t\t<tr> <td> " + st + "</td> <td>" + date + "</td> <td> " + poss
						+ "</td> <td> " + hosps + "</td> <td> " + deaths + "</td> </tr>";
				System.out.println(st + " | " + hosps + " | " + poss + " | " + deaths);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			System.err.println("selectByState " + e.getClass().getName() + ": " + e.getMessage());
		}
		return htmlResults;
	}

	/**
	 * Performs a "select by date" query on table specified
	 * 
	 * @param connection the connection to the database
	 * @param tableName  the table to be queried
	 * @param date_user  the specific date to fetch data for
	 * @return the results of the query formatted in an html table
	 */
	public String selectByDate(Connection connection, String tableName, String date_user) {
		String htmlResults = "";
		String pattern = "MMMM d";
		DateFormat df = new SimpleDateFormat(pattern);
		DecimalFormat decF = new DecimalFormat("#,###");

		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT date, state, positive, hospitalizedCumulative, death FROM " + tableName
					+ " WHERE date = \'2020-" + date_user + "\' ORDER BY positive desc;";
			System.out.println("Retrieving records from " + tableName + " for date = 2020-" + date_user);
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String st = rs.getString("state");
				Date date = rs.getDate("date");
				String dateF = df.format(date);
				int pos = rs.getInt("positive");
				String poss = decF.format(pos);
				int hosp = rs.getInt("hospitalizedCumulative");
				String hosps = decF.format(hosp);
				if (hosp == 0)
					hosps = "NA";
				int death = rs.getInt("death");
				String deaths = decF.format(death);
				htmlResults = htmlResults + "\n<tr> <td> " + dateF + "</td> <td>" + st + "</td> <td> " + poss
						+ "</td> <td> " + hosps + "</td> <td> " + deaths + "</td> </tr>";
				System.out.println(st + " | " + hosps + " | " + poss + " | " + deaths);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			System.out.println("Oops. Invalid date format. Please try again.");
			// runQueries(connection, tableName);
			// System.exit(0);
			// System.err.println("selectByDate " + e.getClass().getName() + ": " +
			// e.getMessage());
		}

		return htmlResults;
	}

	/**
	 * Performs a "select by state and by date" query on table specified
	 * 
	 * @param connection the connection to the database
	 * @param tableName  the table to be queried
	 * @param state      the specific state to fetch data for
	 * @param date_user  the specific date to fetch data for
	 * @return the results of the query formatted in an html table
	 */
	public String selectByStateDate(Connection connection, String tableName, String state, String date_user) {
		String htmlResults = "";
		String pattern = "MMMM d";
		DateFormat df = new SimpleDateFormat(pattern);
		DecimalFormat decF = new DecimalFormat("#,###");

		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT date, state, positive, hospitalizedCumulative, death FROM " + tableName
					+ " WHERE date = \'2020-" + date_user + "\' AND state=  \'" + state.toUpperCase()
					+ "\' ORDER BY date asc;";
			System.out.println("Retrieving records from " + tableName + " for state = " + state.toUpperCase()
					+ " and date = 2020-" + date_user);
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String st = rs.getString("state");
				Date date = rs.getDate("date");
				String dateF = df.format(date);
				int pos = rs.getInt("positive");
				String poss = decF.format(pos);
				int hosp = rs.getInt("hospitalizedCumulative");
				String hosps = decF.format(hosp);
				if (hosp == 0)
					hosps = "NA";
				int death = rs.getInt("death");
				String deaths = decF.format(death);
				htmlResults = htmlResults + "\n<tr> <td> " + dateF + "</td> <td>" + st + "</td> <td> " + poss
						+ "</td> <td> " + hosps + "</td> <td> " + deaths + "</td> </tr>";
				System.out.println(st + " | " + hosps + " | " + poss + " | " + deaths);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			System.out.println("Oops. Invalid date format. Please try again.");
			// runQueries(connection, tableName);
			// System.err.println("selectByStateDate " + e.getClass().getName() + ": " +
			// e.getMessage());
		}
		return htmlResults;
	}

	/**
	 * Creates a HashMap of the full name of the abbreviation of each state.<br>
	 * This is used when checking to see if valid state was entered. <br>
	 * This is also used when spelling out full name of state in place of
	 * abbreviation.
	 * 
	 * @return a Hashmap of the abbreviations and full names of each of the 50
	 *         states.
	 */
	public HashMap<String, String> createListOfStates() {
		HashMap<String, String> statesList = new HashMap<String, String>();
		statesList.put("AL", "Alabama");
		statesList.put("AK", "Alaska");
		statesList.put("AZ", "Arizona");
		statesList.put("AR", "Arkansas");
		statesList.put("CA", "California");
		statesList.put("CO", "Colorado");
		statesList.put("CT", "Connecticut");
		statesList.put("DE", "Delaware");
		statesList.put("FL", "Florida");
		statesList.put("GA", "Georgia");
		statesList.put("HI", "Hawaii");
		statesList.put("ID", "Idaho");
		statesList.put("IL", "Illinois");
		statesList.put("IN", "Indiana");
		statesList.put("IA", "Iowa");
		statesList.put("KS", "Kansas");
		statesList.put("KY", "Kentucky");
		statesList.put("LA", "Louisiana");
		statesList.put("ME", "Maine");
		statesList.put("MD", "Maryland");
		statesList.put("MA", "Massachusetts");
		statesList.put("MI", "Michigan");
		statesList.put("MN", "Minnesota");
		statesList.put("MS", "Mississippi");
		statesList.put("MO", "Missouri");
		statesList.put("MT", "Montana");
		statesList.put("NE", "Nebraska");
		statesList.put("NV", "Nevada");
		statesList.put("NH", "New_Hampshire");
		statesList.put("NJ", "New_Jersey");
		statesList.put("NM", "New_Mexico");
		statesList.put("NY", "New_York");
		statesList.put("NC", "North_Carolina");
		statesList.put("ND", "North_Dakota");
		statesList.put("OH", "Ohio");
		statesList.put("OK", "Oklahoma");
		statesList.put("OR", "Oregon");
		statesList.put("PA", "Pennsylvania");
		statesList.put("RI", "Rhode_Island");
		statesList.put("SC", "South_Carolina");
		statesList.put("SD", "South_Dakota");
		statesList.put("TN", "Tennessee");
		statesList.put("TX", "Texas");
		statesList.put("UT", "Utah");
		statesList.put("VT", "Vermont");
		statesList.put("VA", "Virginia");
		statesList.put("WA", "Washington");
		statesList.put("WV", "West_Virginia");
		statesList.put("WI", "Wisconsin");
		statesList.put("WY", "Wyoming");
		return statesList;
	}
}