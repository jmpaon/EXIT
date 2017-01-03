/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.EXITarguments;
import exit.matrices.EXITImpactMatrix;


/**
 *
 * @author jmpaon
 */
public class EXITinput {
    public final EXITImpactMatrix directImpactMatrix;
    public final EXITarguments arguments;
    
    public EXITinput(EXITImpactMatrix directImpactMatrix, EXITarguments arguments) {
        
        assert directImpactMatrix != null;
        assert arguments != null;
        
        this.directImpactMatrix = directImpactMatrix;
        this.arguments = arguments;
    }
    
    
}
