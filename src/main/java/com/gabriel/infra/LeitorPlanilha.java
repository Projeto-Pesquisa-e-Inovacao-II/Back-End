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

    public void carregarPlanilha(InputStream file, String fileName) {
        logger.info("Iniciando carregamento da planilha: {}", fileName);
        IOUtils.setByteArrayMaxOverride(400_000_000);

        try {
            workbook = new XSSFWorkbook(file);
            logger.info("Planilha carregada com sucesso!");
        }
        catch (IOException e) {
            logger.error("Erro ao carregar planilha '{}'", fileName, e);
            e.printStackTrace();
        }
    }

    public void fecharPlanilha() {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                logger.error("Erro ao fechar o workbook", e);
            }
            workbook = null;  // Remove referência para facilitar GC
        }
    }


    public abstract void processarDados();

}
