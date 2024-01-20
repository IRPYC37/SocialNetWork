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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;





public class Serveur {
    private ServerSocket serverSocket;
    private static String USERPATH = "./BD/users.json";
    private Map<String, List<String>> dico_users;
    private static String fichierMsg = "./BD/messages.json";


    public Serveur(int port) {
        try {
            serverSocket = new ServerSocket(port);

            // Créer un objet ObjectMapper de Jackson
            ObjectMapper objectMapper = new ObjectMapper();

            // Lire le fichier JSON en tant que noeud JSON
            JsonNode rootNode = objectMapper.readTree(new File(USERPATH));

            // Convertir le noeud JSON en un dictionnaire Java
            this.dico_users = convertJsonToMap(rootNode);

            // Afficher le dictionnaire résultant
            System.out.println("Données sous forme de dictionnaire");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() throws IOException {
        System.out.println("Le serveur est en ligne sur le port : " + serverSocket.getLocalPort() + " !");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println(
                        "Nouvelle connection établie ! Adresse : " + clientSocket.getInetAddress().getHostAddress());

                // Handle client communication in a separate thread
                new GestionServeur(clientSocket, this).start();
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

    public Map<String, List<String>> convertJsonToMap(JsonNode jsonNode) {
        Map<String, List<String>> resultMap = new HashMap<>();

        // Parcourir chaque champ du JSON
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();

            // Convertir les valeurs en liste de chaînes
            if (fieldValue.isArray()) {
                List<String> stringList = convertJsonArrayToStringList(fieldValue);
                resultMap.put(fieldName, stringList);
            }
            // Ajouter d'autres conditions selon les types de données que vous attendez

        }

        return resultMap;
    }

    public List<String> convertJsonArrayToStringList(JsonNode arrayNode) {
        List<String> stringList = new ArrayList<>();

        if (arrayNode.isArray()) {
            Iterator<JsonNode> elements = arrayNode.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                if (element.isTextual()) {
                    stringList.add(element.asText());
                }
                // Vous pouvez ajouter des conditions supplémentaires ici selon vos besoins
            }
        }

        return stringList;
    }

    public void addUser(String nom) throws IOException {
        this.dico_users.put(nom, new ArrayList<>());
        this.majUsersBd();

    }

    public void addFollow(String user, String follow) throws IOException {
        this.dico_users.get(user).add(follow);
        this.majUsersBd();
    }

    public void deleteFollow(String user, String follow) throws IOException {
        this.dico_users.get(user).remove(follow);
        this.majUsersBd();
    }

    public JsonNode convertMapToJson(Map<String, List<String>> userMap) {
        // Créer un objet ObjectMapper de Jackson
        ObjectMapper objectMapper = new ObjectMapper();

        // Créer un nœud JSON à partir de la structure de données Java
        ObjectNode rootNode = objectMapper.createObjectNode();
        userMap.forEach((key, value) -> {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            value.forEach(arrayNode::add);
            rootNode.set(key, arrayNode);
        });

        return rootNode;
    }

    public void majUsersBd() throws IOException {
        // Créer un objet ObjectMapper de Jackson
        ObjectMapper objectMapper = new ObjectMapper();

        // Convertir la structure de données Java en JSON
        JsonNode jsonNode = convertMapToJson(this.dico_users);

        // Écrire le JSON dans le fichier
        objectMapper.writeValue(new File(USERPATH), jsonNode);
    }

    public Map<String, List<String>> getUsers() {
        return this.dico_users;
    }

    public static void main(String[] args) throws IOException {
        Serveur server = new Serveur(5555);
        server.startServer();
    }
}
