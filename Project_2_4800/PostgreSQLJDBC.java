//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.sql.ResultSet;
//
//import org.postgresql.copy.CopyManager;
//import org.postgresql.core.BaseConnection;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.Reader;
//import java.io.*;
//
//public class PostgreSQLJDBC {
//
////*************CONNECT TO DATABASE**************************//		
//	public Connection connectToDatabase(String db, String username, String password) {
//		Connection c = null;
//		try {
//			Class.forName("org.postgresql.Driver");
//			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + db, username, password);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			System.err.println(e.getClass().getName() + ": " + e.getMessage());
//			System.exit(0);
//		}
//		System.out.println("Opened database successfully");
//		return c;
//	}
//
////*************DROP TABLE**************************//	
//	public void dropTable(Connection c, String table) {
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "DROP TABLE IF EXISTS " + table + ";";
//			stmt.executeUpdate(sql);
//			stmt.close();
//			// System.out.println("Dropped table " + table);
//		}
//		catch (Exception e) {
//			System.err.println(e.getClass().getName() + ": " + e.getMessage());
//			System.exit(0);
//		}
//	}
//
////*************CREATE TABLE**************************//	
//	public void createTable(Connection c, String table, String field) {
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "CREATE TABLE " + table + "(ID serial primary key not null, date date not null, " + field
//					+ " integer references " + field
//					+ "(id),cases_total integer, cases_new integer, hosp_total integer,hosp_new integer, death_total integer, death_new integer); ";
//			stmt.executeUpdate(sql);
//			stmt.close();
//			System.out.println("Created table " + table);
//		}
//		catch (Exception e) {
//			System.err.println(e.getClass().getName() + ": " + e.getMessage());
//			System.exit(0);
//		}
//	}
//
//	// *************IMPORT DATA**************************//
//	public void importData(Connection c, String table, String filename) {
//		String path = "C:\\Users\\Rachel\\Documents\\Corona Virus\\nyc data\\";
//		try {
//			String file = path + filename + ".csv";
//			String sql = "copy " + table + " FROM stdin DELIMITER ',' CSV header";
//			BaseConnection connection = (BaseConnection) c;
//			CopyManager mgr = new CopyManager(connection);
//
//			try {
//				Reader in = new BufferedReader(new FileReader(new File(file)));
//				long rowsaffected = mgr.copyIn(sql, in);
//
//				System.out.println("Records imported: " + rowsaffected);
//			}
//			catch (FileNotFoundException e) {
//				System.err.println(e.getClass().getName() + ": " + "File not found");
//			}
//		} // first try
//		catch (Exception e) {
//			System.err.println(e.getClass().getName() + ": " + e.getMessage());
//		}
//	}
//
////*************SELECT ALL**************************//	
//	public void selectAll(Connection c, String table, String field) {
//		
//		
//		String pattern = "MMMM d";
//		DateFormat df = new SimpleDateFormat(pattern);
//		
//		
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "SELECT * FROM " + table + " ORDER BY date, " + field + " asc;";
//			ResultSet rs = stmt.executeQuery(sql);
//			System.out.println("VIEWING ALL RECORDS IN " + table);
//			System.out.println("Date | " + field + " | Cases_Total | Cases_New | Hosp_Total | Hosp_New | Death_Total | Death_New");
//			
//			
//			while (rs.next()) {
//				int id = rs.getInt("id");
//				Date date = rs.getDate("date");
//				String dateF = df.format(date);
//				int category = rs.getInt(field);
//				int cases_total = rs.getInt("cases_total");
//				int cases_new = rs.getInt("cases_new");
//				int hosp_total = rs.getInt("hosp_total");
//				int hosp_new = rs.getInt("hosp_new");
//				int death_total = rs.getInt("death_total");
//				int death_new = rs.getInt("death_new");
//
////				if (sex == 1) {
////					System.out.println();
////					System.out.println(date);
////					System.out.println("F Total Cases: " + cases_total + "  New Cases: " + cases_new);
////				}
////				else if (sex == 2) {
////					System.out.println("M Total Cases: " + cases_total + "  New Cases: " + cases_new);
////				}
////				else if (sex == 3) {
////					System.out.println("U Total Cases: " + cases_total + "  New Cases: " + cases_new);
////				}
////			}
//				System.out.println(dateF + " | " + category + " | " + cases_total + " | " + cases_new + " | " + hosp_total + " | "
//						+ hosp_new + " | " + death_total + " | " + death_new);
//			}		
//			rs.close();
//			stmt.close();
//		}
//		catch (Exception e) {
//			System.err.println("in selectAll " + e.getClass().getName() + ": " + e.getMessage());
//			System.exit(0);
//		}
//	}
//
//// *************INSERT DATA**************************//
//	public static void insertData(Connection c, String table, int[] values) {
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "INSERT INTO " + table
//					+ "(id, date, sex, cases_total, cases_new, hosp_total, hosp_new, death_total, death_new) VALUES(100, '2020-04-10', 2, "
//					+ values[0] + ", " + values[1] + ", " + values[2] + ", " + values[3] + ", " + values[4] + ", "
//					+ values[5] + ");";
//			stmt.executeUpdate(sql);
//			stmt.close();
//			// c.commit();
//		}
//		catch (Exception e) {
//			System.err.println("insertData " + e.getClass().getName() + ": " + e.getMessage());
//		}
//		System.out.println("Record created successfully. ");
//	}
//
//// *************DELETE ROW**************************//
//	public static void deleteRow(Connection c, String table, String field, String value) {
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "DELETE FROM " + table + " WHERE " + field + " = " + value + ";";
//			stmt.executeUpdate(sql);
//			stmt.close();
//			System.out.println("Successfully deleted from " + table + " for " + field + " = " + value);
//		}
//		catch (Exception e) {
//			System.err.println("deleteRow " + e.getClass().getName() + ": " + e.getMessage());
//		}
//	}
//
//// *************UPDATE RECORD**************************//
//	public static void updateRecord(Connection c, String table, String updateField, String updateValue,
//			String condField, String condValue) {
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "UPDATE " + table + " set " + updateField + " = " + updateValue + "WHERE " + condField + " = "
//					+ condValue;
//			stmt.executeUpdate(sql);
//			stmt.close();
//			System.out.println("Successfully updated " + table + " and set " + updateField + " = " + updateValue);
//		}
//		catch (Exception e) {
//			System.err.println("updateRecord " + e.getClass().getName() + ": " + e.getMessage());
//		}
//	}
//
//// *************SELECT QUERY**************************//
//	public static void selectAllWithCond(Connection c, String table, String sex) {
//		String pattern = "MMMM d";
//		DateFormat df = new SimpleDateFormat(pattern);
//
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "SELECT * FROM " + table + " WHERE sex = " + sex + " ORDER BY date asc;";
//			System.out.println("Retrieving all records from " + table + " for sex = " + sex);
//			ResultSet rs = stmt.executeQuery(sql);
//
//			while (rs.next()) {
//				int id = rs.getInt("id");
//				Date date = rs.getDate("date");
//				String dateF = df.format(date);
//				int cases_total = rs.getInt("cases_total");
//				int cases_new = rs.getInt("cases_new");
//				int hosp_total = rs.getInt("hosp_total");
//				int hosp_new = rs.getInt("hosp_new");
//				int death_total = rs.getInt("death_total");
//				int death_new = rs.getInt("death_new");
//
//				System.out.println(dateF + " | " + cases_total + " | " + cases_new + " | " + hosp_total + " | "
//						+ hosp_new + " | " + death_total + " | " + death_new);
//			}
//			rs.close();
//			stmt.close();
//		}
//		catch (Exception e) {
//			System.err.println("selectAllWithCond " + e.getClass().getName() + ": " + e.getMessage());
//		}
//
//	}
//
//	// *************SELECT OneWithCond**************************//
//	public static void selectOneWithCond(Connection c, String table, String selection, String sex) {
//		String pattern = "MMMM d";
//		DateFormat df = new SimpleDateFormat(pattern);
//
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "SELECT date, " + selection + " FROM " + table + " WHERE sex = " + sex + " ORDER BY date asc;";
//			System.out.println("Retrieving " + selection + " from " + table + " for sex = " + sex);
//			ResultSet rs = stmt.executeQuery(sql);
//
//			while (rs.next()) {
//				Date date = rs.getDate("date");
//				String dateF = df.format(date);
//				int result = rs.getInt(selection);
//
//				System.out.println(dateF + " | " + result);
//			}
//			rs.close();
//			stmt.close();
//		}
//		catch (Exception e) {
//			System.err.println("selectOneWithCond " + e.getClass().getName() + ": " + e.getMessage());
//		}
//
//	}
//
//	// *************SELECT QUERY**************************//
//	public static ResultSet selectOne(Connection c, String table, String selection) {
//		String pattern = "MMMM d";
//		DateFormat df = new SimpleDateFormat(pattern);
//		ResultSet rs = null;
//
//		try {
//			Statement stmt = c.createStatement();
//			String sql = "SELECT date, sex, " + selection + " FROM " + table + " ORDER BY date, sex  asc;";
//			System.out.println("Retrieving " + selection + " from " + table + " for all sexes");
//			rs = stmt.executeQuery(sql);
//
//			while (rs.next()) {
//				Date date = rs.getDate("date");
//				String dateF = df.format(date);
//				int s = rs.getInt("sex");
//				int result = rs.getInt(selection);
//
//				System.out.println(dateF + " | " + s + " | " + result);
//			}
//			rs.close();
//			stmt.close();
//		}
//		catch (Exception e) {
//			System.err.println("selectOne " + e.getClass().getName() + ": " + e.getMessage());
//		}
//		return rs;
//	}
//
////***************************MAIN***********************//	
////	public static void main(String args[]) {
////		Connection connection = connectToDatabase("covid19_nyc", "postgres", "postgres");
////		dropTable(connection, "data_sex");
////		createTable(connection, "data_sex", "sex");
////		importData(connection, "data_sex", "sex_nyc");
////		selectAll(connection, "data_sex");
////		int[] test = { 1000, 2000, 3000, 400, 5000, 6000 };
////		insertData(connection, "data_sex", test);
////		// deleteRow(connection, "data_sex", "id", "100");
////		updateRecord(connection, "data_sex", "cases_total", "2", "id", "100");
////		selectAllWithCond(connection, "data_sex", "1");
////		selectOneWithCond(connection, "data_sex", "cases_total", "1");
////		selectOne(connection, "data_sex", "cases_total");
////		try {
////			connection.close();
////		}
////		catch (SQLException e) {
////			System.err.println("in main " + e.getClass().getName() + ": " + e.getMessage());
////		}
////
////	}// main
//
//}// class