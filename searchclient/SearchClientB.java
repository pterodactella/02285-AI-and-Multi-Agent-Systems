package searchclient;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

public class SearchClientB {
	public static State parseLevel(BufferedReader serverMessages) throws IOException {
		// We can assume that the level file is conforming to specification, since the
		// server verifies this.

        while (!serverMessages.readLine().startsWith("#")) {
        }
    
		Color[] agentColors = new Color[50];
        Color[] boxColors = new Color[50];
        String line;

		//  the method creates two arrays to hold the colors of agents and boxes. 
		//  reads in lines from serverMessages until it encounters #,
		//  For each non-comment line, it splits the line into a color starting with : and a 
		//  comma-separated list of entities (agents and/or boxes) that have that color. It then sets the color of each entity
		//  in the appropriate array based on its first character (which should be either be a digit for an agent or a capital letter for a box).
		
        while (!(line = serverMessages.readLine()).startsWith("#")) {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");

            for (String entity : entities) {
				char c = entity.strip().charAt(0);
				if ('0' <= c && c <= '9') {
					agentColors[c - '0'] = color;
				} else if ('A' <= c && c <= 'Z') {
					boxColors[c - 'A'] = color;
				}
			}
            
        }
		
        ArrayList<String> levelLines = new ArrayList<>(64);
		while (!(line = serverMessages.readLine()).startsWith("#")) {
			levelLines.add(line);
		}

        //levelLines.stream() creates a stream of elements from the levelLines collection.

        // .mapToInt(String::length) maps each element (which is a String object) to its length.

        // .max().orElse(0) finds the maximum element in the stream (which is the length of the longest String in levelLines) and returns it as an OptionalInt object. If the stream is empty (i.e., levelLines is empty), orElse(0) returns a default value of 0.

        // So, altogether, int numCols = levelLines.stream().mapToInt(String::length).max().orElse(0); sets numCols to the length of the longest String in levelLines, or 0 if levelLines is empty.

        // In other words, this line of code is calculating the number of columns in the level, based on the length of the longest row in the level.

		int numRows = levelLines.size();
		int numCols = levelLines.stream().mapToInt(String::length).max().orElse(0);
		int numAgents = 0;
		int[] agentRows = new int[10];
		int[] agentCols = new int[10];
		boolean[][] walls = new boolean[numRows][numCols];
		char[][] boxes = new char[numRows][numCols];

		for (int row = 0; row < numRows; ++row) {
			line = levelLines.get(row);
			for (int col = 0; col < line.length(); ++col) {
				char c = line.charAt(col);
				if ('0' <= c && c <= '9') {
					agentRows[c - '0'] = row;
					agentCols[c - '0'] = col;
					++numAgents;
				} else if ('A' <= c && c <= 'Z') {
					boxes[row][col] = c;
				} else if (c == '+') {
					walls[row][col] = true;
				}
			}
		}
		agentRows = Arrays.copyOf(agentRows, numAgents);
		agentCols = Arrays.copyOf(agentCols, numAgents);

		// Read goal state
		// line is currently "#goal"
		char[][] goals = new char[numRows][numCols];
        int row = 0;

		while (!(line = serverMessages.readLine()).startsWith("#")) {
			for (int col = 0; col < line.length(); ++col) {
				char c = line.charAt(col);

				if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
					goals[numRows - levelLines.size() + row][col] = c;
				}
			}
		}

		// End
		// line is currently "#end"

		return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);

	}

	public static Action[][] search(State initialState, Frontier frontier) {
		System.err.format("Starting %s.\n", frontier.getName());

		return GraphSearch.search(initialState, frontier);
	}

	public static void main(String[] args) throws IOException {
		// Use stderr to print to the console.
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");

		// Send client name to server.
		System.out.println("SearchClient");

		// We can also print comments to stdout by prefixing with a #.
		System.out.println("#This is a comment.");

		// Parse the level.
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
		State initialState = SearchClient.parseLevel(serverMessages);

		// Select search strategy.
		Frontier frontier;
		if (args.length > 0) {
			switch (args[0].toLowerCase(Locale.ROOT)) {
			case "-bfs":
				frontier = new FrontierBFS();
				break;
			case "-dfs":
				frontier = new FrontierDFS();
				break;
			case "-astar":
				frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
				break;
			case "-wastar":
				int w = 5;
				if (args.length > 1) {
					try {
						w = Integer.parseUnsignedInt(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("Couldn't parse weight argument to -wastar as integer, using default.");
					}
				}
				frontier = new FrontierBestFirst(new HeuristicWeightedAStar(initialState, w));
				break;
			case "-greedy":
				frontier = new FrontierBestFirst(new HeuristicGreedy(initialState));
				break;
			default:
				frontier = new FrontierBFS();
				System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or "
						+ "-greedy to set the search strategy.");
			}
		} else {
			frontier = new FrontierBFS();
			System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to "
					+ "set the search strategy.");
		}

		// Search for a plan.
		Action[][] plan;
		try {
			plan = SearchClient.search(initialState, frontier);
		} catch (OutOfMemoryError ex) {
			System.err.println("Maximum memory usage exceeded.");
			plan = null;
		}

		// Print plan to server.
		if (plan == null) {
			System.err.println("Unable to solve level.");
			System.exit(0);
		} else {
			System.err.format("Found solution of length %,d.\n", plan.length);

			for (Action[] jointAction : plan) {
				System.out.print(jointAction[0].name);
				for (int action = 1; action < jointAction.length; ++action) {
					System.out.print("|");
					System.out.print(jointAction[action].name);
				}
				System.out.println();
				// We must read the server's response to not fill up the stdin buffer and block
				// the server.
				serverMessages.readLine();
			}
		}
	}
}
