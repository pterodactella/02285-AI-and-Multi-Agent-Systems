package searchclient.CBS.Conflicts;

public class BoxOrderedConflict implements GenericConflict {
	public int leaderIndex;
	public int followerWithBoxIndex;
	public int forbiddenLocationX;
	public int forbiddenLocationY;
	public int timestamp;

	// TODO:Include boxes
	public BoxOrderedConflict(int leaderIndex, int followerWithBoxIndex, int forbiddenLocationX, int forbiddenLocationY,
			int timestamp) {

		this.leaderIndex = leaderIndex;
		this.followerWithBoxIndex = followerWithBoxIndex;
		this.forbiddenLocationX = forbiddenLocationX;
		this.forbiddenLocationY = forbiddenLocationY;
		this.timestamp = timestamp;

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("forbiddenLocationX: " + forbiddenLocationX + "; ");
		s.append("forbiddenLocationY: " + forbiddenLocationY + "; ");
		s.append("leader: " + leaderIndex + "; ");
		s.append("followerWithBoxIndex: " + followerWithBoxIndex + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}
}
