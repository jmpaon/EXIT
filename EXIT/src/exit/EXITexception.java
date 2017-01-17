/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exit;

/**
 *
 * @author jmpaon
 */
public class EXITexception extends RuntimeException {

    /**
     * Creates a new instance of
     * <code>EXITException</code> without detail message.
     */
    public EXITexception() {
        super();
    }

    /**
     * Constructs an instance of
     * <code>EXITException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public EXITexception(String msg) {
        super(msg);
    }
    
    public EXITexception(String msg, Object... objs) {
        super(String.format(msg, objs));
    }
}
