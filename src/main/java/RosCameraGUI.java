import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Base64;

public class RosCameraGUI extends JFrame {
    private JLabel imageLabel;

    public RosCameraGUI() {
        setTitle("ROS 2 Camera Viewer via WebSocket");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);

        // Label que abrigará os frames da câmera
        imageLabel = new JLabel("Aguardando conexão com rosbridge...", SwingConstants.CENTER);
        add(imageLabel, BorderLayout.CENTER);
    }

    public void updateImage(String base64Data, int width, int height) {
        // Decodifica a string Base64 que veio do ROS para um array de bytes
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // Como o OpenCV/C++ enviou no formato "bgr8", usamos TYPE_3BYTE_BGR
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, width, height, imageBytes);

        // Atualiza a interface gráfica na Thread do Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(new ImageIcon(image));
            imageLabel.setText(""); // Remove o texto de aviso inicial

            // Ajusta o tamanho da janela na primeira vez para caber a resolução da câmera
            if (getWidth() < width || getHeight() < height) {
                setSize(width, height + 40); // +40 para compensar a barra de título do SO
            }
        });
    }

    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(null);
            imageLabel.setText(message);
        });
    }
}
