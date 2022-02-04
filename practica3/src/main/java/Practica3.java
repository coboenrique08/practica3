import java.io.IOException;
import java.util.Random;

public class Practica3 {
	public static void main(String[] args) {
		ElectionNode nd;
		Random r = new Random();
		try {
			nd = new ElectionNode("node" + r.nextInt());
			nd.start();
			
			while (true) {
				Thread.sleep(5000);
				nd.escritura();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
