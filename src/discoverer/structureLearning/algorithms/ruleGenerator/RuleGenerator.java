package discoverer.structureLearning.algorithms.ruleGenerator;

import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;

/**
 * Created by EL on 20.4.2016.
 */
public interface RuleGenerator {
    public Pair<Predicate,List<String>> generateRules(Set<Predicate> templateHeads, Set<Predicate> basePredicates, List<String> template);
}
