package servicios;

public class Mensaje {
	private String id;
	private int time;
	private int type;
	private String state;
	
	//Constructor
	public Mensaje(String id, int time, int type, String state) {
		this.setId(id);
		this.setTime(time);
		this.setType(type);
		this.setState(state);
	}

	//Métodos Getter y Setter
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}