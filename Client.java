import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient() {
        try {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Lit la console
                String userInput = scanner.nextLine();

                // Envoie la commande au serveur
                outputStream.writeObject(userInput);

                if ("/info".equals(userInput)) {
                    // Recevoir et afficher les informations du serveur
                    String serverResponse;
                    try {
                        serverResponse = (String) inputStream.readObject();
                        System.out.println("Server : " + serverResponse);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("Erreur d'InputStream !");
                    }
                }

                if ("/exit".equals(userInput)) {
                    System.out.println("Fermeture du client.");
                    break;
                }
                
            }  
            scanner.close();
            outputStream.close();
            inputStream.close();
            socket.close(); 
        } 
            catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 55555);
        client.startClient();
    }
}
