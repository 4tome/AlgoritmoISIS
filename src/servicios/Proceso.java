package servicios;

public class Proceso extends Thread {
	private String id;
	//Constructor
	public Proceso( String id){
		this.id = id;
	}
	
	//M�todos para el incremento del tiempo l�gico (Lamport)
	public void LC1(){
		
	}
	
	public void LC2(){
		
	}
	//M�todos para enviar los diferentes tipos de mensajes
	
	
	//M�todos para recibir los diferentes tipos de mensajes
	
	
	//M�todos para el envio de mensajes (multicast, unicast)
	public void multicast(){
		
	}
	public void unicast(){
		
	}
	
	//M�todo run
	public void run(){
		System.out.println("Hola, soy el proceso" + this.id);
	}


}