import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;





class GestionServeur extends Thread {
    private Serveur serveur;
    private Socket clientSocket;
    private Integer idConnection = 0;
    private String nom;

    public GestionServeur(Socket socket, Serveur s) {
        this.clientSocket = socket;
        this.serveur = s;
    }
        

    @Override
    public void run() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {

                // Lire la commande du client
                String clientCommand;
                
                if (idConnection == 0) {
                    try {

                        clientCommand = (String) inputStream.readObject();
                        this.nom = clientCommand;
                        if (this.serveur.getUsers().containsKey(this.nom)) {
                            outputStream.writeObject("Bienvenue " + this.nom + "\n");
                            idConnection = 2;
                        } else {
                            outputStream
                                    .writeObject("Vous devez créer un compte. Créer votre compte? O/N \n");
                            idConnection = 1;
                        }
                    } catch (Exception e) {
                    }
                    ;
                } else if (idConnection == 1) {
                    try {
                      
                        String reponse = (String) inputStream.readObject();
                        if ("O".equals(reponse)) {
                            // Renvoyer des informations au client (dans cet exemple, l'adresse IP)
                            this.serveur.addUser(this.nom);
                            outputStream.writeObject(
                                    "Votre compte a été créé avec succes, bienvenue: " + this.nom + "\n");
                            idConnection = 2;
                        } else {
                            System.out.println(
                                    "Au revoir ! Info -> : " + clientSocket.getInetAddress().getHostAddress());
                            break;
                        }

                    } catch (Exception e) {
                    }
                } else {
                  
                  
                  
                
                    try {
                      clientCommand = (String) inputStream.readObject();
                      
                    if (clientCommand.startsWith("/like")) {
                        msg = clientCommand.split(" ")[1]
        
                        System.out.println("Like le message à l'ID : " + msg);
                        this.serv.like(msg);
                        outputStream.writeObject("Message " + msg + " +1 Like \n");
                    }

                    else if (clientCommand.startsWith("/delete")) {
                        msg = clientCommand.split(" ")[1]
                        System.out.println("Supprime le message à l'ID : " + msg);
                        this.serv.delete(msg,"User");
                        outputStream.writeObject("Message de " + "User" + " à l'ID "+msg+" supprimer \n");
                    }

                    else if ("/exit".equals(clientCommand)) {
                        System.out.println("Client deconnecté ! Info -> : " + clientSocket.getInetAddress().getHostAddress());
                        break;
                    }

                    else if (clientCommand.startsWith("/message")) {
                        msg = clientCommand.split(" ",2)
                        System.out.println("Message : " + msg);
                        this.serv.addMessage("User",msg);
                        outputStream.writeObject("Message Envoyé -> " + msg + "\n");
                        
                    }
                        // Traitement de la commande spécifique
                        else if ("/info".equals(clientCommand)) {
                            // Renvoyer des informations au client (dans cet exemple, l'adresse IP)
                            String clientAddress = clientSocket.getInetAddress().getHostAddress();
                            System.out.println(this.serv.loadJSON());
                            outputStream.writeObject(
                                    "Votre adresse IP est : " + clientAddress + "\n" + this.serveur.getUsers());

                        } else if (clientCommand.startsWith("/follow")) {
                            String user = clientCommand.split(" ")[1];
                            if (this.serveur.getUsers().containsKey(user)) {
                                this.serveur.addFollow(this.nom, user);
                                outputStream.writeObject(
                                        "Vous êtes maintenant abonné à " + user + "\n");
                            } else {
                                outputStream.writeObject(
                                        "Cet utilisateur n'existe pas\n");
                            }

                        }

                        else if (clientCommand.startsWith("/unfollow")) {
                            String user = clientCommand.split(" ")[1];
                            if (this.serveur.getUsers().containsKey(user)) {
                                this.serveur.deleteFollow(this.nom, user);
                                outputStream.writeObject(
                                        "Vous êtes maintenant désabonné de " + user + "\n");
                            } else {
                                outputStream.writeObject(
                                        "Cet utilisateur n'existe pas\n");
                            }

                        }

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }

            outputStream.close();
            inputStream.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
