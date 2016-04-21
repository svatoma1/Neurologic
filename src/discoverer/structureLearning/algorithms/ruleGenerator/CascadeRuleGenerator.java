package discoverer.structureLearning.algorithms.ruleGenerator;


import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;

/**
 * Created by EL on 20.4.2016.
 */
public class CascadeRuleGenerator implements RuleGenerator{
    @Override
    public Pair<Predicate, List<String>> generateRules(Set<Predicate> templateHeads, Set<Predicate> basePredicates, List<String> template) {
        Pair<Predicate, List<String>> rules = RuleTools.generateFreshPredicateAndCascadeRules(basePredicates, templateHeads);

        Predicate currentLambda = new Predicate(rules.getKey().getName() + "Lambda", 0);
        List<String> results = rules.getValue();
        results.add(RuleTools.constructAndRuleWithZeroArity(currentLambda, rules.getKey()));
        return new Pair<>(currentLambda, results);
    }
}
