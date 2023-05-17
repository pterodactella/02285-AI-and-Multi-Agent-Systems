package searchclient.CBS.Conflicts;

public class AgentInBoxConflict implements GenericConflict {
	public int agentIndex;
	public int forbiddenLocationX;
	public int forbiddenLocationY;
	public int timestamp;

	// TODO:Include boxes
	public AgentInBoxConflict(int agentIndex, int forbiddenLocationX, int forbiddenLocationY,
			int timestamp) {
		this.agentIndex = agentIndex;
		this.forbiddenLocationX = forbiddenLocationX;
		this.forbiddenLocationY = forbiddenLocationY;
		this.timestamp = timestamp;

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("agentIndex: " + agentIndex + "; ");
		s.append("forbiddenLocationX: " + forbiddenLocationX + "; ");
		s.append("forbiddenLocationY: " + forbiddenLocationY + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}

}
