package searchclient.CBS.Conflicts;

public class OrderedConflict implements GenericConflict {
	public int leaderIndex;
	public int followerIndex;
	public int forbiddenLocationX;
	public int forbiddenLocationY;
	public int timestamp;

	// TODO:Include boxes
	public OrderedConflict(int leaderIndex, int followerIndex, int forbiddenLocationX, int forbiddenLocationY,
			int timestamp) {

		this.leaderIndex = leaderIndex;
		this.followerIndex = followerIndex;
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
		s.append("follower: " + followerIndex + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}

}