package com.gabriel.services;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.xb.ltgfmt.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public abstract class LeitorPlanilha {
    protected Workbook workbook;

    public void carregarPlanilha(String filePath) {
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
