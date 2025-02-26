package es.gob.fire.persistence.dto;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PROPIEDADES")
public class Property {

    @Id
    @Column(name = "CLAVE", nullable = false, length = 255)
    private String key;

    @Column(name = "VALOR_TEXTO", length = 4000)
    private String textValue;

    @Column(name = "VALOR_NUMERICO", precision = 19)
    private Long numericValue;

    @Column(name = "VALOR_FECHA")
    private LocalDateTime dateValue;

    @Column(name = "TIPO", nullable = false, length = 20)
    private String type;

    // Constructor vacío requerido por JPA
    public Property() {
    }

    // Constructor con parámetros
    public Property(String key, String textValue, Long numericValue, LocalDateTime dateValue, String type) {
        this.key = key;
        this.textValue = textValue;
        this.numericValue = numericValue;
        this.dateValue = dateValue;
        this.type = type;
    }

    // Getters y Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Long getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(Long numericValue) {
        this.numericValue = numericValue;
    }

    public LocalDateTime getDateValue() {
        return dateValue;
    }

    public void setDateValue(LocalDateTime dateValue) {
        this.dateValue = dateValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // toString()
    @Override
    public String toString() {
        return "Property{" +
                "key='" + key + '\'' +
                ", textValue='" + textValue + '\'' +
                ", numericValue=" + numericValue +
                ", dateValue=" + dateValue +
                ", type='" + type + '\'' +
                '}';
    }
}
