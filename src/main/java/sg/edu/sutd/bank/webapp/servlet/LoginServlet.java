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
import static sg.edu.sutd.bank.webapp.servlet.ServletPaths.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.User;
import sg.edu.sutd.bank.webapp.model.UserStatus;
import sg.edu.sutd.bank.webapp.service.UserDAO;
import sg.edu.sutd.bank.webapp.service.UserDAOImpl;
//for password hashing
import java.security.MessageDigest;

import java.util.regex.Pattern;

@WebServlet(LOGIN)
public class LoginServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private transient UserDAO userDAO = new UserDAOImpl();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String userName = req.getParameter("username");
			User user = userDAO.loadUser(userName);
	        	if (userName.indexOf("<script>")!=-1) {
					sendError(req, "Potential XML Injection Attack Detected.");
					forward(req, resp);
					return;
	            }
	        
			if (user != null && (user.getStatus() == UserStatus.APPROVED)) {
				req.login(userName, hashpassword(req.getParameter("password")));
				HttpSession session = req.getSession(true);
				session.setAttribute("authenticatedUser", req.getRemoteUser());
				setUserId(req, user.getId());
				if (req.isUserInRole("client")) {
					// update database attempt to 0
					userDAO.updateAttemptSuccess(req.getParameter("username"));
					redirect(resp, CLIENT_DASHBOARD_PAGE);
				} else if (req.isUserInRole("staff")) {
					redirect(resp, STAFF_DASHBOARD_PAGE);
				}
				return;
			}
				// check if user is lockout or failed login
				if (userDAO.checklockout(req.getParameter("username"))) {
					sendError(req, "Account Locked. Email sutdbank.contact@gmail.com for support");
				}else if (userDAO.checkApproved(req.getParameter("username"))==false) {
					sendError(req, "Account have not been approved by administrator. Email sutdbank.contact@gmail.com for support");
				}else
					sendError(req, "Invalid username/password!");
				{
				
			}
		} catch(ServletException | ServiceException ex) {
			try {
				// update database to increase attempt++
				userDAO.updateAttemptFailed(req.getParameter("username"));
			} catch (ServiceException e) {
				sendError(req, e.getMessage());
			}
			sendError(req, ex.getMessage());
		}
		forward(req, resp);
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
