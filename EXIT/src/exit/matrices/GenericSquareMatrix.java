/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.matrices;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author jmpaon
 * @param <V> Type of matrix entries
 * @param <I> Type of matrix identifiers
 */
public abstract class GenericSquareMatrix<V,I> {
    
    private final List<List<V>> values;
    private final List<I> identifiers;
    private final int varCount;
    
    public GenericSquareMatrix(int varCount) {
        this.varCount = varCount;
        this.values = new ArrayList<>(varCount);
        for(List l : values) l = new ArrayList<>(varCount);
        this.identifiers = new ArrayList<>();
    }

    public int getVarCount() {
        return varCount;
    }
    
    public I getId(int index) {
        assert isValid(index);
        return identifiers.get(index);
    }
    
    public V getValue(int rowIndex, int columnIndex) {
        assert isValid(rowIndex,columnIndex);
        return values.get(rowIndex).get(columnIndex);
    }
    
    public void setValue(int rowIndex, int columnIndex, V value) {
        assert isValid(rowIndex,columnIndex);
        assert isValidValue(value);
        this.values.get(rowIndex).set(columnIndex, value);
    }
    
    public void setId(int index, I identifier) {
        assert isValid(index);
        assert isValidId(identifier);
        this.identifiers.set(index, identifier);
    }
    
    private boolean isValid(int index) {
        return index > 0 && index <= this.varCount;
    }
    
    private boolean isValid(int... indices) {
        for(int i : indices) if(i < 1 || i > this.varCount) return false;
        return true;
    }
    
    public void transform(Consumer<V> function) {
        throw new UnsupportedOperationException();
    }
    
    abstract protected boolean isValidId(I identifier);
    abstract protected boolean isValidValue(V value);
    
    
    
}
