package sg.edu.sutd.bank.webapp.model;

public class TransactionCodes extends AbstractIdEntity {
	private String code;
	private Integer userId;
	private int used;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public int getUsed() {
		return used;
	}
	public void setUsed(int used) {
		this.used = used;
	}
}
