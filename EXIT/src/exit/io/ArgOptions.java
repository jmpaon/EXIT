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
                option.parseSet(arg);
                int removeIndex = commandLineArguments.indexOf(option.id);
                commandLineArguments.remove(removeIndex); // remove argument label
                commandLineArguments.remove(removeIndex); // remove argument value
            }
            else {
                option.parseSet(option.id);
                commandLineArguments.remove(commandLineArguments.indexOf(option.id));
            }
            
        }
        
        else {
            if(option.isRequired) {
                throw new EXITargumentException(String.format("Required argument %s is missing", option.toString()));
            }
            else {
                option.parseSet(null);
            }
            
        }
        
    }
    
    public void parse(String[] args) throws EXITargumentException {
        this.commandLineArguments = new ArrayList(Arrays.asList(args));
        System.out.println("Size of commandlineargs: " + commandLineArguments.size());
        List<Exception> errors = new LinkedList<>();
        
        for(ArgOption option : options) {
            try {
                consumeArgument(option);
            } catch(Exception e) {
                errors.add(e);
            }
        }
        
        if(commandLineArguments.size() != 0) {
            errors.add(new EXITargumentException(String.format("Unrecognized options: " + commandLineArguments.toString())));
        }
        
        if(!errors.isEmpty()) {
            StringBuilder errorMessages = new StringBuilder();
            for(Exception e : errors) errorMessages.append(e.getMessage()).append("\n");
            throw new EXITargumentException(errorMessages.toString());
        }
        
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(ArgOption option : options) sb.append(option.state()).append("\n\n");
        return sb.toString();
    }
    
    
 
    
    
    
    
    
}
