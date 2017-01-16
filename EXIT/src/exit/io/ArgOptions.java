/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jmpaon
 */
public class ArgOptions {
    
    public List<String> commandLineArguments;
    public final List<ArgOption> options;
    
    public ArgOptions() {
        this.options = new ArrayList<>();
    }
    
    public void addOption(ArgOption option) {
        this.options.add(option);
    }
    
    public void addOptions(ArgOption... options) {
        for(ArgOption option : options) addOption(option);
    }
    
    public ArgOption query(String id) {
        for(ArgOption o : options) if(o.id.equals(id)) return o;
        return null;
    }
    
    
    
    private String fetchArgument(String id) throws EXITargumentException {
        try {
            int indexOfArgLabel = commandLineArguments.indexOf(id);
            int indexOfArgValue = indexOfArgLabel + 1;
            if(commandLineArguments.get(indexOfArgValue).charAt(0) == '-') {
                throw new EXITargumentException("No value for argument " + id);
            }
            return commandLineArguments.get(indexOfArgValue);
        } catch(Exception e) {
            throw new EXITargumentException(String.format("No value for argument %s", id));
        }
    }
    
    private String fetchArgument(ArgOption option) throws EXITargumentException {
        return fetchArgument(option.id);
    }
    
    private void consumeArgument(ArgOption option) throws EXITargumentException {
        
        if(commandLineArguments.contains(option.id)) {
            if(option.hasValue) {
                String arg = fetchArgument(option);
                option.readIn(arg);
                int removeIndex = commandLineArguments.indexOf(option.id);
                commandLineArguments.remove(removeIndex); // remove argument label
                commandLineArguments.remove(removeIndex); // remove argument value
            }
            
            else {
                option.readIn(option.id);
                commandLineArguments.remove(commandLineArguments.indexOf(option.id));
            }
            
        }
        
        else {
            if(option.isRequired) {
                throw new EXITargumentException(String.format("Required argument %s is missing", option.toString()));
            }
            else {
                option.readIn(null);
            }
            
        }
        
    }
    
    public void parse(String[] args) throws EXITargumentException {
        
        this.commandLineArguments = new ArrayList(Arrays.asList(args));
        
        System.out.println("Size of commandlineargs: " + commandLineArguments.size());

        for(ArgOption option : options) {
            System.out.printf("parsing option %s", option);
            consumeArgument(option);
            System.out.printf(" -> %s%n", Objects.nonNull(option.value) ? option.value.toString() : "null" );
        }        
        
        if(!commandLineArguments.isEmpty()) {
            throw new EXITargumentException(String.format("Unrecognized options: " + commandLineArguments.toString()));
        }
        

        
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(ArgOption option : options) sb.append(option.state()).append("\n\n");
        return sb.toString();
    }
    
    
 
    
    
    
    
    
}
