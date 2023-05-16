package searchclient.CBS;

import java.util.Arrays;

public class Constraint {
	public int agentIndex;
	public int locationX;
	public int locationY;
	public int timestamp;
	//TODO:Include boxes
	public Constraint(int agentIndex, int locationX, int locationY, int timestamp ) {
		this.agentIndex = agentIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;

	}
	
	public Constraint(Constraint copy) {
		this.agentIndex = copy.agentIndex;
		this.locationX = copy.locationX;
		this.locationY = copy.locationY;
		this.timestamp = copy.timestamp;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("agentIndex: " + agentIndex + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}
	
    @Override
    public int hashCode() {
    	final int prime = 101;
        int result = 1;
        result = result * prime + this.agentIndex;
        result = result * prime + this.locationX;
        result = result * prime + this.locationY;
        result = result * prime + this.timestamp;
        return result;
    }
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Constraint other = (Constraint) obj;
		return this.agentIndex == other.agentIndex &&
				this.locationX == other.locationX &&
				this.locationY == other.locationY &&
				this.timestamp == other.timestamp;
	}
	
}
