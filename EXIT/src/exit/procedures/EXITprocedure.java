/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 *
 * @author jmpaon
 */
public abstract class EXITprocedure {
    public abstract EXITresult compute (EXITinput input, PrintStream reportingStream) throws FileNotFoundException ;
    public abstract EXITresult compute (EXITinput input) throws FileNotFoundException ;
    
}
