package discoverer.structureLearning.algorithms.ruleGenerator;

import discoverer.structureLearning.logic.Predicate;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;

/**
 * Created by EL on 20.4.2016.
 */
public interface RuleGenerator {

    /**
     * Creates fresh new predicate atomX, connected to all base predicates (OR), e.g. w atomX :- male(X0),  w atomX :- sibling(X0,X1).
     * And returns lambda predicate atomXLambda, connected to (AND) atomX, e.g. atomXLambda :- atomX.
     *
     * @param templateHeads
     * @param basePredicates
     * @param template
     * @return
     */
    public Pair<Predicate,List<String>> generateRules(Set<Predicate> templateHeads, Set<Predicate> basePredicates, List<String> template);
}
