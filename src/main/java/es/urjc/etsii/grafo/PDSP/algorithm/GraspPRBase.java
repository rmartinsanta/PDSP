package es.urjc.etsii.grafo.PDSP.algorithm;

import es.urjc.etsii.grafo.PDSP.model.PDSPInstance;
import es.urjc.etsii.grafo.PDSP.model.PDSPSolution;
import es.urjc.etsii.grafo.PDSP.sc.AbstractMPR;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Comparator.comparing;

public abstract class GraspPRBase extends Algorithm<PDSPSolution, PDSPInstance> {

    private static final Logger log = LoggerFactory.getLogger(GraspPRBase.class);

    protected final AbstractMPR mpr;

    /**
     * Number of initial generated solutions
     */
    private final int size;

    /**
     * Number of solution for the path relinking by best value
     */
    private final int nSolutionsByScore;

    /**
     * Number of solutions for the path relinking by diversity
     */
    private final int nSolutionsByDiversity;

    private final Constructive<PDSPSolution, PDSPInstance> constructive;
    private final Improver<PDSPSolution, PDSPInstance> improver;

    public GraspPRBase(String name, int nSolutions, int nSolutionsByDiversity, int nSolutionsByScore, AbstractMPR mpr, Constructive<PDSPSolution, PDSPInstance> constructive, Improver<PDSPSolution, PDSPInstance> improver) {
        super(name);
        this.mpr = mpr;
        this.size = nSolutions;
        this.nSolutionsByScore = nSolutionsByScore;
        this.nSolutionsByDiversity = nSolutionsByDiversity;
        this.constructive = constructive;
        this.improver = improver;
    }

    @Override
    public PDSPSolution algorithm(PDSPInstance instance) {
        mpr.clearCache();
        mpr.stats.remove();

        var initialSolutions = new PDSPSolution[this.size];
        int i;
        for (i = 0; i < initialSolutions.length; i++) {
            initialSolutions[i] = buildPDSPSolution(instance);
            log.debug("Initial solution {} generated with score {}", i, initialSolutions[i].getScore());
            if(TimeControl.isTimeUp()) break;
        }

        Arrays.sort(initialSolutions, 0, i, comparing(PDSPSolution::getScore));
        if(TimeControl.isTimeUp()) return initialSolutions[0];

        var bestSolutionSet = new HashSet<PDSPSolution>();
        i = 0;
        while(bestSolutionSet.size() < nSolutionsByScore && i < initialSolutions.length){
            bestSolutionSet.add(initialSolutions[i++]);
        }

        // If there are missing solutions print warning but keep going
        if(bestSolutionSet.size() < this.nSolutionsByScore){
            log.warn("Generated {} initial solutions, wanted {} for restricted set by quality but only {} are different", initialSolutions.length, this.nSolutionsByScore, bestSolutionSet.size());
        }

        // Get best unique solutions and sort them by score
        var bestSolutions = bestSolutionSet.toArray(new PDSPSolution[0]);
        Arrays.sort(bestSolutions, comparing(PDSPSolution::getScore));

        // Run PR or MPR only over the best N solutions
        var diverseSolutions = getDiverseSolutions(initialSolutions, bestSolutions, nSolutionsByDiversity);

        long startTime = System.nanoTime();
        var best = runPR(diverseSolutions, bestSolutions);
        long totalMillis = TimeUtil.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS);
        var stats = this.mpr.stats.get();
        long nIteraciones = stats.nIteraciones().get();
        long nSteps = stats.nSteps().get();
        double hitRatio = this.mpr.cacheHitRatio();
        log.debug("%n%n%s,%s,%s,%s,%s,%s%n%n".formatted(instance.getId(), this.getName(), nIteraciones, nSteps, totalMillis, hitRatio));
        return best;
    }

    private PDSPSolution[] getDiverseSolutions(PDSPSolution[] initialSolutions, PDSPSolution[] bestSolutions, int nSolutionsByDiversity) {
        var orderedByDiversity = orderByDiversity(initialSolutions, bestSolutions);
        var solutionsChosenByDiversity = new HashSet<PDSPSolution>();

        int i = 0;
        while(solutionsChosenByDiversity.size() < nSolutionsByDiversity && i < initialSolutions.length){
            solutionsChosenByDiversity.add(orderedByDiversity[i++]);
        }

        // If there are missing solutions print warning but keep going
        if(solutionsChosenByDiversity.size() < this.nSolutionsByDiversity){
            log.warn("Generated {} initial solutions, wanted {} for restricted set by diversity but only {} are different", initialSolutions.length, this.nSolutionsByDiversity, solutionsChosenByDiversity.size());
        }

        return solutionsChosenByDiversity.toArray(new PDSPSolution[0]);
    }

    private PDSPSolution[] orderByDiversity(PDSPSolution[] initialSolutions, PDSPSolution[] bestSolutions) {
        int[] nAppearences = countEachNode(bestSolutions);
        var copy = initialSolutions.clone();
        Arrays.sort(copy, Comparator.comparingInt(s -> similarity(s, nAppearences)));
        return copy;
    }

    private int[] countEachNode(PDSPSolution[] solutions){
        if(solutions.length == 0){
            throw new IllegalArgumentException("No solutions provided");
        }

        int nNodes = solutions[0].getInstance().nNodes();
        int[] nAppearences = new int[nNodes];

        for(var solution: solutions){
            for(var node: solution.getChosen()){
                nAppearences[node]++;
            }
        }
        return nAppearences;
    }

    private int similarity(PDSPSolution solution, int[] nodeCount){
        int score = 0;
        // The bigger the score, the more similar is a solution to the given set
        // Java sorts by default from smaller to biggest, those with the smallest score
        // will be at the start of the array
        for(var node: solution.getChosen()){
            score += nodeCount[node];
        }
        return score;
    }

    private PDSPSolution getBest(Set<PDSPSolution> mprSolutions){
        double minV = Integer.MAX_VALUE;
        PDSPSolution bestInSet = null;
        for(var s: mprSolutions){
            if(s.getScore() < minV){
                minV = s.getScore();
                bestInSet = s;
            }
        }
        assert bestInSet != null;
        return bestInSet;
    }

    protected abstract Set<PDSPSolution> runPRInterceptable(PDSPSolution[] diverseSolutions, PDSPSolution[] bestSolutions);

    private PDSPSolution runPR(PDSPSolution[] diverseSolutions, PDSPSolution[] bestSolutions) {
        if(this.mpr == null){
            // No path relinking configured, return best solution without PR
            return bestSolutions[0];
        }
        double bestExisting = bestSolutions[0].getScore();


        var mprSolutions = runPRInterceptable(diverseSolutions, bestSolutions);
        // if there are no solutions generated by the PR, return current best

        if(mprSolutions.isEmpty()){
            log.debug("No new solutions generated by the PR");
            return bestSolutions[0];
        }

        // If PR generated solutions, return best found
        var bestMPR = getBest(mprSolutions);
        double minV = bestMPR.getScore();

        // Log debug what has happened
        if(DoubleComparator.isLess(minV, bestExisting)){
            log.debug("Improved value after MPR {} -> {}", bestExisting, minV);
            return bestMPR;
        } else if (DoubleComparator.equals(minV, bestExisting)){
            log.debug("Equal best value after MPR {} -> {}", bestExisting, minV);
            return bestMPR;
        } else {
            log.debug("Worse best value after MPR {} -> {}", bestExisting, minV);
            return bestSolutions[0];
        }
    }

    private PDSPSolution buildPDSPSolution(PDSPInstance instance) {
        var solution = this.newSolution(instance);
        solution = this.constructive.construct(solution);
        solution = this.improver.improve(solution);
        return solution;
    }
}
