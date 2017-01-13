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
 */
public class ArgOption<V> {
        
    public final String id;
    public final String name;
    public final boolean hasValue;
    public final boolean isRequired;

    V value;
    final Map<Predicate<V>, String> conditions;
    final Function<String, V> parser;

    public ArgOption(String id, String name, boolean hasValue, boolean isRequired, Function<String, V> parser) {
        if(isRequired && !hasValue) throw new IllegalArgumentException("Attempt to create ArgOption with required argument and no value");
        this.id = id;
        this.name = name;
        this.hasValue = hasValue;
        this.isRequired = isRequired;
        this.parser = parser;
        this.conditions = new LinkedHashMap<>();
    }

    public ArgOption addCondition(Predicate<V> condition, String failMessage) {
        assert condition != null;
        assert failMessage != null;
        assert this.hasValue : "This option is set not to have a value";
        this.conditions.put(condition, failMessage);
        return this;
    }
    
    public void parseSet(String stringValue) throws EXITargumentException {
        set(parse(stringValue));
    }
    

    public V parse(String stringValue) {
        return this.parser.apply(stringValue);
    }

    public void set(V value) throws EXITargumentException {
        // assert this.hasValue : String.format("Option %s cannot have a value", this.toString());
        for(Map.Entry<Predicate<V>, String> e : this.conditions.entrySet() ) {
            if(!e.getKey().test(value)) {
                String context = String.format("Invalid value (%s) for %s", value.toString(), this.toString() );
                throw new EXITargumentException(context + ": " + e.getValue());
            }
        }
        this.value = value;
    }

    public V get() {
        return value;
    }

    @Override
    public String toString() {
        if(this.name == null) return this.id;
        return String.format("%s (%s)", this.id, this.name);
    }
    
    public String state() {
        StringBuilder sb = new StringBuilder();
                sb
                .append("name\t").append(this.name).append("\n")
                .append("id\t").append(this.id).append("\n")
                .append("hasvalue\t").append(this.hasValue).append("\n")
                .append("isRequired\t").append(this.isRequired).append("\n")
                .append("parser\t").append(this.parser).append("\n")
                .append("value\t").append(this.value.toString());
        return sb.toString();
                
    }

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
