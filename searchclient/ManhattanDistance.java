package searchclient;

import java.util.ArrayList;
import java.util.HashMap;

public class ManhattanDistance extends Distances {
	public ManhattanDistance(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		super(agentRows, agentCols, goals, boxes);
	}

	@Override
	public int calculate() {
		int sum = 0;
		for (HashMap.Entry<Character, ArrayList<int[]>> set : this.coordinates.entrySet()) {
			for (int[] location : set.getValue()) {
				sum += Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]);
			}
		}
		return sum;
	}
}
