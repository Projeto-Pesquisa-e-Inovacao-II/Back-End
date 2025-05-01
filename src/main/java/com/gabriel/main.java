package com.gabriel;

import com.gabriel.services.DadosEvasaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class main {
    private static final Logger logger = LoggerFactory.getLogger(main.class);

    public static void main(String[] args) throws IOException {
        String NOME_BUCKET = System.getenv("NOME_BUCKET");

        logger.info("Iniciando aplicação...");
        logger.debug("Nome do bucket: {}", NOME_BUCKET);

        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();

        String filePath = "D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L03_01-2024.xlsx";

        logger.info("Carregando planilha: {}", filePath);
        dadosEvasaoService.carregarPlanilha(filePath);

        logger.info("Processando dados...");
        dadosEvasaoService.processarDados();

        logger.info("Inserindo dados de evasão...");
        dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), 3, filePath);

        logger.info("Processo concluído com sucesso.");


    }
}