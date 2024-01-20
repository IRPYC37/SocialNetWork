import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Serveur {
    private ServerSocket serverSocket;
    private String fichierMsg = "messages.json";

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
                new GestionServeur(clientSocket,this).start();
            } catch (IOException e) {
                e.printStackTrace();
            }       
        }
    }


    public JsonNode loadJSON(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(new File(this.fichierMsg));
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERR");
            return null; 
        }
    }

    public void addMessage(String userName,String message){
        JsonNode toutLesMessages = loadJSON();
        LocalDateTime dateHeureActuelles = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String dateFormatee = dateHeureActuelles.format(formatter);
        int idM= getMaxIdMessage() +1;
        String date = dateFormatee ;

        if (toutLesMessages != null) {
            ObjectNode messagesNode = (ObjectNode) toutLesMessages.get("messages");

            // fabrique le message
            ObjectNode newMessageNode = messagesNode.putObject(String.valueOf(idM));
            newMessageNode.put("user", userName);
            newMessageNode.put("content", message);
            newMessageNode.put("date", date);
            newMessageNode.put("likes", 0);

            // Sauvegarder le fichier JSON mis à jour
            sauvegarderFichierJson(toutLesMessages);
        }

    }

    public void like(String id){
        JsonNode json = loadJSON();
        if (json != null) {
            ObjectNode toutLesMessages = (ObjectNode) json.get("messages");
            JsonNode leMessage = toutLesMessages.get(id);
            if (leMessage != null) {
                int likes = leMessage.get("likes").asInt();
                ((ObjectNode) leMessage).put("likes", likes + 1);
                sauvegarderFichierJson(json);
            }
        }
    }

    public void delete(String id,String userName){
        JsonNode json = loadJSON();
        if (json != null) {
            ObjectNode toutLesMessages = (ObjectNode) json.get("messages");
            JsonNode leMessage = toutLesMessages.get(id);
            
            if (leMessage != null && leMessage.get("user").asText().equals(userName)) {
                toutLesMessages.remove(id);
                sauvegarderFichierJson(json);
            }
        }
    }

    public String getMessage(String id) {
        JsonNode json = loadJSON();
        if (json != null) {
            JsonNode toutLesMessages = json.get("messages");
            if (toutLesMessages != null) {
                JsonNode leMessage = toutLesMessages.get(id);
                if (leMessage != null) {
                    return leMessage.toString();
                }
            }
        }
        return null;
    }

    public void sauvegarderFichierJson(JsonNode toutLesMessages) {
        try {
            // Créer un objet ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(this.fichierMsg), toutLesMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public int getMaxIdMessage() {
    // Charger le fichier JSON
    JsonNode jsonNode = loadJSON();

    if (jsonNode != null && jsonNode.has("messages")) {
        // Récupérer le nœud "messages"
        JsonNode messagesNode = jsonNode.get("messages");

        if (messagesNode.isObject()) {
            // Parcourir les clés sous le nœud "messages" et trouver le maximum
            Iterator<Map.Entry<String, JsonNode>> iterator = messagesNode.fields();
            int maxID = 0;

            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                try {
                    int currentID = Integer.parseInt(entry.getKey());
                    maxID = Math.max(maxID, currentID);
                } catch (NumberFormatException e) {
                    // Ignorer les clés non numériques
                }
            }

            return maxID;
        }
    }

    // En cas d'erreur ou si le nœud "messages" n'est pas trouvé
    return -1;
}


    public static void main(String[] args) {
        Serveur server = new Serveur(55555);
        server.startServer();
    }
}
