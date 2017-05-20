package tests.java;

import main.java.lp.LinearProgram;
import main.java.lp.MatrixEntry;
import main.java.lp.MatrixRow;
import main.java.lp.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.java.parser.MILPParser;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Verena on 28.03.2017.
 */
public class MILPParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MILPParserTest.class);

    @Test
    public void testMILPParser() {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = null;
        try {
            lp = milpParser.parseMPS("input/tests/bienst2_test.mps", false);
        } catch (IOException e) {
            LOGGER.error("", e);
        }

        assertNotNull(lp);
        assertNotNull(lp.getName());
        assertNotNull(lp.getConstraints());
        assertNotNull(lp.getObjectiveFunction());
        assertNotNull(lp.getRows());
        assertNotNull(lp.getVariables());

        // test variables
        for (Variable var : lp.getVariables().values()) {

            // variables from 'xa*' to 'xe*' are integer variables
            if (var.getName().startsWith("x") && var.getName().charAt(1) >= 'a' && var.getName().charAt(1) <= 'e') {
                Assert.assertTrue(var.isInteger());
            } else {
                Assert.assertTrue(!var.isInteger());
            }

            // test bounds
            if (var.getName().equals("floab")) {
                Assert.assertEquals(-3, (Double) var.getLowerBound(), 0);
            }
            if (var.getName().equals("floac")) {
                Assert.assertEquals(5, (Double) var.getLowerBound(), 0);
                Assert.assertEquals(5, (Double) var.getUpperBound(), 0);
            }
            if (var.getName().equals("fload")) {
                Assert.assertNull(var.getLowerBound());
            }
        }

        for (String variableName : lp.getVariables().keySet()) {
            boolean nameCorrect = lp.getVariables().get(variableName).getName().equals(variableName);
            assertFalse("Variables map is not correct for " + variableName, !nameCorrect);
        }

        // test objective function
        Assert.assertEquals(lp.getObjectiveFunction().getName(), "r_0");

        // test matrix rows
        for (MatrixRow row : lp.getConstraints()) {

            // equality correct
            if (row.getName().startsWith("CON")) {
                Assert.assertEquals(row.getEquality(), LinearProgram.Equality.GREATER_THAN);
                Assert.assertEquals(0.0, row.getRightHandSide(), 0.0);
            } else if (row.getName().startsWith("VUB")) {
                Assert.assertEquals(row.getEquality(), LinearProgram.Equality.LESS_THAN);
                Assert.assertEquals(0.0, row.getRightHandSide(), 0.0);
            } else {
                Assert.assertEquals(row.getEquality(), LinearProgram.Equality.EQUAL);
                if (row.getName().startsWith("IN") || row.getName().startsWith("OUT") || row.getName().startsWith("BAL")) {
                    Assert.assertNotEquals(0.0, row.getRightHandSide(), 0.0);
                }
            }
        }

        // test coefficients
        for (MatrixEntry entry : lp.getRows().get("r_0").getEntries()) {
            if (entry.getVariable().getName().equals("z")) {
                Assert.assertEquals(1, entry.getCoefficient(), 0.0);
            }
        }

        for (MatrixEntry entry : lp.getRows().get("VUBbha").getEntries()) {
            if (entry.getVariable().getName().equals("xha")) {
                Assert.assertEquals(-72, entry.getCoefficient(), 0.0);
            }
            if (entry.getVariable().getName().equals("fbha")) {
                Assert.assertEquals(1, entry.getCoefficient(), 0.0);
            }
        }

        // test RHS
        Assert.assertEquals(10, lp.getConstraints().stream().filter(entry -> entry.getName().equals("BALhe")).findFirst().get().getRightHandSide(), 0.0f);

    }

    /*
    Tests the MILPParser in case that the COLUMNS section contains for one variable two MatrixEntries in one line
     */
    @Test
    public void testMoreColumns() {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = null;
        try {
            lp = milpParser.parseMPS("./../input/benchmarks/roll3000.mps", false);
        } catch (IOException e) {
            LOGGER.error("", e);
        }

        // test objective function
        Assert.assertEquals(lp.getObjectiveFunction().getName(), "obj");
        Assert.assertEquals(lp.getConstraints().get(0).getEquality(), LinearProgram.Equality.GREATER_THAN);

        // test objective function
        for (MatrixEntry entry : lp.getRows().get("obj").getEntries()) {
            // only one entry
            Assert.assertEquals(entry.getVariable().getName(), "x1");
            assertEquals(1, entry.getCoefficient());
        }

        // test coefficients
        assertEquals(17, lp.getRows().get("c617").getEntries().size());
        List<MatrixEntry> entries = lp.getRows().get("c617").getEntries();

        assertEquals(1, entries.get(0).getCoefficient());
        Assert.assertEquals("x6", entries.get(0).getVariable().getName());
        assertEquals(47, entries.get(1).getCoefficient());
        Assert.assertEquals("x244", entries.get(1).getVariable().getName());
        assertEquals(47, entries.get(3).getCoefficient());
        Assert.assertEquals("x246", entries.get(3).getVariable().getName());

        // test integer variables
        for (Variable var : lp.getVariables().values()) {

            // variables from x244 to x981 are integer variables
            int numVariable = Integer.valueOf(var.getName().substring(1).trim());
            if (numVariable >= 244 && numVariable <= 981) {
                Assert.assertTrue(var.isInteger());
            } else {
                Assert.assertTrue(!var.isInteger());
            }
        }

        // test bounds
        // x1 free variable
        Assert.assertNull(lp.getVariables().get("x1").getUpperBound());
        Assert.assertNull(lp.getVariables().get("x1").getLowerBound());

        Assert.assertNotNull(lp.getVariables().get("x2").getUpperBound());
        assertEquals(1000000000.0, (double) lp.getVariables().get("x2").getUpperBound());
        Assert.assertNull(lp.getVariables().get("x2").getLowerBound());
    }

    private void assertEquals(double expected, double actual) {
        Assert.assertEquals(expected, actual, 0.0);
    }
}
