package es.urjc.etsii.grafo.PDSP.ls;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.BestObjective;
import es.urjc.etsii.grafo.metrics.Metrics;

import java.util.HashSet;

public class RemoveNodesLS extends Improver<PDSPSolution, PDSPInstance> {

    public RemoveNodesLS() {
        super(FMode.MINIMIZE);
    }

    @Override
    protected PDSPSolution _improve(PDSPSolution solution) {
        boolean improved;
        do {
            improved = false;
            // Candidates that can be removed from the solution are all chosen candidates
            // except those that are critical nodes, as they are mandatory so the solution is feasible
            var removeCandidates = solution.getRemovableNodes();

            for(var node: removeCandidates){
                var copy = solution.cloneSolution();
                copy.removeNode(node);
                if(copy.isCovered()){
                    solution = copy;
                    improved = true;
                    Metrics.add(BestObjective.class, solution.getScore());
                }
            }

        } while(improved);
        return solution;
    }
}
