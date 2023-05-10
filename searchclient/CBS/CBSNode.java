package searchclient.CBS;

import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.PriorityQueue;

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

		ConstraintState constraintState = new ConstraintState(this.state, agentIndex, this.constraints, 0);

		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
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

	}

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
