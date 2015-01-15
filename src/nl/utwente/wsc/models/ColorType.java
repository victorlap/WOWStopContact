package nl.utwente.wsc.models;

/**
 * The type of color a WSc currently emits.
 * 
 * @author rvemous
 */
public enum ColorType {
	/**
	 * LED of the WSc is turned off.
	 */
    NONE, 
    /**
     * LED of WSc emits green light.
     */
    GREEN, 
    /**
     * LED of WSc emits orange light.
     */
    ORANGE, 
    /**
     * LED of WSc emits red light.
     */
    RED;
                
    /**
     * Gets the color type belonging to this string.
     * 
     * @param description to use
     * @return the type
     */
    public static ColorType getType(String description) { 
        for (ColorType type : values()) {
        	if (type.toString().equalsIgnoreCase(description)) {
        		return type;
        	}
        }
    	return null;
    }
    
    /**
     * Gets the description of this color type.
     * 
     * @return the description
     */
    public String toString() {
        return this.name();
    }
}
