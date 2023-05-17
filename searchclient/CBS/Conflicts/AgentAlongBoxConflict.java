package searchclient.CBS.Conflicts;

public class AgentAlongBoxConflict implements GenericConflict {
	public int agentWithoutBoxIndex;
	public int agentWithoutBoxLocationX;
	public int agentWithoutBoxLocationY;
	public int agentWithBoxIndex;
	public int timestamp;

	// TODO:Include boxes
	public AgentAlongBoxConflict(int agentWithoutBoxIndex, int agentWithoutBoxLocationX, int agentWithoutBoxLocationY, int agentWithBoxIndex,
			int timestamp) {

		this.agentWithoutBoxIndex = agentWithoutBoxIndex;
		this.agentWithoutBoxLocationX = agentWithoutBoxLocationX;
		this.agentWithoutBoxLocationY = agentWithoutBoxLocationY;
		this.agentWithBoxIndex = agentWithBoxIndex;
		this.timestamp = timestamp;

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("agentWithoutBoxIndex: " + agentWithoutBoxIndex + "; ");
		s.append("agentWithoutBoxLocationX: " + agentWithoutBoxLocationX + "; ");
		s.append("agentWithoutBoxLocationY: " + agentWithoutBoxLocationY + "; ");
		s.append("agentWithBoxIndex: " + agentWithBoxIndex + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}
}
