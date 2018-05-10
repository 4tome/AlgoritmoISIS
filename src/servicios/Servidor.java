package servicios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

@Singleton 
@Path("Servidor")
public class Servidor {
	//Procesos que controla el Dispatcher
	private Proceso p1;
	private Proceso p2;
	private int numProcesos = 2;
	//Array de los procesos del sistema
	private String[][] arrayProcesos = {{"P01","localhost"},{"P02","localhost"}};
	
	public Servidor(){
		//Creamos los procesos
		p1 = new Proceso("01", 0, numProcesos, arrayProcesos);
		p2 = new Proceso("02", 0, numProcesos, arrayProcesos); 
		//Arrancamos los procesos
		p1.start();
		p2.start();
	}
	
	
	@Path("start")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String start()
	{
		//Servicio para arrancar los procesos.
		return "Procesos creados satisfactoriamente";
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("enviarMensaje") //ruta al m�todo
	public String enviarMensaje(@QueryParam(value="mensaje")String msg,
								@QueryParam(value="destino")String destino) //el m�todo debe retornar String
	{ 
		if (destino.equals("P01")) {
			p1.recibirPropuesta(msg);
			return "PROPUESTA " + msg + " ENVIADO";
		}else {
			p2.recibirPropuesta(msg);
			return "PROPUESTA " + msg + " ENVIADO";
		}
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("multicast") //ruta al m�todo
	public String multicast(@QueryParam(value="mensaje")String msg,
								@QueryParam(value="destino")String destino) //el m�todo debe retornar String
	{ 
		if (destino.equals("P01")) {
			p1.recibirMensaje(msg);
			return "MENSAJE " + msg + " ENVIADO A " + destino;
		}else {
			p2.recibirMensaje(msg);
			return "MENSAJE " + msg + " ENVIADO A " + destino;
		}
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("multicastDefinitivo") //ruta al m�todo
	public String multicastDefinitivo(@QueryParam(value="mensaje")String msg,
								@QueryParam(value="destino")String destino) //el m�todo debe retornar String
	{ 
		if (destino.equals("P01")) {
			p1.recibirAcuerdo(msg);
			return "DEFINITIVO " + msg + " ENVIADO A " + destino;
		}else {
			p2.recibirAcuerdo(msg);
			return "DEFINITIVO " + msg + " ENVIADO A " + destino;
		}
	}

	//Servicio para recoger los logs de las máquinas
	@Path("recogerLog")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String recogerLog(@QueryParam(value="proceso")String idProc)
	{
		String ruta = System.getProperty("user.home");
		BufferedReader br = null;
		FileReader fr = null;
		String contenido = "";
		try {
			fr = new FileReader(ruta + "/proceso" + idProc + ".log");
			br = new BufferedReader(fr);
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contenido = contenido  + sCurrentLine + "\n";
				System.out.println(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		//volver a guardar el contenido //pasar tambien por parametro la IP
		return contenido;
	}
	
	@Path("verificarLogs")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String verificarLogs()
	{
		FileWriter f = null;
		PrintWriter pw = null;
		
		for(int i=0; i<arrayProcesos.length; i++){
			File fichero = new File("/home/amateos/Documentos/logs" + "/proceso" + arrayProcesos[i][0].substring(1, 3) + ".log");
			if(fichero.exists()) {
				fichero.delete();
			}else {
				try {
					fichero.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Client client = ClientBuilder.newClient();
			URI uri = UriBuilder.fromUri( "http://"+ arrayProcesos[i][1] +":8080/AlgoritmoISIS/rest/Servidor/recogerLog/").build();
			WebTarget target = client.target( uri);
			String contenido = target.queryParam("proceso", arrayProcesos[i][0].substring(1, 3)).request(MediaType.TEXT_PLAIN).get( String.class);		
			try {
				f = new FileWriter("/home/amateos/Documentos/logs" + "/proceso" + arrayProcesos[i][0].substring(1, 3) + ".log", true);
				pw = new PrintWriter(f);
				pw.println(contenido);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					if (null != f) {
						f.close();
						pw.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}		
		return "Logs recibidos";
	}
	
}
