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

package sg.edu.sutd.bank.webapp.service;

import java.util.List;

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.User;


public interface UserDAO {

	User loadUser(String userName) throws ServiceException;

	void create(User user) throws ServiceException;

	void updateDecision(List<User> users) throws ServiceException;
	
	//update database to increase attempt++
	void updateAttemptFailed(String userName) throws ServiceException;
	
	// update database attempt to 0
	void updateAttemptSuccess(String userName) throws ServiceException;
	
	// check if user is lockout or failed login
	boolean checklockout(String userName) throws ServiceException;	
	
	// check if user approved
	boolean checkApproved(String userName) throws ServiceException;
}
