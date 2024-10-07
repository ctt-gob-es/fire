package es.gob.fire.persistence.dto;

import java.util.Date;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.persistence.entity.User;

/**
 * DTO for displaying user information in a table format.
 * This class does not include sensitive information such as passwords.
 */
public class UserTableDTO {

	@JsonView(DataTablesOutput.View.class)
    private Long userId;

	@JsonView(DataTablesOutput.View.class)
    private String userName;
	
	@JsonView(DataTablesOutput.View.class)
	private String password;

	@JsonView(DataTablesOutput.View.class)
    private String email;

	@JsonView(DataTablesOutput.View.class)
    private String name;
	
	@JsonView(DataTablesOutput.View.class)
    private String surnames;
	
	@JsonView(DataTablesOutput.View.class)
    private String phone;
	
	@JsonView(DataTablesOutput.View.class)
    private Date startDate;
	
	@JsonView(DataTablesOutput.View.class)
    private Boolean root;
	
	@JsonView(DataTablesOutput.View.class)
    private String rolName;

    // Constructors
    public UserTableDTO() {
    }
    
    public UserTableDTO(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.name = user.getName();
        this.surnames = user.getSurnames();
        this.phone = user.getPhone();
        this.startDate = user.getStartDate();
        this.root = user.getRoot();
        this.rolName = user.getRol() != null ? user.getRol().getRolName() : null;
    }

    public UserTableDTO(Long userId, String userName, String password, String email, String name, String surnames, String phone, Date startDate, Boolean root, String rolName) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.name = name;
        this.surnames = surnames;
        this.phone = phone;
        this.startDate = startDate;
        this.root = root;
        this.rolName = rolName;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurnames() {
        return surnames;
    }

    public void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Boolean getRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public String getRolName() {
        return rolName;
    }

    public void setRolName(String rolName) {
        this.rolName = rolName;
    }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
