package discoverer.structureLearning.algorithms.liftedCascadeCorrelation;

import discoverer.LiftedDataset;
import discoverer.structureLearning.logic.Predicate;

import java.util.List;

/**
 * Created by EL on 20.4.2016.
 */
public class CandidateWrapper {

    private final Predicate predicate;
    private final List<String> rules;
    private final Double correlation;
    private final LiftedDataset grounded;

    public CandidateWrapper(Predicate predicate, List<String> rules, Double correlation, LiftedDataset grounded) {
        this.predicate = predicate;
        this.rules = rules;
        this.correlation = correlation;
        this.grounded = grounded;
    }

    public LiftedDataset getGrounded() {
        return grounded;
    }

    public Double getCorrelation() {
        return correlation;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public List<String> getRules() {
        return rules;
    }

    public static int compare(CandidateWrapper candidateWrapper1, CandidateWrapper candidateWrapper2) {
        return candidateWrapper1.getCorrelation().compareTo(candidateWrapper2.getCorrelation());
    }

}
