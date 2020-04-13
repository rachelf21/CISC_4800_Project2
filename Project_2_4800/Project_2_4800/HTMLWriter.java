package Project_2_4800;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This class writes an HTML file to a specified folder
 * 
 * @author Rachel Friedman
 * @version 1.0
 *
 */

public class HTMLWriter {
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
	
	public void createHTML(String htmlResults, String selection, int option) {
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
}
