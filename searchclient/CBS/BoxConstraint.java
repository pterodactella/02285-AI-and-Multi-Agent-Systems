package searchclient.CBS;

public class BoxConstraint {
		public int agentIndex;
		public int boxLocationX;
		public int boxLocationY;
		public int timestamp;
		//TODO:Include boxes
		public BoxConstraint(int agentIndex, int boxLocationX, int boxLocationY, int timestamp ) {
			this.agentIndex = agentIndex;
			this.boxLocationX = boxLocationX;
			this.boxLocationY = boxLocationY;
			this.timestamp = timestamp;
		}
		
		public BoxConstraint(BoxConstraint copy) {
			this.agentIndex = copy.agentIndex;
			this.boxLocationX = copy.boxLocationX;
			this.boxLocationY = copy.boxLocationY;
			this.timestamp = copy.timestamp;
		}
		
		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("boxLocationX: " + boxLocationX + "; ");
			s.append("boxLocationY: " + boxLocationY + "; ");
			s.append("agentIndex: " + agentIndex + "; ");
			s.append("timestamp: " + timestamp + "; ");
			return s.toString();
		}
		
	    @Override
	    public int hashCode() {
	    	final int prime = 101;
	        int result = 1;
	        result = result * prime + this.agentIndex;
	        result = result * prime + this.boxLocationX;
	        result = result * prime + this.boxLocationY;
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
			BoxConstraint other = (BoxConstraint) obj;
			return this.agentIndex == other.agentIndex &&
					this.boxLocationX == other.boxLocationX &&
					this.boxLocationY == other.boxLocationY &&
					this.timestamp == other.timestamp;
		}

}
