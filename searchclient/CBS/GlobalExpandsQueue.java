// package searchclient.CBS;

// public class GlobalExpandsQueue {
// 	private static GlobalExpandsQueue instance;
// 	private static ConstraintFrontier frontier;

// 	private GlobalExpandsQueue() {
// 		frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar());
// 	}
	
// 	public ConstraintFrontier getQueue() {
// 		return frontier;
// 	}

// 	public static GlobalExpandsQueue getInstance() {
// 		if (instance == null) {
// 			instance = new GlobalExpandsQueue();
// 		}
// 		return instance;

// 	}

// }
