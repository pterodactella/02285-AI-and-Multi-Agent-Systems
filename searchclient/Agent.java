package searchclient;

import java.util.Arrays;

// import java.util.Arrays;

// import java.security.Timestamp;
// import java.sql.Time;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;

public class Agent implements Comparable<Agent>{
  // constraints are agent (ai , v, t)
  public String agentId;
  public State initialState;
  public Color agentColor;
  public int agentIndex;

  public int[] agentCols;
  public int[] agentRows;
  public Constraints[] constraints;

  public Action[][] solution;
  public int cost;
  public int timestamp;


  Agent(String agentId, Color color, State initialState, Frontier frontier, int agentIndex) {
    // calculate the plan
    this.agentColor = color;
    this.agentId = agentId;
    this.initialState = initialState;
    this.agentIndex = agentIndex;

    this.solution = GraphSearch.search(initialState, frontier);
    this.cost = this.solution[0].length;
    this.constraints = new Constraints[solution[0].length];
    this.timestamp = 0;
    // calculate the positions
    // [15] [ Move(N,N), Move(N,N), Move(N,N), Move(N,N)]
    this.agentCols = Arrays.copyOf(initialState.agentCols, initialState.agentCols.length);
    this.agentRows = Arrays.copyOf(initialState.agentRows, initialState.agentRows.length);

    // Calculate the constrain for the 0th timestamp
    this.constraints[0] = new Constraints(agentIndex, this.agentCols[0], this.agentRows[0], 0);

    for (int i = 1; i < solution[0].length; i++) {
      Action a = solution[0][i];
      System.err.println(a);
      // calculate the positions of the agent given by timestamp i
      this.agentCols[i] += a.agentColDelta;
      this.agentRows[i] += a.agentRowDelta;

      // calculate the constratins of the agent given by timestamp i
      this.constraints[i]=new Constraints(agentIndex, this.agentCols[i], this.agentRows[i], i);
    }
  }

  // Constructor to solve the conflicts. It initialises the global constraints in the state
  Agent(String agentId, Color color, State initialState, Frontier frontier, int agentIndex, Constraints[] newConstraints) {
    // calculate the plan
    this.agentColor = color;
    this.agentId = agentId;
    this.initialState = initialState;
    this.agentIndex = agentIndex;
    

    // TODO: here we need to handle the extra, addConstraint
    // Create a new state with the global constraints and confli
    // State parent, Action[] jointAction, Constraints[] newConstraint

    this.solution = GraphSearch.search(new State(initialState, newConstraints), frontier);
    this.cost = this.solution[0].length;
    this.constraints = new Constraints[solution[0].length];
    this.timestamp = 0;


    // calculate the positions
    // [15] [ Move(N,N), Move(N,N), Move(N,N), Move(N,N)]
    this.agentCols = new int[initialState.agentCols.length];
    this.agentRows = new int[initialState.agentRows.length];
    

    // Calculate the constrain for the 0th timestamp
    this.constraints[0] = new Constraints(agentIndex, this.agentCols[0], this.agentRows[0], 0);

    for (int i = 1; i < solution[0].length; i++) {
      Action a = solution[0][i];
      System.err.println(a);
      // calculate the positions of the agent given by timestamp i
      this.agentCols[i] += a.agentColDelta;
      this.agentRows[i] += a.agentRowDelta;

      // calculate the constratins of the agent given by timestamp i
      this.constraints[i]=new Constraints(agentIndex, this.agentCols[i], this.agentRows[i], i);
    }

    // TODO: Do we need this here?
    // Constraints[] newConstraints = new Constraints[this.constraints.length + 1];
    // System.arraycopy(this.constraints, 0, newConstraints, 0, this.constraints.length);
    // newConstraints[newConstraints.length - 1] = addConstraint;
    // this.constraints = newConstraints;




  }



  @Override
  public int compareTo(Agent other) {
      return Integer.compare(this.cost, other.cost);
  }



}
