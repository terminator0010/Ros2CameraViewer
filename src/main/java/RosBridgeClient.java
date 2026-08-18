import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class RosBridgeClient {
    private WebSocketClient webSocketClient;
    private RosCameraGUI gui;
    
    // Variável para configurar o tempo de reconexão (em milissegundos)
    private int reconnectIntervalMs = 5000; 
    private Timer timer = new Timer();
    private boolean isReconnecting = false;

    public RosBridgeClient(RosCameraGUI gui) {
        this.gui = gui;
    }
    
    public void setReconnectIntervalMs(int ms) {
        this.reconnectIntervalMs = ms;
    }

    public void connect() {
        try {
            // O rosbridge roda por padrão na porta 9090
            URI serverUri = new URI("ws://localhost:9090");

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Conectado ao rosbridge!");
                    gui.setStatus("Conectado ao servidor ROS 2.");

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
                    gui.setStatus("Conexão fechada. Reconectando em " + (reconnectIntervalMs / 1000) + "s...");
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Não foi possível conectar com o Ros2_bridge. Verifique se o servidor está rodando.");
                    gui.setStatus("Falha na conexão. Reconectando em " + (reconnectIntervalMs / 1000) + "s...");
                    scheduleReconnect();
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            System.err.println("Erro ao configurar o endereço do servidor.");
            scheduleReconnect();
        }
    }

    private synchronized void scheduleReconnect() {
        if (!isReconnecting) {
            isReconnecting = true;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isReconnecting = false;
                    connect();
                }
            }, reconnectIntervalMs);
        }
    }
}
