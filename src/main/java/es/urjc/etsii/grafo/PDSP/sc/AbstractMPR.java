package es.urjc.etsii.grafo.PDSP.sc;

import es.urjc.etsii.grafo.PDSP.model.EvaluationCache;
import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.collections.BitSet;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractMPR extends SolutionCombinator<PDSPSolution, PDSPInstance> {

    protected final Reconstructive<PDSPSolution, PDSPInstance> repair;
    private final Improver<PDSPSolution, PDSPInstance> improver;

    private final double evaluationProbability;

    private final ThreadLocal<EvaluationCache> cache;
    public final ThreadLocal<Stats> stats;
    public record Stats(AtomicInteger nIteraciones, AtomicInteger nSteps){
        Stats(){
            this(new AtomicInteger(0), new AtomicInteger(0));
        }
    }

    protected AbstractMPR(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver, double evaluationProbability) {
        this.repair = repair;
        this.improver = improver;
        this.cache = ThreadLocal.withInitial(() -> new EvaluationCache(this));
        this.stats = ThreadLocal.withInitial(Stats::new);
        this.evaluationProbability = evaluationProbability;
    }

    protected AbstractMPR(Reconstructive<PDSPSolution, PDSPInstance> repair, Improver<PDSPSolution, PDSPInstance> improver) {
        this(repair, improver, 1);
    }

    /**
     * Calculate how many times appears each node for all solutions provided
     * @param guidingSolutions solutions to use as guide, see Figure 1 of paper
     * @return frequency array where each index contains how many times the given element appears
     */
    public Frequency freq(PDSPSolution[] guidingSolutions, boolean weighted){
        if(guidingSolutions == null || guidingSolutions.length == 0){
            throw new IllegalArgumentException("Provide at least 1 guiding solution");
        }

        int nodesInInstance = guidingSolutions[0].getInstance().nNodes();
        double[] freq = new double[nodesInInstance];

        // Al calcular la distribucion, el denominador es la suma de todos los nodos de todas las soluciones que hacen de guia
        int totalNodes = 0;

        for(var s: guidingSolutions){
            double weight = weighted? (1/s.getScore()): 1;

            for(var node: s.getChosen()){
                totalNodes++;
                freq[node] += weight;
            }
        }

        double[] normalizedFreq = new double[nodesInInstance];
        for (int i = 0; i < normalizedFreq.length; i++) {
            normalizedFreq[i] = freq[i] / (double) totalNodes;
        }
        return new Frequency(normalizedFreq);
    }

    public void clearCache() {
        this.cache.get().invalidate();
    }

    public record FreqForIndex(double freq, int nodeId) {
        private static final Comparator<FreqForIndex> sortFreqDecreasing = Comparator.comparing(FreqForIndex::freq).reversed();
    }

    protected record Frequency(double[] arr, List<FreqForIndex> list){

        public Frequency(double[] freq){
            this(freq, toSortedList(freq));
        }

        private static List<FreqForIndex> toSortedList(double[] freq) {
            var list = new ArrayList<FreqForIndex>(freq.length);
            for (int i = 0; i < freq.length; i++) {
                list.add(new FreqForIndex(freq[i], i));
            }
            list.sort(FreqForIndex.sortFreqDecreasing);
            return list;
        }
    }

    protected List<PDSPSolution> repairAndLS(List<PDSPSolution> list) {
        var result = new ArrayList<PDSPSolution>();
        for(var solution: list){
            if(TimeControl.isTimeUp()) break;
            PDSPSolution repaired = repairAndLS(solution);
            result.add(repaired);
        }
        return result;
    }

    public PDSPSolution repairAndLS(PDSPSolution solution) {
        var repaired = this.repair.reconstruct(solution);
        repaired = this.improver.improve(solution);
        return repaired;
    }

    protected List<PDSPSolution> walk(PDSPSolution solution, Frequency f) {
        var stats = this.stats.get();
        stats.nIteraciones.getAndIncrement();
        // TODO Si hay empate porque el minimo tiene frecuencia igual para el candidato a eliminar, resuelve aleatorio
        //   Para insertar igual. Ahora es determinista

        assert f.arr.length == solution.getInstance().nNodes();
        var generatedSolutions = new ArrayList<BitSet>();
        // Frequency list is ordered from highest frequency to lowest.
        int leftPos = 0, rightPos = f.list().size() - 1;

        while(leftPos < rightPos && !TimeControl.isTimeUp()){
            int leftId = getNodeId(leftPos, f), rightId = getNodeId(rightPos, f);
            while(!solution.addable(leftId) && leftPos < rightPos){
                // Saltamos todos los nodos frequentes que ya son parte de la solucion actual
                leftPos++;
                leftId = getNodeId(leftPos, f);
            }
            while(!solution.removable(rightId) && rightPos > leftPos){
                // Saltamos todos los nodos NO frequentes que no son parte de la solucion o no se pueden eliminar
                rightPos--;
                rightId = getNodeId(rightPos, f);
            }

            // Una vez colocados los dos punteritos, si se han cruzado los punteros o las frequencias son iguales
            // es que no hay intercambios validos, finalizar
            if(leftPos >= rightPos){
                break;
            }
            double freqLeft = f.arr()[leftId], freqRight = f.arr()[rightId];
            if(DoubleComparator.equals(freqLeft, freqRight)){
                break;
            }

            // Si hemos llegado aquí, generamos una nueva solucion sin el nodo a eliminar y con el nodo nuevo
            // El nodo a insertar es el de mayor frequencia (left), a eliminar menor frecuencia (right)
            var newSolutionNodes = solution.getChosenCopy();
            newSolutionNodes.add(leftId);
            newSolutionNodes.remove(rightId);
            generatedSolutions.add(newSolutionNodes);
            stats.nSteps.getAndIncrement();

            // Actualizamos punteritos y seguimos
            rightPos--;
            leftPos++;
        }

        // From all candidate solutions generated during the walk, return only all that have the same score as the best
        // Removes all duplicates automatically

        return filterSolutions(solution.getInstance(), generatedSolutions);
    }

    public List<PDSPSolution> filterSolutions(PDSPInstance instance, ArrayList<BitSet> generatedSolutions){
        if(generatedSolutions.isEmpty()){
            return List.of();
        }
        if(generatedSolutions.size() == 1){
            // If only one solution, directly evaluate and return it
            return List.of(EvaluationCache.evaluator(this, instance, generatedSolutions.get(0)));
        }
        // Else, there are 2 or more solutions, always evaluate first and last, use probability to decide rest
        var random = RandomManager.getRandom();
        var solutionsToEvaluate = new ArrayList<BitSet>();
        solutionsToEvaluate.add(generatedSolutions.get(0));
        solutionsToEvaluate.add(generatedSolutions.get(generatedSolutions.size()-1));
        for (int i = 1; i < generatedSolutions.size() - 1; i++) {
            if (random.nextDouble() < evaluationProbability) { // If a uniformly distributed random number is range [0,1] is less than configured number
                solutionsToEvaluate.add(generatedSolutions.get(i));
            }
        }
        var c = cache.get();
        double best = Integer.MAX_VALUE;
        BitSet bestNodes = null;
        for(var nodes: solutionsToEvaluate){
            double score = c.score(nodes, instance);
            if(score < best){
                best = score;
                bestNodes = nodes;
            }
        }
        assert bestNodes != null;

        return List.of(EvaluationCache.evaluator(this, instance, bestNodes));
    }

    public abstract Set<PDSPSolution> generateNewSet(PDSPSolution[] originSolutions, PDSPSolution[] guidingSolutions);

    @Override
    public Set<PDSPSolution> newSet(PDSPSolution[] currentSet, Set<PDSPSolution> newSolutions) {
        return generateNewSet(currentSet, newSolutions.toArray(PDSPSolution[]::new));
    }

    @Override
    protected List<PDSPSolution> apply(PDSPSolution left, PDSPSolution right) {
        throw new UnsupportedOperationException("This method should never be called in the context of MPR");
    }

    protected int getNodeId(int pos, Frequency f){
        return f.list().get(pos).nodeId();
    }

    /**
     * Pick k elements randomly from the array. REPETITIONS ARE ALLOWED
     * @param k number of elements to pick. can be greater than solutions.length
     * @param solutions source array
     * @return a new array that may be bigger than solutions and contain repeated elements
     */
    public static PDSPSolution[] pickK(int k, PDSPSolution[] solutions){
        var result = new PDSPSolution[k];
        var random = RandomManager.getRandom();
        for (int i = 0; i < k; i++) {
            result[i] = solutions[random.nextInt(solutions.length)];
        }
        return result;
    }

    public double cacheHitRatio(){
        return this.cache.get().getHitRate();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "evalProp=" + evaluationProbability +
                "}";
    }
}
