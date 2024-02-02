import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * La classe Client représente un client dans une application de messagerie.
 * Elle gère la connexion au serveur, l'envoi et la réception de messages.
 */
public class Client {
    private Socket socket; // Le socket pour la connexion au serveur
    private ObjectOutputStream outputStream; // Le flux de sortie pour envoyer des objets au serveur
    private ObjectInputStream inputStream; // Le flux d'entrée pour recevoir des objets du serveur
    private String nom; // Le nom du client

    /**
     * Constructeur de la classe Client.
     * Il initialise la connexion au serveur et les flux d'entrée/sortie.
     *
     * @param serverAddress L'adresse du serveur à laquelle se connecter
     * @param serverPort Le port du serveur sur lequel se connecter
     * @param nom Le nom du client
     */
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

    /**
     * Cette méthode démarre le client.
     * Elle lit les entrées de l'utilisateur et les envoie au serveur jusqu'à ce que l'utilisateur tape "/exit".
     */
    public void startClient() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String userInput = scanner.nextLine();
                if ("/exit".equals(userInput)) {
                    outputStream.writeObject(userInput);
                    System.out.println("Au revoir.");
                    break;
                }
                outputStream.writeObject(userInput);
                String serverResponse;
                try {
                    serverResponse = (String) inputStream.readObject();
                    System.out.println("Server : " + serverResponse);
                    if (serverResponse.equals("Au revoir\n")) {
                        break;
                    }
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

    /**
     * Le point d'entrée de l'application.
     * Il crée un nouveau client et démarre le client.
     *
     * @param args Les arguments de la ligne de commande
     */
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