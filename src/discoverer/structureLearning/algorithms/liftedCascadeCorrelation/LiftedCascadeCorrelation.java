package discoverer.structureLearning.algorithms.liftedCascadeCorrelation;

import discoverer.LiftedDataset;
import discoverer.bridge.*;
import discoverer.grounding.network.GroundKappa;
import discoverer.grounding.network.GroundLambda;
import discoverer.grounding.network.groundNetwork.GroundNeuron;
import discoverer.learning.Sample;
import discoverer.learning.Weights;
import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.StructureLearnable;
import discoverer.structureLearning.algorithms.ruleGenerator.CascadeRuleGenerator;
import discoverer.structureLearning.algorithms.ruleGenerator.RuleGenerator;
import discoverer.structureLearning.logic.Predicate;
import discoverer.structureLearning.tools.Tools;
import javafx.util.Pair;
import org.apache.commons.cli.CommandLine;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.stream.Collectors;

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

        grounded.sampleSplitter.samples.forEach(sample -> {
            System.out.println("" + sample.neuralNetwork.allNeurons + "\t" + sample.neuralNetwork.allNeurons.length);


            //GroundDotter.drawAVG(sample.getBall(),"atom");

            for (int idx = 0; idx < sample.neuralNetwork.allNeurons.length; idx++) {
                GroundNeuron neuron = sample.neuralNetwork.allNeurons[idx];
                if (null != neuron) {
                    System.out.println(idx + "\t" + neuron);
                }
            }

            sample.getBall().groundNeurons.forEach(s -> {
                System.out.println(s.getId());

                if (s instanceof GroundKappa) {
                    System.out.println("\tkappa");
                    GroundKappa kappa = (GroundKappa) s;
                    System.out.println("\t" + kappa.toString());


                } else if (s instanceof GroundLambda) {
                    System.out.println("\tlambda");
                    GroundLambda lambda = (GroundLambda) s;
                    System.out.println("\t" + lambda.toString());
                    lambda.getConjuncts().forEach(e -> System.out.println("\t\t" + e.toString()));

                } else {
                    System.out.println("\t" + s.getClass());
                }
            });

        });

        System.exit(-1111);
        //

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

        double weight = 0.0d;// misto toho taky weightInitializer
        Predicate predicate = generated.getKey();
        List<String> extension = generated.getValue();
        extension.add(RuleTools.constructOrRuleWithZeroArity(finalRulePredicate, predicate, weight));
        LiftedDataset candidateGrounded = regrounder.reGroundMe(grounded, extension);

        // learning
        Pair<Double, List<String>> learned = gradientAscent(candidateGrounded, predicate);

        List<String> newRules = learned.getValue(); // TODO
        double correlation = learned.getKey();

        return new CandidateWrapper(predicate, newRules, correlation, candidateGrounded);
    }

    private Pair<Double, List<String>> gradientAscent(LiftedDataset candidateGrounded, Predicate lambdaPredicate) {
        List<Sample> samples = candidateGrounded.sampleSplitter.samples; // TODO select which ones
        Map<Sample, GroundLambda> sampleLambda = retrieveLambdas(samples, lambdaPredicate);
        Map<Sample, Set<Weights>> sampleWeight = retrieveWeights(samples, sampleLambda);
        //return new Pair<>(correlation,null);
        return null;
    }

    private Map<Sample, Set<Weights>> retrieveWeights(List<Sample> samples, Map<Sample, GroundLambda> sampleLambda) {
        Map<Sample, Set<Weights>> mapping = new HashMap<>();
        samples.forEach(sample -> mapping.put(sample, retrieveWeight(sample, sampleLambda.get(sample))));
        return mapping;
    }

    private Set<Weights> retrieveWeight(Sample sample, GroundLambda lambda) {
        //lambda.
        throw new NotImplementedException();
    }

    private Map<Sample, GroundLambda> retrieveLambdas(List<Sample> samples, Predicate lambdaPredicate) {
        Map<Sample, GroundLambda> mapping = new HashMap<>();

        int kappaLength = "Kappa".length();
        String rootName = lambdaPredicate.getName().substring(0, lambdaPredicate.getName().length() - kappaLength);
        String notNulArityName = rootName + "(";
        String closingBracket = rootName + ")#";
        String hashtagOnly = rootName + "#";

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
