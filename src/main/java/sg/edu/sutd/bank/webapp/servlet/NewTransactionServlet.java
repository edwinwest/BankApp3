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

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.ClientAccount;
import sg.edu.sutd.bank.webapp.model.ClientTransaction;
import sg.edu.sutd.bank.webapp.model.TransactionCodes;
import sg.edu.sutd.bank.webapp.model.User;
import sg.edu.sutd.bank.webapp.service.ClientAccountDAO;
import sg.edu.sutd.bank.webapp.service.ClientAccountDAOImpl;
import sg.edu.sutd.bank.webapp.service.ClientTransactionDAO;
import sg.edu.sutd.bank.webapp.service.ClientTransactionDAOImpl;
import sg.edu.sutd.bank.webapp.service.TransactionCodesDAO;
import sg.edu.sutd.bank.webapp.service.TransactionCodesDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

import static sg.edu.sutd.bank.webapp.servlet.ServletPaths.NEW_TRANSACTION;

@WebServlet(NEW_TRANSACTION)
public class NewTransactionServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private ClientTransactionDAO clientTransactionDAO = new ClientTransactionDAOImpl();
	private TransactionCodesDAO transactionCodeDAO = new TransactionCodesDAOImpl();
	private transient ClientAccountDAO clientAccountDAO = new ClientAccountDAOImpl();
	BigDecimal bi2 = new BigDecimal("10");


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			for (int i =0; i < 3; i++) {
				
				if (req.getParameter("transcode" + i) != null && req.getParameter("amount" + i) != null 
						& (req.getParameter("toAccountNum" + i) != null && !req.getParameter("toAccountNum" + i).equals(""))) {
					String transcode = req.getParameter("transcode" + i);
					BigDecimal amount = new BigDecimal(req.getParameter("amount" + i));
					String toAccountNum = req.getParameter("toAccountNum" + i);
					
					// check if transaction code is used
					TransactionCodes validateTransaction = transactionCodeDAO.getTransationCodes(transcode);
					if (validateTransaction == null || (validateTransaction.getUsed() == 1)) {
						sendError(req, "Transaction code " + (i+1) + " incorrect, please use another code. Email sutdbank.contact@gmail.com for support");
						forward(req, resp);
						return;
					}
					
					// check the remain balance from client before doing the transaction
					ClientAccount clientAccount = clientAccountDAO.checkBalance(getUserId(req));
					if (clientAccount.getAmount().compareTo(amount) < 0) {
						sendError(req, "Not enough balance for this transaction");
						forward(req, resp);
						return;
					}
				
					ClientTransaction clientTransaction = new ClientTransaction();
					User user = new User(getUserId(req));
					clientTransaction.setUser(user);
					clientTransaction.setAmount(amount);
					clientTransaction.setTransCode(transcode);
					clientTransaction.setToAccountNum(toAccountNum);

					
					if ((clientTransaction.getAmount()).compareTo(bi2)<=0)	{
						clientTransactionDAO.createAutoApproval(clientTransaction);
						transactionCodeDAO.setUsed(transcode, true);
						
					}	
					
					else if ((clientTransaction.getAmount()).compareTo(bi2)>0) {
						clientTransactionDAO.createNeedApproval(clientTransaction);
						transactionCodeDAO.setUsed(transcode, true);
							
		            } 
				}
				
				
			}
			
			redirect(resp, ServletPaths.CLIENT_DASHBOARD_PAGE);
							
		} catch (ServiceException e) {
			sendError(req, e.getMessage());
			forward(req, resp);
		}
	}
}
