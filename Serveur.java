import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
    private ServerSocket serverSocket;

    public Serveur(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        System.out.println("Le serveur est en ligne sur le port : " + serverSocket.getLocalPort() + " !");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connection établie ! Adresse : " + clientSocket.getInetAddress().getHostAddress());

                // Handle client communication in a separate thread
                new GestionServeur(clientSocket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Serveur server = new Serveur(55555);
        server.startServer();
    }
}
