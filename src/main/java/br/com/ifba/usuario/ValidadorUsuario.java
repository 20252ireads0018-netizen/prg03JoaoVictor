package br.com.ifba.usuario;

public class ValidadorUsuario {

    public static boolean contemPalavraProibida(String texto) {

        String[] palavrasProibidas = {
            "admin",
            "teste",
            "root",
            "senha123"
        };

        for (String palavra : palavrasProibidas) {
            if (texto.toLowerCase().contains(palavra.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}