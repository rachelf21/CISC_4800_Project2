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
 * This program downloads covid19 tracking information for each of the fifty
 * states in the United States, from
 * <a href="https://covidtracking.com/api" target="_blank">
 * covidtracking.com</a>. It then creates and accesses a PostgreSQL database,
 * based on the following user specifications: database name, username and
 * password. The user is granted full access to the database. The following
 * tables are then created: states, positive, hospitalizations, and deaths. The
 * user is then presented with options to query the database and to display data
 * by state, date, or both state and date. The results are then output as an
 * HTML document, formatted with DataTable in Bootstrap 4. Upon completion, the
 * program deletes the session's database and user. The result of the queries
 * remain saved as html files in an output folder.
 * 
 * @author Rachel Friedman
 * @version 1.0
 */
public class Database {

	static int exit = 1;

	/**
	 * launching point of program
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		String tableName = "covid_data";
		System.out.println("--COVID19 DATABASE--");
		String allFields = retrieveDataFromWebsite();
		String[] credentials = createDatabaseAndUser();
		Connection connection = connectToDatabase(credentials[2], credentials[0], credentials[1]);
		convertToTable(connection, tableName, allFields);
		addRecords(connection, "data.csv", tableName);
		deleteRecords(connection, tableName);
		convertToTable(connection, "states", "id integer primary key, ST text, state text");
		addRecords(connection, "states.csv", "states");
		createTable(connection, "positive", "date, state, positive");
		createTable(connection, "hospitalizations", "date, state, hospitalizedcumulative");
		createTable(connection, "death", "date, state, death");
		runQueries(connection, tableName);
		if (exit == 0) {
			deleteDatabaseAndUser(connection, credentials[2], credentials[0]);
			System.gc();
			System.out.println("Exiting Program... Goodbye.");
			System.exit(0);
		}
	}// main

	/**
	 * Retrieves latest data from covidtracking.com. <br>
	 * Saves all data to data/data.csv in local directory.
	 * 
	 * @return a String with all the fields from the header row with data types,
	 *         separated by a comma. <br>
	 *         This string can be used in a CREATE TABLE SQL statement for creating
	 *         the columns in the table.
	 */
	public static String retrieveDataFromWebsite() {
		DecimalFormat decimalFormat = new DecimalFormat("###.###");
		ArrayList<String> header = new ArrayList<String>();
		String allFields = "";
		StringBuffer stringBuffer = new StringBuffer();
		byte[] data = null;
		File file = new File("data\\data.csv");

		try {
			URL url = new URL("https://covidtracking.com/api/v1/states/daily.csv");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			System.out.println("Retrieving data from covidttracking.com...");
			InputStream inputStream = connection.getInputStream();
			long read_start = System.nanoTime();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			int i;
			while ((i = reader.read()) != -1) {
				char c = (char) i;
				if (c == '\n') {
					stringBuffer.append("\n");
				}
				else {
					stringBuffer.append(String.valueOf(c));
				}
			}
			reader.close();
			long read_end = System.nanoTime();
			// System.out.println("Finished reading response in "+
			// decimalFormat.format((read_end - read_start) / Math.pow(10, 6)) + "
			// milliseconds");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			data = stringBuffer.toString().getBytes();
		}
		try (FileOutputStream fop = new FileOutputStream(file)) {

			if (!file.exists()) {
				file.createNewFile();
			}

			// System.out.println("Initializing write.....");
			long now = System.nanoTime();
			fop.write(data);
			fop.flush();
			fop.close();
			// System.out.println("Finished writing CSV in " +
			// decimalFormat.format((System.nanoTime() - now) / Math.pow(10, 6)) + "
			// milliseconds!");
			System.out.println("Data retrieved successfully.");

			header = getHeaderRow("data\\data.csv");
			for (int i = 0; i < header.size(); i++) {
				if (i == 0 || i == 13)
					header.set(i, header.get(i) + " date");
				else if (i == 1 || i == 12)
					header.set(i, header.get(i) + " text");
				else
					header.set(i, header.get(i) + " integer");
				if (i < header.size() - 1)
					allFields = allFields + header.get(i) + ", ";
				else
					allFields = allFields + header.get(i);
				// System.out.println(header.get(i));
			}
			// allFields = "ID serial primary key not null, " + allFields;
			// System.out.println(allFields);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return allFields;
	}

	/**
	 * Retrieves the header row from the csv file
	 * 
	 * @param filename the csv file with the header row
	 * @return all fields in header row
	 */
	private static ArrayList<String> getHeaderRow(String filename) {
		ArrayList<String> header = new ArrayList<String>();
		try {
			Scanner data = new Scanner(new File(filename));
			String[] line = data.nextLine().split(",");
			for (int i = 0; i < line.length; i++) {
				header.add(line[i]);
				// System.out.print(line[i] + ", ");
			}
			data.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("Missing file " + e.getMessage());
		}
		catch (NumberFormatException e) {
			System.out.println("invalid number");
		}
		catch (Exception e) {
			System.out.println("An error has occured " + e.getMessage());
		}
		return header;
	}

	/**
	 * Prompts user for a database name, username and password. Creates database
	 * with name specified. Creates user with user and password as specified. Grants
	 * user all privileges for newly created database.
	 * 
	 * @return an array with 3 Strings: database name, username, password
	 */
	public static String[] createDatabaseAndUser() {
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
	public static Connection connectToDatabase(String db, String username, String password) {
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
	public static void convertToTable(Connection connection, String tableName, String allFields) {
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
	public static void createTable(Connection connection, String tableName, String columns) {
		try {
			Statement stmt = connection.createStatement();
			String sql = "DROP TABLE IF EXISTS " + tableName + ";";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS " + tableName + " AS(SELECT " + columns + " FROM covid_data); ";
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
	public static void addRecords(Connection connection, String filename, String tableName) {
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

	public static void deleteRecords(Connection connection, String tableName) {
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
	public static void deleteDatabaseAndUser(Connection conn, String dbName, String user) {
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
	 * Prompts the user with a menu of different queries that can be performed on
	 * the database. <br>
	 * Calls upon the relevant method after user selects one of the options
	 * presented.
	 * 
	 * @param connection the connection to the database
	 * @param tableName  the table to be queried
	 */
	public static void runQueries(Connection connection, String tableName) {
		Scanner console = new Scanner(System.in);
		HashMap<String, String> hm = createListOfStates();

		try {
			int selection = 1;
			do {
				System.out.println("\nHow would you like to query the data?");
				System.out.println("1| View by state");
				System.out.println("2| View by date");
				System.out.println("3| View by state and date");
				System.out.print("Enter 1, 2, 3 or 0 to exit: ");

				selection = console.nextInt();
				// LocalDate currentDate = java.time.LocalDate.now();
				String state = "";
				String date = "";
				String htmlResults = "";
				switch (selection) {
				case 0:
					exit = 0;
					break;
				case 1:
					System.out.print("\nEnter state: ");
					state = console.next().toUpperCase();
					while (!hm.containsKey(state)) {
						System.out.print("Invalid state");
						System.out.print("\nEnter state: ");
						state = console.next().toUpperCase();
					}
					htmlResults = selectByState(connection, tableName, state);
					createHTML(htmlResults, hm.get(state), 1);
					break;
				case 2:
					System.out.print("\nEnter date in this format mm-dd:  ");
					date = console.next();
					htmlResults = selectByDate(connection, tableName, date);
					createHTML(htmlResults, date, 2);
					break;

				case 3:
					System.out.print("\nEnter state: ");
					state = console.next().toUpperCase();
					while (!hm.containsKey(state)) {
						System.out.print("Invalid state");
						System.out.print("\nEnter state: ");
						state = console.next().toUpperCase();
					}
					System.out.print("Enter date in this format mm-dd: ");
					date = console.next();
					try {
						htmlResults = selectByStateDate(connection, tableName, state, date);
						createHTML(htmlResults, hm.get(state) + " " + date, 3);
					}
					catch (Exception e) {
						System.out.println("Invalid date entry");
						System.err.println(e.getClass().getName() + ": " + e.getMessage());
						System.exit(0);
					}
					break;
				default:
					System.out.println("\nInvalid selection. Enter 1, 2, 3 or 0 to exit.");
				}
			} while (selection != 0 || selection > 3);
		}
		catch (InputMismatchException e) {
			System.out.println("\nInvalid entry. Enter 1, 2, 3 or 0 to exit.");
			runQueries(connection, tableName);
		}
		console.close();
	}

	/**
	 * Performs a "select by state" query on table specified
	 * 
	 * @param connection the connection to the database
	 * @param tableName  the table to be queried
	 * @param state      the specific state to fetch data for
	 * @return the results of the query formatted in an html table
	 */
	public static String selectByState(Connection connection, String tableName, String state) {
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
	public static String selectByDate(Connection connection, String tableName, String date_user) {
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
			runQueries(connection, tableName);
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

	public static String selectByStateDate(Connection connection, String tableName, String state, String date_user) {
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
			runQueries(connection, tableName);
			// System.err.println("selectByStateDate " + e.getClass().getName() + ": " +
			// e.getMessage());
		}
		return htmlResults;
	}

	/**
	 * Creates the HTML page of the query results, using DataTable bootstrap
	 * 
	 * @param htmlResults the results of the query formatted in in html table
	 * @param selection   the date and/or state that was used as a condition for the
	 *                    query
	 * @param option      the number of the corresponding query that user selected
	 *                    at the prompt
	 * 
	 */
	public static void createHTML(String htmlResults, String selection, int option) {
		String colHeader = "";
		switch (option) {
		case 1:
			colHeader = "            <th>State</th>\r\n            <th>Date</th>\r\n";
			break;
		case 2:
			colHeader = "            <th>Date</th>\r\n            <th>State</th>\r\n";
			break;
		case 3:
			colHeader = "            <th>Date</th>\r\n            <th>State</th>\r\n";
			break;
		default:
			colHeader = "            <th>State</th>\r\n            <th>Date</th>\r\n";
			break;
		}
		String htmlFile = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "  <head>\r\n"
				+ "    <meta charset=\"UTF-8\" />\r\n"
				+ "    <!--meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" /-->\r\n"
				+ "    <title>" + selection + " Results</title>\r\n"
				+ "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css\" />\r\n"
				+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.19/css/dataTables.bootstrap4.min.css\" />\r\n"
				+ "  </head>\r\n" + "  <body>\r\n" + "    <div class=\"container mb-5 mt-5\">\r\n"
				+ "\r\n <h1 align=\"center\"> Results from Query for " + selection + "</H1><HR>"
				+ "      <table class=\"table table-striped table-bordered text-center\" style=\"width: 100%;\" id=\"mydatatable\">\r\n"
				+ "        <thead>\r\n" + "          <tr>\r\n" + colHeader + "            <th>Positive Cases</th>\r\n"
				+ "            <th>Hospitalizations</th>\r\n" + "            <th>Deaths</th>\r\n"
				+ "          </tr>\r\n" + "        </thead>\r\n" + "        <tbody>" + htmlResults
				+ "        </tbody>\r\n" + "      </table>\r\n" + "    </div>\r\n" + "\r\n"
				+ "    <script src=\"https://code.jquery.com/jquery-3.3.1.min.js\"></script>\r\n"
				+ "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js\"></script>\r\n"
				+ "    <script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js\"></script>\r\n"
				+ "\r\n"
				+ "    <script src=\"https://cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js\"></script>\r\n"
				+ "    <script src=\"https://cdn.datatables.net/1.10.19/js/dataTables.bootstrap4.min.js\"></script>\r\n"
				+ "\r\n" + "    <script>\r\n" + "      $(\"#mydatatable\").DataTable({\r\n"
				+ "        pageLength: 10,\r\n" + "        filter: true,\r\n" + "        deferRender: true,\r\n"
				+ "        scrollY: 600,\r\n" + "        scrollCollapse: true,\r\n" + "        scroller: true,\r\n"
				+ "        ordering: true,\r\n" + "        select: true,\r\n" + "      });\r\n" + "    </script>\r\n"
				+ "  </body>\r\n" + "</html>";

		try {
			String path = "output/";
			File file2 = new File(path + selection + "_data.html");
			FileOutputStream fop = new FileOutputStream(file2);
			if (!file2.exists()) {
				file2.createNewFile();
			}

			byte[] data = htmlFile.getBytes();
			fop.write(data);
			fop.flush();
			fop.close();
			System.out.println("Done!");
			Desktop desktop = Desktop.getDesktop();
			java.net.URI url;

			try {
				url = new java.net.URI(
						"C:/Users/Rachel/git/CISC4800_Project2/Project_2_4800/output/" + selection + "_data.html");
				desktop.browse(url);
			}
			catch (URISyntaxException e) {
				System.out.println("Navigate to folder to open html file.");
				e.printStackTrace();
			}

		}
		catch (FileNotFoundException e) {
			System.out.println("file not found exception can't write date to file");
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
	private static HashMap<String, String> createListOfStates() {
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