import java.util.*;
import java.sql.Timestamp;


public class Node {

	static int PID;
	static boolean entrouRC;
	static int maxNodes = 3;
	static int relogioLogico;
	static public int numPendingRes;
	static int regiaoCritica;
	static boolean reqParaRC;


	int[] ports = { 3425, 3435, 3445 };

	static List<REQ> listaAdiados = new ArrayList<REQ>();

	static List<Client> clientObjList = new ArrayList<Client>();
	static List<Server> serverObjList = new ArrayList<Server>();
	static List<Thread> serverThreadList = new ArrayList<Thread>();
	
	Timestamp ts;
	
	public Node(int id) {
		Node.PID = id;
		Node.entrouRC = false;
		Node.reqParaRC = false;
		Node.relogioLogico = 0;
		Node.numPendingRes = 0;
		Node.regiaoCritica = 1; // recurso critico
	}

	public Node()
	{

	}
	
	public void preRegiaoCritica(int rec)
	{

		if(rec != regiaoCritica) {
			Node.reqParaRC = false;
		}
		else
			Node.reqParaRC = true;

		Node.relogioLogico++;
		Node.numPendingRes = Node.maxNodes - 1;

		for(int i = 0; i < Node.maxNodes; i++)
		{
			if(i != Node.PID)
				Node.clientObjList.get(i).SendRequest(relogioLogico, Node.PID, rec);
		}

	}
	
	public void ProcessaRequestsAdiados() {
		for(int i = 0; i < listaAdiados.size();i++)
		{
			if( listaAdiados.get(i).relogioLogico != -1)
			{
				Node.serverObjList.get(i).ProcessaRequest(i, listaAdiados.get(i).relogioLogico, listaAdiados.get(i).recurso);
				listaAdiados.get(i).relogioLogico = -1;
				listaAdiados.get(i).recurso = -1;
			}
		}
	}
	
	

	public void MainFunction()
	{
		// inicializa lista
		listaAdiados.add(0, new REQ(-1, -1));
		listaAdiados.add(1, new REQ(-1, -1));
		listaAdiados.add(2, new REQ(-1, -1));

		try
		{

			System.out.println("Processo " + Node.PID);

			// for para criar servidores
			for(int i = 0; i < Node.maxNodes; i++)
			{

				if(i == Node.PID)
				{
					Node.serverObjList.add( i, new Server(0, i));
					Thread t1 = new Thread(Node.serverObjList.get(i));
					Node.serverThreadList.add(i, t1);
				}
				else
				{
					Node.serverObjList.add( i, new Server(ports[i] + Node.PID, i));
					Thread t1 = new Thread(Node.serverObjList.get(i));
					Node.serverThreadList.add( i, t1);
					t1.start();
				}

			}
		
			Thread.sleep(2000);
		
			// for para criar clientes
			for(int i = 0; i < Node.maxNodes; i++)
			{
				if(i == Node.PID)
				{
					for(int j = 0; j < Node.maxNodes; j++)
					{
						if(j == Node.PID)
						{
							clientObjList.add( j, new Client(ports[i] + j));
						}
						else
						{
							clientObjList.add( j, new Client(ports[i] + j));
							clientObjList.get(j).connect(ports[i] + j);
						}

					}

				}

			}

			int rec = 0;
			Scanner scanner;

			while(rec != -1)
			{
				Thread.sleep(2000);

				scanner = new Scanner(System.in);
				rec = Integer.parseInt(scanner.nextLine());

				preRegiaoCritica(rec);

				if(Node.reqParaRC) {

					while (!Node.entrouRC) {
						Thread.sleep(10);
					}

					//entrou na Região Critica
					ts = new Timestamp(System.currentTimeMillis());

					System.out.println("Processo " + PID + " entrou na Regiao Critica as " + ts);
					System.out.println("\tUtilizando recurso...");
					Thread.sleep(15000);

					//reseta as variaveis
					Node.entrouRC = false;
					Node.reqParaRC = false;
					ts = new Timestamp(System.currentTimeMillis());
					System.out.println("Processo " + PID + " saiu da Regiao Critica as " + ts);

					ProcessaRequestsAdiados();

				}
			}

			Thread.sleep(2000);

			for(int i = 0; i < Node.maxNodes; i++)
			{
				if(i != Node.PID)
				{
					serverObjList.get(i).server.close();
				}
			}

			System.out.println("Teste Finalizado");
			System.in.read();
			System.exit(0);
		    
		}

		catch(Exception e)
		{
			System.out.println("Erro: " + e);
		}
		
	}

}
