package servicios;

public class Proceso extends Thread {
	private String id;
	//Constructor
	public Proceso( String id){
		this.id = id;
	}
	
	//Métodos para el incremento del tiempo lógico (Lamport)
	public void LC1(){
		
	}
	
	public void LC2(){
		
	}
	//Métodos para enviar los diferentes tipos de mensajes
	
	
	//Métodos para recibir los diferentes tipos de mensajes
	
	
	//Métodos para el envio de mensajes (multicast, unicast)
	public void multicast(){
		
	}
	public void unicast(){
		
	}
	
	//Método run
	public void run(){
		System.out.println("Hola, soy el proceso" + this.id);
	}


}