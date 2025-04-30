package com.gabriel;

import com.gabriel.services.DadosEvasaoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class main {
    public static void main(String[] args) throws IOException {
        String NOME_BUCKET = System.getenv("NOME_BUCKET");

        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();

        String filePath = "/home/ubuntu/Dados/L03_01-2024.xlsx";

        dadosEvasaoService.carregarPlanilha(filePath);

        dadosEvasaoService.processarDados();
        dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), 3, filePath);

    }
}