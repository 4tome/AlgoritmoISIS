package servicios;

public class Mensaje implements Comparable<Mensaje> {
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

	@Override
	public int compareTo(Mensaje m) {
		if (orden < m.orden) {
			return -1;
		}
		if (orden > m.orden) {
			return 1;
		}
		return 0;
	}
	
	//Métodos de comprobación
	public String imprimir()
	{
		String mensaje = this.id + "-" + this.orden + "-" + this.state + "-" + this.numP;
		return mensaje;
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