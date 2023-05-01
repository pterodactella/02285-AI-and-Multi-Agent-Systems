package searchclient;


public class CBSNode implements Comparable<CBSNode> {

    public State state; // The state of the node
    private CBSNode parent;

    public CBSNode(State state, CBSNode parent) {
        this.state = state;
        this.parent = parent;
    }

    public CBSNode(State state) {
        this.state = state;
        this.parent=null;
    }


    public State getState() {
        return state;
    }

    public CBSNode getParent() {
        return parent;
    }

    public int getCost() {
        int cost = 0;
        CBSNode currentNode = this;
        while (currentNode.getParent() != null) {
            cost++;
            currentNode = currentNode.getParent();
        }
        return cost;
    }

    @Override
    public int compareTo(CBSNode other) {
        return Integer.compare(getCost(), other.getCost());
    }

}