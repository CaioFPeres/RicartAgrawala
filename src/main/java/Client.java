import java.net.*; 
import java.io.*;

public class Client extends Node {
	
	Socket clientSocket;
	DataOutputStream clientDataOut;
	
	int portNum;
	
	public Client(int port)
	{
		this.portNum = port;
	}

	public void connect(int port)
	{
		try
        { 
        	clientSocket = new Socket("localhost", port);
			clientDataOut = new DataOutputStream(clientSocket.getOutputStream());
        } 
       
        catch(Exception e) {
			System.out.println("Erro: " + e);
		}
	}
	
	
	
	
	public void SendRequest(int relogioLogico, int PID, int recurso)
	{ 
	
		try
		{
  			clientDataOut.writeUTF("REQUEST:" + PID + ":" + relogioLogico + ":" + recurso);
		}
		catch(Exception e)
		{
			System.out.println("Erro: " + e);
		}
		
	}
	
	public void SendReply(int to_PID, int from_PID, String msg, int rec)
	{
		
		try
		{
			System.out.println("Mandando reply pro Processo " + to_PID);
			clientDataOut.writeUTF("REPLY:" + from_PID + ":" + msg + ":" + rec);
		}
		catch(Exception e)
		{
			System.out.println("Erro: " + e);
		}
	}

}
