/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.estimators;

import exit.matrices.CrossImpactMatrix;
import exit.matrices.EXITImpactMatrix;
import exit.matrices.ImpactChain;
import exit.matrices.SquareMatrix;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Set;

/**
 * Objects of this class perform the estimation of summed impacts 
 * based on the pruning strategy.
 * The 2-variable impact chains representing direct impacts are generated first.
 * If the relative impact of the generated chains are greater than or equal to
 * the threshold value, <u>expansions</u> of those <u>significant</u> chains
 * are generated.
 * @author jmpaon
 */
public class ImpactThresholdPruner extends Pruner {

    public ImpactThresholdPruner(EXITImpactMatrix matrix) {
        super(matrix);
    }

    public ImpactThresholdPruner(EXITImpactMatrix matrix, PrintStream reportingStream) {
        super(matrix, reportingStream);
    }

    @Override
    public CrossImpactMatrix estimateSummedImpactMatrix(double threshold) {
        return prunedSummedImpactMatrix(threshold, matrix);
    }

    /**
     * Estimates by pruning strategy and returns
     * a new <code>EXITImpactMatrix</code> that contains
     * the summed direct and indirect values between the variables.
     * In the returned matrix,
     * impactor variables are in rows
     * and impacted variables are in columns.
     * @param impactThreshold The low bound for inclusion for the impact of chains that are summed in the matrix.
     * See {@link ImpactChain#highImpactChains(double)}.
     * @return <code>EXITImpactMatrix</code> with the summed direct and indirect values between variables
     */
    private CrossImpactMatrix prunedSummedImpactMatrix(double impactThreshold, EXITImpactMatrix exitImpactMatrix) {
        CrossImpactMatrix resultMatrix = new CrossImpactMatrix(new SquareMatrix(exitImpactMatrix).copy().flush());
        double totalCount = 0;
        for (int impactor = 1; impactor <= resultMatrix.getVarCount(); impactor++) {
            ImpactChain chain = new ImpactChain(exitImpactMatrix, Arrays.asList(impactor));
            double count = sumImpacts(chain, impactThreshold, resultMatrix);
            totalCount += count;
        }
        return resultMatrix;
    }

    /**
     * If impact of <b>chain</b> is higher than <b>impactThreshold</b>,
     * it is added to <b>resultMatrix</b> and the possible immediate expansions of
     * <b>chain</b> are generated
     * and {@link CrossImpactMatrix#sumImpacts(exit.ImpactChain, double, exit.CrossImpactMatrix)}
     * is called recursively on the expansion chains.
     * @param chain <code>ImpactChain</code> to consider for addition to <b>resultMatrix</b>
     * @param impactThreshold <b>chain</b> must have an impact of at least this value to be added to <b>resultMatrix</b>
     * @param resultMatrix <code>EXITImpactMatrix</code> where the significant values are summed
     * @return count of significant impact chains found in <b>chain</b> and its expansions.
     */
    private double sumImpacts(ImpactChain chain, double impactThreshold, CrossImpactMatrix resultMatrix) {
        double count = 0;
        if (Math.abs(chain.impact()) >= impactThreshold) {
            count++;
            int impactor = chain.impactorIndex();
            int impacted = chain.impactedIndex();
            double accumulatedValue = resultMatrix.getValue(impactor, impacted);
            double additionValue = chain.impact();
            double newValue = accumulatedValue + additionValue;
            if (impactor != impacted) {
                resultMatrix.setValue(impactor, impacted, newValue);
            }
            if (chain.hasExpansion()) {
                Set<ImpactChain> expansions = chain.continuedByOne();
                for (ImpactChain ic : expansions) {
                    count += sumImpacts(ic, impactThreshold, resultMatrix);
                }
            }
        }
        return count;
    }
    
}
