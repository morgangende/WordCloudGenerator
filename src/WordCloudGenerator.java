import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public final class WordCloudGenerator {
	
	// Returns the comparison of map entries first by keys, then by values
	private static class AlphabeticalComparator implements Comparator<Map.Entry<String, Integer>> {
		@Override
		public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
			String key1 = entry1.getKey().toLowerCase();
			String key2 = entry2.getKey().toLowerCase();
			int keyComparison = key1.compareTo(key2);
			
			Integer val1 = entry1.getValue();
			Integer val2 = entry2.getValue();
			int valueComparison = val1.compareTo(val2);
			
			return keyComparison != 0 ? keyComparison : valueComparison;
		}
	}

	// Returns the comparison of map entries first by values, then by keys
	private static class DecreasingComparator implements Comparator<Map.Entry<String, Integer>> {
		@Override
		public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
			Integer val1 = entry1.getValue();
			Integer val2 = entry2.getValue();
			int valueComparison = -1 * (val1.compareTo(val2));
			
			String key1 = entry1.getKey().toLowerCase();
			String key2 = entry2.getKey().toLowerCase();
			int keyComparison = key1.compareTo(key2);
			
			return valueComparison != 0 ? valueComparison : keyComparison;
		}
	}
	
	// Constants
	private static final int NUM_FONT_SIZES = 37;
	private static final int MIN_FONT_SIZE = 11;
	
	// Global variables
	private static Map<String, Integer> map = new HashMap<String, Integer>();
	private static ArrayList<Map.Entry<String, Integer>> cloudWords = new ArrayList<>();

	// Private constructor so this utility class cannot be instantiated.
	private WordCloudGenerator() { }
	
	// Reads words from the file and places them appropriately into the map
	private static void populateMap(Scanner in) {
		while(in.hasNext()) {
			String token = in.next().replaceAll("[^a-zA-Z0-9]", ""); // Cleans words of punctuation
			int count = map.containsKey(token) ? map.get(token) + 1 : 1;
			map.put(token, count);
		}
	}

	// Gets the n most frequently occurring words in the map and alphabetizes them
	private static void getCloudWords(int n) {
		Comparator<Map.Entry<String, Integer>> ac = new AlphabeticalComparator();		
		Comparator<Map.Entry<String, Integer>> dc = new DecreasingComparator();
		
		ArrayList<Map.Entry<String, Integer>> temp = new ArrayList<>();
       	Set<Map.Entry<String, Integer>> s = map.entrySet();
    	for(Map.Entry<String, Integer> e : s) {
    		temp.add(e);
    	}
    	Collections.sort(temp, dc);
    	
		for (int i = 0; i < n; i++) {
			Map.Entry<String, Integer> entry = temp.remove(i);
			cloudWords.add(entry);
		}
		Collections.sort(cloudWords, ac);
	}
	
	// Creates, formats, and styles the word cloud in an html file
	private static void generatePage(String inputFile, String outputFileName) {
		try {
			File outputFile = new File(outputFileName);
			
			if(!outputFile.exists()) {
				outputFile.createNewFile();
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile.getAbsoluteFile()));
			
			// Create the header of the html document
			out.write("<html>\n");
			out.write("<head>\n");
			out.write("<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">\n");
			out.write("<title>Top " + cloudWords.size() + " words in " + inputFile + "</title>\n");
			out.write("</head>\n");
			out.write("<body>\n");

			// Format the TagCloud
			String cloudSize = "bigCloud";			
			if(cloudWords.size() < 75) {
				cloudSize = "smallCloud";
			}
			
			out.write("<div class = " + cloudSize + ">\n");
			out.write("<p class = cbox>\n");

			// Get the maximum and minimum counts from Pairs in w
			int min = cloudWords.size();
			int max = 0;
			for (Map.Entry<String, Integer> p : cloudWords) {
				int val = p.getValue();
				if (val > max) {
					max = val;
				}
				if (val < min) {
					min = val;
				}
			}

			// Print all the words in w with the appropriate font size and randomized colors
			for (Map.Entry<String, Integer> p : cloudWords) {
				int fontSize = NUM_FONT_SIZES * (p.getValue() - min) / (max - min) + MIN_FONT_SIZE;
				
				String color;				
				switch ((int)(Math.random() * 5)) {
				case 0:
					color = "col1";
					break;
				case 1:
					color = "col2";
					break;
				case 2:
					color = "col3";
					break;
				case 3:
					color = "col4";
					break;
				case 4:
					color = "col5";
					break;
				default:
					color = "";
					break;
				}
				
				out.write("<span class=\"" + color + "\"><span class=\"f" + fontSize + "\" title=\"" + p.getValue() + " occurrences\">" + p.getKey() + "</span></span>\n");
			}

			// Create the footer of the html document
			out.write("</p>\n");
			out.write("</div>\n");
			out.write("</body>\n");
			out.write("</html>");

			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// Read arguments
		String inputFileName = args[0];
		String outputFileName = args[1];
		int n = Integer.parseInt(args[2]);
		
		// Read input and populate map
		Path p = Paths.get(inputFileName);
		Scanner in = new Scanner(p);		
		populateMap(in);
		in.close();
		
		// Determine the word cloud and generate the html page
		getCloudWords(n);
		generatePage(p.getFileName().toString(), outputFileName);

		System.out.println("Word Cloud Generated");
	}
}