package servicios;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Proceso extends Thread {
	private String id;
	private int reloj;
	private int contador = 0;
	private List<Mensaje> cola = new ArrayList<Mensaje>();
	
	//Constructor
	public Proceso(String id, int time){
		this.id = id;
		this.reloj = time;
	}
	
	//M�todos para el incremento del tiempo l�gico (Lamport)
	public void LC1(){
		
	}
	
	public void LC2(){
		
	}
	//M�todos para enviar los diferentes tipos de mensajes
	public String newMsg()
	{
		//Creamos el Mensaje
		String newId = "P" + id + " " + this.contador;
		Mensaje mensaje = new Mensaje(newId, this.reloj);
		//Creamos el String que se enviará.
		String msg = mensaje.getId() + ";" + mensaje.getTime();
		return msg;
	}
	
	//M�todos para recibir los diferentes tipos de mensajes
	public void recibirMsg(String msg)
	{
		//Comprobación
		//System.out.println("He recibido el mensaje: " + msg);
		String[] parts = msg.split(";");
		Mensaje mensaje = new Mensaje(parts[0], Integer.parseInt(parts[1]));
		cola.add(mensaje);
		//Comprobación
		System.out.println("Mensajes: " + cola.size());		
	}
	
	//M�todos para el envio de mensajes (multicast, unicast)
	public void multicast(){
		//Creamos el mensaje
		String msg = newMsg();
		//Lanzamos el servicio del dispatcher
		Client proceso = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/AlgoritmoISIS").build();
		WebTarget target = proceso.target(uri);
		//Llamar al servicio
		System.out.println(target.path("rest/Servidor/enviarMensaje").queryParam("id", msg).request(MediaType.TEXT_PLAIN).get(String.class));
	}
	
	public void unicast(){
		
	}
	
	//M�todo run
	public void run(){
		//prueba de funcionamiento
		//System.out.println("Hola, soy el proceso" + " " + this.id);
		//Mandar 1 mensaje normal
		this.multicast();
	}


}