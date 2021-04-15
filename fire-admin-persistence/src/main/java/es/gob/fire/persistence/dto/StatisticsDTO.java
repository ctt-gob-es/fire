package es.gob.fire.persistence.dto;

public class StatisticsDTO {
	
	/**
	 * Attribute that represents the value of the application, provider, format or improved format signature.
	 */
	private String name;
	
	/**
	 * Attribute that represents the value of the correct signatures.
	 */
	private Integer corrects;
	
	/**
	 * Attribute that represents the value of the incorrect signatures.
	 */
	private Integer incorrects;
	
	/**
	 * Attribute that represents the value of the total signatures.
	 */
	private Integer total;
	
	public StatisticsDTO() {
		
	}

	public StatisticsDTO(final String name, final Integer corrects, final Integer incorrects, final Integer total) {
		this.name = name;
		this.corrects = corrects;
		this.incorrects = incorrects;
		this.total = total;
	}
	
	public String getName() {
		return name;
	}

	public void setName(final String nameP) {
		this.name = nameP;
	}

	public Integer getCorrects() {
		return corrects;
	}

	public void setCorrects(final Integer correctsP) {
		this.corrects = correctsP;
	}

	public Integer getIncorrects() {
		return incorrects;
	}

	public void setIncorrects(final Integer incorrectsP) {
		this.incorrects = incorrectsP;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(final Integer totalP) {
		this.total = totalP;
	}

}
