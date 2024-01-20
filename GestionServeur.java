import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;





class GestionServeur extends Thread {
    private Socket clientSocket;
    private Serveur serv;

    public GestionServeur(Socket socket, Serveur serv) {
        this.clientSocket = socket;
        this.serv = serv;
    }
        

    @Override
    public void run() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                // Lire la commande du client
                String clientCommand;
                String[] args;
                try {
                    clientCommand = (String) inputStream.readObject();
                    
                    if (!clientCommand.startsWith("/message")){
                        args = clientCommand.split(" ");
                    }
                    else {
                        args = clientCommand.split(" ", 2);
                    }
                    // Traitement de la commande spécifique
                    if ("/info".equals(args[0])) {
                        String clientAddress = clientSocket.getInetAddress().getHostAddress();
                        System.out.println(this.serv.loadJSON());
                        outputStream.writeObject("Votre adresse IP est : " + clientAddress + "\n");
                    }

                    if ("/like".equals(args[0])) {
                        System.out.println("Like le message à l'ID : " + args[1]);
                        this.serv.like(args[1]);
                        outputStream.writeObject("Message " + args[1] + " +1 Like \n");
                    }

                    if ("/delete".equals(args[0])) {
                        System.out.println("Supprime le message à l'ID : " + args[1]);
                        this.serv.delete(args[1],"User");
                        outputStream.writeObject("Message de " + "User" + " à l'ID "+args[1]+" supprimer \n");
                    }

                    if ("/exit".equals(args[0])) {
                        System.out.println("Client deconnecté ! Info -> : " + clientSocket.getInetAddress().getHostAddress());
                        break;
                    }

                    if ("/message".equals(args[0])) {
                        System.out.println("Message : " + args[1]);
                        this.serv.addMessage("User",args[1]);
                        outputStream.writeObject("Message Envoyé -> " + args[1] + "\n");
                        
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
