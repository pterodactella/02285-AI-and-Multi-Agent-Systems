package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;

import searchclient.Action;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.Logger;
import searchclient.State;

public class CBSNode {
	public State state;
	public HashSet<Constraint> constraints;
	public PlanStep[][] solution;
	public int[] costs;
	private int longestPath;
	public int totalCost;
	private int hash = 0;

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
//					System.err.println("The agents positions are: ")
//					if(agentsPositions[0] == -1 || agentsPositions[1] == -1)
//						continue;

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

	public void findIndividualPlan(int agentIndex, PlanStep[][] individualPlans) {

		ConstraintState constraintState = new ConstraintState(this.state, agentIndex, this.constraints, 0);

		
		
		//TODO: Instead of initializing the frontier again and again for evey agent, we need to modify so that it re-uses the same frontier. This will optimize a lot the run-speed.
		//HAVE THE FRONTIER AS A SINGLETON OR GLOBAL CLASS THAT WILL BE RE-USED IN CONSTRAINT GRAPHSEARCH AND HERE IN CBSNODE!!!
//		ConstraintFrontier frontier = GlobalExpandsQueue.getInstance().getQueue();
		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar());
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, agentIndex);
//		System.err.println("plan for agent " + agentIndex + " is: " + Arrays.toString(plan));

//		Logger logger = Logger.getInstance();
//		logger.log("^^^^^ .... ^^^^^");
//		logger.log("THESE ARE THE CONSTRAINTS: ");
//		for (Constraint constr: this.constraints) {
//			logger.log(constr.toString());
//		}
//		logger.log("^^^^^ .... ^^^^^");
//
//		logger.log("THE PLAN FOR: " + agentIndex);
//		for (PlanStep step : plan) {
//			logger.log("Step: " + step.toString());
//		}
//		logger.log("");
		if (plan != null && plan.length > this.longestPath) {
			this.longestPath = plan.length;
		}

		individualPlans[agentIndex] = plan;
		this.costs[agentIndex] = plan[plan.length - 1].timestamp;

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

//		System.err.println("WAS CALLED HASH CODE!");
//		return 1;
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
//			System.err.println("EQUALS!");
			return true;
		}
//		return this.constraints.equals(other.constraints) && this.solution.equals(other.solution);
		return false;
	}
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
