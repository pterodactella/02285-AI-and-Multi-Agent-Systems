package searchclient;

import java.util.ArrayList;

public class MetaAgent {

    private ArrayList<Agent> agents; // The agents that move together
    private Constraints constraints; // The shared constraints of the agents
    private int cost; // The cost of the meta-agent's plan

    public MetaAgent(ArrayList<Agent> agents, Constraints constraints, int cost) {
        this.agents = agents;
        this.constraints = constraints;
        this.cost = cost;
    }

    public ArrayList<Agent> getAgents() {
        return agents;
    }

    public Constraints getConstraints() {
        return constraints;
    }

    public int getCost() {
        return cost;
    }

}
