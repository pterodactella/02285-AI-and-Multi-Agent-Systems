package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

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

	
	// numRows = levelLines.size(); // Add this line
	
	// 	int numAgents = 0;
	// 	int[] agentRows = new int[10];
	// 	int[] agentCols = new int[10];
	// 	boolean[][] walls = new boolean[numRows][numCols];
	// 	char[][] boxes = new char[numRows][numCols];
	// 	for (int row = 0; row < numRows; ++row) {
	// 		line = levelLines.get(row);
	// 		for (int col = 0; col < line.length(); ++col) {
	// 			char c = line.charAt(col);

	// 			if ('0' <= c && c <= '9') {
	// 				agentRows[c - '0'] = row;
	// 				agentCols[c - '0'] = col;
	// 				++numAgents;
	// 			} else if ('A' <= c && c <= 'Z') {
	// 				boxes[row][col] = c;
	// 			} else if (c == '+') {
	// 				walls[row][col] = true;
	// 			}
	// 		}
	// 	}
	// 	agentRows = Arrays.copyOf(agentRows, numAgents);
	// 	agentCols = Arrays.copyOf(agentCols, numAgents);

	// 	// Read goal state
	// 	// line is currently "#goal"
	// 	char[][] goals = new char[numRows][numCols];
	// 	line = serverMessages.readLine();
	// 	int row = 0;
	// 	while (!line.startsWith("#")) {
	// 		for (int col = 0; col < line.length(); ++col) {
	// 			char c = line.charAt(col);

	// 			if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
	// 				goals[row][col] = c;
	// 			}
	// 		}

	// 		++row;
	// 		line = serverMessages.readLine();
	// 	}

	// 	// End
	// 	// line is currently "#end"

		return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
	}

	public static Action[][] ConflictBasedSearch(State initialState, Frontier frontier) {

		// Create an agent object for all agents, make an array of them
		// The object has the cost, solution, constraints
		ArrayList<Agent> agents = new ArrayList<>();

		ArrayList<Agent> checkedAgents = new ArrayList<>();
		
		for (int i = 0; i< initialState.agentCols.length; i ++) {
			// System.err.println(State.agentColors[i].toString() +" i:"+ i);
			// System.out.println(agents.size());
//   Agent(String agentId, Color color, State initialState, Frontier frontier, int agentIndex) {
			agents.add(new Agent(State.agentColors[i].toString() + i, State.agentColors[i], initialState, frontier, i));
			// System.out.println(agents.size());
			// System.out.println("agents.size()");
		}
		ArrayList<Constraints> globalConstraints = new ArrayList<>(64);


		// iterate over the array of agents
		while (!agents.isEmpty()) {
			int minimumCost = agents.get(0).cost;
			int index = 0;
			for (int i = 0; i < agents.size(); i++) {
					if(agents.get(i).cost < minimumCost) {
					minimumCost = agents.get(i).cost;
					index = i;
					}
			}
			// validate the constraints of the agent[index] regarding the global constratins
			Agent agentWithLowestCost = agents.get(index);
			
			//loop through all constraints within
			for (int agentConstI = 0; agentConstI < agents.get(index).constraints.length; agentConstI++) {
				// Compare wth global constraints
				for (int globalConstJ = 0; globalConstJ < globalConstraints.size(); globalConstJ++) {
					if ( globalConstraints.get(globalConstJ).isConflicting(agents.get(index).constraints[agentConstI])  ) {
						// this is a conflict

						Agent newAgent1 = new Agent(agentWithLowestCost.agentId, agentWithLowestCost.agentColor, initialState, frontier, agentWithLowestCost.agentIndex, globalConstraints.get(globalConstJ) );

						// Constraints conflict = agents.get(index).constraint
						// We need to add the constrain to one of the child and calculate a new path 
						// 


					} else {
						// this is not a conflict
						globalConstraints.add(agents.get(index).constraints[agentConstI]);
					}

				}
			}



			checkedAgents.add(agents.get(index));
			agents.remove(index);
		
		}

		return checkedAgents.get(0).solution;
		

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
		boolean isConflictBasedSearch = false;
		if (args.length > 0) {
			switch (args[0].toLowerCase(Locale.ROOT)) {
			case "-cbs": 
			 	isConflictBasedSearch = true;
				frontier =  new FrontierBestFirst(new HeuristicAStar(initialState));
				break;
			case "-bfs":
				frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
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

		// Run search.
		Action[][] plan;
		if(isConflictBasedSearch){
			System.err.println("Running Conflict Based Search");
			try {
				plan = SearchClient.ConflictBasedSearch(initialState, frontier);
				System.err.println("Found CBS plan");
			} catch (OutOfMemoryError ex) {
				System.err.println("Maximum memory usage exceeded.");
				plan = null;
			}
		} else {
			try {
				plan = SearchClient.search(initialState, frontier);
			} catch (OutOfMemoryError ex) {
				System.err.println("Maximum memory usage exceeded.");
				plan = null;
			}
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
