package main.java.lp;

import java.util.List;

/**
 * Created by Verena on 07.03.2017.
 */
public class Row {
    private String name;
    private List<MatrixEntry> entries;

    public Row() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MatrixEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MatrixEntry> entries) {
        this.entries = entries;
    }
}
