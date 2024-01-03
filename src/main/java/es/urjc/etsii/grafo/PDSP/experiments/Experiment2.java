package es.urjc.etsii.grafo.PDSP.experiments;

import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.PDSPGRASPMove;
import es.urjc.etsii.grafo.PDSP.constructives.grasp.SmartPDSPListManager;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

/**
 * Experimento 2: Objetivo --> decidir valor de alpha
 * SimpleAlgorithm: Constructivo GRASP + LS. Combinaciones de parametros
 * - Valores alpha: 0, 0.25, 0.5, 0.75, 1, + RANDOM
 * - Funcion Objetivo: howManyMarks, getNUnmarkedNeighbors
 */
public class Experiment2 extends AbstractExperiment<PDSPSolution, PDSPInstance> {

    @Override
    public List<Algorithm<PDSPSolution, PDSPInstance>> getAlgorithms() {
        double[] alphaValues = new double[]{-1, 0, 0.25, 0.5, 0.75, 1};
        var algorithms = new ArrayList<Algorithm<PDSPSolution, PDSPInstance>>();
        var candidateList = new SmartPDSPListManager(true);
        var ls = new RemoveNodesLS();

        for(double alpha: alphaValues){
            algorithms.add(new SimpleAlgorithm<>(
                    "GRASP-%s-nMark".formatted(alpha),
                    new CSharpGRGrasp(alpha, candidateList, PDSPGRASPMove::getHowManyMarks),
                    ls)
            );
            algorithms.add(new SimpleAlgorithm<>(
                    "GRASP-%s-nUnmarkedNeigh".formatted(alpha),
                    new CSharpGRGrasp(alpha, candidateList, PDSPGRASPMove::getNUnmarkedNeighbors),
                    ls)
            );
        }

        return algorithms;
    }
}
