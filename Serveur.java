import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;

public class Serveur {
    private ServerSocket serverSocket;
    private static String USERPATH = "./BD/users.json";
    private Map<String, List<String>> dico_users;

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
