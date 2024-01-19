import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class GestionServeur extends Thread {
    private Socket clientSocket;

    public GestionServeur(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                // Lire la commande du client
                String clientCommand;
                try {
                    clientCommand = (String) inputStream.readObject();
                    // Traitement de la commande spécifique
                    if ("/info".equals(clientCommand)) {
                        // Renvoyer des informations au client (dans cet exemple, l'adresse IP)
                        String clientAddress = clientSocket.getInetAddress().getHostAddress();
                        outputStream.writeObject("Votre adresse IP est : " + clientAddress + "\n");
                    }

                    if ("/exit".equals(clientCommand)) {
                        System.out.println("Client deconnecté ! Info -> : " + clientSocket.getInetAddress().getHostAddress());
                        break;
                        
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } 
            outputStream.close();
            inputStream.close(); 
            clientSocket.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
