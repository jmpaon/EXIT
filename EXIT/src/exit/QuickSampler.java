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
    
    private static final double SAMPLING_THRESHOLD = 8000;

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
                    //summedImpactMatrix.setValue(impactor, impacted, estimateSummedImpact(impactor, impacted, sampleSize));
                    summedImpactMatrix.setValue(impactor, impacted, smartEstimationOfSummedImpact(impactor, impacted, sampleSize));
                }
            }
        }
        return summedImpactMatrix;
    }
    
    public double smartEstimationOfSummedImpact(int impactorIndex, int impactedIndex, int sampleSize) {
        double summedImpact = 0;
        for(int length=2;length<=matrix.varCount;length++) {
            if(EXITImpactMatrix.factorial(length-2) < SAMPLING_THRESHOLD ) {
                summedImpact += calculateImpactOfAll(impactorIndex, impactedIndex, length);
                System.out.printf("Computed all chains between %s and %s of length %d\n", matrix.getName(impactorIndex), matrix.getName(impactedIndex), length);
            } else {
                summedImpact += estimateSummedImpact(impactorIndex, impactedIndex, length, sampleSize);
                System.out.printf("Estimated sum of chains between %s and %s of length %d\n", matrix.getName(impactorIndex), matrix.getName(impactedIndex), length);
            }
        }
        return summedImpact;        
    }
    

    @Override
    public double estimateSummedImpact(int impactorIndex, int impactedIndex, int sampleSize) {
        double summedImpact = 0;
        for(int length=2;length<=matrix.varCount;length++) {
            summedImpact += estimateSummedImpact(impactorIndex, impactedIndex, length, sampleSize);
        }
        return summedImpact;
    }

    @Override
    public double estimateSummedImpact(int impactorIndex, int impactedIndex, int chainLength, int sampleSize) {
        assert sampleSize > 0 : "Sample size 0 or smaller";
        assert chainLength > 1 : "Chain length must be greater than 1";
        
        /* Chains with length 2 need not be sampled */
        if (chainLength == 2) {
            return matrix.getValue(impactorIndex, impactedIndex)/matrix.getMaxImpact();
        }
        
        int i=0;
        double mean=0;
        while(sampleSize-- > 0) {
            double rndImpact = impactOfChain(randomChainIndices(impactorIndex, impactedIndex, chainLength));
            mean = (rndImpact + mean * i) / (i+1);
            i++;
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
