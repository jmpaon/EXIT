/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

import exit.matrices.ImpactChain;
import java.util.Comparator;

/**
 *
 * @author jmpaon
 */
public class ImpactComparator implements Comparator<ImpactChain> {
    
    @Override
    public int compare(ImpactChain ic1, ImpactChain ic2) {
        
        int result = Double.compare(Math.abs(ic2.impact()), Math.abs(ic1.impact()));
        
        if(result == 0) {
            result = ic1.compareTo(ic2);
        }
        
        return result;
    }
    
}

