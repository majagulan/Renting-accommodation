package com.project.Rentingaccommodation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="user")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", updatable = false, nullable = false, insertable=false)
	private Long id;
	
	@Column(name="name", columnDefinition="VARCHAR(50)", nullable=false)
	private String name;
	
	@Column(name="surname", columnDefinition="VARCHAR(50)", nullable=false)
	private String surname;
	
	@Column(name="password", columnDefinition="VARCHAR(100)", nullable=false)
	private String password;
	
	@Column(name="email", columnDefinition="VARCHAR(50)", unique=true, nullable=false)
	private String email;

	@OneToOne
	@JoinColumn(name = "city_id", nullable = false)
	private City city;
	
	@Column(name="street", columnDefinition="VARCHAR(50)", nullable=false)
	private String street;
	
	@Column(name="phone", columnDefinition="VARCHAR(50)", nullable=false)
	private String phone;
	
	@Column(name="question", columnDefinition="VARCHAR(100)", nullable=false)
	private String question;
	
	@Column(name="answer", columnDefinition="VARCHAR(100)", nullable=false)
	private String answer;
	
	@Column(name="block_time", columnDefinition="VARCHAR(50)", nullable=true)
	private String block_time;
	
	@Column(name="max_tries", columnDefinition="INT(11) DEFAULT 0", nullable=false)
	private int max_tries;
	
	@Enumerated(EnumType.STRING)
	private UserStatus status;
	
	public User() {
		
	}

	public User(String name, String surname, String password, String email, City city,
			String street, String phone, String question, String answer, UserStatus status) {
		super();
		this.name = name;
		this.surname = surname;
		this.password = password;
		this.email = email;
		this.city = city;
		this.street = street;
		this.phone = phone;
		this.question = question;
		this.answer = answer;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public int getMax_tries() {
		return max_tries;
	}

	public void setMax_tries(int max_tries) {
		this.max_tries = max_tries;
	}

	public String getBlock_time() {
		return block_time;
	}

	public void setBlock_time(String block_time) {
		this.block_time = block_time;
	}
}
