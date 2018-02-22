package rest.models;

public class BasicResponse {

	private String message;
	
	public BasicResponse() {}
	public BasicResponse(String message) {
		setMessage(message);
	}
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
