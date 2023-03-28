package searchclient;

// import java.security.Timestamp;
// import java.sql.Time;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;

public class Agent {
  // constraints are agent (ai , v, t)
  public String agentId;
  public State initialState;
  
  public int[] agentCols;
  public int[] agentRows;
  public Constraints[] constraints;

  public Action[][] solution;
  public int cost;
  // public int timeStep;


  Agent(String agentId, State initialState, Frontier frontier) {
    // calculate the plan
    this.agentId = agentId;
    this.initialState = initialState;
    this.solution = GraphSearch.search(initialState, frontier);
    this.cost = this.solution[0].length;

    // calculate the positions
    // [15] [ Move(N,N), Move(N,N), Move(N,N), Move(N,N)]
    // we need the zero to be dynamic somehow bridging between state and agent, TODO
    this.agentCols = initialState.agentCols; // ???????????
    this.agentRows = initialState.agentRows;

    // Calculate the constrain for the 0th timestamp
    constraints[0]=new Constraints(agentId, this.agentCols[0], this.agentRows[0], 0);

    for (int i = 1; i < solution[0].length; i++) {
      Action a = solution[0][i];
      System.err.println(a);
      // calculate the positions of the agent given by timestamp i
      this.agentCols[i] += a.agentColDelta;
      this.agentRows[i] += a.agentRowDelta;

      // calculate the constratins of the agent given by timestamp i
      constraints[i]=new Constraints(agentId, this.agentCols[i], this.agentRows[i], i);
    }
  }

}
