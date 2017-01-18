/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.io.Options;
import exit.matrices.EXITImpactMatrix;


/**
 *
 * @author jmpaon
 */
public class EXITinput {
    public final EXITImpactMatrix directImpactMatrix;
    public final Options options;
    
    public EXITinput(EXITImpactMatrix directImpactMatrix, Options options) {
        
        assert directImpactMatrix != null;
        assert options != null;
        
        this.directImpactMatrix = directImpactMatrix;
        this.options = options;
    }
    
    
}
