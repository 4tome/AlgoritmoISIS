package servicios;

public class Mensaje {
	private String id;
	private int time;
	
	//Constructor
	public Mensaje(String id, int time) {
		this.setId(id);
		this.setTime(time);
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
	
}