/*
 * Copyright 2017 SUTD Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

https://opensource.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
 */

package sg.edu.sutd.bank.webapp.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.ClientInfo;
import sg.edu.sutd.bank.webapp.model.Role;
import sg.edu.sutd.bank.webapp.model.User;
import sg.edu.sutd.bank.webapp.model.UserRole;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAO;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAOImpl;
import sg.edu.sutd.bank.webapp.service.EmailService;
import sg.edu.sutd.bank.webapp.service.EmailServiceImp;
import sg.edu.sutd.bank.webapp.service.UserDAO;
import sg.edu.sutd.bank.webapp.service.UserDAOImpl;
import sg.edu.sutd.bank.webapp.service.UserRoleDAO;
import sg.edu.sutd.bank.webapp.service.UserRoleDAOImpl;

/**
 * @author SUTD
 */
@WebServlet("/register")
public class RegisterServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private transient ClientInfoDAO clientAccountDAO = new ClientInfoDAOImpl();
	private transient UserDAO userDAO = new UserDAOImpl();
	private transient UserRoleDAO userRoleDAO = new UserRoleDAOImpl();
	private transient EmailService emailService = new EmailServiceImp();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		User user = new User();
		user.setUserName(request.getParameter("username"));
		user.setPassword(request.getParameter("password"));
		boolean hasLetter = false;
	    boolean hasDigit = false;
	    boolean hassymbol = false;
	    String s = "~!@#$%^&*()_+=-}{[]?></|`";  
		 if (user.getPassword().length() < 8) {
			 sendError(request, "Password needs to be at least 8 characters long.");
			 forward(request, response);
			 return;
		 }
		 for (int i = 0; i < user.getPassword().length(); i++) {
            char x = user.getPassword().charAt(i);
            if (Character.isLetter(x)) {
                hasLetter = true;
            }
            else if (Character.isDigit(x)) {
                hasDigit = true;
            }
            if (s.contains(Character.toString(x))) {
            	hassymbol = true;
            }
        
            // no need to check further, break the loop	                
		 }
         if(!(hasLetter && hasDigit && hassymbol)){
			 sendError(request, "Password needs to contain at least 1 letter, 1 digit and 1 symbol");
			 forward(request, response);
			 return;
         }
 		
        user.setPassword(hashpassword(request.getParameter("password"))); 

		ClientInfo clientAccount = new ClientInfo();
		clientAccount.setFullName(request.getParameter("fullName"));
		clientAccount.setFin(request.getParameter("fin"));
		clientAccount.setDateOfBirth(Date.valueOf(request.getParameter("dateOfBirth")));
		clientAccount.setOccupation(request.getParameter("occupation"));
		clientAccount.setMobileNumber(request.getParameter("mobileNumber"));
		clientAccount.setAddress(request.getParameter("address"));
		clientAccount.setEmail(request.getParameter("email"));
		clientAccount.setUser(user);
		
		try {
			userDAO.create(user);
			clientAccountDAO.create(clientAccount);
			UserRole userRole = new UserRole();
			userRole.setUser(user);
			userRole.setRole(Role.client);
			userRoleDAO.create(userRole );
			emailService.sendMail(clientAccount.getEmail(), "SutdBank registration", "Thank you for the registration!");
			sendMsg(request, "You are successfully registered...");
			redirect(response, ServletPaths.WELCOME);
		} catch (ServiceException e) {
			sendError(request, e.getMessage());
			forward(request, response);
		}
	}
	//for password hashing
	private static String hashpassword(String value) {
	    try{
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(value.getBytes());
	        return bytesToHex(md.digest());
	    } catch(Exception ex){
	        throw new RuntimeException(ex);
	    }
	 }
	//to convert from bytes to hex
	private static String bytesToHex(byte[] bytes) {
	    StringBuffer result = new StringBuffer();
	    for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
	    return result.toString();
	 }
}
