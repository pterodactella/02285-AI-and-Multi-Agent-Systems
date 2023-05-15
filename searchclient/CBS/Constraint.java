package searchclient.CBS;

import java.util.Arrays;

import searchclient.Color;

public class Constraint {
	public int agentIndex;
	public int locationX;
	public int locationY;
	public int timestamp;
	public char box; // The box associated with the constraint
	public Color boxColor; // The color of the box

	// TODO:Include boxes
	public Constraint(int agentIndex, int locationX, int locationY, int timestamp) {
		this.agentIndex = agentIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;

	}

	public Constraint(int locationX, int locationY, int timestamp, char box, Color boxColor) {
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.box = box;
		this.boxColor = boxColor;
	}

	public Constraint(Constraint copy) {
		this.agentIndex = copy.agentIndex;
		this.locationX = copy.locationX;
		this.locationY = copy.locationY;
		this.timestamp = copy.timestamp;
		this.box = copy.box;

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("agentIndex: " + agentIndex + "; ");
		s.append("timestamp: " + timestamp + "; ");
		s.append("box: " + box + "; ");
		s.append("boxColor: " + boxColor + "; ");
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
				this.timestamp == other.timestamp &&
				this.box == other.box &&
				(this.boxColor == null ? other.boxColor == null : this.boxColor.equals(other.boxColor));
	}

}
