package tests.java;

import main.java.lp.LinearProgram;
import main.java.lp.MatrixRow;
import main.java.lp.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests whether a MILP instance is parsed correctly
 */
public class MILPParserTest extends StructuralParametersTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MILPParserTest.class);

    @Test
    public void testMILPParser() throws InterruptedException {
        lp = createLinearProgram("../input/tests/bienst2_test.mps");

        assertCorrectResult();
    }

    private void assertCorrectResult() {
        assertLPNotNull();
        testSingleMatrixEntryInColumns();
    }

    private void testSingleMatrixEntryInColumns() {
        // test variables
        for (Variable var : lp.getVariables().values()) {

            // variables from 'xa*' to 'xe*' are integer variables
            if (var.getName().startsWith("x") && var.getName().charAt(1) >= 'a' && var.getName().charAt(1) <= 'e') {
                Assert.assertTrue(var.isInteger());
            } else {
                Assert.assertTrue(!var.isInteger());
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

        // test RHS
        Assert.assertEquals(10, lp.getConstraints().stream().filter(entry -> entry.getName().equals("BALhe")).findFirst().get().getRightHandSide(), 0.0f);
    }

    private void assertLPNotNull() {
        assertNotNull(lp);
        assertNotNull(lp.getName());
        assertNotNull(lp.getConstraints());
        assertNotNull(lp.getObjectiveFunction());
        assertNotNull(lp.getRows());
        assertNotNull(lp.getVariables());
    }

    /*
    Tests the MILPParser in case that the COLUMNS section contains for one variable two MatrixEntries in one line
     */
    @Test
    public void testMoreColumns() throws InterruptedException {
        lp = createLinearProgram("./../input/benchmarks/roll3000.mps");
        assertLPNotNull();
        testDoubleMatrixEntryInColumns();
    }

    private void testDoubleMatrixEntryInColumns() {
        // test objective function
        Assert.assertEquals(lp.getObjectiveFunction().getName(), "obj");
        Assert.assertEquals(lp.getConstraints().get(0).getEquality(), LinearProgram.Equality.GREATER_THAN);

        // test objective function
        for (Variable entry : lp.getRows().get("obj").getVariableEntries()) {
            // only one entry
            Assert.assertEquals(entry.getName(), "x1");
        }

        // test MatrixEntries
        assertEquals(17, lp.getRows().get("c617").getVariableEntries().size());
        List<Variable> entries = lp.getRows().get("c617").getVariableEntries();

        Assert.assertEquals("x6", entries.get(0).getName());
        Assert.assertEquals("x244", entries.get(1).getName());
        Assert.assertEquals("x246", entries.get(3).getName());

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
    }

    private void assertEquals(double expected, double actual) {
        Assert.assertEquals(expected, actual, 0.0);
    }
}
