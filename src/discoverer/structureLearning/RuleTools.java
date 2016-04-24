package discoverer.structureLearning;

import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by EL on 19.4.2016.
 */
public class RuleTools {


    /**
     * Returns set of predicates (thus name and arity).
     *
     * @param examples
     * @return
     */
    public static Set<Predicate> retrievePredicates(String[] examples) {
        Set<Predicate> result = new HashSet<>();
        IntStream.range(0, examples.length)
                .mapToObj(idx -> examples[idx])
                .forEach(example -> result.addAll(retrievePredicate(example)));
        return result;
    }

    private static Set<Predicate> retrievePredicate(String example) {
        Set<Predicate> result = new HashSet<>();

        example = cutWeightNumber(example);
        example = example.trim().replaceAll("\\s", "");
        int endPoint = example.indexOf('.');
        example = example.substring(0, endPoint) + ",";

        String parsed = example;
        while (parsed.length() > 0) {
            int comma = parsed.indexOf(',');
            int opening = parsed.indexOf('(');
            if (comma < opening || -1 == opening) {
                if (-1 == comma) {
                    comma = parsed.length();
                }
                String predicate = parsed.substring(0, comma);
                result.add(new Predicate(predicate, 0));
                int shift = (comma == parsed.length()) ? 0 : 1; // ending case
                parsed = parsed.substring(comma + shift);
            } else {
                String predicate = parsed.substring(0, opening);
                int closingBracket = parsed.indexOf(')');
                String body = parsed.substring(opening + 1, closingBracket);
                result.add(new Predicate(predicate, body.split(",").length));
                int shift = (closingBracket + 1 == parsed.length()) ? 1 : 2; // ending case
                parsed = parsed.substring(closingBracket + shift);
            }
        }

        return result;
    }

    public static Pair<String, List<String>> generateFreshBaseRule(Set<Predicate> basePredicates, Set<Predicate> templatePredicates) {
        Set<Predicate> allPredicates = new HashSet<>(basePredicates);
        allPredicates.addAll(templatePredicates);
        String freshName = generateFreshName(allPredicates);

        double baseWeight = 0.0;
        List<String> rules = basePredicates.stream()
                .map(predicate -> constructOrRuleWithZeroArity(freshName, predicate, baseWeight))
                .collect(Collectors.toCollection(ArrayList::new));
        return new Pair<>(freshName, rules);
    }

    public static String constructOrRuleWithZeroArity(String freshName, Predicate predicate, double baseWeight) {
        // TODO change baseWeight to WeightInitializer
        return baseWeight + " " + freshName + " :- " + freshPredicate(predicate) + ".";
    }

    public static String constructAndRuleWithZeroArity(Predicate head, Predicate body) {
        return constructAndRuleWithZeroArity(head.getName(),body);
    }

    public static String constructAndRuleWithZeroArity(String freshName, Predicate predicate) {
        return freshName + " :- " + freshPredicate(predicate) + ".";
    }

    public static String freshPredicate(Predicate predicate) {
        if (0 == predicate.getArity()) {
            return predicate.getName();
        }

        String body = "";
        for (int i = 0; i < predicate.getArity(); i++) {
            if (body.length() > 0) {
                body += ",";
            }
            body += "X" + i;
        }

        return predicate.getName() + "(" + body + ")";
    }

    private static String generateFreshName(Set<Predicate> predicates) {
        Set<String> set = predicates.stream().map(predicate -> predicate.getName()).collect(Collectors.toSet());

        String kappaToken = "Kappa";
        String ruleName = "atom";
        long longCounter = (long) set.size();
        while (set.contains(ruleName + longCounter ) || set.contains(ruleName + longCounter + kappaToken )) {
            longCounter++;
        }
        return ruleName + longCounter;
    }

    public static Set<Predicate> retrieveHeadPredicates(List<String> template) {
        return template.stream()
                .map(rule -> retrieveHeadPredicate(rule))
                .filter(p -> null != p)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Returns null if rule is in shape of " :- pred(A)...", otherwise return "head/arity" from rule "[weight] head :- body"
     *
     * @param rule
     * @return
     */
    private static Predicate retrieveHeadPredicate(String rule) {
        String IMPICATION_DELIMITER = ":-";
        rule = cutWeightNumber(rule).trim();

        if (rule.startsWith(IMPICATION_DELIMITER)) {
            return null;
        } else {
            String[] headAndBody = rule.split(":-");
            String head = headAndBody[0];
            int arity = 0;
            String name = null;
            if (!head.contains("(")) {
                name = head;
                arity = 0;
            } else {
                name = head.split("\\(")[0];
                arity = head.split(",").length;
            }

            return new Predicate(name, arity);
        }
    }

    private static String cutWeightNumber(String example) {
        example = example.trim();
        String[] test = example.split(" ", 2);
        try {
            Double.parseDouble(test[0]);
            if (test.length > 1) {
                example = test[1];
            }
        } catch (NumberFormatException e) {
            // nothing to do
        }
        return example;
    }

    public static List<String> constructNodeOutputNodes(Set<Predicate> basePredicates, String finalRulePredicate, Double weight) {
        return basePredicates.stream()
                .map(bodyPredicate -> constructOrRuleWithZeroArity(finalRulePredicate, bodyPredicate, weight))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Pair<Predicate, List<String>> generateFreshPredicateAndCascadeRules(Set<Predicate> basePredicates, Set<Predicate> templateHeads) {
        Set<Predicate> allPredicates = new HashSet<>(basePredicates);
        allPredicates.addAll(templateHeads);
        String freshName = generateFreshName(allPredicates);

        double baseWeight = 0.0;

        List<String> rules = allPredicates.stream()
                .map(predicate -> constructOrRuleWithZeroArity(freshName, predicate, baseWeight))
                .collect(Collectors.toCollection(ArrayList::new));
        return new Pair<>(new Predicate(freshName, 0), rules);
    }
}
