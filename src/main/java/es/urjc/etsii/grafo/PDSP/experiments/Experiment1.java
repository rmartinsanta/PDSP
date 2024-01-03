//package es.urjc.etsii.grafo.PDSP.experiments;
//
//import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
//import es.urjc.etsii.grafo.PDSP.constructives.grasp.PDSPGRASPMove;
//import es.urjc.etsii.grafo.PDSP.constructives.grasp.SmartPDSPListManager;
//import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
//import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
//import es.urjc.etsii.grafo.algorithms.Algorithm;
//import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
//import es.urjc.etsii.grafo.experiment.AbstractExperiment;
//
//import java.util.List;
//
///**
// * Experimento 1: Objetivo --> demostrar la contribucion de los nodos soporte y eliminar las hojas
// * SimpleAlgorithm: Constructivo voraz, con las siguientes combinaciones de parametros:
// * - Funcion objetivo: howManyMarks, getNUnmarkedNeighbors
// * - Parametro booleano: incluir nodos soporte y no soporte
// * <p>
// * 4 algoritmos diferentes, 100 repeticiones
// */
//public class Experiment1 extends AbstractExperiment<PDSPSolution, PDSPInstance> {
//
//    @Override
//    public List<Algorithm<PDSPSolution, PDSPInstance>> getAlgorithms() {
//        return List.of(
//                // GR-naive-nMark
//                new SimpleAlgorithm<>(
//                        "GR-naive-nMark",
//                        new CSharpGRGrasp(
//                                0,
//                                new SmartPDSPListManager(true, false, false),
//                                PDSPGRASPMove::getHowManyMarks
//                        )
//                ),
//
//                // GR-smart-nMark
//                new SimpleAlgorithm<>(
//                        "GR-smart-nMark",
//                        new CSharpGRGrasp(
//                                0,
//                                new SmartPDSPListManager(true, false, true),
//                                PDSPGRASPMove::getHowManyMarks
//                        )
//                ),
//
//                // GR-naive-unmarkedNeighs
//                new SimpleAlgorithm<>(
//                        "GR-naive-unmarkedNeighs",
//                        new CSharpGRGrasp(
//                                0,
//                                new SmartPDSPListManager(true, false, false),
//                                PDSPGRASPMove::getNUnmarkedNeighbors
//                        )
//                ),
//
//                // GR-smart-unmarkedNeighs
//                new SimpleAlgorithm<>(
//                        "GR-smart-unmarkedNeighs",
//                        new CSharpGRGrasp(
//                                0,
//                                new SmartPDSPListManager(true, false, true),
//                                PDSPGRASPMove::getNUnmarkedNeighbors
//                        )
//                )
//        );
//    }
//}
