/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.estimators;

import exit.matrices.CrossImpactMatrix;
import exit.matrices.EXITImpactMatrix;
import java.io.PrintStream;

/**
 *
 * @author jmpaon
 */
public abstract class Pruner {
    
    public final EXITImpactMatrix matrix;
    public final PrintStream reportingStream;    
    
    public Pruner(EXITImpactMatrix matrix) {
        this(matrix, null);
    }
    
    public Pruner(EXITImpactMatrix matrix, PrintStream reportingStream) {
        this.matrix = matrix;
        this.reportingStream = reportingStream;
    }
    
    protected void report(String text) {
        assert text != null;
        if(this.reportingStream != null) reportingStream.println(text);
    }
    
    /**
     * Estimates a summed impact matrix for <b>matrix</b> based on a pruning strategy.
     * @param threshold The threshold value for significant relative impact.
     * Impact chains with relative impact higher than or equal to threshold
     * are expanded and the relative impacts of the expansions are summed into the estimate,
     * and the next order expansions are generated for the expansions if their
     * relative impact exceeds the threshold value.
     * @return CrossImpactMatrix : total (summed direct and indirect) impacts estimated by pruning
     */
    public abstract CrossImpactMatrix estimateSummedImpactMatrix(double threshold);
    
}
