import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String nom;

    public Client(String serverAddress, int serverPort, String nom) {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            this.nom = nom;
            outputStream.writeObject(this.nom);
            String serverResponse = (String) inputStream.readObject();
            System.out.println("Server : " + serverResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startClient() {

        try {

            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Lit la console
                String userInput = scanner.nextLine();

                if ("/exit".equals(userInput)) {
                    outputStream.writeObject(userInput);
                    System.out.println("Au revoir.");
                    break;
                }
                // Envoie la commande au serveur
                outputStream.writeObject(userInput);
                String serverResponse;
                try {
                    serverResponse = (String) inputStream.readObject();
                    System.out.println("Server : " + serverResponse);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("Erreur d'InputStream !");
                }

            }
            scanner.close();
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(
                "Bienvenue sur notre application de messagerie, veuillez renseigner le port du serveur ainsi que votre nom \n");
        Scanner scanner = new Scanner(System.in);

        String userInput = scanner.nextLine();
        String[] a = userInput.split(" ");

        Client client = new Client("localhost", Integer.parseInt(a[0]), a[1]);
        client.startClient();

        scanner.close();
    }
}
