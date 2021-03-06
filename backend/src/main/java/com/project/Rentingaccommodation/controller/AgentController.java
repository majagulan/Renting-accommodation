package com.project.Rentingaccommodation.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.project.Rentingaccommodation.model.Admin;
import com.project.Rentingaccommodation.model.Agent;
import com.project.Rentingaccommodation.model.AgentStatus;
import com.project.Rentingaccommodation.model.City;
import com.project.Rentingaccommodation.model.Country;
import com.project.Rentingaccommodation.model.UserPrivileges;
import com.project.Rentingaccommodation.model.UserRoles;
import com.project.Rentingaccommodation.model.DTO.AgentDTO;
import com.project.Rentingaccommodation.model.DTO.LoginDTO;
import com.project.Rentingaccommodation.model.DTO.PasswordChangeDTO;
import com.project.Rentingaccommodation.model.DTO.SecurityQuestionDTO;
import com.project.Rentingaccommodation.security.JwtAgent;
import com.project.Rentingaccommodation.security.JwtGenerator;
import com.project.Rentingaccommodation.security.JwtUser;
import com.project.Rentingaccommodation.security.JwtUserPermissions;
import com.project.Rentingaccommodation.security.JwtValidator;
import com.project.Rentingaccommodation.service.AdminService;
import com.project.Rentingaccommodation.service.AgentService;
import com.project.Rentingaccommodation.service.CityService;
import com.project.Rentingaccommodation.service.CountryService;
import com.project.Rentingaccommodation.service.UserService;
import com.project.Rentingaccommodation.utils.PasswordUtil;
import com.project.Rentingaccommodation.utils.SendMail;
import com.project.Rentingaccommodation.utils.UserUtils;

@RestController
@RequestMapping(value = "/api/agents")
public class AgentController {

	@Autowired
	private AgentService service;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	private JwtGenerator jwtGenerator;
	
	@Autowired
	private JwtValidator jwtValidator;
	
	@Autowired
	private JwtUserPermissions jwtUserPermissions;
	
	@Autowired
	private CountryService countryService;
	
	private static final Charset charset = Charset.forName("UTF-8");
	
	public static String url = "http://localhost:8082/certificates/generateCertificate";

	
	@RequestMapping(value="", method=RequestMethod.GET)
	public ResponseEntity<List<Agent>> getAgents() {
		return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getCountries",method = RequestMethod.GET)
	public ResponseEntity<List<Country>> getCountries() 
	{
		List<Country> countries = countryService.findAll();
        return new ResponseEntity<List<Country>>(countries, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getCities/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<City>> getCities(@PathVariable String id) 
	{
		List<City> cities = cityService.findByCountryId(Long.valueOf(id));
		
        return new ResponseEntity<List<City>>(cities, HttpStatus.OK);
	}
	
	@RequestMapping(value="/approved", method=RequestMethod.GET)
	public ResponseEntity<List<Agent>> getApprovedAgents() {
		return new ResponseEntity<>(service.findApprovedAgents(), HttpStatus.OK);
	}
	
	@RequestMapping(value="/{id}/approved", method=RequestMethod.GET)
	public ResponseEntity<Object> getApprovedAgent(@PathVariable Long id) {
		Agent agent = service.findOne(id);
		if (agent == null) {
			return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(service.findApprovedAgent(agent), HttpStatus.OK);
	}
	
	@RequestMapping(value="/waiting", method=RequestMethod.GET)
	public ResponseEntity<List<Agent>> getWaitingAgents() {
		return new ResponseEntity<>(service.findWaitingAgents(), HttpStatus.OK);
	}
	
	@RequestMapping(value="/{id}/waiting", method=RequestMethod.GET)
	public ResponseEntity<Object> getWaitingAgent(@PathVariable Long id) {
		Agent agent = service.findOne(id);
		if (agent == null) {
			return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(service.findWaitingAgent(agent), HttpStatus.OK);
	}
	
	@RequestMapping(value="/declined", method=RequestMethod.GET)
	public ResponseEntity<List<Agent>> getDeclinedAgents() {
		return new ResponseEntity<>(service.findDeclinedAgents(), HttpStatus.OK);
	}
	
	@RequestMapping(value="/{id}/declined", method=RequestMethod.GET)
	public ResponseEntity<Object> getDeclinedAgent(@PathVariable Long id) {
		Agent agent = service.findOne(id);
		if (agent == null) {
			return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(service.findDeclinedAgent(agent), HttpStatus.OK);
	}
	
	@RequestMapping(value="/{id}/approve", method=RequestMethod.PUT)
	public ResponseEntity<Object> approveAgent(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
		if(!jwtUserPermissions.hasRoleAndPrivilege(authHeader, UserRoles.ADMIN, UserPrivileges.WRITE_PRIVILEGE)) {
			return new ResponseEntity<>("Not authorized", HttpStatus.UNAUTHORIZED);
		}
		try {
			String token = authHeader.split(" ")[1].trim();
			JwtUser jwtUser = jwtValidator.validate(token);
			if (jwtUser != null) {
				Admin admin = adminService.findByIdAndEmail(jwtUser.getId(), jwtUser.getEmail());
				if (admin == null) {
					return new ResponseEntity<>("Admin not found.", HttpStatus.NOT_FOUND);
				}
				Agent agent = service.findOne(id);
				if (agent == null) {
					return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
				}
				agent.setStatus(AgentStatus.APPROVED);
				return new ResponseEntity<>(service.save(agent), HttpStatus.OK);
			} else {
				return new ResponseEntity<>("User with this email doesn't exist.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>("Error validating token.", HttpStatus.FORBIDDEN);
		}
	}
	
	@RequestMapping(value="/{id}/decline", method=RequestMethod.PUT)
	public ResponseEntity<Object> declineAgent(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
		if(!jwtUserPermissions.hasRoleAndPrivilege(authHeader, UserRoles.ADMIN, UserPrivileges.WRITE_PRIVILEGE)) {
			return new ResponseEntity<>("Not authorized", HttpStatus.UNAUTHORIZED);
		}
		try {
			String token = authHeader.split(" ")[1].trim();
			JwtUser jwtUser = jwtValidator.validate(token);
			if (jwtUser != null) {
				Admin admin = adminService.findByIdAndEmail(jwtUser.getId(), jwtUser.getEmail());
				if (admin == null) {
					return new ResponseEntity<>("Admin not found.", HttpStatus.NOT_FOUND);
				}
				Agent agent = service.findOne(id);
				if (agent == null) {
					return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
				}
				agent.setStatus(AgentStatus.DECLINED);
				return new ResponseEntity<>(service.save(agent), HttpStatus.OK);
			} else {
				return new ResponseEntity<>("User with this email doesn't exist.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>("Error validating token.", HttpStatus.FORBIDDEN);
		}
	}
	
	@RequestMapping(value="/{id}/remove-approval", method=RequestMethod.PUT)
	public ResponseEntity<Object> removeApprovalOfAgent(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
		if(!jwtUserPermissions.hasRoleAndPrivilege(authHeader, UserRoles.ADMIN, UserPrivileges.WRITE_PRIVILEGE)) {
			return new ResponseEntity<>("Not authorized", HttpStatus.UNAUTHORIZED);
		}
		try {
			String token = authHeader.split(" ")[1].trim();
			JwtUser jwtUser = jwtValidator.validate(token);
			if (jwtUser != null) {
				Admin admin = adminService.findByIdAndEmail(jwtUser.getId(), jwtUser.getEmail());
				if (admin == null) {
					return new ResponseEntity<>("Admin not found.", HttpStatus.NOT_FOUND);
				}
				Agent agent = service.findOne(id);
				if (agent == null) {
					return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
				}
				agent.setStatus(AgentStatus.REMOVED_APPROVAL);
				return new ResponseEntity<>(service.save(agent), HttpStatus.OK);
			} else {
				return new ResponseEntity<>("User with this email doesn't exist.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>("Error validating token.", HttpStatus.FORBIDDEN);
		}
	}
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<Object> registerAgent(@RequestBody AgentDTO agent) {
		
		if (UserUtils.userExists(agent.getEmail(), userService, adminService, service)) {
			return new ResponseEntity<>("User with this email already exists.", HttpStatus.FORBIDDEN);
		}
		
		if (agent.getPassword().length() < 8) {
			return new ResponseEntity<>("Password must be at least 8 characters long.", HttpStatus.NOT_ACCEPTABLE);
		}
		
		String name = agent.getName();
		String surname = agent.getSurname();
		int businessId = agent.getBusinessId();
		String email = agent.getEmail();
		City city = cityService.findOne(Long.valueOf(agent.getCity()));
		String street = agent.getStreet();
		String phone = agent.getPhone();
		String password = PasswordUtil.hash(agent.getPassword().toCharArray(), charset);
		String question = agent.getQuestion();	
		String answer = PasswordUtil.hash(agent.getAnswer().toCharArray(), charset);	
		
		Agent regAgent = new Agent(name, surname, password, email, city,
				 street, phone, question, answer, AgentStatus.WAITING, businessId);
		service.save(regAgent);
		
		try {
			createCertificate(regAgent.getEmail());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		buildSessionFactory(agent.getEmail());
		
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/login", method=RequestMethod.POST)
	public ResponseEntity<Object> loginAgent(@RequestBody LoginDTO loginDTO) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		
		System.out.println("jfkajfkl " + loginDTO.getEmail() + " " + loginDTO.getPassword());
		Agent agent = service.findByEmail(loginDTO.getEmail());
		
		if(agent == null)
			return new ResponseEntity<>("You have to register first", HttpStatus.NOT_FOUND);
		
		System.out.println("aaaa " + agent);
		System.out.println("aaaa " + agent.getEmail());
		
		HashMap<String, Object> response = new HashMap<String, Object>();
		
		if (!agent.getStatus().equals(AgentStatus.APPROVED)) {
			return new ResponseEntity<>("This agent is either declined or don't have approval.", HttpStatus.FORBIDDEN);
		}
		
		if(agent != null)
		{			
			System.out.println("agent");
			String verifyHash = agent.getPassword();
			String verifyPass = loginDTO.getPassword();
			
			if(!PasswordUtil.verify(verifyHash, verifyPass.toCharArray(), charset))
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			
			Session session = getSession(agent.getEmail());
			Transaction tx = session.beginTransaction();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".reservation;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".accommodation;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".apartment;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".message;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".accommodation_type;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".accommodation_category;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".image;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".city;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".country;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".bed_type;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".user;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".agent;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".additional_service;").executeUpdate();
			session.createNativeQuery("TRUNCATE db"+agent.getId()+".apartment_additional_service;").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".reservation (SELECT * FROM renting_accommodation_db.reservation "
					+ "WHERE reservation_id IN (SELECT renting_accommodation_db.reservation.reservation_id FROM renting_accommodation_db.reservation "
					+ "INNER JOIN renting_accommodation_db.apartment ON renting_accommodation_db.reservation.apartment_id = renting_accommodation_db.apartment.apartment_id "
					+ "INNER JOIN renting_accommodation_db.accommodation "
					+ "ON renting_accommodation_db.apartment.accommodation_id = renting_accommodation_db.accommodation.accommodation_id "
					+ "WHERE agent_id="+agent.getId()+"));").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".accommodation (SELECT * FROM renting_accommodation_db.accommodation WHERE agent_id = "+ agent.getId()+")").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".apartment (SELECT * FROM renting_accommodation_db.apartment WHERE accommodation_id IN "
					+ "(SELECT renting_accommodation_db.accommodation.accommodation_id FROM renting_accommodation_db.accommodation "
					+ "INNER JOIN renting_accommodation_db.apartment ON "
					+ "renting_accommodation_db.accommodation.accommodation_id = renting_accommodation_db.apartment.accommodation_id WHERE agent_id= " + agent.getId() +"));").executeUpdate();
					
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".message (SELECT * FROM renting_accommodation_db.message WHERE agent_id = "+agent.getId()+")").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".accommodation_type (SELECT * FROM renting_accommodation_db.accommodation_type where status='ACTIVE')").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".accommodation_category (SELECT * FROM renting_accommodation_db.accommodation_category where status='ACTIVE')").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".image (SELECT * FROM renting_accommodation_db.image "
					+ "WHERE accommodation_id IN (SELECT renting_accommodation_db.accommodation.accommodation_id FROM renting_accommodation_db.accommodation "
					+ "WHERE agent_id ="+ agent.getId()+"));").executeUpdate();
					 
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".image (SELECT * FROM renting_accommodation_db.image "
					+ "WHERE apartment_id IN (SELECT renting_accommodation_db.apartment.apartment_id FROM renting_accommodation_db.apartment "
					+ "INNER JOIN renting_accommodation_db.accommodation ON renting_accommodation_db.accommodation.accommodation_id = renting_accommodation_db.apartment.accommodation_id WHERE agent_id ="+ agent.getId()+"));").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".city (SELECT * FROM renting_accommodation_db.city)").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".country (SELECT * FROM renting_accommodation_db.country)").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".user (SELECT * FROM renting_accommodation_db.user)").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".agent (SELECT * FROM renting_accommodation_db.agent where agent_id=" + agent.getId() + ")").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".additional_service (SELECT * FROM renting_accommodation_db.additional_service)").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".bed_type (SELECT * FROM renting_accommodation_db.bed_type)").executeUpdate();
			
			session.createNativeQuery("INSERT INTO db"+agent.getId()+".apartment_additional_service (SELECT * FROM renting_accommodation_db.apartment_additional_service "
					+ "WHERE apartment_id IN (SELECT renting_accommodation_db.apartment.apartment_id FROM renting_accommodation_db.apartment "
					+ "INNER JOIN renting_accommodation_db.accommodation ON renting_accommodation_db.accommodation.accommodation_id = renting_accommodation_db.apartment.accommodation_id "
					+ "WHERE agent_id ="+ agent.getId()+"));").executeUpdate();	
			
			tx.commit();
			session.close();
			
			String token = jwtGenerator.generateAgent(new JwtAgent(agent.getId(), agent.getEmail(), UserRoles.AGENT.toString(), UserPrivileges.READ_WRITE_PRIVILEGE.toString()));
			System.out.println("jwt " + new JwtAgent(agent.getId(), agent.getEmail(), UserRoles.AGENT.toString(), UserPrivileges.WRITE_PRIVILEGE.toString()));
			
			System.out.println("toke " + token);
			response = new HashMap<String, Object>();
			response.put("token", token);
			System.out.println("res " + response);
			
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> changePassword(@RequestBody PasswordChangeDTO passDTO) {
		System.out.println(passDTO);
		if (passDTO.getOldPassword() == null || passDTO.getOldPassword() == "" ||
			passDTO.getNewPassword() == null || passDTO.getNewPassword() == "" ||
			passDTO.getToken() == null || passDTO.getToken() == "") {
			return new ResponseEntity<>("Old password, new password and token must be provided.", HttpStatus.FORBIDDEN);
		}
		
		Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[A-Z])(.{10,})$");
		Matcher oldPasswordMatcher = passwordPattern.matcher(passDTO.getOldPassword());
		Matcher newPasswordMatcher = passwordPattern.matcher(passDTO.getNewPassword());
		
		if (!oldPasswordMatcher.find()) {
			return new ResponseEntity<>("Old password must be one uppercase, one lowercase, one number and at least 10 characters long.", HttpStatus.NOT_ACCEPTABLE);
		}
		
		if (!newPasswordMatcher.find()) {
			return new ResponseEntity<>("New password must be one uppercase, one lowercase, one number and at least 10 characters long.", HttpStatus.NOT_ACCEPTABLE);
		}
		
		String oldPassword = passDTO.getOldPassword();
		String newPassword = passDTO.getNewPassword();
		
		if (oldPassword.equals(newPassword)) {
			return new ResponseEntity<>("Old password and new password must be different.", HttpStatus.NOT_ACCEPTABLE);
		}
		
		JwtAgent jwtAgent = jwtValidator.validateAgent(passDTO.getToken());
		
		if (jwtAgent == null) {
			return new ResponseEntity<>("User with the given token doesn't exist.", HttpStatus.NOT_FOUND);
		}
		
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		Agent loggedInAgent =  service.findByIdAndEmail(jwtAgent.getId(), jwtAgent.getEmail());
		
		if (loggedInAgent.getMax_tries() == 3) {
			try {
				String dateTime = dateTimeFormatter.format(new Date());
				Date currentDateTime = dateTimeFormatter.parse(dateTime);
				Date userBlockDateTime = dateTimeFormatter.parse(loggedInAgent.getBlock_time());
				if (currentDateTime.getTime() - userBlockDateTime.getTime() >= 1*60*1000) {
					loggedInAgent.setBlock_time(null);
					loggedInAgent.setStatus(AgentStatus.APPROVED);
					loggedInAgent.setMax_tries(0);
				} else {
					return new ResponseEntity<>("This user is blocked for 10 minutes.", HttpStatus.FORBIDDEN);
				}
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
		
		String verifyHash = loggedInAgent.getPassword();
		
		if(!PasswordUtil.verify(verifyHash, oldPassword.toCharArray(), charset)) {
			loggedInAgent.setMax_tries(loggedInAgent.getMax_tries() + 1);
			if (loggedInAgent.getMax_tries() == 3) {
				loggedInAgent.setStatus(AgentStatus.BLOCKED);
				loggedInAgent.setBlock_time(dateTimeFormatter.format(new Date()));
				loggedInAgent.setMax_tries(3);
				service.save(loggedInAgent);
				return new ResponseEntity<>(loggedInAgent, HttpStatus.FORBIDDEN);
			}
			service.save(loggedInAgent);
			return new ResponseEntity<>("Old password is incorrect.", HttpStatus.FORBIDDEN);
		}
		String password = PasswordUtil.hash(newPassword.toCharArray(), charset);
		
		loggedInAgent.setStatus(AgentStatus.APPROVED);
		loggedInAgent.setPassword(password);
		service.save(loggedInAgent);
		return new ResponseEntity<>(jwtAgent, HttpStatus.OK);
	}

	@RequestMapping(value = "/question/{email}", method = RequestMethod.GET)
	public ResponseEntity<Object> getQuestion(@PathVariable String email) throws ParseException {
		if (email == null || email == "") {
			return new ResponseEntity<>("Email address is required.", HttpStatus.FORBIDDEN);
		}
		
		Agent agent = service.findByEmail(email);
		if (agent == null) {
			return new ResponseEntity<>(new String("This agent doesn't exist."), HttpStatus.NOT_FOUND);
		}
		
		String question = agent.getQuestion();
		String jsonString = "{\"question\":\""+question+"\"}";
		JSONParser parser = new JSONParser(); 
		JSONObject json = (JSONObject) parser.parse(jsonString);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public ResponseEntity<Object> resetPassword(@RequestBody SecurityQuestionDTO questionDTO) throws ParseException {
		if (questionDTO.getEmail() == null || questionDTO.getEmail() == "" ||
			questionDTO.getAnswer() == null || questionDTO.getAnswer() == "") {
			return new ResponseEntity<>("Email and answer must be provided.", HttpStatus.FORBIDDEN);
		}
		String email = questionDTO.getEmail();
		String answer = questionDTO.getAnswer();
		Agent agent = service.findByEmail(email);
		
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		if (agent.getMax_tries() == 3) {
			try {
				String dateTime = dateTimeFormatter.format(new Date());
				Date currentDateTime = dateTimeFormatter.parse(dateTime);
				Date userBlockDateTime = dateTimeFormatter.parse(agent.getBlock_time());
				if (currentDateTime.getTime() - userBlockDateTime.getTime() >= 1*60*1000) {
					agent.setBlock_time(null);
					agent.setStatus(AgentStatus.APPROVED);
					agent.setMax_tries(0);
				} else {
					return new ResponseEntity<>("This user is blocked for 10 minutes.", HttpStatus.FORBIDDEN);
				}
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
		
		if(!PasswordUtil.verify(agent.getAnswer(), answer.toCharArray(), charset)) {
			agent.setMax_tries(agent.getMax_tries() + 1);
			if (agent.getMax_tries() == 3) {
				agent.setStatus(AgentStatus.BLOCKED);
				agent.setBlock_time(dateTimeFormatter.format(new Date()));
				agent.setMax_tries(3);
				service.save(agent);
				return new ResponseEntity<>(agent, HttpStatus.FORBIDDEN);
			}
			service.save(agent);
			return new ResponseEntity<>("Answer is incorrect.", HttpStatus.NOT_ACCEPTABLE);
		}
		
		String randomPassword = SendMail.sendEmail("\""+email+"\"");
		
		String password = PasswordUtil.hash(randomPassword.toCharArray(), charset);
		agent.setPassword(password);
		service.save(agent);
		return new ResponseEntity<>(agent, HttpStatus.OK);
	}
	
	
	private void buildSessionFactory(String email) {
		Configuration conf = new Configuration().configure();
	      // <!-- Database connection settings -->
        String url = "jdbc:mysql://localhost:3306/db" + service.findByEmail(email).getId() + "?createDatabaseIfNotExist=true&useSSL=false";

	    conf.setProperty("hibernate.connection.url", url);
	    conf.setProperty("hibernate.hbm2ddl.auto", "create");
		SessionFactory sessionFactory = conf.buildSessionFactory();
		sessionFactory.close();
	}
	
	public Session getSession(String email) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		Configuration conf = new Configuration().configure();
			// <!-- Database connection settings -->
	    String url = "jdbc:mysql://localhost:3306/db" + service.findByEmail(email).getId() + "?createDatabaseIfNotExist=true&useSSL=false";
	    conf.setProperty("hibernate.connection.url", url);
	    conf.setProperty("hibernate.hbm2ddl.auto", "none");
		SessionFactory sessionFactory = conf.buildSessionFactory();
			
		return sessionFactory.openSession();
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public ResponseEntity<Object> getAgent(@PathVariable Long id) {
		Agent agent = service.findOne(id);
		if (agent == null) {
			return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(agent, HttpStatus.OK);	
	}
	
	@RequestMapping(value="/business-id/{businessId}", method=RequestMethod.GET)
	public ResponseEntity<Object> getAgentByBusinessId(@PathVariable Long businessId) {
		Agent agent = service.findByBusinessId(businessId);
		if (agent == null) {
			return new ResponseEntity<>("Agent not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(agent, HttpStatus.OK);	
	}
	
	public void createCertificate(String email) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(email,charset));
		HttpResponse response = client.execute(post);
		System.out.println("Response Code : " 
                + response.getStatusLine().getStatusCode());

	}
	
	public String generate(JwtAgent jwtAgent) {
    	if (jwtAgent.getEmail() == null) {
    		return "agent with this email doesn't exist.";
    	}
        return jwtGenerator.generateAgent(jwtAgent);
    }
}
