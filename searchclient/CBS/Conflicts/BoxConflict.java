package searchclient.CBS.Conflicts;

public class BoxConflict implements GenericConflict {
	public int[] agentIndexes;
	public int boxLocationX;
	public int boxLocationY;
	public int timestamp;

	// TODO:Include boxes
	public BoxConflict(int agentAIndex, int agentBIndex, int boxLocationX, int boxLocationY, int timestamp) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.boxLocationX = boxLocationX;
		this.boxLocationY = boxLocationY;
		this.timestamp = timestamp;

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("boxLocationX: " + boxLocationX + "; ");
		s.append("boxLocationY: " + boxLocationY + "; ");
		s.append("agentB: " + agentIndexes[1] + "; ");
		s.append("agentA: " + agentIndexes[0] + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}

}
