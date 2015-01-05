package nl.utwente.wowstopcontact.communication.model.exception;

/**
 * Used to signal a packets' contents are malformed or 
 * invalid.<br>
 *
 * @author rvemous
 */
public class InvalidPacketException extends Exception {
  
    /**
     * Create a new packet exception.
     *
     * @param processId the name of the process that throws the exception
     * @param cause of the exception
     */
    public InvalidPacketException(String processId, String cause) {
        super(processId + " - " + cause);
    }
   
    /**
     * Create a new packet exception.
     */
    public InvalidPacketException() {
        super();
    }
    
}
