import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class Cliente {

    private String senhaUsuario; 
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame; // Janela principal do chat
    private JTextField campoTexto = new JTextField(30);
    private JTextPane areaTexto = new JTextPane();
    private String nomeUsuario;
    private ResourceBundle mensagens;
    private Locale idiomaSelecionado;
    private JFrame loginFrame;
    private JComboBox<String> comboUsuarios; // ComboBox para selecionar o destinatário
    private DefaultComboBoxModel<String> modeloUsuarios; // Modelo para atualizar a lista de usuários
    private boolean chatConfigurado = false; // Flag para verificar se a tela do chat já foi configurada

    public Cliente() {

        // Configuração inicial do idioma padrão
        idiomaSelecionado = new Locale("pt"); // Português como idioma padrão
        mensagens = ResourceBundle.getBundle("Messages", idiomaSelecionado);

        // Tela de Login Personalizada
        criarTelaLogin();
    }

    private void criarTelaLogin() {
        // Cria a tela principal
        loginFrame = new JFrame(mensagens.getString("login_title"));
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(600, 350);
        loginFrame.setLocationRelativeTo(null);

        // Cria o painel principal com uma cor de fundo
        JPanel painelLogin = new JPanel(new GridBagLayout());
        painelLogin.setBackground(new Color(60, 63, 65)); // Cor de fundo escura

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo ou título no topo
        JLabel labelLogo = new JLabel("CalvApp");
        labelLogo.setFont(new Font("SansSerif", Font.BOLD, 50));
        labelLogo.setForeground(Color.WHITE);
        labelLogo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        painelLogin.add(labelLogo, gbc);

        // Rótulo e campo de nome de usuário
        JLabel labelNomeUsuario = new JLabel(mensagens.getString("choose_username"));
        labelNomeUsuario.setForeground(Color.WHITE);
        labelNomeUsuario.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        painelLogin.add(labelNomeUsuario, gbc);

        JTextField campoNomeUsuario = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        campoNomeUsuario.setFont(new Font("SansSerif", Font.PLAIN, 20));
        painelLogin.add(campoNomeUsuario, gbc);

        // Rótulo e campo de senha
        JLabel labelSenha = new JLabel(mensagens.getString("password_label"));
        labelSenha.setFont(new Font("SansSerif", Font.BOLD, 20));
        labelSenha.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        painelLogin.add(labelSenha, gbc);

        JPasswordField campoSenha = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        campoSenha.setFont(new Font("SansSerif", Font.PLAIN, 20));
        painelLogin.add(campoSenha, gbc);

        // Seleção de idioma
        JLabel labelIdioma = new JLabel(mensagens.getString("language_label"));
        labelIdioma.setFont(new Font("SansSerif", Font.BOLD, 20));
        labelIdioma.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        painelLogin.add(labelIdioma, gbc);

        String[] nomesIdiomas = { "Português", "English", "Español", "Français", "Deutsch", "Italiano" };
        Locale[] locais = {
                new Locale("pt"),
                new Locale("en"),
                new Locale("es"),
                new Locale("fr"),
                new Locale("de"),
                new Locale("it")
        };
        JComboBox<String> comboIdiomas = new JComboBox<>(nomesIdiomas);
        comboIdiomas.setSelectedIndex(0); // Idioma padrão
        gbc.gridx = 1;
        gbc.gridy = 3;
        comboIdiomas.setFont(new Font("SansSerif", Font.PLAIN, 20));
        painelLogin.add(comboIdiomas, gbc);

        // Botão de Login
        JButton botaoLogin = new JButton("Login");
        botaoLogin.setBackground(new Color(30, 215, 96)); // Cor verde do Spotify
        botaoLogin.setFont(new Font("SansSerif", Font.BOLD, 20));
        botaoLogin.setForeground(Color.WHITE);
        botaoLogin.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        painelLogin.add(botaoLogin, gbc);

        // Botão para sair
        JButton botaoSair = new JButton(mensagens.getString("quit"));
        botaoSair.setBackground(new Color(215, 30, 30)); // Cor vermelha
        botaoSair.setFont(new Font("SansSerif", Font.BOLD, 20));
        botaoSair.setForeground(Color.WHITE);
        botaoSair.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 4;
        painelLogin.add(botaoSair, gbc);

        // Dicas de ferramentas
        campoNomeUsuario.setToolTipText("Digite seu nome de usuário");
        campoSenha.setToolTipText("Digite sua senha");

        // Atualiza textos com base no idioma selecionado
        ActionListener atualizarIdioma = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int indiceSelecionado = comboIdiomas.getSelectedIndex();
                idiomaSelecionado = locais[indiceSelecionado];
                mensagens = ResourceBundle.getBundle("Messages", idiomaSelecionado);
                labelNomeUsuario.setText(mensagens.getString("choose_username"));
                labelSenha.setText(mensagens.getString("password_label"));
                labelIdioma.setText(mensagens.getString("language_label"));
                loginFrame.setTitle(mensagens.getString("login_title"));
                botaoSair.setText(mensagens.getString("quit"));
                botaoLogin.setText(mensagens.getString("login_label"));
            }
        };
        comboIdiomas.addActionListener(atualizarIdioma);

        // Adiciona o listener de ação para o botão de login
        botaoLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nomeUsuario = campoNomeUsuario.getText();
                senhaUsuario = new String(campoSenha.getPassword());
                if (nomeUsuario == null || nomeUsuario.trim().isEmpty() ||
                        senhaUsuario == null || senhaUsuario.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                            loginFrame,
                            mensagens.getString("invalid_credentials"),
                            mensagens.getString("login_title"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // Tenta autenticar o usuário
                    try {
                        Usuario usuario = new Usuario(nomeUsuario, senhaUsuario);
                        if (usuario.autenticar()) {
                            loginFrame.dispose();

                            // Configura a interface de chat após o login bem-sucedido
                            if (!chatConfigurado) {
                                configurarChat();
                            }

                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.setLocationRelativeTo(null);
                            frame.setSize(700, 400);
                            frame.setVisible(true);

                            // Executa o cliente
                            executar();
                        } else {
                            JOptionPane.showMessageDialog(
                                    loginFrame,
                                    mensagens.getString("invalid_credentials"),
                                    mensagens.getString("login_title"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        botaoSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Adiciona o painel principal ao frame
        loginFrame.getContentPane().add(painelLogin);
        loginFrame.setVisible(true);
    }

    private void configurarChat() {
        // Configuração da interface gráfica do chat
        frame = new JFrame();
        campoTexto.setEditable(false);
        areaTexto.setEditable(false);

        // Gera uma cor de fundo aleatória
        Random rand = new Random();
        int r = 200 + rand.nextInt(56); // 200 a 255
        int g = 200 + rand.nextInt(56);
        int b = 200 + rand.nextInt(56);
        Color randomColor = new Color(r, g, b);

        // Define a cor de fundo da área de texto
        areaTexto.setBackground(randomColor);

        // Cria um painel para o título
        JPanel painelTitulo = new JPanel();
        painelTitulo.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel labelTitulo = new JLabel(mensagens.getString("messages_title"));
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        painelTitulo.add(labelTitulo);

        // Painel principal
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout());
        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipal.add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        // Painel inferior com campo de texto e combo box
        JPanel painelInferior = new JPanel(new FlowLayout());

        // Modelo para o combo box de usuários
        modeloUsuarios = new DefaultComboBoxModel<>();
        comboUsuarios = new JComboBox<>(modeloUsuarios);
        comboUsuarios.addItem(mensagens.getString("everyone_message")); // Adiciona a opção "Everyone" inicialmente
        comboUsuarios.setSelectedIndex(0); // Seleciona "Everyone" por padrão

        // Adiciona os componentes ao painel inferior
        JLabel labelDestinatario = new JLabel(mensagens.getString("to_message"));
        labelDestinatario.setFont(new Font("Arial", Font.PLAIN, 20));
        painelInferior.add(labelDestinatario);
        painelInferior.add(comboUsuarios);

        campoTexto.setFont(new Font("Arial", Font.PLAIN, 20));
        painelInferior.add(campoTexto);

        frame.getContentPane().add(painelPrincipal, BorderLayout.CENTER);
        frame.getContentPane().add(painelInferior, BorderLayout.SOUTH);

        // Listener para o campo de texto
        campoTexto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String mensagem = campoTexto.getText();
                String destinatario = (String) comboUsuarios.getSelectedItem();
                if (destinatario.equals(mensagens.getString("everyone_message"))) {
                    out.println("PUBLIC:" + mensagem);
                } else {
                    out.println("PRIVATE:" + destinatario + ":" + mensagem);
                }
                campoTexto.setText("");
            }
        });

        frame.setTitle(MessageFormat.format(mensagens.getString("chat_title"), nomeUsuario));

        // Marca o chat como configurado
        chatConfigurado = true;
    }

    private void executar() throws IOException {
        String enderecoServidor = "localhost";
        int porta = 9001;
        Socket socket = new Socket(enderecoServidor, porta);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Envia o nome de usuário para o servidor
        out.println(nomeUsuario);

        // Habilita o campo de texto
        campoTexto.setEditable(true);

        out.println(nomeUsuario);
        out.println(senhaUsuario);

        // Thread para receber mensagens do servidor
        new Thread(new Runnable() {
            public void run() {
                try {
                    String mensagem;
                    while ((mensagem = in.readLine()) != null) {
                        if (mensagem.startsWith("MESSAGE:")) {
                            // Mensagem pública
                            String[] partes = mensagem.substring(8).split(":", 2);
                            String remetente = partes[0];
                            String conteudo = partes[1];
                            adicionarMensagem(remetente + ": " + conteudo);
                        } else if (mensagem.startsWith("PRIVATE:")) {
                            // Mensagem privada
                            String[] partes = mensagem.substring(8).split(":", 2);
                            String remetente = partes[0];
                            String conteudo = partes[1];
                            adicionarMensagem("(Privado) " + remetente + ": " + conteudo);
                        } else if (mensagem.startsWith("USER_LIST:")) {
                            // Atualiza a lista de usuários
                            String lista = mensagem.substring(10);
                            atualizarListaUsuarios(lista);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexão encerrada: " + e.getMessage());
                }
            }
        }).start();
    }

    // Método para adicionar mensagens com cores
    private void adicionarMensagem(String mensagem) {
        // Obtém o estilo do documento
        StyledDocument doc = areaTexto.getStyledDocument();

        // Determina a cor com base no remetente
        SimpleAttributeSet estilo = new SimpleAttributeSet();

        if (mensagem.startsWith("Servidor")) {
            StyleConstants.setForeground(estilo, Color.GRAY);
            StyleConstants.setBold(estilo, true);
        } else if (mensagem.contains(nomeUsuario)) {
            StyleConstants.setForeground(estilo, Color.BLUE);
            StyleConstants.setBold(estilo, true);
        } else {
            StyleConstants.setForeground(estilo, Color.BLACK);
        }

        try {
            doc.insertString(doc.getLength(), mensagem + "\n", estilo);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Método para atualizar a lista de usuários
    private void atualizarListaUsuarios(String lista) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                modeloUsuarios.removeAllElements();
                modeloUsuarios.addElement(mensagens.getString("everyone_message"));
                String[] usuarios = lista.split(",");
                for (String usuario : usuarios) {
                    if (!usuario.equals(nomeUsuario)) {
                        modeloUsuarios.addElement(usuario);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
    }
}
