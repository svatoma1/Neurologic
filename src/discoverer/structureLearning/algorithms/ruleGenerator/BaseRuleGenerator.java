package discoverer.structureLearning.algorithms.ruleGenerator;

import discoverer.structureLearning.RuleTools;
import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;

/**
 * Created by EL on 20.4.2016.
 */
public class BaseRuleGenerator implements RuleGenerator {


    @Override
    public Pair<Predicate, List<String>> generateRules(Set<Predicate> templateHeadsWithoutFinal, Set<Predicate> basePredicates, List<String> template) {
        Pair<String, List<String>> baseRules = RuleTools.generateFreshBaseRule(basePredicates, templateHeadsWithoutFinal);

        Predicate currentLambda = new Predicate(baseRules.getKey() + "Lambda", 0);
        List<String> results = baseRules.getValue();
        results.add(RuleTools.constructAndRuleWithZeroArity(currentLambda, new Predicate(baseRules.getKey(), 0)));
        return new Pair<>(currentLambda, results);
    }
}
