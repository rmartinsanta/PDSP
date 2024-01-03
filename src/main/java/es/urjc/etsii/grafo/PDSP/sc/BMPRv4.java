package es.urjc.etsii.grafo.PDSP.sc;

import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.HashSet;
import java.util.Set;

public class BMPRv4 extends BMPR {

    private final int k = 5;

    private final int nSubconjuntos = 9;

    public BMPRv4(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        super(repair, improver);
    }

    public BMPRv4(CSharpGRGrasp constructive, RemoveNodesLS ls, double evaluationProbability) {
        super(constructive, ls, evaluationProbability);
    }

    @Override
    public Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions) {
        var newSet = new HashSet<PDSPSolution>();

        for (var solution : originSolutions) {
            for (int i = 0; i < nSubconjuntos; i++) {
                if (TimeControl.isTimeUp()) break;
                var pick = pickK(k, guidingSolutions);
                var frequencies = freq(pick, false);
                var list = walk(solution, frequencies);
                list = repairAndLS(list);
                newSet.addAll(list);
            }
        }

        return newSet;
    }
}
