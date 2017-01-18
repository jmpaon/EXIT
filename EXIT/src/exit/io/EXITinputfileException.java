/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.io;

import exit.EXITexception;

/**
 *
 * @author jmpaon
 */
public class EXITinputfileException extends EXITexception {

    /**
     * Constructs an instance of <code>EXITinputfileException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public EXITinputfileException(String msg) {
        super(msg);
    }
    
    public EXITinputfileException(String msg, Object... objs) {
        super(String.format(msg,objs));
    }
    
}
