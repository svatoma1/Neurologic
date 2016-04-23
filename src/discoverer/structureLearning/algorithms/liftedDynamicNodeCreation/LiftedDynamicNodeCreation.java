package discoverer.structureLearning.algorithms.liftedDynamicNodeCreation;

import discoverer.GroundedDataset;
import discoverer.bridge.*;
import discoverer.learning.Result;
import discoverer.learning.Results;
import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.StructureLearnable;
import discoverer.structureLearning.algorithms.ruleGenerator.BaseRuleGenerator;
import discoverer.structureLearning.algorithms.ruleGenerator.RuleGenerator;
import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;
import org.apache.commons.cli.CommandLine;
import structureLearning.StructureLearning;

import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * Created by EL on 19.4.2016.
 */
public class LiftedDynamicNodeCreation implements StructureLearnable {

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

        StructureLearning structureBridge = new StructureLearning();
        String params = "-r " + cmd.getOptionValue("r") + " -e " + cmd.getOptionValue("e");
        GroundedDataset grounded = structureBridge.init(params);

        List<String> template = new ArrayList<>();

        Set<Predicate> templateLambdaHead = new HashSet<>();
        Set<Predicate> basePredicates = RuleTools.retrievePredicates(exs);

        Pair<Predicate, List<String>> newRules = generator.generateRules(templateLambdaHead, basePredicates, template);
        template.addAll(newRules.getValue());
        template.add(generateBaseRuleConnectedToOutput(newRules.getKey()));
        templateLambdaHead.add(newRules.getKey());

        // nebo musi byt uplne vzdy na konci?
        template.add(RuleTools.constructAndRuleWithZeroArity("finalLambda", new Predicate("finalKappa", 0)));

        grounded = structureBridge.reGroundMe(grounded, template.toArray(new String[template.size()]));

        System.out.println("rules");
        template.forEach(e -> System.out.println("\t" + e));

        Map<Long, Double> averagesErrors = new HashMap<>();
        long iteration = 0;
        long timeOfAddingLastNode = 0;
        long numberOfAddedPredicates = 0;
        while (true) {
            // TODO correct what is inside
            Results trainResults = structureBridge.train(grounded.network, grounded.sampleSplitter.getTrain(), 1, Integer.MAX_VALUE, 0); // one step BP only

            double error = trainResults.actualResult.getError();
            averagesErrors.put(iteration, error);

            double maxError = maxSquaredError(trainResults); // max error over outputs of every sample

            if (canStopNodeGrowth(iteration, averagesErrors, maxError) || MAX_NUMBER_OF_ADDED_PREDICATES < numberOfAddedPredicates) {
                System.out.println("\tending bacause of nod growth");
                break;
            }

            if (addNewNode(averagesErrors, iteration, timeOfAddingLastNode)
                    && numberOfAddedPredicates < MAX_NUMBER_OF_ADDED_PREDICATES) {
                newRules = generator.generateRules(templateLambdaHead, basePredicates, template);
                List<String> currentRules = new ArrayList<>(newRules.getValue());
                String finalOutputRule = generateBaseRuleConnectedToOutput(newRules.getKey());
                currentRules.add(finalOutputRule);
                templateLambdaHead.add(newRules.getKey());
                template.addAll(currentRules);

                grounded = structureBridge.reGroundMe(grounded, template.toArray(new String[template.size()]));
                timeOfAddingLastNode = iteration;
                numberOfAddedPredicates++;
                System.out.println("\tadded next\t" + newRules.getKey().getName() + "\t" + numberOfAddedPredicates);
            }
            iteration++;
            System.out.print(", " + iteration);
        }

        System.out.println("learned with added predicates: " + grounded);
    }

    private double maxSquaredError(Results results) {
        ToDoubleFunction<? super Result> squaredError = res -> Math.pow(res.getExpected() - res.getActual(), 2);
        return results.results.stream()
                .mapToDouble(squaredError)
                .max().orElse(0);
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
        return RuleTools.constructOrRuleWithZeroArity("finalKappa", predicate, 0.5);
    }

    private boolean canStopNodeGrowth(long time, Map<Long, Double> averagesErrors, double maxError) {
        double aT = averagesErrors.get(time);
        return aT < Ca && maxError < Cm;
    }
}
