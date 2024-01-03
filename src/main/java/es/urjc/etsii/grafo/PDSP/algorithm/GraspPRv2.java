package es.urjc.etsii.grafo.PDSP.algorithm;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.PDSP.sc.AbstractMPR;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;

import java.util.Set;

import static java.util.Comparator.comparing;

public class GraspPRv2 extends GraspPRBase {

    public GraspPRv2(String name, int nSolutions, int nSolutionsByDiversity, int nSolutionsByScore, AbstractMPR mpr, Constructive<PDSPSolution, PDSPInstance> constructive, Improver<PDSPSolution, PDSPInstance> improver) {
        // pass al data as is
        super(name, nSolutions, nSolutionsByDiversity, nSolutionsByScore, mpr, constructive, improver);
    }

    @Override
    protected Set<PDSPSolution> runPRInterceptable(PDSPSolution[] diverseSolutions, PDSPSolution[] bestSolutions) {
        var originSolutions = diverseSolutions;
        var guidingSolutions = bestSolutions;
        return mpr.generateNewSet(originSolutions, guidingSolutions);
    }
}
