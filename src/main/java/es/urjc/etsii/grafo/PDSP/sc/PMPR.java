package es.urjc.etsii.grafo.PDSP.sc;

import es.urjc.etsii.grafo.PDSP.constructives.grasp.CSharpGRGrasp;
import es.urjc.etsii.grafo.PDSP.ls.RemoveNodesLS;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.collections.BitSet;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PMPR extends AbstractMPR {

    private static final SwapIndexes INVALID = new SwapIndexes(-1, -1);

    public PMPR(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        super(repair, improver);
    }

    public PMPR(CSharpGRGrasp constructive, RemoveNodesLS ls, double evaluationProbability) {
        super(constructive, ls, evaluationProbability);
    }

    @Override
    public Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions) {
        var newSet = new HashSet<PDSPSolution>();
        var frequencies = freq(guidingSolutions, false);

        for (var solution : originSolutions) {
            if (TimeControl.isTimeUp()) break;
            // Calculate probability of picking each solution according to how good their scores are,
            // ignoring current starting solution
            var list = _walk(solution, guidingSolutions, frequencies);
            list = repairAndLS(list);
            newSet.addAll(list);
        }

        return newSet;
    }

    protected List<PDSPSolution> _walk(PDSPSolution solution, PDSPSolution[] allSolutions, Frequency f) {
        var stats = this.stats.get();
        stats.nIteraciones().getAndIncrement();
        var generatedSolutions = new ArrayList<BitSet>();

        var weightedSolutions = probabilityOfPickingEachSolution(allSolutions);
        var addedNodes = new HashSet<Integer>();
        var removedNodes = new HashSet<Integer>();

        // 3 strike rule: end after 3 failures to swap
        int strikes = 0;
        while (strikes < 3) {
            var guide = chooseGuidingSolution(weightedSolutions);
            var swapIndexes = getSwapIndexes(solution, guide, f);
            if (swapIndexes == INVALID) {
                strikes++;
                continue;
            }
            if (addedNodes.contains(swapIndexes.left) || removedNodes.contains(swapIndexes.right)) {
                // Element already added or removed, invalid swap of nodes, retry
                strikes++;
                continue;
            }
            addedNodes.add(swapIndexes.left);
            removedNodes.add(swapIndexes.right);

            // Generamos la nueva solucion
            // El nodo a insertar es el de mayor frequencia (left), a eliminar menor frecuencia (right)
            var newSolutionNodes = solution.getChosenCopy();
            newSolutionNodes.add(swapIndexes.left);
            newSolutionNodes.remove(swapIndexes.right);
            generatedSolutions.add(newSolutionNodes);
            stats.nSteps().getAndIncrement();
        }

        return filterSolutions(solution.getInstance(), generatedSolutions);
    }

    private record WeightedSolution(PDSPSolution solution, double probability) {
    }

    private record SwapIndexes(int left, int right) {
    }

    private SwapIndexes getSwapIndexes(PDSPSolution solution, PDSPSolution guide, Frequency f) {
        // Frequency list is ordered from highest frequency to lowest.
        int leftPos = 0, rightPos = f.list().size() - 1;
        var currentNodes = solution.getChosen();
        var referenceNodes = guide.getChosen();

        int leftId = getNodeId(leftPos, f), rightId = getNodeId(rightPos, f);
        while ((!solution.addable(leftId) || !referenceNodes.contains(leftId)) && leftPos < rightPos) {
            // Saltamos todos los nodos "buenos" que ya son parte de la solucion actual
            // o que no son parte de la solucion guia (y por lo tanto no podemos meter)
            leftPos++;
            leftId = getNodeId(leftPos, f);
        }
        while ((!solution.removable(rightId) || referenceNodes.contains(rightId)) && rightPos > leftPos) {
            // Saltamos todos los nodos "malos" que no son parte de la solucion
            // o que son parte de la solucion guia (y por lo tanto no podemos eliminar)
            rightPos--;
            rightId = getNodeId(rightPos, f);
        }

        // Una vez colocados los dos punteritos, si se han cruzado los punteros o las frequencias son iguales
        // es que no hay intercambios validos
        if (leftPos >= rightPos) {
            return INVALID;
        }
        // Puede ocurrir que aunque no se hayan cruzado, las frecuencias sean iguales,
        // por lo que tampoco intercambiamos
        double freqLeft = f.arr()[leftId], freqRight = f.arr()[rightId];
        if (DoubleComparator.equals(freqLeft, freqRight)) {
            return INVALID;
        }

        // Los indices son validos
        return new SwapIndexes(leftId, rightId);
    }

    public WeightedSolution[] probabilityOfPickingEachSolution(PDSPSolution[] currentSet) {
        double[] inverseScores = new double[currentSet.length];
        double totalInversedScore = 0;

        for (int i = 0; i < currentSet.length; i++) {
            PDSPSolution solution = currentSet[i];
            double inverseScore = 1 / solution.getScore();
            totalInversedScore += inverseScore;
            inverseScores[i] = inverseScore;
        }

        // We have the scores, return them normalized
        var weightedSolutions = new WeightedSolution[inverseScores.length];
        for (int i = 0; i < inverseScores.length; i++) {
            weightedSolutions[i] = new WeightedSolution(currentSet[i], inverseScores[i] / totalInversedScore);
        }
        return weightedSolutions;
    }

    public PDSPSolution chooseGuidingSolution(WeightedSolution[] guidingCandidates) {
        var random = RandomManager.getRandom();
        double d = random.nextDouble(); // Random number in range [0, 1]
        double acc = 0;

        // The sum of all elements in the guidingCandidates array is 1 because probabilities are normalized
        // Choose the first element which the sum of probabilities of all elements to its right are bigger than the random number
        for (var guidingCandidate : guidingCandidates) {
            acc += guidingCandidate.probability;
            if (acc > d) {
                return guidingCandidate.solution;
            }
        }
        throw new IllegalStateException("Impossible to reach if the probabilities of all elements in the array sum 1");
    }

    @Override
    protected List<PDSPSolution> walk(PDSPSolution solution, Frequency f) {
        throw new UnsupportedOperationException();
    }
}
