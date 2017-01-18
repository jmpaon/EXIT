/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import exit.EXITexception;
import exit.matrices.EXITImpactMatrix;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author jmpaon
 */
public class Reader {
    
    final PrintStream reportingStream;
    
    public Reader() {
        this(null);
    }
    
    public Reader(PrintStream reportingStream) {
        this.reportingStream = reportingStream;
    }
    
    
    public EXITImpactMatrix readInputMatrixFromFile(String filename, double maxImpact, char separator) throws FileNotFoundException, IOException {
        
        report(String.format("Reading impact matrix data from file %s%n", filename));
        
        if(! fileExists(filename)) throw new FileNotFoundException(String.format("Input file %s not found", filename));
        
        List<String> lines = Files.readAllLines(Paths.get(filename));
        eliminateEmptyLines(lines);

        int variableCount = lines.size();
        int variable=1;
        
        EXITImpactMatrix inputMatrix = new EXITImpactMatrix(maxImpact, variableCount, false);
        
        for(String l : lines) {
            Scanner sc = new Scanner(l).useDelimiter(String.valueOf(separator));
            
            inputMatrix.setName(variable, sc.next() );
            int impact=0;
            while(sc.hasNextDouble()) {
                impact++;
                inputMatrix.setValue(variable, impact, sc.nextDouble());
            }

            if (impact != variableCount) {
                throw new EXITinputfileException("Wrong number of impact values: number of lines in input file suggests that there are %d variables, but line %d (Variable '%s') contains %d impact values", variableCount, variable, inputMatrix.getName(variable), impact);
            }

            variable++;
        }

        inputMatrix.lock();
        report(String.format("Read %d variables from input file.%n", inputMatrix.getVarCount(),4));
        return inputMatrix;        
    }
    
    protected void report(String text) {
        if(this.reportingStream != null) reportingStream.println(text);
    }

    
    /**
     * Processes a list of <tt>String</tt>s representing lines of content  
     * in an input file so that lines that only contain 
     * whitespace or are empty are removed.
     * @param lines List of <tt>String</tt>s representing lines of content in an input file
     */
    void eliminateEmptyLines(List<String> lines) {        
        List<String> removed = new LinkedList<>();        
        for(String l : lines) {
            if(l.trim().length() == 0) { removed.add(l); }
        }        
        lines.removeAll(removed);        
    }
    
    /**
     * Tests that <i>filename</i> exists and is not a directory.
     * @param filename path to be tested
     * @return true if file exists
     */
    boolean fileExists(String filename) {
        File f = new File(filename);
        return f.exists() && !f.isDirectory();
    }
    
    boolean isInteger(String str) {  
        try {int d = Integer.parseInt(str);}  
        catch(NumberFormatException nfe){ return false;}  
        return true;  
    }        
    
}
