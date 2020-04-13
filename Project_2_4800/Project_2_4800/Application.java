package Project_2_4800;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.*;
import java.sql.Connection;

/**
 * This class launches the program. <br>
 * This program downloads covid19 tracking information for each of the fifty
 * states and six territories in the United States, from
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
 * @version 1.1 This version tests the constructor which creates a database object with a specified connection string
 */
public class Application {
	static int exit = 1;
/**
 * Launches the program
 * @param args not used
 */
	public static void main(String[] args) {
		String[] initialConnection = { "jdbc:postgresql://localhost:5432/", "postgres", "postgres" };
		System.out.println("--COVID19 DATABASE--");
		String tableName = "covidData";
		WebScraper ws = new WebScraper();
		String address = "https://covidtracking.com/api/v1/states/daily.csv";
		String filename = "data\\data.csv";
		byte[] content = ws.retrieveDataFromWebsite(address);
		String allFields = ws.saveToFile(filename, content);
		HTMLWriter htmlWriter = new HTMLWriter();
		
		Database db2 = new Database("postgres",initialConnection[1],initialConnection[2] );
		String[] credentials = promptForCredentials();
		db2.createDatabaseAndUser(initialConnection, credentials);
		Connection connection = db2.connectToDatabase(credentials[0],credentials[1],credentials[2]);
		//Connection connection = db2.getConnection();
		db2.convertToTable(connection, tableName, allFields);
		db2.addRecords(connection, "data\\data.csv", tableName);
		db2.deleteOldRecords(connection, tableName,"03-15");
		db2.convertToTable(connection, "states", "id integer primary key, ST text, state text");
		db2.addRecords(connection, "data\\states.csv", "states");
		db2.createTable(connection, "positive", "date, state, positive");
		db2.createTable(connection, "hospitalizations", "date, state, hospitalizedcumulative");
		db2.createTable(connection, "death", "date, state, death");
		viewData(connection, db2, htmlWriter, tableName); // consider changing this to include in this file

		if (exit == 0) {
			db2.deleteDatabaseAndUser(connection, credentials[2], credentials[0]);
			System.gc();
			System.out.println("Exiting Program... Goodbye.");
			System.exit(0);
		}
		
//		Database db = new Database();
//		String[] credentials = promptForCredentials();
//		db.createDatabaseAndUser(initialConnection, credentials);
//		Connection connection = db.connectToDatabase(credentials[2], credentials[0], credentials[1]);
//		db.convertToTable(connection, tableName, allFields);
//		db.addRecords(connection, "data\\data.csv", tableName);
//		db.deleteOldRecords(connection, tableName,"03-15");
//		db.convertToTable(connection, "states", "id integer primary key, ST text, state text");
//		db.addRecords(connection, "data\\states.csv", "states");
//		db.createTable(connection, "positive", "date, state, positive");
//		db.createTable(connection, "hospitalizations", "date, state, hospitalizedcumulative");
//		db.createTable(connection, "death", "date, state, death");
//		viewData(connection, db, htmlWriter, tableName); // consider changing this to include in this file
//
//		if (exit == 0) {
//			db.deleteDatabaseAndUser(connection, credentials[2], credentials[0]);
//			System.gc();
//			System.out.println("Exiting Program... Goodbye.");
//			System.exit(0);
//		}
	}

	/**
	 * Prompts the user to select a username, password and name for database
	 * 
	 * @return the username, password, and name for database
	 */
	public static String[] promptForCredentials() {
		String[] credentials = new String[3];
		System.out.println("\nDATABASE SETUP");
		Scanner console = new Scanner(System.in);
		System.out.print("Select a username: ");
		credentials[0] = console.next();
		System.out.print("Select a password: ");
		credentials[1] = console.next();
		System.out.print("Select a name for your database: ");
		credentials[2] = console.next();
		return credentials;
	}

	/**
	 * Prompts the user to select from a menu of different queries that can be performed on
	 * the database. <br>
	 * Calls upon the relevant method after user selects one of the options
	 * presented.
	 * 
	 * @param connection the connection to the database
	 * @param db         the database that contains the tables being queried
	 * @param html       the htmlWriter used to generate the html file
	 * @param tableName  the table to be queried
	 */
	public static void viewData(Connection connection, Database db, HTMLWriter html, String tableName) {
		Scanner console = new Scanner(System.in);
		HashMap<String, String> hm = db.createListOfStates();

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
					htmlResults = db.selectByState(connection, tableName, state);
					html.createHTML(htmlResults, hm.get(state), 1);
					break;
				case 2:
					System.out.print("\nEnter a date in this format mm-dd:  ");
					date = console.next();
					while (!Pattern.matches("[01][0-9]-[0-3][0-9]", date)) {
						System.out.print("Enter a valid date in this format mm-dd:  ");
						date = console.next();
					}
					htmlResults = db.selectByDate(connection, tableName, date);
					html.createHTML(htmlResults, date, 2);
					break;

				case 3:
					System.out.print("\nEnter state: ");
					state = console.next().toUpperCase();
					while (!hm.containsKey(state)) {
						System.out.print("Invalid state");
						System.out.print("\nEnter state: ");
						state = console.next().toUpperCase();
					}
					System.out.print("Enter a date in this format mm-dd: ");
					date = console.next();
					while (!Pattern.matches("[01][0-9]-[0-3][0-9]", date)) {
						System.out.print("Enter a valid date in this format mm-dd:  ");
						date = console.next();
					}
					try {
						htmlResults = db.selectByStateDate(connection, tableName, state, date);
						html.createHTML(htmlResults, hm.get(state) + "_" + date, 3);
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
			viewData(connection, db, html, tableName);
		}
		console.close();
	}

}
