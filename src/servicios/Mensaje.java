package servicios;

public class Mensaje {
	private String id;
	private int orden;
	private String state;
	private int numP;
	private String content;
	
	//Constructor
	public Mensaje(String id, int time, String state, int numProcess) {
		this.setId(id);
		this.setOrden(time);
		this.setState(state);
		this.setNumP(numProcess);
	}

	//Métodos Getter y Setter

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public int getNumP() {
		return numP;
	}

	public void setNumP(int numP) {
		this.numP = numP;
	}
	
}