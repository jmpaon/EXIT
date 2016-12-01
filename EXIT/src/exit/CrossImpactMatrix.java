/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
            if(this == CRITICAL) return "Critical";
            if(this == REACTIVE) return "Reactive";
            if(this == DRIVER)   return "Driver";
            if(this == STABLE)   return "Stable";
            return "Unknown";
        }
    }
    
    public enum Orientation {
        INFLUENCE,
        DEPENDENCE
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
    
}
