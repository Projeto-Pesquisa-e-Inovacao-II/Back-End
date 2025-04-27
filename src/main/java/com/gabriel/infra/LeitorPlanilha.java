package com.gabriel.infra;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class LeitorPlanilha {
    protected Workbook workbook;

    public void carregarPlanilha(String filePath) {
        IOUtils.setByteArrayMaxOverride(200_000_000);

        try {
            System.out.println("Carregando planilha " + filePath);

            Path file = Path.of(filePath);
            InputStream arquivo = Files.newInputStream(file);

            workbook = new XSSFWorkbook(arquivo);
    }
        catch (IOException e) {
        e.printStackTrace();
        }
    }

    public abstract void processarDados();

}
