/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

/**
 *
 * @author juha
 */
public class RandomInputMatrixGenerator {
    
    public static CrossImpactMatrix generateCrossImpactMatrix(int varCount, double density, double greatestImpact) {
        assert varCount > 1;
        assert density >= 0 && density <= 1;
        assert greatestImpact > 0;
        CrossImpactMatrix matrix = new CrossImpactMatrix(varCount);
        
        for(int impactor=1;impactor<=matrix.varCount;impactor++)
            for(int impacted=1;impacted<=matrix.varCount;impacted++) {
                if(impactor != impacted && Math.random() >= density) {
                    double newValue = Math.random() * greatestImpact;
                    matrix.setValue(impactor, impacted, newValue);
                }
            }
        
        return matrix;
    }
    
    public static CrossImpactMatrix generateCrossImpactMatrix(int varCount, double density, double greatestImpact, double meanImpact) {
        throw new UnsupportedOperationException();
    }
    
    public static EXITImpactMatrix generateEXITImpactMatrix(int varCount, double density, double greatestImpact, double maxImpact) {
        assert greatestImpact <= maxImpact;
        return new EXITImpactMatrix(generateCrossImpactMatrix(varCount, density, greatestImpact), maxImpact);
    }
    
    
}
