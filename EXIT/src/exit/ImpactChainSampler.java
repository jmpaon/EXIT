/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author juha
 */
public class ImpactChainSampler {
    
    public final EXITImpactMatrix matrix;
    
    
    public ImpactChainSampler(EXITImpactMatrix matrix) {
        this.matrix = matrix;
    }
    
    
    
    public List<ImpactChain> generateChains(int impactorIndex, int impactedIndex, int length) {
        assert length < matrix.varCount - 2;
        
        
        
        throw new UnsupportedOperationException();
    }
    
    
    public EXITImpactMatrix testSampling(int sampleSize) {
        EXITImpactMatrix im = matrix.copy().flush();
        for(int impactor=1;impactor<=matrix.varCount;impactor++) {
            for(int impacted=1;impacted<=matrix.varCount;impacted++) {
                if (impactor != impacted) {
                    
                    
                    
                    
                }
            }
        }
        
        //List<ImpactChain> sample = sampleChains(sampleSize, sampleSize, sampleSize, sampleSize)
    }
    
    double sampleAverage(List<ImpactChain> sample) {
        double sum=0;
        for(ImpactChain i : sample) {
            sum += i.impact();
        }
        return sum / sample.size();
    }
    
    
    
    
    public List<ImpactChain> sampleChains(int impactorIndex, int impactedIndex, int length, int count) {
        List<ImpactChain> sample = new LinkedList<ImpactChain>();
        while(count-- > 0) {
            sample.add(generateChain(impactorIndex, impactedIndex, length));
        }
        return sample;
    }
    
    public ImpactChain generateChain(int impactorIndex, int impactedIndex, int length) {
        assert indexIsValid(impactorIndex);
        assert indexIsValid(impactedIndex);
        assert length > 1;
        assert length < matrix.varCount-2;
        length -= 2;
        List<Integer> chainMembers = new ArrayList<>();
        List<Integer> l = availableIndices(impactorIndex, impactedIndex);
        int i=0;
        Collections.shuffle(l);
        chainMembers.add(impactorIndex);
        while(length-- > 0) chainMembers.add(l.get(i++));
        chainMembers.add(impactedIndex);
        return new ImpactChain(matrix, chainMembers);
    }
    
    
    /**
     * Returns a list of possible indices of intermediary variables for chain generation for <b>matrix</b>.
     * @param impactorIndex Index of impactor variable of the chain
     * @param impactedIndex Index of impacted variable of the chain
     * @return 
     */
    private List<Integer> availableIndices(int impactorIndex, int impactedIndex) {
        assert indexIsValid(impactorIndex) && indexIsValid(impactedIndex);
        List<Integer> indices = new ArrayList<>();
        for(int i=1;i<=matrix.varCount;i++) {
            if(i != impactorIndex && i != impactedIndex) {
                indices.add(i);
            }
        }
        return indices;
    }

    
    private boolean indexIsValid(int index) {
        return index > 0 && index <= matrix.varCount;
    }
    
    
}
