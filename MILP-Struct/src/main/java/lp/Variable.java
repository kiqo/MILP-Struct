package main.java.lp;


/**
 * Created by Verena on 07.03.2017.
 */
public abstract class Variable<T extends Number> {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean isInteger();
}
