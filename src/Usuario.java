import java.io.File;
import java.io.IOException;
import java.sql.*;

public class Usuario {
    private int id;
    private String nome;
    private String senha; // Plain text password (for comparison)
    private byte[] senhaCriptografada; // Encrypted password

    public Usuario(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
    }

    // Método para verificar se o usuário já existe na base de dados
    public boolean usuarioExiste() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnFactory.getConnection();
            String sql = "SELECT id, password FROM User WHERE name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, this.nome);

            rs = stmt.executeQuery();

            // Se retornar algum resultado, significa que o usuário já existe
            if (rs.next()) {
                this.id = rs.getInt("id");
                this.senhaCriptografada = rs.getBytes("password");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            fecharRecursos(conn, stmt, rs);
        }

        return false;
    }

    // Método para adicionar um novo usuário ao banco de dados
    public void criarUsuario() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Criptografar a senha usando CryptoDummy
            CryptoDummy crypto = new CryptoDummy();
            File chaveFile = new File("chave.dummy");

            // Generate the key only if it doesn't exist
            if (!chaveFile.exists()) {
                crypto.geraChave(chaveFile);
            }

            // Encrypt using the existing key
            crypto.geraCifra(senha.getBytes(), chaveFile);
            

            try {
                this.senhaCriptografada = crypto.getTextoCifrado();
            } catch (Exception e) {
                e.printStackTrace();
            }

            conn = ConnFactory.getConnection();
            String sql = "INSERT INTO User (name, password) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, this.nome);
            stmt.setBytes(2, this.senhaCriptografada);

            stmt.executeUpdate();

            // Obter o ID gerado
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                this.id = generatedKeys.getInt(1);
            }

            System.out.println("Usuário '" + nome + "' criado com sucesso com ID: " + id);
        } catch (SQLException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            fecharRecursos(conn, stmt, null);
        }
    }

    // Método para autenticar o usuário
    public boolean autenticar() {
        if (usuarioExiste()) {
            try {
                // Descriptografar a senha armazenada
                CryptoDummy crypto = new CryptoDummy();
                File chaveFile = new File("chave.dummy");

                // Ensure the key exists
                if (!chaveFile.exists()) {
                    System.err.println("Chave de criptografia não encontrada.");
                    return false;
                }

                // Do not generate a new key here!
                // Decrypt using the existing key
                crypto.geraDecifra(senhaCriptografada, chaveFile);
                String senhaDescriptografada = new String(crypto.getTextoDecifrado());

                // Comparar a senha fornecida com a senha armazenada
                return senha.equals(senhaDescriptografada);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Se o usuário não existir, criar um novo
            criarUsuario();
            return true;
        }
        return false;
    }

    // Método para fechar os recursos da conexão
    private void fecharRecursos(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) ConnFactory.disconnect(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
