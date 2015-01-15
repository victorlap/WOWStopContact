package nl.utwente.wsc.communication;

public enum ValueType {

	IS_ON, TURN_OFF, TURN_ON, VALUES_POWER, VALUES_COLOR, CONNECTING, DISCONNECTING, CONN_DEAD;

	/**
	 * Gets the value type belonging to this string.
	 * 
	 * @param description to use
	 * @return the type
	 */
	public static ValueType getType(String description) {
		for (ValueType type : values()) {
			if (type.toString().equalsIgnoreCase(description)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Gets the description of this value type.
	 * 
	 * @return the description
	 */
	public String toString() {
		return this.name();
	}

}
