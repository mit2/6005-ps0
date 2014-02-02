package rules;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JUnit tests for RulesOf6005. 
 */
public class RulesOf6005Test {
    
    /**
     * Tests the mayUseCodeInAssignment method.
     */
    @Test
    public void testMayUseCodeInAssignment() {
        assertEquals(false, RulesOf6005.mayUseCodeInAssignment(false, true, false, false, false));
        assertEquals(true, RulesOf6005.mayUseCodeInAssignment(true, false, true, true, true));
    }
}
