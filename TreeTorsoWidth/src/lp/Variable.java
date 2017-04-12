package lp;


/**
 * Created by Verena on 07.03.2017.
 */
public abstract class Variable<T extends Number> {
    private String name;
    private T value;

    private T upperBound = null;
    private T lowerBound = null;

    public Variable(String name) {
        this.name = name;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(T upperBound) {
        this.upperBound = upperBound;
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(T lowerBound) {
        this.lowerBound = lowerBound;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean isInteger();
}
