/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

import exit.matrices.CrossImpactMatrix;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jmpaon
 */
public class EXITresult {
    public final EXITinput input;
    public final CrossImpactMatrix resultMatrix;
    public final List<String> printables;
    public final PrintStream output;

    public EXITresult(EXITinput input, CrossImpactMatrix resultMatrix) throws FileNotFoundException {
        this.input = input;
        this.resultMatrix = resultMatrix;
        this.printables = new LinkedList<>();

        if(input.arguments.outputFilename == null) {
            this.output = System.out;
        } else {
            this.output = new PrintStream(input.arguments.outputFilename);
        }
        
    }
    
    public void addPrintable(String printable) {
        assert printable != null;
        printables.add(printable);
    }
    
    public void addPrintable(String heading, String printable) {
        assert printable != null && heading != null;
        printables.add(heading);
        printables.add(printable);
    }
    
    public void print() {
        for(String s : printables) {
            output.println(s);
            output.println();
        }
    }
        
    
    
    
    
}
