import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {

    // Map of clients connected: username -> ClientInfo
    private static Map<String, ClienteInfo> clientes = new HashMap<>();

    public static void main(String[] args) throws Exception {
        int porta = 9001;
        ServerSocket listener = new ServerSocket(porta);
        System.out.println("Servidor iniciado na porta: " + porta);

        try {
            while (true) {
                Socket socket = listener.accept();
                new Thread(new Manipulador(socket)).start();
            }
        } finally {
            listener.close();
        }
    }

    // Classe para armazenar informações do cliente
    private static class ClienteInfo {
        private String nomeUsuario;
        private PrintWriter writer;

        public ClienteInfo(String nomeUsuario, PrintWriter writer) {
            this.nomeUsuario = nomeUsuario;
            this.writer = writer;
        }

        public String getNomeUsuario() {
            return nomeUsuario;
        }

        public PrintWriter getWriter() {
            return writer;
        }
    }

    private static class Manipulador implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String nomeUsuario;

        public Manipulador(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Recebe e armazena o nome de usuário
                nomeUsuario = in.readLine();
                System.out.println("Novo usuário conectado: " + nomeUsuario);

                // Adiciona o cliente à lista
                synchronized (clientes) {
                    clientes.put(nomeUsuario, new ClienteInfo(nomeUsuario, out));
                }

                // Envia a lista atualizada de usuários para todos
                enviarListaUsuarios();

                // Notifica todos os clientes que um novo usuário entrou
                broadcast("Servidor", nomeUsuario + " entrou no chat.", null);

                String mensagem;
                while ((mensagem = in.readLine()) != null) {
                    System.out.println(nomeUsuario + ": " + mensagem);

                    // Verifica se é uma mensagem privada ou para todos
                    if (mensagem.startsWith("PRIVATE:")) {
                        // Formato: PRIVATE:destinatario:conteudo
                        String[] partes = mensagem.split(":", 3);
                        String destinatario = partes[1];
                        String conteudo = partes[2];
                        enviarMensagemPrivada(nomeUsuario, destinatario, conteudo);
                    } else if (mensagem.startsWith("PUBLIC:")) {
                        // Formato: PUBLIC:conteudo
                        String conteudo = mensagem.substring(7);
                        broadcast(nomeUsuario, conteudo, null);
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro de E/S: " + e.getMessage());
            } finally {
                // Remove o cliente da lista ao desconectar
                synchronized (clientes) {
                    clientes.remove(nomeUsuario);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar o socket: " + e.getMessage());
                }
                // Notifica todos os clientes que um usuário saiu
                broadcast("Servidor", nomeUsuario + " saiu do chat.", null);

                // Envia a lista atualizada de usuários para todos
                enviarListaUsuarios();
            }
        }

        // Método para enviar mensagens a todos os clientes ou a um cliente específico
        private void broadcast(String remetente, String mensagem, String excludeUser) {
            synchronized (clientes) {
                for (ClienteInfo cliente : clientes.values()) {
                    if (excludeUser == null || !cliente.getNomeUsuario().equals(excludeUser)) {
                        cliente.getWriter().println("MESSAGE:" + remetente + ":" + mensagem);
                    }
                }
            }
        }

        // Método para enviar mensagens privadas
        private void enviarMensagemPrivada(String remetente, String destinatario, String mensagem) {
            synchronized (clientes) {
                ClienteInfo clienteDestinatario = clientes.get(destinatario);
                if (clienteDestinatario != null) {
                    clienteDestinatario.getWriter().println("PRIVATE:" + remetente + ":" + mensagem);
                    // Também envia uma cópia ao remetente
                    ClienteInfo clienteRemetente = clientes.get(remetente);
                    if (clienteRemetente != null) {
                        clienteRemetente.getWriter().println("PRIVATE:" + remetente + ":" + mensagem);
                    }
                } else {
                    // Se o destinatário não for encontrado, informa o remetente
                    ClienteInfo clienteRemetente = clientes.get(remetente);
                    if (clienteRemetente != null) {
                        clienteRemetente.getWriter().println("MESSAGE:Servidor:Usuário " + destinatario + " não está disponível.");
                    }
                }
            }
        }

        // Envia a lista atualizada de usuários para todos os clientes
        private void enviarListaUsuarios() {
            synchronized (clientes) {
                StringBuilder listaUsuarios = new StringBuilder();
                for (String usuario : clientes.keySet()) {
                    listaUsuarios.append(usuario).append(",");
                }
                // Remove a última vírgula
                if (listaUsuarios.length() > 0) {
                    listaUsuarios.setLength(listaUsuarios.length() - 1);
                }
                for (ClienteInfo cliente : clientes.values()) {
                    cliente.getWriter().println("USER_LIST:" + listaUsuarios.toString());
                }
            }
        }
    }
    
}


