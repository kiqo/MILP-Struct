package main.java.lp;

import java.util.List;

/**
 * Created by Verena on 07.03.2017.
 */
public class Row {
    private String name;
    private List<Variable> variableEntries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Variable> getVariableEntries() {
        return variableEntries;
    }

    public void setVariableEntries(List<Variable> variableEntries) {
        this.variableEntries = variableEntries;
    }
}
