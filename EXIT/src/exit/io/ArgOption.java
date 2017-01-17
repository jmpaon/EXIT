/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author jmpaon
 * @param <V> Type of the value of this <tt>ArgOption</tt>
 */
public class ArgOption<V> {
    /** id flag of the option */
    public final String  id;
    /** longer name of the option */
    public final String  name;
    /** does the option have a value? */
    public final boolean isValued;
    /** is the option required? */
    public final boolean isRequired;
    
    /** Value of the option */
    V value;
    /** Set of validity tests for <b>value</b> and the associated fail messages */
    final Map<Predicate<V>, String> conditions;
    /** Function to convert a <tt>String</tt> value to value of type <tt>V</tt> */
    final Function<String, V> parser;
    
    
    /**
     * Constructor
     * @param id The <tt>ArgOption</tt> id, should start with a hyphen ('-')
     * @param name The longer name of the option
     * @param isValued Does this option have an associated value?
     * @param isRequired Is this option required?
     * @param parser Function to convert a <tt>String</tt> value to value of type <tt>V</tt>
     */
    public ArgOption(String id, String name, boolean isValued, boolean isRequired, Function<String, V> parser) {
        if(isRequired && !isValued) throw new IllegalArgumentException("Attempt to create ArgOption with required argument and no value");
        assert parser != null :"Parser is null";
        assert id.charAt(0) == '-' : "option id should start with a hyphen";
        
        this.id = id;
        this.name = name;
        this.isValued = isValued;
        this.isRequired = isRequired;
        this.parser = parser;
        this.conditions = new LinkedHashMap<>();
    }

    
    /**
     * Add a validity test and a fail message to this <tt>ArgOption</tt>.
     * When a value for this <tt>ArgOption</tt> is parsed,
     * all validity tests are performed before value is assigned.
     * @param condition Predicate that must return <b>true</b> for the value to be valid
     * @param failMessage Message for the exception reporting failed validity test
     * @return ArgOption : this <tt>ArgOption</tt> to enable method call chaining
     */
    public ArgOption addCondition(Predicate<V> condition, String failMessage) {
        assert condition != null;
        assert failMessage != null;
        assert this.isValued : "This option is set not to have a value";
        this.conditions.put(condition, failMessage);
        return this;
    }
    
    
    /**
     * Parses a <tt>String</tt> value to <tt>V</tt> and sets it as the value 
     * of this <tt>ArgOption</tt>.
     * @param stringValue
     * @throws EXITargumentException 
     */
    public void readIn(String stringValue) throws EXITargumentException {
        set(parse(stringValue));
    }

    /**
     * Parses the value in a <tt>String</tt> to <tt>V</tt> 
     * by applying <b>parser</b> on <b>stringValue</b>.
     * @param stringValue a <tt>String</tt> containing the value of the option
     * @return V : parsed value
     */
    V parse(String stringValue) {
        if(Objects.nonNull(stringValue)) return this.parser.apply(stringValue);
        return null;
    }

    
    /**
     * Sets the value of this <tt>ArgOption</tt>.
     * @param value 
     * @throws EXITargumentException 
     */
    public void set(V value) throws EXITargumentException {
        
        if(this.isRequired && value == null) {
            throw new EXITargumentException("Required option %s has no value", this.toString());
        }
        
        /* Perform argument validity tests */
        if(Objects.nonNull(value)) {
            for(Map.Entry<Predicate<V>, String> e : this.conditions.entrySet() ) {
                if(!e.getKey().test(value)) {
                    String context = String.format("Invalid value (%s) for %s", value.toString(), this.toString() );
                    throw new EXITargumentException(context + ": " + e.getValue());
                }
            }
            this.value = value;
        } else {
            this.value = null;
        }
    }

    /**
     * Returns the value of this <tt>ArgOption</tt>
     * @return V : value
     */
    public V get() {
        return value;
    }

    /**
     * String representation consisting of <b>id</b> 
     * OR <b>id</b> and <b>name</b> if <b>name</b> is available
     * @return String 
     */
    @Override
    public String toString() {
        if(this.name == null) return this.id;
        return String.format("%s (%s)", this.id, this.name);
    }
    
    /**
     * String representation of the details of this <tt>ArgOption</tt>
     * @return String
     */
    public String state() {
        StringBuilder sb = new StringBuilder();
        String conditionsAsString = "Conditions:\n"; int conditionNumber = 1;
        for(Map.Entry<Predicate<V>, String > e : conditions.entrySet()) {
            String conditionSignature = e.getKey() != null ? "Test" : "null";
            conditionsAsString += String.format("\t[%d] %s :: %s\n", conditionNumber++, conditionSignature, e.getValue());
        }
        
        
        String valueAsString = Objects.nonNull(value) ? value.toString() : "null";
                sb
                .append("name\t").append(this.name).append("\n")
                .append("id\t").append(this.id).append("\n")
                .append("hasvalue\t").append(this.isValued).append("\n")
                .append("isRequired\t").append(this.isRequired).append("\n")
                .append("parser\t").append(this.parser).append("\n")
                .append(conditionsAsString)
                .append("value\t").append(valueAsString);
        return sb.toString();
                
    }

    /**
     * Returns a hashcode based on the <tt>id</tt> property
     * @return int : hashcode
     */
    @Override
    public int hashCode() {
        int hash = this.id.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArgOption<?> other = (ArgOption<?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
        
    
        

}
