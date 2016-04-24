package discoverer.structureLearning.algorithms.liftedCascadeCorrelation;

import discoverer.GroundedDataset;
import discoverer.LiftedDataset;
import discoverer.construction.network.rules.KappaRule;
import discoverer.construction.template.Kappa;
import discoverer.drawing.GroundDotter;
import discoverer.grounding.network.GroundKappa;
import discoverer.grounding.network.GroundLambda;
import discoverer.learning.Results;
import discoverer.learning.Sample;
import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.StructureLearnable;
import discoverer.structureLearning.algorithms.ruleGenerator.CascadeRuleGenerator;
import discoverer.structureLearning.algorithms.ruleGenerator.RuleGenerator;
import discoverer.structureLearning.logic.Predicate;
import discoverer.structureLearning.tools.Tools;
import javafx.util.Pair;
import org.apache.commons.cli.CommandLine;
import structureLearning.StructureLearning;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Created by EL on 19.4.2016.
 */
public class LiftedCascadeCorrelation implements StructureLearnable {

    // TODO parametrize
    private final long RULES_LIMIT = 50;
    private static final long POOL_SIZE = 1;
    private static final Double EPSILON_CONVERGENT = 0.1;
    private static final Integer LONG_TIME_WINDOW = 30;
    private static final Integer SHORT_TIME_WINDOW = 10;
    private static final Double CONVERGENT_ERROR = 0.01;
    int learningCycleLimits = 2500;
    int shortTimeWindow = 10;
    int longTimeWindow = shortTimeWindow * 3;
    double epsilonConvergent = 0.1;
    private final Double learningRate = 0.5;


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
        String finalRulePredicate = "finalKappa";

        StructureLearning structureBridge = new StructureLearning();
        String params = "-r " + cmd.getOptionValue("r") + " -e " + cmd.getOptionValue("e");
        GroundedDataset grounded = structureBridge.init(params);

        List<String> template = new ArrayList<>();
        Set<Predicate> templateLambdaHeads = new HashSet<>();
        Set<Predicate> basePredicates = RuleTools.retrievePredicates(exs);

        template.addAll(RuleTools.constructNodeOutputNodes(basePredicates, finalRulePredicate, 0.0));
        template.add(RuleTools.constructAndRuleWithZeroArity("finalLambda", new Predicate("finalKappa", 0))); // nebo musi byt uplne vzdy na konci?

        /*grounded.sampleSplitter.samples.forEach(sample -> {
            if (sample.neuralNetwork != null) {
                System.out.println("" + sample.neuralNetwork.allNeurons + "\t" + sample.neuralNetwork.allNeurons.length);
            }


            GroundDotter.drawAVG(sample.getBall(), "atom2");

            sample.getBall().groundNeurons.forEach(s -> {
                System.out.println(s.getId());

                if (s instanceof GroundKappa) {

//
//                    System.out.println("\tkappa");
//                    GroundKappa kappa = (GroundKappa) s;
//                    System.out.println("\t" + kappa.toString());

                } else if (s instanceof GroundLambda) {
                    System.out.println("\tlambda");
                    GroundLambda lambda = (GroundLambda) s;


                    System.out.println("\t" + lambda.toString());
                    lambda.getConjunctsAvg().entrySet().forEach(e -> {
                        System.out.println("\t\t" + e.getKey().toString());
                        e.getKey().getGeneral().getRules().forEach(ee -> {
                            System.out.println("\t\t\t" + ee.getHead() + "\t" + ee.getWeight());
                        });
                    });

                } else {
                    System.out.println("\t" + s.getClass());
                }
            });

        });

        System.exit(-1111);*/

        grounded = structureBridge.reGroundMe(grounded, template.toArray(new String[template.size()]));

        long numberOfAddedPredicates = 0;
        List<Double> errors = new ArrayList<>();

        while (true) {
            Results trainResults = structureBridge.train(grounded.network, grounded.sampleSplitter.getTrain(), Integer.MAX_VALUE, 1, 0); // learn only output connections

            double currentError = trainResults.actualResult.getError();
            errors.add(currentError);

            if (stopCascadeCorrelation(numberOfAddedPredicates, errors)) {
                break;
            }

            final GroundedDataset finalGrounded = grounded;
            final List<String> finalTemplate = template;
            CandidateWrapper bestCandidate = LongStream.range(0, POOL_SIZE)
                    //.parallel()
                    .mapToObj(i -> makeAndLearnCandidate(finalGrounded, structureBridge, new ArrayList<>(finalTemplate), basePredicates, new HashSet<>(templateLambdaHeads), finalRulePredicate))
                    .max(CandidateWrapper::compare)
                    .get();

            templateLambdaHeads.add(bestCandidate.getPredicate());
            template = bestCandidate.getRules();
            grounded = bestCandidate.getGrounded();

            numberOfAddedPredicates++;
        }

        System.out.println("learned with added predicates: " + grounded);
    }


    private boolean stopCascadeCorrelation(long numberOfAddedRules, List<Double> errors) {
        return numberOfAddedRules > RULES_LIMIT
                || Tools.hasConverged(errors, LONG_TIME_WINDOW, SHORT_TIME_WINDOW, EPSILON_CONVERGENT)
                || (errors.size() > 0 && errors.get(errors.size() - 1) < CONVERGENT_ERROR);
    }

    private CandidateWrapper makeAndLearnCandidate(GroundedDataset grounded, StructureLearning regrounder, List<String> template, Set<Predicate> basePredicates, Set<Predicate> templateLambdaHeads, String finalRulePredicate) {
        // making phase
        Pair<Predicate, List<String>> generated = generator.generateRules(templateLambdaHeads, basePredicates, template);

        Predicate lambdaPredicate = generated.getKey();
        List<String> extension = generated.getValue();
        extension.add(RuleTools.constructOrRuleWithZeroArity(finalRulePredicate, lambdaPredicate, 0.0));
        template.addAll(extension);
        GroundedDataset candidateGrounded = regrounder.reGroundMe(grounded, template.toArray(new String[template.size()]));


        // learning phase
        List<Sample> samples = candidateGrounded.sampleSplitter.samples; // TODO select which ones
        Map<Sample, Double> cachedOutputErrors = new HashMap<>();
        samples.forEach(sample -> {
            double error = Math.pow(sample.targetValue - sample.neuralNetwork.outputNeuron.outputValue, 2);
            cachedOutputErrors.put(sample, error);
        });

        Pair<Double, List<String>> learned = gradientAscent(candidateGrounded, samples, lambdaPredicate, template, extension, cachedOutputErrors);
        List<String> adjustedWholeTemplate = learned.getValue(); // TODO - prepocist vahy nebo tak neco
        double correlation = learned.getKey();

        return new CandidateWrapper(lambdaPredicate, adjustedWholeTemplate, correlation, candidateGrounded);
    }

    private Pair<Double, List<String>> gradientAscent(LiftedDataset candidateGrounded, List<Sample> samples, Predicate lambdaPredicate, List<String> templateWithExtension, List<String> extension, Map<Sample, Double> cachedOutputErrors) {
        double outputAverageError = cachedOutputErrors.entrySet().stream()
                .mapToDouble(e -> e.getValue())
                .average().orElse(0);

        Map<Sample, GroundLambda> sampleLambda = retrieveLambdas(samples, lambdaPredicate);
        //Map<Sample, List<GroundKappa>> sampleKappas = retrieveKappas(samples, sampleLambda);

        List<Double> correlations = new ArrayList<>();

        // gradient ascent
        while (correlations.size() < learningCycleLimits && !hasConverged(correlations)) {
            Double candidateAverage = samples.stream()
                    .mapToDouble(sample -> sampleLambda.get(sample).getValueAvg())
                    .average().orElse(0);

            // should be after update, but... whatever
            double correlation = computeCorrelation(samples, sampleLambda, candidateAverage, outputAverageError, cachedOutputErrors);
            correlations.add(correlation);

            updateWeights(candidateGrounded, samples, sampleLambda, candidateAverage, outputAverageError);
        }

        // TODO remapping weights from learned weights to template?
        return new Pair<>(correlations.get(correlations.size() - 1), templateWithExtension);
    }

    private void updateWeights(LiftedDataset candidateGrounded, List<Sample> samples, Map<Sample, GroundLambda> sampleLambda, Double candidateAverage, Double outputAverageError) {
        Map<KappaRule, Double> updates = new HashMap<>();
        samples.forEach(sample -> updateWeights(sample, sampleLambda, candidateAverage, outputAverageError, updates, candidateGrounded));

        // adding deltas
        //updates.entrySet().forEach(entry -> candidateGrounded.network.sharedWeights[entry.getKey()] += learningRate * entry.getValue());
        updates.entrySet().forEach(entry -> {
            double oldValue = entry.getKey().getWeight();
            double updated = oldValue + learningRate * entry.getValue();
            entry.getKey().setWeight(updated);
        });
    }

    private void updateWeights(Sample sample, Map<Sample, GroundLambda> sampleLambda, Double candidateAverage, Double outputAverageError, Map<KappaRule, Double> updates, LiftedDataset candidateGrounded) {
        Set<Kappa> set = sampleLambda.get(sample).getConjunctsAvg().keySet().stream()
                .map(groundKappa -> groundKappa.getGeneral()).collect(Collectors.toSet());
        set.stream().forEach(kappa -> kappa.getRules().forEach(kappaRule -> updateKappaRule(kappaRule, kappa, sample, sampleLambda, candidateAverage, outputAverageError, updates, candidateGrounded)));
    }

    private void updateKappaRule(KappaRule kappaRule, Kappa kappa, Sample sample, Map<Sample, GroundLambda> sampleLambda, Double candidateAverage, Double outputAverageError, Map<KappaRule, Double> updates, LiftedDataset candidateGrounded) {
//        Double value = Math.signum(sample.neuralNetwork.outputNeuron.outputValue - outputAverageError)
//                * kappa.getFirstDerivationAtX(kappa.getSummedInputs())
//                * kappaRule.getOutputValue();
//
//        if (!updates.containsKey(kappaRule)) {
//            updates.put(kappaRule, 0.0d);
//        }
//        Double oldValue = updates.get(kappaRule);
//        updates.put(kappaRule, oldValue + value);
    }

    private boolean hasConverged(List<Double> correlations) {
        if (correlations.size() < longTimeWindow) {
            return false;
        }
        int maxId = correlations.size();
        return Math.abs(average(correlations.subList(maxId - longTimeWindow, maxId)) - average(correlations.subList(maxId - shortTimeWindow, maxId))) < epsilonConvergent;
    }

    private double average(List<Double> list) {
        return list.stream().mapToDouble(e -> e).average().orElse(0);
    }

    private double computeCorrelation(List<Sample> samples, Map<Sample, GroundLambda> sampleLambda, Double candidateAverage, Double outputAverageError, Map<Sample, Double> cachedOutputsFromOldTemplate) {
        Double value = samples.stream()
                .mapToDouble(sample -> {
                    GroundLambda lambda = sampleLambda.get(sample);
                    return (lambda.getValueAvg() - candidateAverage) * (cachedOutputsFromOldTemplate.get(sample) - outputAverageError);
                }).sum();

        return Math.abs(value);
    }

    private Map<Sample, List<GroundKappa>> retrieveKappas(List<Sample> samples, Map<Sample, GroundLambda> sampleLambda) {
        Map<Sample, List<GroundKappa>> mapping = new HashMap<>();
        samples.forEach(sample -> mapping.put(sample, retrieveKappa(sampleLambda.get(sample))));
        return mapping;
    }

    private List<GroundKappa> retrieveKappa(GroundLambda lambda) {
        return lambda.getConjunctsAvg().entrySet().stream()
                .map(entry -> entry.getKey())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<Sample, GroundLambda> retrieveLambdas(List<Sample> samples, Predicate lambdaPredicate) {
        Map<Sample, GroundLambda> mapping = new HashMap<>();

        int kappaLength = "Kappa".length();
        String rootName = lambdaPredicate.getName().substring(0, lambdaPredicate.getName().length() - kappaLength);
        String notNulArityName = rootName + "(";
        String closingBracket = rootName + ")#";
        String hashtagOnly = rootName + "#";

        // no other check here needed, because we are guaranteed that that node will bind (at least for predicate with zero arity)
        samples.forEach(sample -> mapping.put(sample, retrieveLambda(sample, notNulArityName, closingBracket, hashtagOnly)));

        return mapping;
    }

    private GroundLambda retrieveLambda(Sample sample, String notNulArityName, String closingBracket, String hashtagOnly) {
        return sample.getBall().groundNeurons.stream()
                .filter(neuron -> neuron instanceof GroundLambda)
                .filter(neuron ->
                        neuron.toString().equals(notNulArityName)
                                || neuron.toString().equals(closingBracket)
                                || neuron.toString().equals(hashtagOnly)) // we assume that automatically added neurons are unique by name
                .map(neuron -> (GroundLambda) neuron)
                .collect(Collectors.toList())
                .get(0); // there should be only one instance of that lambda predicate within a sample, thus it must be the first one; more or none such lambda node would be a fault
    }

}
