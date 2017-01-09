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
import java.util.Map;

/**
 *
 * @author jmpaon
 */
public class EXITresult {
    public final EXITinput input;
    public final CrossImpactMatrix resultMatrix;
    public final List<Object> printables;
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
    
//    public void addPrintable(String printable) {
//        assert printable != null;
//        printables.add(printable);
//    }
    
    public void addPrintable(Object printable) {
        assert printable != null;
        printables.add(printable);
    }
    
    public void addPrintable(String heading, Object printable) {
        printables.add(underlined(heading));
        printables.add(printable);
    }
    
    public void print() {
        for(Object o : printables) {
            output.println(o.toString());
        }
    }
    
    protected String underlined(String text) {
        StringBuilder sb = new StringBuilder(text);
        sb.append("\n");
        for(int i=0;i<text.length();i++) {
            sb.append("=");
        }
        return sb.toString();
    }
    
    
    
}
