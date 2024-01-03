package es.urjc.etsii.grafo.PDSP.algorithm;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.PDSP.sc.AbstractMPR;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

public class GraspPRv5 extends GraspPRBase {

    public GraspPRv5(String name, int nSolutions, int nSolutionsByDiversity, int nSolutionsByScore, AbstractMPR mpr, Constructive<PDSPSolution, PDSPInstance> constructive, Improver<PDSPSolution, PDSPInstance> improver) {
        // pass al data as is
        super(name, nSolutions, nSolutionsByDiversity, nSolutionsByScore, mpr, constructive, improver);
    }

    @Override
    protected Set<PDSPSolution> runPRInterceptable(PDSPSolution[] diverseSolutions, PDSPSolution[] bestSolutions) {
        var originSolutions = ArrayUtils.addAll(bestSolutions, diverseSolutions);
        var guidingSolutions = ArrayUtils.addAll(bestSolutions, diverseSolutions);
        return mpr.generateNewSet(originSolutions, guidingSolutions);
    }
}
