/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
    
    public List<ImpactChain> sampleChains(int impactorIndex, int impactedIndex, int length, int count) {
        
        
        throw new UnsupportedOperationException();
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
