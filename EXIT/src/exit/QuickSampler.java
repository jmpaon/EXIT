/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.Iterator;
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
        assert sampleSize > 0 : "SampleSize must be greater than 0";
        CrossImpactMatrix summedImpactMatrix = new CrossImpactMatrix(matrix.copy().flush());
        for(int impactor = 1; impactor <= matrix.varCount; impactor++) {
            for(int impacted = 1; impacted <= matrix.varCount; impacted++) {
                if(impactor != impacted) {
                    summedImpactMatrix.setValue(impactor, impacted, estimateSummedImpact(impactor, impacted, sampleSize));
                }
            }
        }
        return summedImpactMatrix;
    }

    @Override
    public double estimateSummedImpact(int impactorIndex, int impactedIndex, int sampleSize) {
        double summedImpact = matrix.getValue(impactorIndex, impactedIndex);
        for(int length=3;length<=matrix.varCount;length++) {
            summedImpact += estimateSummedImpact(impactorIndex, impactedIndex, length, sampleSize);
        }
        return summedImpact;
    }

    @Override
    public double estimateSummedImpact(int impactorIndex, int impactedIndex, int chainLength, int sampleSize) {
        assert sampleSize > 0 : "Sample size 0 or smaller";
        int i=0;
        double mean=0;
        while(sampleSize-- >0) {
            double rndImpact = impactOfChain(randomChainIndices(impactorIndex, impactedIndex, chainLength));
            mean = (rndImpact + mean * i) / (i+1);
        }
        
        double chainCount = EXITImpactMatrix.chainCount_intermediary(matrix.varCount, chainLength-2);
        
        return mean * chainCount;
    }
    

    
    
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
        assert indices.size() > 1 : "Chain length must be greater than 1";
        double impact = 1;
        double max = matrix.getMaxImpact();
        Iterator<Integer> it1 = indices.iterator();
        Iterator<Integer> it2 = indices.iterator(); it2.next();
        while(it1.hasNext() && it2.hasNext()) {
            impact *= matrix.getValue(it1.next(), it2.next()) / max;
        }
        return impact;
    }
    
    
    
}
