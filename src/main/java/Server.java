import java.net.*;
import java.io.*;


public class Server extends Node implements Runnable
{
	
	 Socket socket = null;
	 ServerSocket server = null;
	 DataInputStream dataIn = null;

	 int portNum;
	 int clientIndex;


	public Server(int portNum, int clientIndex)
	{
		this.portNum = portNum;
		this.clientIndex = clientIndex;
	}


	public void LerMensagem()
	{

		try {
			if (dataIn.available() > 0) {

				String line = dataIn.readUTF();
				System.out.println("Mensagem recebida:\t" + line);

				// request tem 4 partes: REQUEST:PID:RelogioLogico:RECURSO
				// e o reply é: REPLY:PID:OK:RECURSO
				String resArray[] = line.split(":", 4);

				if (resArray[0].equals("REQUEST")) {
					ProcessaRequest(Integer.parseInt(resArray[1]), Integer.parseInt(resArray[2]), Integer.parseInt(resArray[3]));
				} else if(resArray[2].equals("OK")){
					ProcessaReply();
				}
			} else {
				Thread.sleep(2000);
			}
		}
		catch(Exception e)
		{

		}

	}
	
	

	synchronized public void ProcessaRequest(int PID, int relogioLogico, int recurso)
	{
		// verifica se é request para usar a RC.
		// se for um req adiado, não entra no if
		// se existe um processo na regiao critica e um outro processo tenta acessar outro recurso: não entra no if
		if( Node.reqParaRC && recurso == Node.regiaoCritica)
		{
			if(relogioLogico < Node.relogioLogico)
			{
				//envia reply
				Node.clientObjList.get(clientIndex).SendReply(PID, Node.PID, "OK", recurso);
			}
			else if (relogioLogico > Node.relogioLogico)
			{
				//adia request
				Node.clientObjList.get(clientIndex).SendReply(PID, Node.PID, "NOK", recurso);
				Node.listaAdiados.get(PID).relogioLogico = relogioLogico;
				Node.listaAdiados.get(PID).recurso = recurso;
				Node.relogioLogico = relogioLogico;
				System.out.println("Request do Processo " + PID + " foi adiado.");
			}
			else
			{
				if(PID < Node.PID)
				{
					//envia reply
					Node.clientObjList.get(clientIndex).SendReply(PID, Node.PID, "OK", recurso);
				}
				else
				{
					//adia request
					Node.clientObjList.get(clientIndex).SendReply(PID, Node.PID, "NOK", recurso);
					Node.listaAdiados.get(PID).relogioLogico = relogioLogico;
					Node.listaAdiados.get(PID).recurso = recurso;
					System.out.println("Request do Processo " + PID + " foi adiado.");
				}
			}
			
		}
		else
		{
			//envia reply
			Node.clientObjList.get(clientIndex).SendReply(PID, Node.PID, "OK", recurso);
			if(Node.relogioLogico < relogioLogico)
				Node.relogioLogico = relogioLogico;
		}
		
	}

	// processa as mensagens de OK
	synchronized public void ProcessaReply()
	{
		try {

			Node.numPendingRes--;

			// caso queira acessar um recurso qualquer, não entra no if
			if(Node.numPendingRes == 0 && Node.reqParaRC) {
				Node.entrouRC = true;

				Thread.sleep(10);
			}
		}
		catch(Exception e)
		{
			System.out.println("Erro: " + e);
		}
	}
	
	public void run()
	{
		try
		{
			System.out.println("Server rodando na porta: " + portNum);
			server = new ServerSocket(portNum);
			Thread.sleep(10);
			socket = server.accept();
			dataIn = new DataInputStream(socket.getInputStream());
			System.out.println("Conexao Aceita na porta: " + portNum);
		}
		catch(Exception e) {
			System.out.println("Erro: " + e);
		}
		
		while(true)
		{
			try 
			{
				LerMensagem();
				Thread.sleep(10);
			}
			catch(Exception e)
			{
				System.out.println("Erro: " + e);
				
			}
		}
	}
}
