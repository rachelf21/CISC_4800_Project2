package Project_2_4800;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is used to retrieve data from a specified website.
 * 
 * @author Rachel Friedman
 * @version 1.0
 *
 */
public class WebScraper {
	/**
	 * Retrieves data from website specified in urlAddress and saves it to a csv
	 * file.
	 * 
	 * @param urlAddress the website to retrieve data from
	 * @return column headings and data types from retrieved data.
	 */
	public String retrieveDataFromWebsite(String urlAddress) {
		DecimalFormat decimalFormat = new DecimalFormat("###.###");
		ArrayList<String> header = new ArrayList<String>();
		String allFields = "";
		StringBuffer stringBuffer = new StringBuffer();
		byte[] data = null;
		File file = new File("data\\data.csv");

		try {
			URL url = new URL(urlAddress);
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
			}
			// allFields = "ID serial primary key not null, " + allFields;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return allFields;
	}

	/**
	 * Helper method to aid in retrieving headers from csv file
	 * 
	 * @param filename the csv file with the header row
	 * @return all fields in header row (data types not included)
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

}
