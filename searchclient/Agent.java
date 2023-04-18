package searchclient;

import java.util.Arrays;

// import java.security.Timestamp;
// import java.sql.Time;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;

public class Agent {
  // constraints are agent (ai , v, t)
  public String agentId;
  public Color color;
  public int agentIndex;

  public int col;
  public int row;

  private Constraints[] constraints;

  // public int timeStep;
  Agent(int row, int col, Color color, int agentIndex) {
    this.color = color;
    this.agentIndex = agentIndex;

    this.col = col;
    this.row = row;

    this.constraints = new Constraints[0];
  }

  Agent(Color color, int row, int col, int agentIndex, Constraints addConstraint) {
    this(row, col, color, agentIndex);

    Constraints[] newConstraints = new Constraints[this.constraints.length + 1];
    System.arraycopy(this.constraints, 0, newConstraints, 0, this.constraints.length);
    newConstraints[newConstraints.length - 1] = addConstraint;
    this.constraints = newConstraints;
  }

  public Agent createChildAgent(Constraints newConstraint) {
    Agent childAgent = new Agent(agentId, row, col, color, agentIndex);
    childAgent.setConstraints(Arrays.copyOf(constraints, constraints.length + 1));
    childAgent.getConstraints()[constraints.length] = newConstraint;
    return childAgent;
  }

  public Constraints[] getConstraints() {
    return constraints;
  }

  public void setConstraints(Constraints[] constraints) {
    this.constraints = constraints;
  }

  public int[] toArray() {
    return new int[] { this.agentIndex, this.row, this.col };
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Agent ");
    sb.append(this.agentId);
    sb.append(" (");
    sb.append(this.color.toString());
    sb.append(") at (");
    sb.append(this.row);
    sb.append(",");
    sb.append(this.col);
    sb.append(") with constraints: ");
    sb.append(Arrays.toString(this.constraints));
    return sb.toString();
  }
  public Agent copy() {
    return new Agent(this.agentId, this.row, this.col, this.color, this.agentIndex);
  }
}
