import java.util.ArrayList;

// Yes, you're right. The Agent class in your code does act as nodes. However, the current implementation of the Agent class might not be ideal for the CBS algorithm, as it tightly couples the agent's properties and the CBS node's properties.

// In the CBS algorithm, a node in the search tree represents a partial solution with a set of constraints. Each node has multiple agents, and each agent has its path, which satisfies the given constraints.

// A better approach would be to separate the Agent class from the node concept. The Agent class should store properties specific to the agent, such as its color, initial position, and possibly its constraints. The node class should store the partial solution, which includes the paths for all agents and the global constraints.

// Here's an example of how you could define a new CBSNode class:

public class CBSNode {
  private ArrayList<Agent> agents;
  private ArrayList<Constraints> globalConstraints;
  private Action[][] solution;
  private int cost;
  private State state;

  public CBSNode(State initialState) {
    this.state = initialState;
    this.agents = initialState.agents;
    this.globalConstraints = new ArrayList<>();
    this.cost = calculateCost();
  }

  public CBSNode(CBSNode parent, int agentId, int conflictX, int conflictY, int conflictTime) {
    this.state = parent.state;
    this.agents = parent.agents;
    this.globalConstraints = new ArrayList<>(parent.globalConstraints);
    this.globalConstraints.add(new Constraints(agentId, conflictX,conflictY, conflictTime));
    this.cost = calculateCost();
  }
  
public Conflict findFirstConflict() {
    // Check all pairs of agents for conflicts
    for (int i = 0; i < agents.size(); i++) {
        for (int j = i + 1; j < agents.size(); j++) {
            Agent a = agents.get(i);
            Agent b = agents.get(j);

            // Check if the agents' paths intersect at any time step
            for (int t = 0; t < solution[a.agentIndex].length; t++) {
                if (solution[a.agentIndex][t].equals(solution[b.agentIndex][t])) {
                    // Found a conflict between agents a and b at time step t
                    return new Conflict(a, b, a.row, a.col, solution[a.agentIndex][t], t);
                }
            }
        }
    }

    // No conflicts found
    return null;
}


  public Action[][] extractSolution() {
    // Implement your logic to extract the solution from the current node
    return null; // Replace with your implementation
  }

  public boolean isSolvable() {
    // Implement your logic to check if the current node is solvable
    return false; // Replace with your implementation
  }

  public int getCost() {
    return cost;
  }

  private int calculateCost() {
    // Implement your logic to calculate the cost of the current node
    return 0; // Replace with your implementation
  }

  // public CBSNode(State initialState) {

  // this.solution = GraphSearch.search(initialState, frontier);
  // this.cost = this.solution[0].length;
  // this.constraints[0] = new Constraints(Integer.parseInt(agentId,
  // this.agentCols[0], this.agentRows[0], 0);

  // for (int i = 1; i < solution[0].length; i++) {
  // Action a = solution[0][i];
  // System.err.println(a);
  // // calculate the positions of the agent given by timestamp i
  // this.agentCols[i] += a.agentColDelta;
  // this.agentRows[i] += a.agentRowDelta;

  // // calculate the constratins of the agent given by timestamp i
  // this.constraints[i] = new Constraints(Integer.parseInt(agentId),
  // this.agentCols[i], this.agentRows[i], i);
  // }
  // }
}