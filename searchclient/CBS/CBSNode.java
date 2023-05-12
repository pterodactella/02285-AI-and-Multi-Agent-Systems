package searchclient.CBS;





import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.PriorityQueue;
import java.util.Arrays;

import searchclient.Color;
// import searchclient.Action;
// import searchclient.Frontier;
// import searchclient.GraphSearch;
// import searchclient.Heuristic;
import searchclient.State;

public class CBSNode {
	public State state;
	public ArrayList<Constraint> constraints;
	public PlanStep[][] solution;
	public int[] costs;
	private int longestPath;
	public int totalCost;
	private InitialState initialStateForStorage;

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

		ConstraintState constraintState = new ConstraintState(searchSpecificState, agentIndex, this.constraints, 0); // we create a state here
		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));

		// WE need to create the specific maps here since we have the index here 
		// and call search on it
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, agentIndex);
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
		initialStateForStorage = new InitialState();

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
		System.out.println("!!!!!!!!!!!!!!!!!Creating specific state for agent: " + agent);
		// Create a new state with only the appropriately colored agents
		// Find the color of the agent  
		Color agentColor = State.agentColors[agent];
		System.out.println("Agent color: " + agentColor);
		// Find everything from that color
		ArrayList<Integer> sameColoredAgents = new ArrayList<>(State.agentColors.length);
		// agents
		for (int i = 0; i < State.agentColors.length; i++) {
			 if (State.agentColors[i] == agentColor) {
				  sameColoredAgents.add(i);
			 }
		}
		// boxes
		ArrayList<Integer> sameColoredBoxes = new ArrayList<>(State.boxColors.length);
		for (int i = 0; i < State.boxColors.length; i++) {
			 if (State.boxColors[i] == agentColor) {
				  sameColoredBoxes.add(i);
			 }
		}

		System.out.println("================================ Index of agent: " + agent +",  Color of agent: " + agentColor+ ", Same colored agents: " + sameColoredAgents + ",  Same colored boxes: " + sameColoredBoxes);

      InitialStateObject initialStateObject = initialStateForStorage.getInitialState();


		// TODO: Initialize these variables
		int[] newAgentRows = new int[sameColoredAgents.size() ];
		int[] newAgentCols =  new int[sameColoredAgents.size() ];
		Color[] newAgentColors = new Color[sameColoredAgents.size() ]; 
		// Color[] newAgentColors = { agentColor, agentColor}; //FIX this
		boolean[][] newWalls = initialStateObject.wallsIntial ;
		char[][] newBoxes = initialStateObject.boxesInitial; ;
		Color[] newBoxColors = initialStateObject.boxColors ;
		char[][] newGoals = initialStateObject.goals ;

		// Set the agent details
		newAgentRows = setAgentRows(initialStateObject, sameColoredAgents);
		newAgentCols = setAgentCols(initialStateObject, sameColoredAgents);
		newAgentColors = setAgentColors(initialStateObject, sameColoredAgents);

		System.out.println("newAgentRows: "+ Arrays.toString(newAgentRows));

		// TODO set box details
		// TODO set extra walls
		setExtraWalls(initialStateObject, sameColoredAgents, newWalls);



		// TODO: create new state, for this new spcificState class is rqeuired WITHOUT THE STATIC fields!
		// Create a new state with only the appropriately colored elements
		State newState = new State(newAgentRows, newAgentCols, newAgentColors, newWalls, newBoxes, newBoxColors, newGoals);

		System.out.println("========================================================================");
		System.out.println(newState.toString() );

		System.out.println("========================================================================");
		// State newState = cbsNode.state;

		return newState;
	}

	// TODO set Box details

	private int[] setAgentRows(InitialStateObject initialStateObject, ArrayList agentIndexes  ) {
		int [] agentRowsFilled = new int[agentIndexes.size() ];
		// debug

		System.out.println("length of the same colored agents: "+ agentIndexes.size() );
		for (int i = 0; i < agentIndexes.size(); i++) {
			// debug
			// System.out.println("agentRowsInitial.get(i): "+i+": " + initialStateObject.agentRowsInitial[i]);
			agentRowsFilled[i] = initialStateObject.agentRowsInitial[i];
		}
		return agentRowsFilled;
	}

	private int[] setAgentCols(InitialStateObject initialStateObject, ArrayList agentIndexes) {
		int [] agentColsFilled = new int[agentIndexes.size() ];
		for (int i = 0; i < agentIndexes.size(); i++) {
			// debug
			System.out.println("agentColsInitial.get(i): "+i+": " + initialStateObject.agentColsInitial[i]);

			agentColsFilled[i] = initialStateObject.agentColsInitial[i];
		}
		return agentColsFilled;
	}

	private Color[] setAgentColors(InitialStateObject initialStateObject, ArrayList agentIndexes ) {
		Color [] agentColorsFilled = new Color[agentIndexes.size() ];
		for (int i = 0; i < agentIndexes.size(); i++) {
			// debug
			System.out.println("agentColors.get(i): "+i+": " + initialStateObject.agentColors[i]);
			agentColorsFilled[i] = initialStateObject.agentColors[i];
		}
		return agentColorsFilled;
	}


	private void setExtraWalls(InitialStateObject initialStateObject, ArrayList agentIndexes, boolean[][] newWalls  ) {
		// for (int i = 0; i < agentIndexes.size(); i++) {
		// 	System.out.println("agentIndexes: "+ agentIndexes.get(i));
		// }
		

		for (int i = 1; i < initialStateObject.agentColsInitial.length+1; i++) {
			// if not agentIndexes  have i set it as aa wall
			if ( !agentIndexes.contains(i-1) ) {
				// debug
				// System.out.println("coordinates: "+ initialStateObject.agentRowsInitial[i-1]+ "; "+ initialStateObject.agentColsInitial[i-1]);
				newWalls[initialStateObject.agentRowsInitial[i-1]] [initialStateObject.agentColsInitial[i-1]] = true;
			}
		}



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
