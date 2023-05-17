package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EnhancedHammingDistance extends Distances{
	public EnhancedHammingDistance(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		super(agentRows, agentCols, goals, boxes);
	}

	@Override
	public int calculate() {
		int misplacedTiles = 0;
		int minDistance = -1;
		
		for (HashMap.Entry<Character, ArrayList<int[]>> set : this.coordinates.entrySet()) {
			for (int[] location : set.getValue()) {
				if (location[2] - location[0] != 0 || location[3] - location[1] != 0) {
					misplacedTiles++;
				} else {
					continue ;
				}
				if (minDistance == -1) {
					minDistance = Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]);

				}
				else if (Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]) < minDistance) {
					minDistance = Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]);
				}
			}
		}
		
		int minAgentDistance = -1;
		
		for (int[] location : this.agentCoordinates.values()) {
//			System.err.println("minAgentDistance " + minAgentDistance);
			
			if (minAgentDistance == -1) {
				minAgentDistance = Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]);

			}
			if (Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]) < minAgentDistance) {
				minAgentDistance = Math.abs(location[2] - location[0]) + Math.abs(location[3] - location[1]);
			}
		}
		
//		System.err.println(Arrays.toString(this.agentCoordinates.get('0')) + " " +  Arrays.toString(this.agentCoordinates.get('1')));
//		System.err.println("minAgentDistance " + minAgentDistance);
//		System.exit(0);
		
		return misplacedTiles * 400 + minDistance * 20 + minAgentDistance;
	}
}
