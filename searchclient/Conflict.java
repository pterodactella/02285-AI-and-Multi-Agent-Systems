import java.util.ArrayList;

public class Conflict {
  public Agent agent1;
  public Agent agent2;
  public Action action;
  public int col;
  public int row;
  public int time;

  public Conflict(Agent agent1, Agent agent2, int row, int col, Action action, int time) {
    this.agent1 = agent1;
    this.agent2 = agent2;
    this.col = col;
    this.row= row;
    this.action = action;
    this.time = time;
  }
  public Agent[] getInvolvedAgents() {
      return new Agent[] {agent1,agent2};
    }

  @Override
  public String toString() {
    return "Conflict between agent " + agent1.agentId + " and agent " + agent2.agentId + " at position " + row + ", " + col
        + " at time " + time;
  }
}
