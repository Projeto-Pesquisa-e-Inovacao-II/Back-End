package com.gabriel.infra;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class LeitorPlanilha {
    protected Workbook workbook;
    private static final Logger logger = LoggerFactory.getLogger(LeitorPlanilha.class);

    public void carregarPlanilha(String filePath) {
        logger.info("Iniciando carregamento da planilha: {}", filePath);
        IOUtils.setByteArrayMaxOverride(400_000_000);

        try {
            Path file = Path.of(filePath);
            InputStream arquivo = Files.newInputStream(file);

            workbook = new XSSFWorkbook(arquivo);

            int sheets = workbook.getNumberOfSheets();
            logger.info("Planilha carregada com sucesso! Número de abas: {}", sheets);

        }
        catch (IOException e) {
            logger.error("Erro ao carregar planilha '{}'", filePath, e);

        e.printStackTrace();
        }
    }

    public abstract void processarDados();

}
