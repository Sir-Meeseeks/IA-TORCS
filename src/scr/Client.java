
package scr;

import java.util.StringTokenizer;
import java.io.IOException;
import scr.Controller.Stage;

/**
 * @author Daniele Loiacono
 * 
 */
public class Client {

	private static int UDP_TIMEOUT = 10000;
	private static int port;
	private static String host;
	private static String clientId;
	private static boolean verbose;
	private static int maxEpisodes;
	private static int maxSteps;
	private static Stage stage;
	private static String trackName;

	/**
	 * @param args
	 *            is used to define all the options of the client.
	 *            <port:N> is used to specify the port for the connection (default is 3001)
	 *            <host:ADDRESS> is used to specify the address of the host where the server is running (default is localhost)  
	 *            <id:ClientID> is used to specify the ID of the client sent to the server (default is championship2009) 
	 *            <verbose:on> is used to set verbose mode on (default is off)
	 *            <maxEpisodes:N> is used to set the number of episodes (default is 1)
	 *            <maxSteps:N> is used to set the max number of steps for each episode (0 is default value, that means unlimited number of steps)
	 *            <stage:N> is used to set the current stage: 0 is WARMUP, 1 is QUALIFYING, 2 is RACE, others value means UNKNOWN (default is UNKNOWN)
	 *            <trackName:name> is used to set the name of current track
	 */
	public static void main(String[] args) {
		parseParameters(args);
		SocketHandler mySocket = new SocketHandler(host, port, verbose);
		String inMsg;

		Controller driver = load(args[0]);
		driver.setStage(stage);
		driver.setTrackName(trackName);
                
                // Creem el fitxer
		try {driver.creaFitxer();}
                catch (IOException hapetao) {}
                
                
		/* Build init string */
		float[] angles = driver.initAngles();
		String initStr = clientId + "(init";
		for (int i = 0; i < angles.length; i++) {
			initStr = initStr + " " + angles[i];
		}
		initStr = initStr + ")";
		
		long curEpisode = 0;
		boolean shutdownOccurred = false;
		do {

			/*
			 * Client identification
			 */

			do {
				mySocket.send(initStr);
				inMsg = mySocket.receive(UDP_TIMEOUT);
			} while (inMsg == null || inMsg.indexOf("***identified***") < 0);

			/*
			 * Start to drive
			 */
			long currStep = 0;
			while (true) {
				/*
				 * Receives from TORCS the game state
				 */
				inMsg = mySocket.receive(UDP_TIMEOUT);

				if (inMsg != null) {

					/*
					 * Check if race is ended (shutdown)
					 */
					if (inMsg.indexOf("***shutdown***") >= 0) {
						shutdownOccurred = true;
						System.out.println("Server shutdown!");
						break;
					}

					/*
					 * Check if race is restarted
					 */
					if (inMsg.indexOf("***restart***") >= 0) {
						driver.reset();
						if (verbose)
							System.out.println("Server restarting!");
						break;
					}

					Action action = new Action();
                                        
					if (currStep < maxSteps || maxSteps == 0)
						action = driver.control(new MessageBasedSensorModel(
								inMsg));
					else
						action.restartRace = true;

					currStep++;
					mySocket.send(action.toString());
				} else
					System.out.println("Server did not respond within the timeout");
			}

		}
                while (++curEpisode < maxEpisodes && !shutdownOccurred);
                
		int contadorDeu = 0;
                double[] dada = new double[2]; dada[0] = dada[1] = 0;
                
                // -------------------------------------
                // Suavitzat d'error amb agrupacions de 10 valors
                // -------------------------------------
                for (int j = 0;j < driver.vectorDades.size(); j++) {
                    dada[1] += driver.vectorDades.get(j)[1];
                    contadorDeu++;
                    if (contadorDeu == 10 && j <= driver.vectorDades.size()) {
                        dada[0] = driver.vectorDades.get(j)[0];
                        dada[1] = dada[1] / 10;
                        if(dada[1] < 0.02 && dada[1] > - 0.02)
                            dada[1]=0;
                        driver.vectorSuavitzat.add(dada);
                        dada = new double[2];
                        dada[1] = 0;
                        contadorDeu = 0;
                    }
                }
                for (int i = 0; i < driver.vectorSuavitzat.size(); i++) {
                    //System.out.print(driver.vectorSuavitzat.get(i)[0]+" "+driver.vectorSuavitzat.get(i)[1]+"\n");
                }
                dada[0] = dada[1] = 0;
                
                // --------------------------------------------
                // Mapeig del cami condensat de 30 en 30 valors
                // --------------------------------------------
                for (int k = 0; k < driver.vectorSuavitzat.size() - 30; k++) {
                    dada[0] = driver.vectorSuavitzat.get(k)[0];
                    for (int l = 0; l < 30; l++) {
                            dada[1] += driver.vectorSuavitzat.get(k+l)[1];
                    }
                    driver.vectorMapejat.add(dada);
                    dada = new double[2];
                }
                //System.out.println("MAPEIG: ");
                for (int i = 0; i < driver.vectorMapejat.size(); i++) {
                    //System.out.print(driver.vectorMapejat.get(i)[0]+" "+driver.vectorMapejat.get(i)[1]+"\n");
                }
                
                /*
		 * Shutdown the controller
		 */
		driver.shutdown();
		mySocket.close();
		System.out.println("Client shutdown.");
		System.out.println("Bye, bye!");

	}

	private static void parseParameters(String[] args) {
		/*
		 * Set default values for the options
		 */
		port = 3001;
		host = "localhost";
		clientId = "SCR";
		verbose = false;
		maxEpisodes = 1;
		maxSteps = 0;
		stage = Stage.UNKNOWN;
		trackName = "unknown";
		
		for (int i = 1; i < args.length; i++) {
			StringTokenizer st = new StringTokenizer(args[i], ":");
			String entity = st.nextToken();
			String value = st.nextToken();
			if (entity.equals("port")) {
				port = Integer.parseInt(value);
			}
			if (entity.equals("host")) {
				host = value;
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("verbose")) {
				if (value.equals("on"))
					verbose = true;
				else if (value.equals(false))
					verbose = false;
				else {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("stage")) {
				stage = Stage.fromInt(Integer.parseInt(value));
			}
			if (entity.equals("trackName")) {
				trackName = value;
			}
			if (entity.equals("maxEpisodes")) {
				maxEpisodes = Integer.parseInt(value);
				if (maxEpisodes <= 0) {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("maxSteps")) {
				maxSteps = Integer.parseInt(value);
				if (maxSteps < 0) {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
		}
	}

	private static Controller load(String name) {
		Controller controller=null;
		try {
			controller = (Controller) (Object) Class.forName(name)
					.newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println(name	+ " is not a class name");
			System.exit(0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controller;
	}
}
