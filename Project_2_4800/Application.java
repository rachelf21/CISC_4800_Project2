//import java.util.Scanner;
//import java.sql.Connection;
//
//public class Application {
//	static Scanner console;
//	static String cont;
//	int option = 0;
//
//	public static void main(String[] args) {
//		console = new Scanner(System.in);
//
////CONNECT TO DATABASE		
//		PostgreSQLJDBC database = new PostgreSQLJDBC();
//		Connection connection = database.connectToDatabase("covid19_nyc", "postgres", "postgres");
//
////CREATE TABLES		
//		String[] table = { "data_age", "data_borough", "data_sex" };
//		String[] category = { "age", "borough", "sex" };
//		int val = -1;
//		for (int i = 0; i < 3; i++) {
//			database.dropTable(connection, table[i]);
//			database.createTable(connection, table[i], category[i]);
//			database.importData(connection, table[i], category[i] + "_nyc");
//		}
//
////VIEW ALL RECORDS
//		System.out.println("\nVIEW ALL RECORDS");
//		System.out.println("To view all records, type a for age, s for sex, b for borough, or x to exit");
//		String letter = console.next();
//		while (!letter.equalsIgnoreCase("x")) {
//			if (letter.equalsIgnoreCase("a"))
//				val = 0;
//			else if (letter.equalsIgnoreCase("b"))
//				val = 1;
//			else if (letter.equalsIgnoreCase("s"))
//				val = 2;
//			else {
//				System.out.println("Invalid entry");
//				letter = console.next();
//			}
//			database.selectAll(connection, table[val], category[val]);
//			letter = console.next();
//		}
//		
////VIEW SPECIFIC DATA
//		System.out.println("To view specific data for specific table, enter Y to continue");
//		cont = console.next();		
//		
//		while (cont.equalsIgnoreCase("Y")) {
//				//choose which table to filter data by
//				//choose which field to display
//				//choose a date
//		}
//			database.selectAll(connection, table[val], category[val]);
//			letter = console.next();
//				
//		
//		
//		
//		System.out.println("Exiting Program");
//		System.exit(0);
//	}// main
//
//	public static int printMenu() {
//		int option = 0;
//		while (option == 0) {
//			try {
//				System.out.println("\nChoose from the following options below: ");
//				System.out.println("1: Create Tables ");
//				System.out.println("2: View all records ");
//				System.out.println("3: Select based on criteria ");
//				System.out.println("4: Update records ");
//				System.out.println("5: Delete records ");
//				option = console.nextInt();
//			}
//			catch (Exception e) {
//				// System.err.println(e.getClass().getName() + ": " + e.getMessage());
//				System.out.println("Invalid input. Must enter a number from 1 to 5.");
//			}
//		}
//		return option;
//	}
//
//	public static void executeCommand(PostgreSQLJDBC database, Connection connection, int option) {
//		switch (option) {
//		case 1:
//			System.out.println("You selected option " + option + " CREATE TABLES:");
//			String[] table = { "data_age", "data_sex", "data_borough" };
//			String[] category = { "age", "sex", "borough" };
//			for (int i = 0; i < 3; i++) {
//				database.dropTable(connection, table[i]);
//				database.createTable(connection, table[i], category[i]);
//				database.importData(connection, table[i], category[i] + "_nyc");
//			}
//			option = printMenu();
//			break;
//		case 2:
//			System.out.println("You selected option " + option + " VIEW ALL RECORDS: ");
//			System.out.println("Enter a for age, s for sex, and b for borough: ");
//			String letter = console.next();
//			if (letter.equalsIgnoreCase("a")) {
//				database.selectAll(connection, "data_age", "age");
//			}
//			else if (letter.equalsIgnoreCase("s")) {
//				database.selectAll(connection, "data_sex", "sex");
//			}
//			else if (letter.equalsIgnoreCase("b")) {
//				database.selectAll(connection, "data_borough", "borough");
//			}
//			else {
//				System.out.println("Invalid entry");
//				option = 0;
//			}
//			option = printMenu();
//			break;
//		case 3:
//			System.out.println("You selected option " + option);
//			break;
//		case 4:
//			System.out.println("You selected option " + option);
//			break;
//		case 5:
//			System.out.println("You selected option " + option);
//			break;
//		default:
//			System.out.println("You selected an invalid number.");
//			option = printMenu();
//		}
//		// System.exit(1);
//
//	}
//
//}// class
//
