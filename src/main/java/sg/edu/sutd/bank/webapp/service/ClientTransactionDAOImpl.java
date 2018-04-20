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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.ClientAccount;
import sg.edu.sutd.bank.webapp.model.ClientTransaction;
import sg.edu.sutd.bank.webapp.model.TransactionStatus;
import sg.edu.sutd.bank.webapp.model.User;

public class ClientTransactionDAOImpl extends AbstractDAOImpl implements ClientTransactionDAO {
	private ClientAccount clientAccount = new ClientAccount();
	private ClientAccountDAO clientAccountDAO = new ClientAccountDAOImpl();
	BigDecimal bi1 = new BigDecimal("-1");
	
	@Override
	public synchronized void createAutoApproval(ClientTransaction clientTransaction) throws ServiceException {  
		Connection conn = connectDB();
		PreparedStatement ps=null;
		try {
			ps = prepareStmt(conn, "INSERT INTO client_transaction(trans_code, amount, to_account_num, user_id, status)"
					+ " VALUES(?,?,?,?,?)");
			int idx = 1;
			ps.setString(idx++, clientTransaction.getTransCode());
			ps.setBigDecimal(idx++, clientTransaction.getAmount());
			ps.setString(idx++, clientTransaction.getToAccountNum());
			ps.setInt(idx++, clientTransaction.getUser().getId());
			ps.setString(idx++, "APPROVED");
			executeInsert(clientTransaction, ps);			
			//Sender
			User user1 = clientTransaction.getUser();
			clientAccount.setUser(user1);
			clientAccount.setId(clientTransaction.getUser().getId());
			clientAccount.setAmount((clientTransaction.getAmount()).multiply(bi1));
			clientAccountDAO.update(clientAccount);				
			//Receiver
			String clientId = clientTransaction.getToAccountNum();
			User user2 = new User(Integer.parseInt(clientId));
			clientAccount.setUser(user2);
			clientAccount.setId(clientTransaction.getUser().getId());
			clientAccount.setAmount((clientTransaction.getAmount()));
			clientAccountDAO.update(clientAccount);			
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, null);
		}
	}
	
	@Override
	public synchronized void createNeedApproval(ClientTransaction clientTransaction) throws ServiceException {
		Connection conn = connectDB();
		PreparedStatement ps=null;
		try {
			ps = prepareStmt(conn, "INSERT INTO client_transaction(trans_code, amount, to_account_num, user_id)"
					+ " VALUES(?,?,?,?)");
			int idx = 1;
			ps.setString(idx++, clientTransaction.getTransCode());
			ps.setBigDecimal(idx++, clientTransaction.getAmount());
			ps.setString(idx++, clientTransaction.getToAccountNum());
			ps.setInt(idx++, clientTransaction.getUser().getId());
			executeInsert(clientTransaction, ps);	
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, null);
		}
	}
	
	@Override
	public synchronized void Transfer(int transferId) throws ServiceException{
		Connection conn = connectDB();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(
					"SELECT * FROM client_transaction WHERE id = ?");
			int idx = 1;
			ps.setInt(idx++, transferId);
			rs = ps.executeQuery();
			while (rs.next()) {
				BigDecimal transferAmt = rs.getBigDecimal("amount");				
				//sender
				int senderID = rs.getInt("user_id");				
				User sender = new User(senderID);
				clientAccount.setUser(sender);
				clientAccount.setId(senderID);
				clientAccount.setAmount((transferAmt).multiply(bi1));
				clientAccountDAO.update(clientAccount);
				
				//receiver
				int receiverID = rs.getInt("to_account_num");
				User receiver= new User(receiverID);
				clientAccount.setUser(receiver);
				clientAccount.setId(receiverID);
				clientAccount.setAmount(transferAmt);
				clientAccountDAO.update(clientAccount);
				
			}
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, rs);
		}
	}

				
	@Override
	public List<ClientTransaction> load(User user) throws ServiceException {
		Connection conn = connectDB();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(
					"SELECT * FROM client_transaction WHERE user_id = ?");
			int idx = 1;
			ps.setInt(idx++, user.getId());
			rs = ps.executeQuery();
			List<ClientTransaction> transactions = new ArrayList<ClientTransaction>();
			while (rs.next()) {
				ClientTransaction trans = new ClientTransaction();
				trans.setId(rs.getInt("id"));
				trans.setUser(user);
				trans.setAmount(rs.getBigDecimal("amount"));
				trans.setDateTime(rs.getDate("datetime"));
				trans.setStatus(TransactionStatus.of(rs.getString("status")));
				trans.setTransCode(rs.getString("trans_code"));
				trans.setToAccountNum(rs.getString("to_account_num"));
				transactions.add(trans);
			}
			return transactions;
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, rs);
		}
	}

	@Override
	public List<ClientTransaction> loadWaitingList() throws ServiceException {
		Connection conn = connectDB();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(
					"SELECT * FROM client_transaction WHERE status is null");
			rs = ps.executeQuery();
			List<ClientTransaction> transactions = new ArrayList<ClientTransaction>();
			while (rs.next()) {
				ClientTransaction trans = new ClientTransaction();
				trans.setId(rs.getInt("id"));
				User user = new User(rs.getInt("user_id"));
				trans.setUser(user);
				trans.setAmount(rs.getBigDecimal("amount"));
				trans.setDateTime(rs.getDate("datetime"));
				trans.setTransCode(rs.getString("trans_code"));
				trans.setToAccountNum(rs.getString("to_account_num"));
				transactions.add(trans);
			}
			return transactions;
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, rs);
		}
	}

	@Override
	public synchronized void updateDecision(List<ClientTransaction> transactions) throws ServiceException {
		StringBuilder query = new StringBuilder("UPDATE client_transaction SET status = Case id ");
		for (ClientTransaction trans : transactions) {
			query.append(String.format("WHEN %d THEN '%s' ", trans.getId(), trans.getStatus().name()));
		}
		query.append("ELSE status ")
			.append("END ")
			.append("WHERE id IN(");
		for (int i = 0; i < transactions.size(); i++) {
			query.append(transactions.get(i).getId());
			if (i < transactions.size() - 1) {
				query.append(", ");
			}
		}
		query.append(");");
		Connection conn = connectDB();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = prepareStmt(conn, query.toString());
			int rowNum = ps.executeUpdate();
			if (rowNum == 0) {
				throw new SQLException("Update failed, no rows affected!");
			}
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, rs);
		}
	}

}