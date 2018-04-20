<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page session="true" %>
<%-- added for dynamic drop down box--%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%ResultSet resultset =null;%>
<%ResultSet resultset2 =null;%>
<%ResultSet Accuser =null;%>

<!DOCTYPE html>
<html lang="en">
  <%@include file="pageHeader.jsp"%>
  <body>
<%
    try{
Class.forName("com.mysql.jdbc.Driver").newInstance();
Connection connection = 
         DriverManager.getConnection
            ("jdbc:mysql://localhost:3306/bankwebapp","root","rootroot");


       Statement statement = connection.createStatement() ;
       resultset =statement.executeQuery("SELECT * FROM user where user_name in (select user_name from user_role where role ='client') order by id desc")  ;
       HashMap<Integer, String> users = new HashMap<Integer, String>();
       while (resultset.next()) {
    	   users.put(resultset.getInt("id"), resultset.getString("user_name"));   
       }
       
%> 
	<%@include file="header.jsp"%>
	
	<main id="content" class="mainContent bank-template" role="main">
	<div class="container">
		<%@include file="errorMessage.jsp"%>
		<div id="createTransaction">
			<form id="newTransactionForm" action="newTransaction" method="post">
			
			<% for(int i = 0; i < 3; i+=1) { %>
				<%-- 3 transactions --%>
				<div id="wrapper" style="width:33%; display:inline-block; border: solid #111111 1px; padding: 0px 10px 0 10px">
					<label style="border-bottom-style: solid;width:100%;"><%=(i+1) %></label>
					<div id="input-group-transcode" style="width:80%">
						<label for="transcode" class="control-label">Transaction code</label>
						<input type="text" class="form-control" id="<%= ("transcode" + i) %>" name="<%= ("transcode" + i) %>" placeholder="Transaction Code(Emailed to you)">
					</div>
					<div id="input-group-toAccount" style="width:80%">
						<label for="toAccountNum" class="control-label">To (account number)</label>
					        <select id="<%= ("toAccountNum" + i) %>" name="<%= ("toAccountNum" + i) %>" style="width:100px;height:34px;">
					        <option value="" select="selected"> --Select--</option>
					        <%  for(Map.Entry<Integer, String> entry : users.entrySet()) { %>
					           <option><%= entry.getKey() %></option>
					        <% } %>
					        </select>
					</div>
					<div id="input-group-toAccount" style="width:80%; display: none">
						<label for="toAccountNumX" class="control-label">To (account number)</label>
					        <select id="<%= ("toAccountNumX" + i) %>" name="<%= ("toAccountNumX" + i) %>" style="width:100px;height:34px;">
					        <option value="" select="selected"> --Select--</option>
					        <%  for(Map.Entry<Integer, String> entry : users.entrySet()) { %>
					           <option value="<%= entry.getKey() %>"><%= entry.getValue() %></option>
					        <% } %>
					        </select>
					</div>
					<div id="input-group-toAccount" style="width:80%">
						<label for="userAcc" class="control-label">Account User</label>
						<input type="text" class="form-control" id="<%=("userAcc" + i) %>" name="<%=("userAcc" + i) %>" placeholder="user" disabled="disabled">
					</div>
					<div id="input-group-amount" style="width:80%">
						<label for="amount" class="control-label">Amount</label>
						<input type="number" step="0.01" min="0" class="form-control" id="<%=("amount" + i) %>" name="<%=("amount" + i) %>" placeholder="amount">
						<br/>
					</div>
				</div>
				
			<%-- END transactions --%>
			<% } %>
				
								

				<button id="createTransBtn" type="submit" class="btn btn-default" style="margin-top: 15px">Submit</button>
			</form>
		</div>
	</div>
	</main>
<%
        }
        catch(Exception e)
        {
             out.println("wrong entry"+e);
        }
%>	
<script>
$(document).ready(function() {
	console.log("ok")

	for (i=0;i <3; i++) {

			$("#toAccountNum" + i).change((function(a) {
					return function() {
						var selected = $("#toAccountNum" + a).find(":selected").val();
							$("#userAcc" + a).val($("#toAccountNumX" + a + " option[value=\"" + selected + "\"]").text())
							
						}
					})(i))
		
		
	}
})
</script>
  </body>
</html>
