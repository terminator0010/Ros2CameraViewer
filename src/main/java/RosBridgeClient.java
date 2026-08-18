import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class RosBridgeClient {
    private WebSocketClient webSocketClient;
    private RosCameraGUI gui;

    public RosBridgeClient(RosCameraGUI gui) {
        this.gui = gui;
    }

    public void connect() {
        try {
            // O rosbridge roda por padrão na porta 9090
            URI serverUri = new URI("ws://localhost:9090");

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Conectado ao rosbridge!");

                    // Envia o comando ROSBridge v2 para assinar o tópico
                    JSONObject subscribeMsg = new JSONObject();
                    subscribeMsg.put("op", "subscribe");
                    subscribeMsg.put("topic", "/camera/image_raw");
                    subscribeMsg.put("type", "sensor_msgs/Image");

                    send(subscribeMsg.toString());
                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject json = new JSONObject(message);

                        // Verifica se a mensagem é uma publicação do nosso tópico
                        if ("publish".equals(json.optString("op")) &&
                                "/camera/image_raw".equals(json.optString("topic"))) {

                            JSONObject msg = json.getJSONObject("msg");
                            String base64Data = msg.getString("data");
                            int width = msg.getInt("width");
                            int height = msg.getInt("height");

                            gui.updateImage(base64Data, width, height);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao processar mensagem JSON: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Conexão fechada: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Não foi possível conectar com o Ros2_bridge. Verifique se o servidor está rodando.");
                    gui.setStatus("Falha na conexão com o servidor ROS 2.");
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            System.err.println("Erro ao configurar o endereço do servidor.");
        }
    }
}
