package es.urjc.etsii.grafo.PDSP.experiments;

import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.PDSPGRASPMove;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.SmartPDSPListManager;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.PDSP.sc.BMPRv4;
import es.urjc.etsii.grafo.PDSP.sc.PDSPDistance;
import es.urjc.etsii.grafo.PDSP.sc.PMPRv4;
import es.urjc.etsii.grafo.PDSP.sc.WMPRv4;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.algorithms.scattersearch.ScatterSearch;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class Experiment5 extends AbstractExperiment<PDSPSolution, PDSPInstance> {
    @Override
    public List<Algorithm<PDSPSolution, PDSPInstance>> getAlgorithms() {
        var candidateList = new SmartPDSPListManager(true);
        var constructive1 = new CSharpGRGrasp(0.25, candidateList, PDSPGRASPMove::getNUnmarkedNeighbors);
        var constructiveD1 = new CSharpGRGrasp(1, candidateList, PDSPGRASPMove::getNUnmarkedNeighbors);
        var constructive2 = new CSharpGRGrasp(0.25, candidateList, PDSPGRASPMove::getEstimationHowManyMarks);
        var constructiveD2 = new CSharpGRGrasp(1, candidateList, PDSPGRASPMove::getEstimationHowManyMarks);
        var ls = new RemoveNodesLS();

        var list = new ArrayList<Algorithm<PDSPSolution, PDSPInstance>>();
        list.add(new ScatterSearch<>("SS1",
                10, 10, constructive1, constructiveD1, ls, new BMPRv4(constructive1, ls, 1),
                FMode.MINIMIZE, 10_000, 0.5, new PDSPDistance(), false
        ));
        list.add(new ScatterSearch<>("SS2",
                10, 10, constructive1, constructiveD1, ls, new WMPRv4(constructive1, ls, 1),
                FMode.MINIMIZE, 10_000, 0.5, new PDSPDistance(), false
        ));
        list.add(new ScatterSearch<>("SS3",
                10, 10, constructive1, constructiveD1, ls, new PMPRv4(constructive1, ls, 1),
                FMode.MINIMIZE, 10_000, 0.5, new PDSPDistance(), false
        ));
        list.add(new ScatterSearch<>("SS4",
                10, 10, constructive2, constructiveD2, ls, new BMPRv4(constructive2, ls, 1),
                FMode.MINIMIZE, 10_000, 0.5, new PDSPDistance(), false
        ));
        list.add(new ScatterSearch<>("SS5",
                10, 10, constructive2, constructiveD2, ls, new WMPRv4(constructive2, ls, 1),
                FMode.MINIMIZE, 10_000, 0.5, new PDSPDistance(), false
        ));list.add(new ScatterSearch<>("SS6",
                10, 10, constructive2, constructiveD2, ls, new PMPRv4(constructive2, ls, 1),
                FMode.MINIMIZE, 10_000, 0.5, new PDSPDistance(), false
        ));
        return list;
    }
}
