package discoverer.structureLearning.algorithms.liftedDynamicNodeCreation;

import discoverer.LiftedDataset;
import discoverer.bridge.*;
import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.StructureLearning;
import discoverer.structureLearning.algorithms.ruleGenerator.BaseRuleGenerator;
import discoverer.structureLearning.algorithms.ruleGenerator.RuleGenerator;
import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;
import org.apache.commons.cli.CommandLine;

import java.util.*;

/**
 * Created by EL on 19.4.2016.
 */
public class LiftedDynamicNodeCreation implements StructureLearning {

    // TODO - shift to setting
    private double deltaT = 0.05;
    private long timeWindow = 10;
    private double Cm = 0.001;
    private double Ca = 0.01;
    private static final int MAX_NUMBER_OF_ADDED_PREDICATES = 50; // todo - change to setting (Global, parameter, etc.)


    private CommandLine cmd;
    private final String[] exs;
    private final String[] args;
    private String[] test;
    private String[] rules;
    private String[] pretrainedRules;

    private RuleGenerator generator = new BaseRuleGenerator();

    public LiftedDynamicNodeCreation(String[] exs, String[] test, String[] rules, String[] pretrainedRules, CommandLine cmd, String[] args) {
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

        Initable init = new DummyInitable();
        LiftedDataset grounded = init.init(args);
        WeightLearner weightLearner = new DummyWeightLearner();
        ReGrounder regrounder = new DummyReGrounder();

        List<String> template = new ArrayList<>();
        Set<Predicate> templateLambdaHead = new HashSet<>();
        Set<Predicate> basePredicates = RuleTools.retrievePredicates(exs);


        Pair<Predicate, List<String>> newRules = generator.generateRules(templateLambdaHead, basePredicates, template);
        template.addAll(newRules.getValue());
        template.add(generateBaseRuleConnectedToOutput(newRules.getKey()));
        templateLambdaHead.add(newRules.getKey());

        grounded = regrounder.reGroundMe(grounded, template);

        Map<Long, Double> averagesErrors = new HashMap<>();
        long iteration = 0;
        long timeOfAddingLastNode = 0;
        long numberOfAddedPredicates = 0;
//        while (true) {
//            grounded = weightLearner.backprop(grounded, grounded.network.sharedWeights, "DNC"); // one step BP only
//
//            double error = Evaluation.totalSquaredError(grounded);
//            averagesErrors.put(iteration, error);
//
//            //double maxError = Evaluation.maxSquaredError(grounded); // max error over outputs of every sample
//            double maxError = 100.0d;
//
//            if (canStopNodeGrowth(iteration, averagesErrors, maxError) || MAX_NUMBER_OF_ADDED_PREDICATES < numberOfAddedPredicates) {
//                break;
//            }
//
//            if (addNewNode(averagesErrors, iteration, timeOfAddingLastNode)) {
//                newRules = generator.generateRules(templateLambdaHead, basePredicates, template);
//                List<String> currentRules = new ArrayList<>(newRules.getValue());
//                String finalOutputRule = generateBaseRuleConnectedToOutput(newRules.getKey());
//                currentRules.add(finalOutputRule);
//                templateLambdaHead.add(newRules.getKey());
//
//                grounded = regrounder.reGroundMe(grounded, currentRules);
//                timeOfAddingLastNode = iteration;
//                numberOfAddedPredicates++;
//                template.addAll(currentRules);
//            }
//            iteration++;
//        }

        System.out.println("learned with added predicates: " + grounded);
    }

    private boolean addNewNode(Map<Long, Double> averagesErrors, long time, long timeOfAddingLastNode) {
        if (timeOfAddingLastNode > time - timeWindow) {
            return false;
        }

        Double averT = averagesErrors.get(time);
        Double averTMinusWindow = averagesErrors.get(time - timeWindow);
        Double averAtAddedNode = averagesErrors.get(timeOfAddingLastNode);

        return Math.abs(averT - averTMinusWindow) / averAtAddedNode < deltaT;
    }

    private String generateBaseRuleConnectedToOutput(Predicate predicate) {
        return RuleTools.constructOrRuleWithZeroArity("finalKappa", predicate, 0.0);
    }

    private boolean canStopNodeGrowth(long time, Map<Long, Double> averagesErrors, double maxError) {
        double aT = averagesErrors.get(time);
        return aT < Ca && maxError < Cm;
    }
}
