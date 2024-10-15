import java.io.*;

public class CryptoDummy {

   private byte[] textoCifrado; // Encrypted text
   private byte[] textoDecifrado; // Decrypted text

   public CryptoDummy() {
      textoCifrado = null;
      textoDecifrado = null;
   }

   /**
    * Gera uma chave Dummy simétrica e a armazena no arquivo fornecido.
    * A chave é gerada aleatoriamente no intervalo de 0 a 100.
    * 
    * @param fDummy Arquivo onde a chave será armazenada.
    */
   public void geraChave(File fDummy) throws IOException {
      int dk = (int) (Math.random() * 101); // Random value between 0 and 100.
      // Save the symmetric key to the file using serialization
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fDummy))) {
         oos.writeObject(dk); // Write the integer dk (key) to the file.
      }
   }

   /**
    * Lê a chave Dummy do arquivo fornecido.
    * 
    * @param fDummy Arquivo contendo a chave Dummy serializada.
    * @return A chave Dummy como um inteiro.
    */
   private int leChave(File fDummy) throws IOException, ClassNotFoundException {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fDummy))) {
         return (Integer) ois.readObject(); // Retrieve the key as an integer
      }
   }

   /**
    * Aplica a cifra no texto (criptografia) usando a chave Dummy do arquivo fornecido.
    * 
    * @param texto  O texto em bytes que será cifrado.
    * @param fDummy Arquivo contendo a chave Dummy serializada.
    */
   public void geraCifra(byte[] texto, File fDummy) throws IOException, ClassNotFoundException {
      int iDummy = leChave(fDummy); // Read the key from the file
      textoCifrado = new byte[texto.length];

      for (int i = 0; i < texto.length; i++) {
         textoCifrado[i] = (byte) (texto[i] + i + iDummy);
      }
   }

   /**
    * Retorna o texto cifrado.
    * 
    * @return O texto cifrado em formato de array de bytes.
    */
   public byte[] getTextoCifrado() {
      return textoCifrado;
   }

   /**
    * Aplica a decifra no texto (descriptografia) usando a chave Dummy do arquivo fornecido.
    * 
    * @param texto  O texto em bytes que será decifrado.
    * @param fDummy Arquivo contendo a chave Dummy serializada.
    */
   public void geraDecifra(byte[] texto, File fDummy) throws IOException, ClassNotFoundException {
      int iDummy = leChave(fDummy); // Read the key from the file
      textoDecifrado = new byte[texto.length];

      for (int i = 0; i < texto.length; i++) {
         textoDecifrado[i] = (byte) (texto[i] - i - iDummy);
      }
   }

   /**
    * Retorna o texto decifrado.
    * 
    * @return O texto decifrado em formato de array de bytes.
    */
   public byte[] getTextoDecifrado() {
      return textoDecifrado;
   }
}

