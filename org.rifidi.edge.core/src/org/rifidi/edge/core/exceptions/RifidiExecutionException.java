/**
 * 
 */
package org.rifidi.edge.core.exceptions;

/**
 * @author andreas
 *
 */
public class RifidiExecutionException extends RifidiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2515394276444517563L;

	/**
	 * 
	 */
	public RifidiExecutionException() {
		super();
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RifidiExecutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	
	}

	/**
	 * @param arg0
	 */
	public RifidiExecutionException(String arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 */
	public RifidiExecutionException(Throwable arg0) {
		super(arg0);
		
	}

}
