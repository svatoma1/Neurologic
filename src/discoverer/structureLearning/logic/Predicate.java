package discoverer.structureLearning.logic;

/**
 * Created by EL on 19.4.2016.
 */
public class Predicate {

    private final String name;
    private final int arity;

    public Predicate(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    public String getName() {
        return name;
    }

    public int getArity() {
        return arity;
    }

    @Override
    public String toString() {
        return "Predicate{" +
                "name='" + name + '\'' +
                ", arity=" + arity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Predicate predicate = (Predicate) o;

        if (arity != predicate.arity) return false;
        if (!name.equals(predicate.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + arity;
        return result;
    }
}
