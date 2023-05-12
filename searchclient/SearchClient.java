package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import searchclient.CBS.CBSNode;
import searchclient.CBS.PathFinder;
import searchclient.CBS.PlanStep;

public class SearchClient {
	public static State parseLevel(BufferedReader serverMessages) throws IOException {
		// We can assume that the level file is conforming to specification, since the
		// server verifies this.
		// Read domain
		serverMessages.readLine(); // #domain
		serverMessages.readLine(); // hospital

		// Read Level name
		serverMessages.readLine(); // #levelname
		serverMessages.readLine(); // <name>

		// Read colors
		serverMessages.readLine(); // #colors
		Color[] agentColors = new Color[10];
		Color[] boxColors = new Color[26];
		String line = serverMessages.readLine();
		while (!line.startsWith("#")) {
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
			line = serverMessages.readLine();
		}

		// Read initial state
		// line is currently "#initial"
		int numRows = 0;
		int numCols = 0;
		ArrayList<String> levelLines = new ArrayList<>(64);
		line = serverMessages.readLine();
		while (!line.startsWith("#")) {
			levelLines.add(line);
			numCols = Math.max(numCols, line.length());
			++numRows;
			line = serverMessages.readLine();
		}
		int numAgents = 0;
		int[] agentRows = new int[20];
		int[] agentCols = new int[20];

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
		line = serverMessages.readLine();
		int row = 0;
		while (!line.startsWith("#")) {
			for (int col = 0; col < line.length(); ++col) {
				char c = line.charAt(col);

				if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
					goals[row][col] = c;
				}
			}

			++row;
			line = serverMessages.readLine();
		}

		// End
		// line is currently "#end"

		for (int boxCol = 0; boxCol < boxColors.length; ++boxCol) {
			boolean foundBoxColor = false;
			for (int agentCol = 0; agentCol < agentColors.length; ++agentCol) {
				if (boxColors[boxCol] == agentColors[agentCol]) {
					foundBoxColor = true;
					break;
				}
			}
			if (!foundBoxColor) {
				SearchClient.replaceBoxWithWall(walls, boxes, (char) ('A' + boxCol));
			}
		}
		// This code prints out all the true false stuff
		// SearchClient.printMatrix(walls);
		return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
	}

	public static void printMatrix(boolean[][] walls) {
		for (int i = 0; i < walls.length; i++) {
			for (int j = 0; j < walls[i].length; j++) {
				System.err.print(walls[i][j] + " ");
			}
			System.err.println();
		}

	}

	public static void replaceBoxWithWall(boolean[][] walls, char[][] boxes, char replacedChar) {
		for (int row = 0; row < boxes.length; ++row) {
			for (int col = 0; col < boxes[row].length; ++col) {
				if (boxes[row][col] == replacedChar) {
					boxes[row][col] = 0;
					walls[row][col] = true;
				}
			}
		}
	}

	public static PlanStep[][] search(State initialState, Frontier frontier) {
		System.err.format("Starting %s.\n", frontier.getName());
		PathFinder solver = new PathFinder(initialState, preprocess(initialState));

		return solver.solveCBS();
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
				case "-cbs":
					frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
					break;
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
		PlanStep[][] plan;
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

			for (PlanStep[] jointAction : plan) {
				System.err.print(jointAction[0].action.name);
				for (int action = 1; action < jointAction.length; ++action) {
					System.err.print("|");
					System.err.println(jointAction[action].action.name);
				}
				System.err.println();
				System.out.print(jointAction[0].action.name);
				for (int action = 1; action < jointAction.length; ++action) {
					System.out.print("|");
					System.out.println(jointAction[action].action.name);
				}
				System.out.println();
				// We must read the server's response to not fill up the stdin buffer and block
				// the server.
				serverMessages.readLine();
			}
		}
	}

	private static HashMap<Color, List<Integer>> preprocess(State initialState) {
		HashMap<Color, List<Integer>> agentBoxDistances = new HashMap<>();

		for (int agentIndex = 0; agentIndex < initialState.agentRows.length; agentIndex++) {
			Color agentColor = initialState.agentColors[agentIndex];
			if (!agentBoxDistances.containsKey(agentColor)) {
				agentBoxDistances.put(agentColor, new ArrayList<>());
			}

			int agentRow = initialState.agentRows[agentIndex];
			int agentCol = initialState.agentCols[agentIndex];

			for (int row = 0; row < initialState.boxes.length; row++) {
				for (int col = 0; col < initialState.boxes[row].length; col++) {
					char box = initialState.boxes[row][col];
					if (box != 0 && initialState.boxColors[box - 'A'] == agentColor) {
						int goalRow = -1;
						int goalCol = -1;
						for (int goalRowIdx = 0; goalRowIdx < initialState.goals.length; goalRowIdx++) {
							for (int goalColIdx = 0; goalColIdx < initialState.goals[goalRowIdx].length; goalColIdx++) {
								if (initialState.goals[goalRowIdx][goalColIdx] == box) {
									goalRow = goalRowIdx;
									goalCol = goalColIdx;
									break;
								}
							}
							if (goalRow != -1 && goalCol != -1) {
								break;
							}
						}
						int agentDistance = Math.abs(agentRow - goalRow) + Math.abs(agentCol - goalCol);
						agentBoxDistances.get(agentColor).add(agentDistance);

						int boxDistance = Math.abs(row - goalRow) + Math.abs(col - goalCol);
						agentBoxDistances.get(agentColor).add(boxDistance);
					}
				}
			}

		}
		for (Color color : agentBoxDistances.keySet()) {
			List<Integer> distances = agentBoxDistances.get(color);
			System.err.println(
					"Agent-Goal distances for color " + color + ": " + distances.subList(0, distances.size() / 2));
			System.err.println("Box-Goal distances for color " + color + ": "
					+ distances.subList(distances.size() / 2, distances.size()));
		}

		return agentBoxDistances;
	}

}
