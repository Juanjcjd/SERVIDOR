import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int PORT = 1010;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Iniciando el servidor...");

            // Esperar conexiones entrantes de clientes
            while (true) {
                Socket socket = serverSocket.accept();
                // Crear un manejador de cliente para manejar la conexión con el cliente
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                // Iniciar un hilo para manejar la conexión con el cliente
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para enviar un mensaje a todos los clientes excepto al remitente
    static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Clase interna para manejar la conexión con un cliente
    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private String username;

        // Constructor para inicializar el manejador del cliente
        ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                // Configurar los flujos de entrada y salida
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // Leer el nombre de usuario enviado por el cliente
                username = reader.readLine();
                // Notificar a todos los clientes que este cliente se ha unido
                broadcastMessage(username + " se unió a la conversación", this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Método para manejar la recepción de mensajes del cliente
        @Override
        public void run() {
            String message;
            try {
                // Leer mensajes del cliente y enviarlos a todos los clientes conectados
                while ((message = reader.readLine()) != null) {
                    broadcastMessage(username + ": " + message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Cerrar la conexión del cliente
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Eliminar este cliente de la lista de clientes
                clients.remove(this);
                // Notificar a todos los clientes que este cliente se ha desconectado
                broadcastMessage(username + " se fue", this);
            }
        }

        // Método para enviar un mensaje al cliente
        void sendMessage(String message) {
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
