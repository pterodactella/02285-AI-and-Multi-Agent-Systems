package searchclient.CBS;

import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.IntSummaryStatistics;

import searchclient.Color;
// import searchclient.Action;
// import searchclient.Frontier;
// import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.State;

public class CBSNode {
	public State state;
	public ArrayList<Constraint> constraints;
	public PlanStep[][] solution;
	public int[] costs;
	private int longestPath;
	public int totalCost;
	private InitialState initialStateForStorage;
	private ArrayList<Integer> shiftedAgents;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new ArrayList<>();
		this.solution = null;
		this.costs = new int[state.agentRows.length];
		this.totalCost = 0;
	}
	
	public CBSNode(CBSNode parent) {
		this.state = parent.state;
		this.constraints = new ArrayList<>();
		for(Constraint constr : parent.constraints) {
			this.constraints.add(new Constraint(constr));
		}
		
		this.solution = new PlanStep[parent.solution.length][];
		for (int i = 0; i < parent.solution.length; i++) {
			this.solution[i] = new PlanStep[parent.solution[i].length];
			for (int j = 0; j < parent.solution[i].length; j++) {
				this.solution[i][j] = new PlanStep(parent.solution[i][j]);
			}
		}
		
		this.costs = parent.costs.clone();
		this.totalCost = 0;
	}

	public Conflict findFirstConflict() {
		int[] agentsPositions = null;
		for (int i = 1; i < this.longestPath; i++) {  
			for (int j = 0; j < this.solution[i].length; j++) {
				if(this.solution[i][j].locationX == -1)
					continue;
				for (int k = j + 1; k < this.solution[i].length; k++) {
					if(this.solution[i][k].locationX == -1)
						continue;
					agentsPositions = new int[] { this.solution[i][j].locationX, this.solution[i][j].locationY,
							this.solution[i][k].locationX, this.solution[i][k].locationY };
					// System.err.println("The agents positions are: ")
					// if(agentsPositions[0] == -1 || agentsPositions[1] == -1)
					// 	continue;
					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3]) {
						return new Conflict(j, k, agentsPositions[0], agentsPositions[1], i);
					}
				}
			}
		}

		// TODO: findConflct
		return null;

	}

	public void findIndividualPlan(int agentIndex, PlanStep[][] individualPlans) {

		// Construct a state with the constructor that takes arguments
		State searchSpecificState = createSpecificState(agentIndex);
		System.out.println("Specific State: agentsLength: "+ searchSpecificState.agentRows.length + " boxesLength: " + searchSpecificState.boxes.length + " goalsLength: " + searchSpecificState.goals.length    );
		this.state = searchSpecificState;

		// Calculate teh shifted agent index that matches the real one in the searchSpecificState
		int shiftedAgentIndex = this.shiftedAgents.indexOf(agentIndex);

		ConstraintState constraintState = new ConstraintState(searchSpecificState, shiftedAgentIndex, this.constraints, 0); // we create a state here
		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));

		// WE need to create the specific maps here since we have the index here 
		// and call search on it
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, shiftedAgentIndex);
		//		System.err.println("plan for agent " + agentIndex + " is: " + Arrays.toString(plan));
		System.err.println("THE PLAN FOR: " + agentIndex);
		for (PlanStep step : plan) {
			System.err.println("Step: " + step.toString());
		}
		if (plan != null && plan.length > this.longestPath) {
			this.longestPath = plan.length;
		}

		individualPlans[agentIndex] = plan;
		this.costs[agentIndex] = plan[plan.length - 1].timestamp;

	};


	public PlanStep[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
		System.err.println("NUMBER OF AGENTS" + numberOfAgents);

		for (int i = 0; i < numberOfAgents; i++) {

			findIndividualPlan(i, individualPlans);
			//			System.out.println("THE PLAN FOR: " + i);
			//			for (PlanStep step : plan) {
			//				System.out.println("Step: " + step.toString());
			//			}
			// TODO: Add search with constraint

		}

		return PlanStep.mergePlans(individualPlans);
	}

	public PlanStep[][] findPlans() {
		

		int numberOfAgents = state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
		System.err.println("NUMBER OF AGENTS" + numberOfAgents);

		for (int i = 0; i < numberOfAgents; i++) {

			findIndividualPlan(i, individualPlans);
			//			System.out.println("THE PLAN FOR: " + i);
			//			for (PlanStep step : plan) {
			//				System.out.println("Step: " + step.toString());
			//			}
			// TODO: Add search with constraint

		}

		return PlanStep.mergePlans(individualPlans);
	}

	public State createSpecificState(  int agent) {
		initialStateForStorage = new InitialState();
		InitialStateObject initialStateObject = initialStateForStorage.getInitialState();

		System.out.println("++++++++++Creating specific state for agent: " + agent + "++++++++++");
		// Create a new state with only the appropriately colored agents
		// Find the color of the agent  
		Color agentColor = initialStateObject.agentColors[agent];
		// Find everything from that color
		ArrayList<Integer> sameColoredAgents = new ArrayList<>(initialStateObject.agentColors.length);
		// agents
		Color[] agentColors = initialStateObject.agentColors;
		for (int i = 0; i < initialStateObject.agentColors.length; i++) {
			 if (initialStateObject.agentColors[i] == agentColor) {
				  sameColoredAgents.add(i);
			 }
		}
		// boxes
		ArrayList<Integer> sameColoredBoxes = new ArrayList<>(initialStateObject.boxColors.length);
		for (int i = 0; i < initialStateObject.boxColors.length; i++) {
			Color box = initialStateObject.boxColors[i] ;
			 if (box == agentColor) {
				  sameColoredBoxes.add(i);
			 }
		}

		System.out.println("++++++++++ Index of agent: " + agent +",  Color of agent: " + agentColor+ ", Agents with this color: " + sameColoredAgents + ",  Boxes with this color: " + sameColoredBoxes +"   ++++++++++");
      // this.shiftedAgents = sameColoredAgents.stream().mapToInt(i -> i).toArray();
		this.shiftedAgents = new ArrayList<>(sameColoredAgents.size());
      this.shiftedAgents = sameColoredAgents;



		// TODO: Initialize these variables
		int[] newAgentRows = new int[sameColoredAgents.size() ];
		int[] newAgentCols =  new int[sameColoredAgents.size() ];
		Color[] newAgentColors = new Color[sameColoredAgents.size() ]; 
		boolean[][] newWalls = new boolean[initialStateObject.wallsIntial.length] [initialStateObject.wallsIntial[0].length ];
		char[][] newBoxes = initialStateObject.boxesInitial; ;
		Color[] newBoxColors = new Color[sameColoredBoxes.size() ];  
		char[][] newGoals = new char[initialStateObject.goals.length] [initialStateObject.goals[0].length];

		// Set the agent details
		newAgentRows = setAgentRows(initialStateObject, sameColoredAgents);
		newAgentCols = setAgentCols(initialStateObject, sameColoredAgents);
		newAgentColors = setAgentColors(initialStateObject, sameColoredAgents);
		newGoals = setGoals(initialStateObject, sameColoredAgents, sameColoredBoxes, initialStateObject.goals);
		// newBoxes = initialStateObject.boxesInitial;
		newBoxes = setBoxes(initialStateObject, sameColoredBoxes, initialStateObject.boxesInitial );
		newBoxColors = setBoxColors(initialStateObject, sameColoredBoxes);

		// System.out.println("newAgentRows: "+ Arrays.toString(newAgentRows));
		
		// TODO set box details
		// TODO set extra walls
		newWalls = setExtraWalls(initialStateObject, sameColoredAgents, sameColoredBoxes);

		// compareCharArrays( newBoxes,initialStateObject.boxesInitial );
		// TODO: create new state, for this new spcificState class is rqeuired WITHOUT THE STATIC fields!
		// Create a new state with only the appropriately colored elements
		State newState = new State(newAgentRows, newAgentCols, newAgentColors, newWalls, newBoxes, newBoxColors, newGoals);

		// Printing out the new state from the specific state
		System.out.println("==========Printing out the new state from the specific state============");
		System.out.print(newState.toString() );
		System.out.println("========================================================================");
		// State newState = cbsNode.state;

		return newState;
	}

	// TODO set Box details

	private int[] setAgentRows(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents  ) {
		int [] agentRowsFilled = new int[sameColoredAgents.size() ];

		// System.out.println("length of the same colored agents: "+ sameColoredAgents.size() );
		for (int i = 0; i < sameColoredAgents.size(); i++) {
			agentRowsFilled[i] = initialStateObject.agentRowsInitial[sameColoredAgents.get(i)];
		}
		return agentRowsFilled;
	}

	private int[] setAgentCols(InitialStateObject initialStateObject,  ArrayList<Integer> sameColoredAgents) {
		int [] agentColsFilled = new int[sameColoredAgents.size() ];

		// System.out.println("length of the same colored agents: "+ sameColoredAgents.size() );
		for (int i = 0; i < sameColoredAgents.size(); i++) {
			agentColsFilled[i] = initialStateObject.agentColsInitial[sameColoredAgents.get(i)];
		}
		return agentColsFilled;
	}

	private Color[] setAgentColors(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents ) {

		Color [] agentColorsFilled = new Color[sameColoredAgents.size() ];
		for (int i = 0; i < sameColoredAgents.size(); i++) {
			agentColorsFilled[i] = initialStateObject.agentColors[sameColoredAgents.get(i)];
		}
 		return agentColorsFilled;
	}

	private char[][] setBoxes(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredBoxes, char[][] boxesInitial ) {
		// Perform deep copy
		char[][] copiedBoxes = new char[boxesInitial.length][boxesInitial[1].length];


		for (int i = 0; i < boxesInitial.length; i++) { // rows =first dim
			for(int j = 0; j < boxesInitial[i].length; j++) { // cols =second dim
				char box = boxesInitial[i][j];
				int val = box - 'A';
				
				if( sameColoredBoxes.contains( boxesInitial[i][j] - 'A')   ){
					int index = sameColoredBoxes.indexOf(boxesInitial[i][j] - 'A');
					char temp = (char) (index + 'A');
					copiedBoxes[i][j] = temp;
				}
				//  else {
				// 	copiedBoxes[i][j] = null;
				// }
			}
		}
		return copiedBoxes;
	}

	private Color[] setBoxColors(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredBoxes ) {

		Color [] boxColorsFilled = new Color[sameColoredBoxes.size() ];
		for (int i = 0; i < sameColoredBoxes.size(); i++) {
			boxColorsFilled[i] = initialStateObject.boxColors[sameColoredBoxes.get(i)];
		}
 		return boxColorsFilled;
	}

	private char[][] setGoals(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents, ArrayList<Integer> sameColoredBoxes, char[][] goalsInitial ) {
		// Perform deep copy
		char[][] copiedGoals = new char[goalsInitial.length][goalsInitial[1].length];

		for (int i = 0; i < goalsInitial.length; i++) { // rows =first dim
			for(int j = 0; j < goalsInitial[i].length; j++) { // cols =second dim
				// check if the goal is one of the samecolored agents
				if ( sameColoredAgents.contains (Character.getNumericValue(goalsInitial[i][j]) ) && Character.getNumericValue(goalsInitial[i][j]) <=9 && Character.getNumericValue(goalsInitial[i][j]) >=0 ) {
					// Convert the goal number to the index of the agent in the sameColoredAgents array. In char form
					copiedGoals[i][j] = (char) (sameColoredAgents.indexOf(Character.getNumericValue(goalsInitial[i][j])) + '0');
				}
				// check if the goal is one of the samecolored boxes
				
				else if( sameColoredBoxes.contains( goalsInitial[i][j] - 'A')   ){
					int index = sameColoredBoxes.indexOf(goalsInitial[i][j] - 'A');
					char temp = (char) (index + 'A');
					copiedGoals[i][j] = temp;
				}else{ 
					copiedGoals[i][j] = ' ';
				}
			}
		}
		return copiedGoals;
	}

	public static boolean compareCharArrays(char[][] array1, char[][] array2) {
		if (array1.length != array2.length || array1[0].length != array2[0].length) {
			 // Arrays have different dimensions
			 System.out.println("Arrays are equal: " + false);
			 return false;
		}

		// Iterate through the arrays and compare each element
		for (int i = 0; i < array1.length; i++) {
			 for (int j = 0; j < array1[i].length; j++) {
				  if (array1[i][j] != array2[i][j]) {
						// Elements are not equal
						char one = array1[i][j];
						char two = array2[i][j];
						System.out.println("Arrays are equal: " + false);
						return false;
				  }
			 }
		}

		// All elements are equal
		System.out.println("Arrays are equal: " + true);
		return true;
  }


	private boolean[][] setExtraWalls(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents, ArrayList<Integer> sameColoredBoxes  ) {
		// Perform deep copy
		boolean[][] copiedWalls = new boolean[initialStateObject.wallsIntial.length][];
		for (int i = 0; i < initialStateObject.wallsIntial.length; i++) {
			copiedWalls[i] = new boolean[ initialStateObject.wallsIntial[i].length];
				System.arraycopy(initialStateObject.wallsIntial[i], 0, copiedWalls[i], 0, initialStateObject.wallsIntial[i].length);
		}

		// Iterates over the agents and sets the walls to true when the agentIndex is not in the sameColoredAgents array
		for (int i = 0; i < initialStateObject.agentColsInitial.length; i++) {
			if ( !sameColoredAgents.contains(i) ) {
				copiedWalls[initialStateObject.agentRowsInitial[i]] [initialStateObject.agentColsInitial[i]] = true;
			}
		}

		ArrayList<Character> boxesToSetAsWall = new ArrayList<>();
		for (int i = 0; i < initialStateObject.boxColors.length; i++) {
			if ( initialStateObject.boxColors[i] != null && !sameColoredBoxes.contains(i)  ) {
				boxesToSetAsWall.add( (char) (i + 'A') );
			}
		}

		for (int row = 0; row < copiedWalls.length; row++) {
			for (int col = 0; col < copiedWalls[0].length; col++) {
				if ( boxesToSetAsWall.contains( initialStateObject.boxesInitial[row][col] ) ) {
					copiedWalls[row][col] = true;
				};
				
			}
			
		}



		return copiedWalls;
	}

	public int sumCosts() {
		int sum = 0;
		for (int i = 0; i < this.costs.length; i++) {
			sum += this.costs[i];
		}
		return sum;
	}
}

class Conflict {
	public int[] agentIndexes;
	public int locationX;
	public int locationY;
	public int timestamp;

	// TODO:Include boxes
	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;

	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("agentB: " + agentIndexes[1] + "; ");
		s.append("agentA: " + agentIndexes[0] + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}

}
