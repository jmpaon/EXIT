/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import exit.EXITexception;


/**
 *
 * @author jmpaon
 */
public class EXITargumentException extends EXITexception {

    public EXITargumentException(String msg) {
        //super(msg);
        System.out.println(msg);
        // printUsage();
        
    }
    
    public EXITargumentException(String msg, Object... objs) {
        super(String.format(msg, objs));
        // printUsage();
    }
    
   
}
                
                
    

