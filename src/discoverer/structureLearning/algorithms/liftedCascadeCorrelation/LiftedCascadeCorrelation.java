package discoverer.structureLearning.algorithms.liftedCascadeCorrelation;

import discoverer.LiftedDataset;
import discoverer.bridge.*;
import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.StructureLearning;
import discoverer.structureLearning.algorithms.ruleGenerator.BaseRuleGenerator;
import discoverer.structureLearning.algorithms.ruleGenerator.CascadeRuleGenerator;
import discoverer.structureLearning.algorithms.ruleGenerator.RuleGenerator;
import discoverer.structureLearning.logic.Predicate;
import discoverer.structureLearning.tools.Tools;
import javafx.util.Pair;
import org.apache.commons.cli.CommandLine;

import java.util.*;
import java.util.stream.LongStream;

/**
 * Created by EL on 19.4.2016.
 */
public class LiftedCascadeCorrelation implements StructureLearning {

    // TODO parametrize
    private final long RULES_LIMIT = 50;
    private static final long POOL_SIZE = 1;
    private static final Double EPSILON_CONVERGENT = 0.1;
    private static final Integer LONG_TIME_WINDOW = 30;
    private static final Integer SHORT_TIME_WINDOW = 10;
    private static final Double CONVERGENT_ERROR = 0.01;


    private CommandLine cmd;
    private final String[] exs;
    private final String[] args;
    private String[] test;
    private String[] rules;
    private String[] pretrainedRules;

    private RuleGenerator generator = new CascadeRuleGenerator();

    public LiftedCascadeCorrelation(String[] exs, String[] test, String[] rules, String[] pretrainedRules, CommandLine cmd, String[] args) {
        this.exs = exs;
        this.test = test;
        this.rules = rules;
        this.pretrainedRules = pretrainedRules;
        this.cmd = cmd;
        this.args = args;
    }

    @Override
    public void learn() {
        // TODO - ha ha ha, aby to fungovalo tak predpokladam ze je v template rule "finalLambda :- finalKappa"

        String finalRulePredicate = "finalKappa";
        Double orRuleWeight = 0.0;

        Initable init = new DummyInitable();
        LiftedDataset grounded = init.init(args);
        WeightLearner weightLearner = new DummyWeightLearner();
        ReGrounder regrounder = new DummyReGrounder();


        List<String> template = new ArrayList<>();
        Set<Predicate> templateLambdaHeads = new HashSet<>();
        Set<Predicate> basePredicates = RuleTools.retrievePredicates(exs);
        template.addAll(RuleTools.constructNodeOutputNodes(basePredicates, finalRulePredicate, orRuleWeight));


        grounded = regrounder.reGroundMe(grounded, template);

        long numberOfAddedPredicates = 0;
        List<Double> errors = new ArrayList<>();

//        while (true) {
//            grounded = weightLearner.backprop(grounded, grounded.network.sharedWeights, "CasCor"); // only output weights
//
//            //double currentError = 1000.0;
//            double currentError = Evaluate.computeAverageSquaredTrainTotalError(grounded);
//            errors.add(currentError);
//
//            if (stopCascadeCorrelation(numberOfAddedPredicates, errors)) {
//                break;
//            }
//
//            final LiftedDataset finalGrounded = grounded;
//            CandidateWrapper bestCandidate = LongStream.range(0, POOL_SIZE)
//                    //.parallel()
//                    .mapToObj(i -> makeAndLearnCandidate(finalGrounded, regrounder, new ArrayList<>(template), basePredicates, new HashSet<>(templateLambdaHeads), finalRulePredicate))
//                    .max(CandidateWrapper::compare)
//                    .get();
//
//            templateLambdaHeads.add(bestCandidate.getPredicate());
//            template.addAll(bestCandidate.getRules());
//
//            numberOfAddedPredicates++;
//        }

        System.out.println("learned with added predicates: " + grounded);
    }


    private boolean stopCascadeCorrelation(long numberOfAddedRules, List<Double> errors) {
        return numberOfAddedRules > RULES_LIMIT
                || Tools.hasConverged(errors, LONG_TIME_WINDOW, SHORT_TIME_WINDOW, EPSILON_CONVERGENT)
                || (errors.size() > 0 && errors.get(errors.size() - 1) < CONVERGENT_ERROR);
    }

    private CandidateWrapper makeAndLearnCandidate(LiftedDataset grounded, ReGrounder regrounder, List<String> template, Set<Predicate> basePredicates, Set<Predicate> templateLambdaHeads, String finalRulePredicate) {
        // making
        Pair<Predicate, List<String>> generated = generator.generateRules(templateLambdaHeads, basePredicates, template);

        double weight = 0.0d;
        Predicate predicate = generated.getKey();
        List<String> extension = generated.getValue();
        extension.add(RuleTools.constructOrRuleWithZeroArity(finalRulePredicate, predicate, weight));
        LiftedDataset candidateGrounded = regrounder.reGroundMe(grounded, extension);


        // learning phase -> todo, learn connection added (leading to predicate node); thereafter transfer those weights to newRules list
        // gradient ascent + zafixovani vah


        List<String> newRules = new ArrayList<>(); // TODO
        double correlation = 0.0d;
        // TODO - tady jeste predat vahy z naucenych do newRules

        return new CandidateWrapper(predicate, newRules, correlation, candidateGrounded);
    }

}
