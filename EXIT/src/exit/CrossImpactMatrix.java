/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author juha
 */
public class CrossImpactMatrix extends SquareMatrix{

    /** Are only integers allowed as matrix values? */
    protected final boolean onlyIntegers;
    
    /** Is the matrix locked? If locked, matrix contents cannot be changed */
    private boolean isLocked;
    
    /**
     * 
     */
    public enum InfluenceDependenceClassification {
        STABLE,   /* Low  influence, low dependence */
        REACTIVE, /* Low influence, high dependence */
        DRIVER,   /* High influence, low dependence */
        CRITICAL;  /* High influence,high dependence */
        
        @Override
        public String toString() {
            switch(this) {
                case CRITICAL: return "Critical";
                case REACTIVE: return "Reactive";
                case DRIVER:   return "Driver";
                case STABLE:   return "Stable";
            }
            throw new EnumConstantNotPresentException(this.getClass(), this.name());
        }
         
    }
    
    public enum Orientation {
        INFLUENCE,
        DEPENDENCE;
        
        @Override
        public String toString() {
            switch(this) {
                case INFLUENCE:  return "Influence";
                case DEPENDENCE: return "Dependence";
            }
            throw new EnumConstantNotPresentException(this.getClass(), this.name());
        }
    }

    public CrossImpactMatrix(int varCount, String[] names, double[] values, boolean onlyIntegers) {
        super(varCount, names, values);
        this.onlyIntegers = onlyIntegers;
        if(onlyIntegers) {
            for(double d : values) assert isInteger(d);
        }
    }

    public CrossImpactMatrix(int varCount, String[] names, boolean onlyIntegers) {
        this(varCount, names, new double[varCount*varCount], onlyIntegers);
    }

    public CrossImpactMatrix(int varCount, boolean onlyIntegers) {
        this(varCount, createNames(varCount), new double[varCount*varCount], onlyIntegers);
    }

    public CrossImpactMatrix(int varCount, String[] names) {
        this(varCount, names, new double[varCount*varCount], false);
    }

    public CrossImpactMatrix(int varCount) {
        this(varCount, createNames(varCount), new double[varCount*varCount], false);
    }

    public CrossImpactMatrix(String[] names, double[][] values, boolean onlyIntegers) {
        this(values.length, names, flattenArray(values), onlyIntegers);
    }
    
    public CrossImpactMatrix(SquareMatrix matrix) {
        this(matrix.varCount, matrix.names, matrix.values, false);
    }
    
    public CrossImpactMatrix(CrossImpactMatrix matrix) {
        this(matrix.varCount, matrix.names, matrix.values, matrix.onlyIntegers);
    }
    
    /**
     * Sets an impact value in an entry of the <tt>CrossImpactMatrix</tt>.
     * Non-zero values cannot be set to any matrix entry (i,j) in case i==j.
     * @param impactor Index of the impactor variable (matrix row index)
     * @param impacted Index of the impacted variable (matrix column index)
     * @param value Value of the matrix entry
     */
    @Override
    public void setValue(int impactor, int impacted, double value) {
        
        // Cannot change locked matrix
        assert !isLocked() : "Attempt to change a locked CrossImpactMatrix";
        // Variables cannot have an impact on themselves
        assert !(impactor == impacted && value != 0): "Variable cannot have impact on itself in a cross-impact matrix";
        
        super.setValue(impactor, impacted, value);
    }
    

    /**
     * Returns true if <code>SquareMatrix</code> is locked, false otherwise.
     * SquareMatrix being locked means
     * that impact values cannot be changed anymore.
     * @return <i>true</i> if matrix is locked, <i>false</i> otherwise.
     */
    public boolean isLocked() {
        return isLocked;
    }

    
    /**
     * Locks the matrix so that contents cannot be changed.
     */
    public void lock() {
        this.isLocked = true;
    }

    
    /**
     * Returns an importanceValue matrix for the impact matrix.
     * Importance for each impactor-impacted pair or matrix entry
     * is calculated by dividing the impact
     * by the sum of the absolute impacts on the impacted variable.
     * @return Importance matrix derived from this <code>EXITImpactMatrix</code>
     */    
    public CrossImpactMatrix importanceMatrix() {
        
        CrossImpactMatrix importance = new CrossImpactMatrix(this);
        for (int impacted = 1; impacted <= this.varCount; impacted++) {
            double columnSum = this.columnSum(impacted, true);
            for (int impactor = 1; impactor <= this.varCount; impactor++) {
                double shareOfAbsoluteSum = columnSum != 0 ? this.getValue(impactor, impacted) / columnSum : 0;
                double absShare = Math.abs(shareOfAbsoluteSum);
                double importanceValue = shareOfAbsoluteSum < 0 ? -absShare : absShare;
                importance.setValue(impactor, impacted, importanceValue);
            }
        }
        return importance;
    }
    
    
    /**
     * Returns a matrix where values of <b>subtractMatrix</b> have
     * been subtracted from values of this matrix.
     * @param subtractMatrix <tt>SquareMatrix</tt> whose values are subtracted from values of this matrix
     * @return Difference matrix
     */
    public CrossImpactMatrix differenceMatrix(SquareMatrix subtractMatrix) {
        assert this.varCount == subtractMatrix.varCount : "Comparison matrix is of different size";
        boolean bothMatricesIntegral = this.allValuesAreIntegers() && subtractMatrix.allValuesAreIntegers();
        CrossImpactMatrix differenceMatrix = new CrossImpactMatrix(this.varCount, this.names, this.values, bothMatricesIntegral);
        for (int i = 0; i < differenceMatrix.values.length; i++) {
            differenceMatrix.values[i] -= subtractMatrix.values[i];
        }
        return differenceMatrix;
    }
    
    
    /**
     * Creates a Normalized copy of this matrix 
     * by dividing each impact value
     * by the average distance of values from zero 
     * (or the mean of absolute values).
     * @return New <tt>CrossImpactMatrix</tt> based on this matrix, 
     * values normalized to unit of mean of absolute values of original.
     */
    public CrossImpactMatrix normalize() {
        double[] normalizedValues = this.values.clone();
        double averageDistanceFromZero = this.matrixMean(true);
        for (int i=0; i < normalizedValues.length; i++) {
            normalizedValues[i] /= averageDistanceFromZero;
        }
        CrossImpactMatrix normalized = new CrossImpactMatrix(this.varCount, this.names, normalizedValues, false);
        return normalized;
    }
    
    /**
     * Returns a new <tt>CrossImpactMatrix</tt> derived from this impact matrix
     * where values are rounded to the nearest integer.
     * @return 
     */
    public CrossImpactMatrix round() {
        double[] rounded = this.values.clone();
        for(int i=0;i<rounded.length;i++) {
            rounded[i] = Math.round(rounded[i]);
        }
        return new CrossImpactMatrix(varCount, names, rounded, true);
    }
    
    /**
     * Returns a new <tt>CrossImpactMatrix</tt> derived from this impact matrix
     * where values are floored to the nearest integer.
     * @return 
     */    
    public CrossImpactMatrix floor() {
        double[] floored = this.values.clone();
        for(int i=0;i<floored.length;i++) {
            floored[i] = Math.floor(floored[i]);
        }
        return new CrossImpactMatrix(varCount, names, floored, true);        
    }


    /**
     * Scales the impact matrix to have its greatest absolute value
     * to be equal to value of <b>to</b>.
     * @param to The value that the greatest absolute impact value in the matrix will be scaled to
     * @return <code>EXITImpactMatrix</code> scaled according to <b>to</b> argument.
     */
    public CrossImpactMatrix scale(double to) {
        assert to != 0 : "Scaling to 0 not possible";
        double max = matrixMax();
        double[] scaled = this.values.clone();
        for (int i = 0; i < values.length; i++) {
            scaled[i] = values[i] / max * to;
        }
        return new CrossImpactMatrix(this.varCount, this.names, scaled, false);
    }
    
    /**
     * Return a <tt>Map</tt> with variable indices and the 
     * influence-dependence classifications of the corresponding variables of the
     * <tt>CrossImpactMatrix</tt>.
     * @return 
     */
    public Map<Integer, InfluenceDependenceClassification> classifyVariables() {
        
        double rowSumAverage=0, colSumAverage=0;
        
        for(int i=1;i<=this.varCount;i++) {
            rowSumAverage += this.rowSum(i, true);
            colSumAverage += this.columnSum(i, true);
        }
        
        rowSumAverage /= this.varCount;
        colSumAverage /= this.varCount;
        
        Map<Integer, InfluenceDependenceClassification> classification = new TreeMap<>();
        for(int i=1;i<=this.varCount;i++) {
            if(this.rowSum(i, true) > rowSumAverage) {
                if(this.columnSum(i, true) > colSumAverage) {
                    classification.put(i, InfluenceDependenceClassification.CRITICAL);
                } else {
                    classification.put(i, InfluenceDependenceClassification.DRIVER);
                }
            } else {
                if(this.columnSum(i, true) > colSumAverage) {
                    classification.put(i, InfluenceDependenceClassification.REACTIVE);
                } else {
                    classification.put(i, InfluenceDependenceClassification.STABLE);
                }                
            }
        }
        return classification;
    }
    
    public Ordering getOrdering(Orientation orientation) {
        return new Ordering(this, orientation);
    }
    
    
    /**
     * This class represents an ordering of the matrix variables.
     */
    public class Ordering implements Comparable<Ordering> {
        
        public final CrossImpactMatrix matrix;
        public final Orientation orientation;
        public final List<VarDetails> ordering;
        
        public Ordering(CrossImpactMatrix matrix, Orientation orientation) {
            this.matrix = matrix;
            this.orientation = orientation;
            this.ordering = new ArrayList<>();
            determineOrdering();
        }
        
        
        /**
         * Returns a distance measure between two orderings.
         * @param o Ordering to compare this ordering against
         * @return Distance measure; 0 if the orderings are identical
         */
        @Override
        public int compareTo(Ordering o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public boolean equals(Object o) {
            if (!o.getClass().isAssignableFrom(this.getClass())) return false;
            
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 19 * hash + Objects.hashCode(this.matrix);
            hash = 19 * hash + Objects.hashCode(this.orientation);
            hash = 19 * hash + Objects.hashCode(this.ordering);
            return hash;
        }
        
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(VarDetails v : ordering) {
                sb.append(v.position).append(": " + "V").append(v.index).append(" (").append(v.sum).append(") ");
            }
            return sb.toString();
        }
        
        
        /**
         * Determines the ordering of <i>matrix</i> variables
         * according to the <i>orientation</i> of this <tt>Ordering</tt>.
         */
        private void determineOrdering() {
            List<VarDetails> sorted = new ArrayList<>();
            
            // Collect the values for the ordering
            for(int i=1;i<=matrix.varCount;i++) {
                switch(orientation) {
                    case INFLUENCE:  this.ordering.add(new VarDetails(i, matrix.rowSum(i, true)));
                    case DEPENDENCE: this.ordering.add(new VarDetails(i, matrix.columnSum(i, true)));
                    default: throw new EnumConstantNotPresentException(orientation.getClass(), orientation.name());
                }
            }
            
            // Sort the ordering by sums (row or column sums)
            Collections.sort(ordering);
            
            // Put greatest value first
            Collections.reverse(ordering);
        }


        /**
         * Container for details of a <i>matrix</i> variable in an <tt>Ordering</tt>
         */
        class VarDetails implements Comparable<VarDetails> {
            public final Integer index;
            public final Double sum;
            public Integer position;
            
            public VarDetails(int index, double sum) {
                this.index = index;
                this.sum = sum;
            }
            
            @Override
            public int compareTo(VarDetails o) {
                int comparison = this.sum.compareTo(o.sum);
                return comparison != 0 ? comparison : this.index.compareTo(o.index);
            }            
            
        }
        
    }
    
}
