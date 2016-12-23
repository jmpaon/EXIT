/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.List;

/**
 *
 * @author juha
 */
public class QuickSampler extends Sampler {

    public QuickSampler(EXITImpactMatrix matrix) {
        super(matrix);
    }

    @Override
    public CrossImpactMatrix estimateSummedImpacts(int sampleSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double estimateSummedImpact(int impactorIndex, int impactedIndex, int sampleSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double estimateSummedImpact(int impactorIndex, int impactedIndex, int chainLength, int sampleSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /* 
    - luo kaikki parittaiset vaikutukset
    - ota otos näistä (ilman ketjujen muodostamista)
    - kerro keskenään mock-ketju muodostaen
    - 
    
    */
    
    
    double impactOfChain(int[] indices) {
        assert indices.length > 1 : "Chain length must be greater than 1";
        double impact = 1;
        double max = matrix.getMaxImpact();
        for(int i=0;i<indices.length-1;i++) {
            impact *= matrix.getValue(indices[i],indices[i+1]) / max;
        }
        return impact;
    }
    
    double impactOfChain(List<Integer> indices) {
        
    }
    
    
    
}
