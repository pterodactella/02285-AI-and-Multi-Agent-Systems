package searchclient.CBS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import searchclient.Action;
import searchclient.ActionType;
import searchclient.Color;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.Logger;
import searchclient.State;
import searchclient.Color;

public class CBSNode {
	public State state;
	public HashSet<Constraint> constraints;
	public PlanStep[][] solution;
	public int[] costs;
	private int longestPath;
	public int totalCost;
	private InitialState initialStateForStorage;
	private ArrayList<Integer> shiftedAgents;
	private int hash = 0;
	private ConstraintFrontier frontier;
	private int numConflicts;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new HashSet<>();
		this.solution = null;
		this.costs = new int[state.agentRows.length];
		this.totalCost = 0;
	}

	public CBSNode(CBSNode parent) {
		this.state = parent.state;
		this.constraints = new HashSet<>();
		for (Constraint constr : parent.constraints) {
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

	public int findConflicts() {
		ArrayList<GenericConflict> conflicts = new ArrayList<>();
		int[] agentsPositions = null;
		for (int i = 1; i <= this.longestPath; i++) {
			for (int j = 0; j < this.solution[i].length; j++) {
				if (this.solution[i][j].locationX == -1)
					continue;
				for (int k = j + 1; k < this.solution[i].length; k++) {
					if (this.solution[i][k].locationX == -1)
						continue;
					agentsPositions = new int[] {
							/* [0] : */ this.solution[i][j].locationX,
							/* [1] : */ this.solution[i][j].locationY,
							/* [2] : */ this.solution[i][k].locationX,
							/* [3] : */ this.solution[i][k].locationY,
							/* [4] : */ this.solution[i][j].originalX,
							/* [5] : */ this.solution[i][j].originalY,
							/* [6] : */ this.solution[i][k].originalX,
							/* [7] : */ this.solution[i][k].originalY
					};

					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3]) {
						conflicts.add(new Conflict(j, k, agentsPositions[0], agentsPositions[1], i));
					}
					if (agentsPositions[2] == agentsPositions[4] && agentsPositions[3] == agentsPositions[5]) {
						conflicts.add(new OrderedConflict(j, k, agentsPositions[4], agentsPositions[5], i));
					}
					if (agentsPositions[0] == agentsPositions[6] && agentsPositions[1] == agentsPositions[7]) {
						conflicts.add(new OrderedConflict(k, j, agentsPositions[6], agentsPositions[7], i));
					}
				}
			}
		}
		return conflicts.size();
	}

	public GenericConflict findFirstConflict() {
		int[] agentsPositions = null;
		for (int i = 1; i <= this.longestPath; i++) {
			for (int j = 0; j < this.solution[i].length; j++) {
				if (this.solution[i][j].locationX == -1)
					continue;
				for (int k = j + 1; k < this.solution[i].length; k++) {
					if (this.solution[i][k].locationX == -1)
						continue;
					agentsPositions = new int[] { /* [0] : */ this.solution[i][j].locationX,
							/* [1] : */ this.solution[i][j].locationY, /* [2] : */ this.solution[i][k].locationX,
							/* [3] : */ this.solution[i][k].locationY, /* [4] : */ this.solution[i][j].originalX,
							/* [5] : */ this.solution[i][j].originalY, /* [6] : */ this.solution[i][k].originalX,
							/* [7] : */ this.solution[i][k].originalY };
					// System.err.println("The agents positions are: ")
					// if(agentsPositions[0] == -1 || agentsPositions[1] == -1)
					// continue;

					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3]) {
						return new Conflict(j, k, agentsPositions[0], agentsPositions[1], i);
					}
					if (agentsPositions[2] == agentsPositions[4] && agentsPositions[3] == agentsPositions[5]) {
						return new OrderedConflict(j, k, agentsPositions[4], agentsPositions[5], i);
					}
					if (agentsPositions[0] == agentsPositions[6] && agentsPositions[1] == agentsPositions[7]) {
						return new OrderedConflict(k, j, agentsPositions[6], agentsPositions[7], i);
					}

				}
			}
		}

		// TODO: findConflct
		return null;

	}


	public PlanStep[] findIndividualPlan(int agentIndex) {

		// TODO: Instead of initializing the frontier again and again for evey agent, we
		// need to modify so that it re-uses the same frontier. This will optimize a lot
		// the run-speed.
		// HAVE THE FRONTIER AS A SINGLETON OR GLOBAL CLASS THAT WILL BE RE-USED IN
		// CONSTRAINT GRAPHSEARCH AND HERE IN CBSNODE!!!
		// ConstraintFrontier frontier = GlobalExpandsQueue.getInstance().getQueue();
		
		ConstraintState n = new ConstraintState(this.state, agentIndex, this.constraints, 0);

		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar());
		frontier.add(n);
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, agentIndex);

		if (plan != null && plan.length > this.longestPath) {
			this.longestPath = plan.length;
		}

		individualPlans[agentIndex] = plan;
		this.costs[agentIndex] = plan[plan.length - 1].timestamp;

	}

	public void setNewIndividualPlanForAgent(int agentIndex) {
		PlanStep[] plan = calculateIndividualPlanForAgent(agentIndex);
		this.costs[agentIndex] = plan[plan.length - 1].timestamp;

		if (this.longestPath > plan.length) {
			// just copy it there
			for (int timestamp = 0; timestamp < this.longestPath; timestamp++) {
				if (timestamp < plan.length) {
					this.solution[timestamp][agentIndex] = plan[timestamp];
				} else {
					this.solution[timestamp][agentIndex] = new PlanStep(Action.NoOp, this.solution[timestamp-1][agentIndex].locationX, this.solution[timestamp-1][agentIndex].locationY, timestamp, this.solution[timestamp-1][agentIndex].locationX, this.solution[timestamp-1][agentIndex].locationY );
				}

			}

		} else {
			int prevLength = this.solution.length;
			this.longestPath = plan.length;
			
			// SOLUTION: [timestamp][agentIndex]
			PlanStep[][] newSolution = new PlanStep[this.longestPath][this.solution[0].length];

			// The 0th actions are all NoOps
			for (int agentInd = 0; agentInd < this.solution[0].length; agentInd++) { // for each agent
					newSolution[0][agentInd] = new PlanStep(Action.NoOp, -1, -1, 0, -1, -1);
			}

			// The real action set starts from 1st timestamp
			for (int timestamp = 0; timestamp < this.longestPath; timestamp++) { // for each timestamp
				if (timestamp < prevLength  ) {
					for (int agentInd = 0; agentInd < this.solution[0].length; agentInd++) { // for each agent
						if (agentInd == agentIndex) { // if it is the agent we are calculating the plan for
							newSolution[timestamp][agentInd] = plan[timestamp];
						} else { // if it is one of the other agents
							newSolution[timestamp][agentInd] = this.solution[timestamp][agentInd];
						}
					}
				} else {
					for (int agentInd = 0; agentInd < this.solution[0].length; agentInd++) { // for each agent
						if (agentInd == agentIndex) { // if it is the agent we are calculating the plan for
							newSolution[timestamp][agentInd] = plan[timestamp];
						} else { // if it is one of the other agents
							newSolution[timestamp][agentInd] = new PlanStep(Action.NoOp, newSolution[timestamp-1][agentIndex].locationX, newSolution[timestamp-1][agentIndex].locationY, timestamp, newSolution[timestamp-1][agentIndex].locationX, newSolution[timestamp-1][agentIndex].locationY );
						}
					}
				}
			}

			this.solution = newSolution;
		}

		

	}

	private PlanStep[]  calculateIndividualPlanForAgent (int agentIndex) {

		// Construct a state with the constructor that takes arguments
		State searchSpecificState = createSpecificState(agentIndex);
		System.out.println("Specific State: agentsLength: "+ searchSpecificState.agentRows.length + " boxesLength: " + searchSpecificState.boxes.length + " goalsLength: " + searchSpecificState.goals.length    );
		this.state = searchSpecificState;

		// Calculate teh shifted agent index that matches the real one in the searchSpecificState
		int shiftedAgentIndex = this.shiftedAgents.indexOf(agentIndex);

		ConstraintState constraintState = new ConstraintState(searchSpecificState, shiftedAgentIndex, this.constraints, 0); // we create a state here
		// ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));

		// // we need to create the specific maps here since we have the index here 
		// // and call search on it
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, shiftedAgentIndex);

		return plan;
	}


	public PlanStep[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
//		System.err.println("NUMBER OF AGENTS" + numberOfAgents);

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

	// TAMAS STUFF

	public int sumCosts() {
		int sum = 0;
		for (int i = 0; i < this.costs.length; i++) {
			sum += this.costs[i];
		}
		return sum;
	}

	@Override
	public int hashCode() {
		if (this.hash == 0) {
			final int prime = 43;
			int result = 1;
			result = prime * result + this.constraints.hashCode();
			result = prime * result + Arrays.deepHashCode(this.solution);

			this.hash = result;
		}
		return this.hash;

		// System.err.println("WAS CALLED HASH CODE!");
		// return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		CBSNode other = (CBSNode) obj;
		if (this.constraints.equals(other.constraints) && Arrays.deepEquals(this.solution, other.solution)) {
			// System.err.println("EQUALS!");
			return true;
		}
		// return this.constraints.equals(other.constraints) &&
		// this.solution.equals(other.solution);
		return false;
	}


class Conflict implements GenericConflict {
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
}
