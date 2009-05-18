package org.rifidi.rmi.utils.exceptions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class was inspired by the three oreilly.com articles on RMI by William
 * Grosso.
 * 
 * Since all the semantics are captured in the name, we might as well make this
 * externalizable (saves a little bit on reflection, saves a little bit on
 * bandwidth).
 */

public class ServerUnavailable extends Exception implements Externalizable {
	
	/**
	 * TODO: Method level comment.  
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
	}

	/**
	 * TODO: Method level comment.  
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
	}
}
