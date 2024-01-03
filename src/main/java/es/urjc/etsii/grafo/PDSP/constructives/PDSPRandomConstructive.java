package es.urjc.etsii.grafo.PDSP.constructives;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Constructive;

import static es.urjc.etsii.grafo.util.CollectionUtil.pickRandom;

public class PDSPRandomConstructive extends Constructive<PDSPSolution, PDSPInstance> {

    @Override
    public PDSPSolution construct(PDSPSolution solution) {

        while(!solution.isCovered()){
            var n = pickRandom(solution.getUnmarked());
            solution.add(n);
        }

        return solution;
    }
}
