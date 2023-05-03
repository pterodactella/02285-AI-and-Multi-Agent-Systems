package searchclient;

// import java.util.ArrayList;
import java.util.Comparator;

public abstract class Heuristic
        implements Comparator<CBSNode>
{
    public Heuristic(CBSNode initialState)
    {
    }

    
    public int h(CBSNode node) {
        // System.err.println("calculate manhattan distance");

    	Distances d = new ManhattanDistance(node.getState().getGoals(), node.getState().getBoxes());
   	// System.err.println("d= " + d.calculate());
        return d.calculate();
    }



    public abstract int f(CBSNode s);

    
    
    @Override
    public int compare(CBSNode s1, CBSNode s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(CBSNode initialState)
    {

        super(initialState);
    }

    @Override
    public int f(CBSNode s)
    {
        // System.err.println("f: " + s.getState().g + this.h(s));
        return s.getState().g + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation" ;
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(CBSNode initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(CBSNode s)
    {
        return s.getState().g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(CBSNode initialState)
    {
        super(initialState);
    }

    @Override
    public int f(CBSNode s)
    {
        return this.h(s);
    }


    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}

// class HeuristicMeta extends Heuristic {

//     // private int[] pathLength;

//     public HeuristicMeta(CBSNode initialState) {
//         super(initialState);
//     }

//     @Override
//     public int f(CBSNode s)
//     {
//         int total = 0;
//         for (State expanded : s.getState().getExpandedStates()) {
//             total += this.h(expanded);
//             if (this.h(expanded) < this.h(s) ) {
//                 return total - this.h(expanded);
//             }
//         }
//         return total - this.h(s);
//     }

//     @Override public String toString() {
//         return "HeuristicMeta";
//     }

// }

// }
