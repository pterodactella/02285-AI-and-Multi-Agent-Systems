package searchclient.CBS;

import java.util.HashSet;

public class GlobalExpandsHashSet {
	private static GlobalExpandsHashSet instance;
	private static HashSet<ConstraintState> stateSet;

	private GlobalExpandsHashSet() {
		stateSet = new HashSet<>();
	}
	
	public void addSet(ConstraintState constrState) {
		stateSet.add(constrState);
	}
	
	public HashSet<ConstraintState> getSet() {
		return stateSet;
	}

	public static GlobalExpandsHashSet getInstance() {
		if (instance == null) {
			instance = new GlobalExpandsHashSet();
		}
		return instance;

	}
}
