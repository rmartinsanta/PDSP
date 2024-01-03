package es.urjc.etsii.grafo.PDSP.constructives.grasp;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.solution.EagerMove;
import es.urjc.etsii.grafo.util.collections.BitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PDSPGRASPMove extends EagerMove<PDSPSolution, PDSPInstance> {

    private static final Logger log = LoggerFactory.getLogger(PDSPGRASPMove.class);
    public static final int NOT_EVALUATED = -1;
    private final int nodeId;
    private final int unmarkedNeighbors;
    private final PDSPSolution solution;
    private int howManyMarks;
    private int estimationHowManyMarks;

    public PDSPGRASPMove(PDSPSolution solution, int nodeId) {
        super(solution);
        this.nodeId = nodeId;
        this.unmarkedNeighbors = solution.node(nodeId).unmarked().size();
        this.howManyMarks = NOT_EVALUATED;
        this.estimationHowManyMarks = NOT_EVALUATED;
        this.solution = solution;
    }

    private int calculateHowManyMarks(PDSPSolution solution, int nodeId) {
        var copy = solution.cloneSolution();
        copy.add(nodeId);
        return solution.getUnmarked().size() - copy.getUnmarked().size();
    }

    public double getNUnmarkedNeighbors(){
        return this.unmarkedNeighbors;
    }

    private int estimateNewNodes(PDSPSolution solution, int nodeId){
        var instance = solution.getInstance();
        var newForNodeId = instance.getNewNodesMarked(nodeId);
        var lowerBound = BitSet.intersection(newForNodeId, solution.getUnmarked());
        return lowerBound.size();
    }

    public int getNodeId() {
        return nodeId;
    }

    public double getEstimationHowManyMarks(){
        // Lazy evaluation for performance, only calculated if requested by algorithm
        if(estimationHowManyMarks == NOT_EVALUATED){
            estimationHowManyMarks = estimateNewNodes(solution, nodeId);
        }
        return estimationHowManyMarks;
    }

    public double getHowManyMarks(){
        // Lazy evaluation for performance, only calculated if requested by algorithm
        if(howManyMarks == NOT_EVALUATED){
            howManyMarks = calculateHowManyMarks(solution, nodeId);
        }
        return howManyMarks;
    }

//    public double getHowManyMarksFAST(){
//        // Lazy evaluation for performance, only calculated if requested by algorithm
//        if(howManyMarks == NOT_EVALUATED){
//            howManyMarks = calculateHowManyMarksFAST(solution, nodeId);
//        }
//        return howManyMarks;
//    }

    @Override
    protected boolean _execute(PDSPSolution solution) {
        //log.info("Added \t{} \tscore {}", this.nodeId, this.howManyMarks);
        solution.add(this.nodeId);
        return true;
    }

    @Override
    public double getValue() {
        return 1; // Each time we add an element the objective function increases by one, we want to minimize it
    }

    @Override
    public String toString() {
        return "Add{" +
                "id=" + nodeId +
                //", f=" + this.howManyMarks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDSPGRASPMove that = (PDSPGRASPMove) o;
        return nodeId == that.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
