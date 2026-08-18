import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Inicia a aplicação Swing
        SwingUtilities.invokeLater(() -> {
            RosCameraGUI gui = new RosCameraGUI();
            gui.setVisible(true);

            RosBridgeClient client = new RosBridgeClient(gui);
            client.connect();
        });
    }
}
