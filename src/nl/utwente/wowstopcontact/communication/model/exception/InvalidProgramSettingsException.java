package nl.utwente.wowstopcontact.communication.model.exception;

/**
 * Used to signal a program settings' contents are malformed or 
 * invalid.<br>
 *
 * @author rvemous
 */
public class InvalidProgramSettingsException extends Exception {
  
    /**
     * Create a new program settings file exception.
     *
     * @param processId the name of the process that throws the exception
     * @param cause of the exception
     */
    public InvalidProgramSettingsException(String processId, String cause) {
        super(processId + " - " + cause);
    }
   
    /**
     * Create a new program settings file exception.
     */
    public InvalidProgramSettingsException() {
        super();
    }
    
}
