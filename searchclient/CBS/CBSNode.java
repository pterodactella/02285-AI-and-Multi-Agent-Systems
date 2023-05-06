package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import searchclient.Action;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.State;


public class CBSNode {
	public State state;
	public ArrayList<Constraint> constraints;
	public Action[][] solution;
	public int cost;
	private int longestPath;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new ArrayList<>();
		this.solution = null;
		this.cost = 0;
	}
	public Conflict findFirstConflict() {
		for (int i = 0; i < this.longestPath; i++) {
			for (int j = 0; j < this.solution.length; j++) {
				for (int k = j; k < this.solution.length; k++) {
					Action action1 = (i < this.solution[j].length) ? this.solution[j][i]
							: this.solution[j][this.solution[j].length - 1];
					Action action2 = (i < this.solution[k].length) ? this.solution[k][i]
							: this.solution[k][this.solution[k].length - 1];
					if (action1 != Action.NoOp && action1 != Action.NoOp
					&& (action1.agentRowDelta != 0
                    || action1.agentColDelta != 0 || action2.agentRowDelta != 0
                    || action2.agentColDelta != 0)) {
						int agent1Row = this.state.agentRows[j];
						int agent1Col = this.state.agentCols[j];
						return new Conflict(j, k, agent1Row + action1.agentRowDelta, agent1Col + action1.agentColDelta, i);

					}
				}
			}
		}
		// TODO: findConflct
		return null;
	}
	

	public State createStateForAgent(int agentIndex) {
		// Create new goals array for the specific agent only
		char[][] newGoals = new char[state.goals.length][state.goals[0].length];
		for (int i = 0; i < newGoals.length; i++) {
			if (i == agentIndex) {
				newGoals[i] = state.goals[i].clone(); // Use the original goals for the specific agent
			} else {
				newGoals[i] = new char[state.goals[i].length];
				Arrays.fill(newGoals[i], '.'); // Set all other agents' goals to empty
			}
		}
	
		// Create new state with updated goals for the specific agent
		return new State(state, agentIndex, newGoals, this.constraints, solution[agentIndex]);
	}
	

	public Action[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		Action[][] individualPlans = new Action[numberOfAgents][];

		for (int i = 0; i < numberOfAgents; i++) {

			State stateForAgent = createStateForAgent(i);
			Frontier frontier = new FrontierBestFirst(new HeuristicAStar(stateForAgent));
			Action[] plan = GraphSearch.search(stateForAgent, frontier)[0];
			if (plan.length > this.longestPath) {
				this.longestPath = plan.length;
			}
			individualPlans[i] = plan;

			// TODO: Add search with constraint
			ArrayList<Constraint> constraints = new ArrayList<>();
			for (int j = 0; j < i; j++) {
				Constraint constraint = new Constraint(j, individualPlans[j]);
				constraints.add(constraint);
			}
			frontier = new FrontierBestFirst(new HeuristicAStar(stateForAgent));
			plan = GraphSearch.search(stateForAgent, frontier)[0];
			individualPlans[i] = plan;

		}

		return individualPlans;
	}

	public int sumCosts() {
		int sum = 0;
		for (int i = 0; i < this.solution.length; i++) {
			sum += this.solution[i].length;
		}
		return sum;
	}
}

class Conflict {
	public int[] agentIndexes;
	public int locationX;
	public int locationY;
	public int timestamp;
	public int[] boxIndexes;

	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.boxIndexes = new int[2];
	}
	// TODO:Include boxes
	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp, int boxAIndex,
			int boxBIndex) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.boxIndexes = new int[2];
		this.boxIndexes[0] = boxAIndex;
		this.boxIndexes[1] = boxBIndex;
	}

}

class FrontierBestFirst implements Frontier {
	private Heuristic heuristic;
	private final PriorityQueue<State> priorityQueue;
	private final HashSet<State> set;

	public FrontierBestFirst(Heuristic h) {
		this.heuristic = h;
		this.priorityQueue = new PriorityQueue<State>(this.heuristic);
		this.set = new HashSet<State>(65536);
	}

	@Override
	public void add(State state) {
		this.priorityQueue.add(state);
		this.set.add(state);
	}

	@Override
	public State pop() {
		State state = this.priorityQueue.poll();
		this.set.remove(state);
		return state;
	}

	@Override
	public boolean isEmpty() {
		return this.priorityQueue.isEmpty();
	}

	@Override
	public int size() {
		return this.priorityQueue.size();
	}

	@Override
	public boolean contains(State state) {
		return this.set.contains(state);
	}

	@Override
	public String getName() {
		return String.format("best-first search using %s", this.heuristic.toString());
	}
}

class HeuristicAStar extends Heuristic {
	public HeuristicAStar(State initialState) {
		super(initialState);
	}

	@Override
	public int f(State s) {
		return s.g() + this.h(s);
	}

	@Override
	public String toString() {
		return "A* evaluation";
	}
}