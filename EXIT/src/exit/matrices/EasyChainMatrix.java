/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

/**
 *
 * @author juha
 */
public class EasyChainMatrix extends CrossImpactMatrix {
    
    public EasyChainMatrix(int varCount, String[] names, double[] values, boolean onlyIntegers) {
        super(varCount, names, values, onlyIntegers);
    }
    
    public EasyChainMatrix(CrossImpactMatrix cim) {
        super(cim);
    }
    
    public EasyChainMatrix(SquareMatrix sm) {
        super(sm);
    }
    
    @Override
    protected double multiplyEntries(int row, int col) {
        return multiplyEntries(this, row, col);
    }
    
    protected double multiplyEntries(SquareMatrix colMatrix, int row, int col) {
        if(row == col) return 0;
        return EasyChainMatrix.multiplyEntries(this, colMatrix, row, col);
    }
    
    
    public EasyChainMatrix power(EasyChainMatrix m) {
        EasyChainMatrix powerMatrix = new EasyChainMatrix(this.copyWithoutValues());
        
        for (int row = 1; row <= varCount; row++) {
            for (int col = 1; col <= varCount; col++) {
                powerMatrix.setValue(row, col, this.multiplyEntries(m, row, col));
            }
        }
        return powerMatrix;
    }
    
    
    
}
