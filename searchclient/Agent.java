package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Agent {
  // constraints are agent (ai , v, t)
  public String agentId;
  
	public State initialState;
  public int agentInitialCols;
  public int agentCols;
  public int agentRows;

  public int timeStep;
  public Constraints[] constraints;

  public Action[][] solution;
  public int cost;

  Agent(String agentId, State initialState, Constraints[] constraints, Frontier frontier, Action[][] solution, int cost) {
    // calculate the plan
    this.initialState = initialState;
    Action[][] plan = GraphSearch.search(initialState,  frontier);


    // calculate the constraints 
    
    //[15] [ Move(N,N), Move(N,N), Move(N,N), Move(N,N)]
    //we need the zero to be dynamic somehow briding between state and agent
    this.agentCols=initialState.agentCols[0]; //???????????
    this.agentRows=initialState.agentRows[0];
    
    for (Action a : plan[0]) {
      System.err.println(a);
      this.agentCols[index+1] += a.agentColDelta;
      this.agentRows += a.agentRowDelta;

  //would it be easier to calculate the agents final position in state class instead since the 
  //coordinates and movement are already calculated within the class

      
    }


    // save the object
    

    this.agentId = agentId;
    this.constraints = constraints;
    this.solution = solution;
    this.cost = cost;
  }


  

}
