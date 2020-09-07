package es.gob.fire.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupedStatistics {
	
	/**
	 * Attribute that represents the field used to calculate the statistics by grouping. 
	 */
	private int field = 0;
	
	/**
	 * Attribute that represents the result. 
	 */
	private Map<String,Long> values = new LinkedHashMap<String, Long>();
	
	/**
	 * Attribute that represents the result. 
	 */
	private Map<String,Double> valuesSize = new LinkedHashMap<String, Double>();

	/**
	 * Constructor method for the class GroupedStatistics.java. 
	 */
	public GroupedStatistics() {
	}

	
	/**
	 * Gets the value of the field used to calculate the statistics by grouping.
	 * @return the value of the field used to calculate the statistics by grouping.
	 */
	public int getField() {
		return field;
	}

	
	/**
	 * Sets the value of the field used to calculate the statistics by grouping.
	 * @param groupedField The value for the field used to calculate the statistics by grouping.
	 */
	public void setField(int groupedField) {
		this.field = groupedField;
	}

	
	/**
	 * Gets the value of the grouped result.
	 * @return the value of the grouped result.
	 */
	public Map<String, Long> getValues() {
		return values;
	}

	
	/**
	 * Sets the value of the grouped result.
	 * @param result The value for the grouped result.
	 */
	public void setValues(Map<String, Long> result) {
		this.values = result;
	}


	/**
	 * Gets the value of the grouped result.
	 * @return the value of the grouped result.
	 */
	public Map<String, Double> getValuesSize() {
		return valuesSize;
	}


	/**
	 * Sets the value of the grouped result.
	 * @param result The value for the grouped result.
	 */
	public void setValuesSize(Map<String, Double> valuesSize) {
		this.valuesSize = valuesSize;
	}
	
	

}
