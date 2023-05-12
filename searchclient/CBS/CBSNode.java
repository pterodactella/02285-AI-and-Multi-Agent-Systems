package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;

import searchclient.Action;
import searchclient.ActionType;
import searchclient.Color;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
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

	public Conflict findFirstConflict() {
		int[] agentsPositions = null;
		for (int i = 1; i < this.longestPath; i++) {
			for (int j = 0; j < this.solution[i].length; j++) {
				if (this.solution[i][j].locationX == -1)
					continue;
				for (int k = j + 1; k < this.solution[i].length; k++) {
					if (this.solution[i][k].locationX == -1)
						continue;
					agentsPositions = new int[] { this.solution[i][j].locationX, this.solution[i][j].locationY,
							this.solution[i][k].locationX, this.solution[i][k].locationY };
					// System.err.println("The agents positions are: ")
					// if(agentsPositions[0] == -1 || agentsPositions[1] == -1)
					// continue;
					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3]) {
						return new Conflict(j, k, agentsPositions[0], agentsPositions[1], i);
					}
				}
			}
		}

		// TODO: findConflct
		return null;

	}

	public Conflict findConflict(PlanStep[] plan1, PlanStep[] plan2, int agentIndex1, int agentIndex2) {
		for (int i = 0; i < plan1.length; i++) {
			for (int j = 0; j < plan2.length; j++) {
				PlanStep step1 = plan1[i];
				PlanStep step2 = plan2[j];

				// Check if agents occupy the same location at the same timestep
				if (step1.timestamp == step2.timestamp && step1.locationX == step2.locationX
						&& step1.locationY == step2.locationY) {
					return new Conflict(agentIndex1, agentIndex2, step1.locationX, step1.locationY, step1.timestamp);
				}

				// Check if agent 1 is moving to the location occupied by agent 2
				if (step1.timestamp + 1 == step2.timestamp && step1.locationX == step2.locationX
						&& step1.locationY == step2.locationY) {

					return new Conflict(agentIndex1, agentIndex2, step1.locationX, step1.locationY,
							step1.timestamp + 1);
				}

				// Check if agent 2 is moving to the location occupied by agent 1
				if (step2.timestamp + 1 == step1.timestamp && step2.locationX == step1.locationX
						&& step2.locationY == step1.locationY) {

					return new Conflict(agentIndex1, agentIndex2, step2.locationX, step2.locationY,
							step2.timestamp + 1);
				}
			}
		}

		return null; // No conflict found
	}

	public void findIndividualPlan(int agentIndex, PlanStep[][] individualPlans) {

		ConstraintState constraintState = new ConstraintState(this.state, agentIndex, this.constraints, 0);

		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, agentIndex);
		// System.err.println("plan for agent " + agentIndex + " is: " +
		// Arrays.toString(plan));
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
		int numberOfAgents = this.state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
		System.err.println("NUMBER OF AGENTS" + numberOfAgents);

		for (int i = 0; i < numberOfAgents; i++) {

			findIndividualPlan(i, individualPlans);

			// TODO: Add search with constraint

		}
		int agentToDelay = -1;
		int delay = 0;
		boolean hasConflicts = true;
		while (hasConflicts) {
			hasConflicts = false;
			for (int i = 0; i < numberOfAgents; i++) {
				for (int j = i + 1; j < numberOfAgents; j++) {
					Conflict conflict = findConflict(individualPlans[i], individualPlans[j], i, j);
					if (conflict != null) {
						int[] delayInfo = calculateDelay(conflict, individualPlans);
						agentToDelay = delayInfo[0];
						delay = delayInfo[1];
						if (delay > 0) {
							individualPlans[agentToDelay] = introduceDelay(individualPlans[agentToDelay], agentToDelay,
									delay, conflict.timestamp);
							this.costs[agentToDelay] = individualPlans[agentToDelay].length - 1;
							System.err.println("individual agent plan after delay " + individualPlans[agentToDelay]);
							hasConflicts = true; // Set to true to recheck the conflicts after the delay is introduced
							break; // Break the inner loop to restart conflict checking from the beginning
						}
					}
				}
				if (hasConflicts) {
					break; // Break the outer loop to restart conflict checking from the beginning
				}
			}
		}
		this.solution = PlanStep.mergePlans(individualPlans); // Update the solution field
		return this.solution;
	}

	private int[] calculateDelay(Conflict conflict, PlanStep[][] individualPlans) {
		int agentIndex1 = conflict.agentIndexes[0];
		int agentIndex2 = conflict.agentIndexes[1];

		// Determine the agent with lower priority
		int agentToDelay = Math.max(agentIndex1, agentIndex2);

		// Calculate the delay based on backtracking and reaching the goal state
		int backtrackSteps = individualPlans[agentToDelay].length - conflict.timestamp;

		Action agentToDelayAction = individualPlans[agentToDelay][backtrackSteps].action;
		Action otherAgentAction = individualPlans[agentIndex1 == agentToDelay ? agentIndex2
				: agentIndex1][backtrackSteps].action;

		// Check if delaying the agent will cause it to block the other agent
		if (agentToDelayAction.type == ActionType.Move && otherAgentAction.type == ActionType.Move) {
			int agentLocationX = individualPlans[agentToDelay][backtrackSteps].locationX;
			int agentLocationY = individualPlans[agentToDelay][backtrackSteps].locationY;
			int otherAgentLocationX = individualPlans[agentIndex1 == agentToDelay ? agentIndex2
					: agentIndex1][backtrackSteps].locationX;
			int otherAgentLocationY = individualPlans[agentIndex1 == agentToDelay ? agentIndex2
					: agentIndex1][backtrackSteps].locationY;

			int deltaX = agentLocationX - otherAgentLocationX;
			int deltaY = agentLocationY - otherAgentLocationY;

			if (Math.abs(deltaX) <= 1 && Math.abs(deltaY) <= 1) {
				// Agents are in adjacent cells or diagonal cells
				int delay = 0;
				if (Math.abs(deltaX) == 1 && Math.abs(deltaY) == 1) {
					// Agents are in diagonal cells
					delay = 1;
				} else if (Math.abs(deltaX) == 1) {
					// Agents are moving horizontally
					delay = (int) Math
							.ceil(Math.abs(agentToDelayAction.agentColDelta + otherAgentAction.agentColDelta) / 2.0);
					System.err.println("Delay: " + delay);

				} else if (Math.abs(deltaY) == 1) {
					// Agents are moving vertically
					delay = (int) Math
							.ceil(Math.abs(agentToDelayAction.agentRowDelta + otherAgentAction.agentRowDelta) / 2.0);
					System.err.println("Delay: " + delay);
				}
				System.err.println("Delay: " + delay);
				return new int[] { agentToDelay, delay };
			}
		}

		return new int[] { agentToDelay, 0 }; // No delay
	}

	private PlanStep[] introduceDelay(PlanStep[] plan, int agentToDelay, int delay, int ConflictTimestamp) {
		// Introduce a delay for the agent with lower priority by extending its plan
		// with NoOps
		System.err.println("Introducing delay " + delay + " for agent: " + agentToDelay);

		int numSteps = plan.length + delay;
		PlanStep[] delayedPlan = new PlanStep[numSteps];

		// Find the index where the delay should be introduced
		int delayIndex = ConflictTimestamp;

		// Copy actions before the delay
		System.arraycopy(plan, 0, delayedPlan, 0, delayIndex);

		// Insert NoOps at the delay index
		for (int i = 0; i < delay; i++) {
			delayedPlan[delayIndex + i] = new PlanStep(Action.NoOp, plan[delayIndex - 1].locationX,
					plan[delayIndex - 1].locationY, delayIndex + i);
		}

		// Copy actions after the delay
		System.arraycopy(plan, delayIndex, delayedPlan, delayIndex + delay, plan.length - delayIndex);

		// Print the delayed plan
		System.err.println("Delayed plan for agent " + agentToDelay + ": ");
		for (PlanStep step : delayedPlan) {
			System.err.println("Step: " + step.toString());
		}

		return delayedPlan;
	}

	private void printDelayedPlans(PlanStep[][] delayedPlans) {
		System.err.println("Delayeeeeeeeeeeeddddddd plans:");
		for (int i = 0; i < delayedPlans.length; i++) {
			System.err.print("Agent " + i + ": ");
			for (int j = 0; j < delayedPlans[i].length; j++) {
				System.err.print(delayedPlans[i][j].toString() + ", ");
			}
			System.err.println();
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
