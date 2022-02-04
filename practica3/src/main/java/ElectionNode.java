
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;


public class ElectionNode  extends LeaderSelectorListenerAdapter implements Closeable {
	String name;
	CuratorFramework client;
	LeaderSelector leaderSelector;
	private static final String PATH = "/practica3";
	static final String localURL = "http://127.0.0.1";	
	private Random rand;

	
	public ElectionNode(String name) {
		this.name=name;
		
		rand = new Random();
		
		String zkConnString = "127.0.0.1:2181";
		client = CuratorFrameworkFactory.newClient(zkConnString,
				new ExponentialBackoffRetry(1000, 3));
		client.start();

		leaderSelector = new LeaderSelector(client, PATH, this);
		leaderSelector.autoRequeue();		
	}

	public void start() throws IOException {
		leaderSelector.start();
	}

	public void close() throws IOException {
		leaderSelector.close();
	}

	public void takeLeadership(CuratorFramework client) throws Exception {
		System.out.println("I'm leading: " + name);
		
        while (true) {
		
        	try {
        		Thread.sleep(5000);
        		lectura();
        		
        	} catch (InterruptedException e) {}
        }
	}
	

	public boolean getImLeading() throws Exception {
		return leaderSelector.hasLeadership();
	}
	
	public void escritura() {
		String valor = String.valueOf(rand.nextInt(200));
		try {
			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
					.forPath("/practica3/escritura/" + name, valor.getBytes());
			System.out.println("Nueva medicion " + valor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void lectura() {
		try {
			byte[] datos;
			int media = 0;
			int cont = 0;
			
			if (client.checkExists().forPath(PATH + "/escritura") != null) {
				
				GetChildrenBuilder childrenBuilder = client.getChildren();
				List<String> children = childrenBuilder.forPath(PATH + "/escritura");
				
				if (children != null) {
					for (String child : children) {

						datos = client.getData().forPath(PATH + "/escritura/" + child);
						media += Integer.parseInt((new String(datos)));
						cont++;
					}
				}
				media = media/cont;
				System.out.println("Leyendo medidas almacenadas. Media obtenia: " + (int) media);
				enviarMedida((int) media);
			} else {
				System.out.println("No existen datos");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void enviarMedida(int valor) {
		try {
			URL url = new URL(localURL + "/nuevo/" + valor);
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			
			urlConnection.setRequestMethod("GET");
			
			urlConnection.connect();
			
			int code = urlConnection.getResponseCode();
			
			if(code == 200) {
				System.out.println("Valor enviado con exito");
			} else {
				System.out.println("No se puedo escribir correctamente. Codigo de error: " + code);
			}

			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
