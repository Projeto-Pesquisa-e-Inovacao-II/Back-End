package com.gabriel;

import com.gabriel.entities.DadosEvasao;
import com.gabriel.entities.handlers.DadosEvasaoHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class main {
    public static void main(String[] args) throws IOException {
        String NOME_BUCKET = System.getenv("NOME_BUCKET");

        DadosEvasaoHandler dadosEvasaoHandler = new DadosEvasaoHandler();

        Integer year = 0;

        //this is actually terrible, but it works
        for (int conc = 1; conc < 33; conc++) {
            year = 2016;
            for (int i = 1; i <= 10; i++) {
                Path doesFileExist;

                if(conc < 10) {
                doesFileExist = Path.of("/home/gabriel/Documentos/2sem2/dados/dados_juntos/L0" + conc + "_" + year + ".xlsx"); // L0" + conc + "_" + year + ".xlsx"
                } else {
                    doesFileExist = Path.of("/home/gabriel/Documentos/2sem2/dados/dados_juntos/L" + conc + "_" + year + ".xlsx"); // L" + conc + "_" + year + ".xlsx"
                }

                if(Files.exists(doesFileExist)) {

                    System.out.println("existe");

                } else {

                    System.out.println("não");

                    for (int j = 1; j <= 12; j++) {
                        Path doesSubfilesExist;

                        if(conc < 10) {
                            if (j < 10) {
                                doesSubfilesExist = Path.of("/home/gabriel/Documentos/2sem2/dados/dados_juntos/L0" + conc +"_0" + j + "-" + year + ".xlsx"); //L0" + conc +"_0" + j + "-" + year + ".xlsx"
                            } else {
                                doesSubfilesExist = Path.of("/home/gabriel/Documentos/2sem2/dados/dados_juntos/L0" + conc +"_" + j + "-" + year + ".xlsx");//L0" + conc +"_" + j + "-" + year + ".xlsx"
                            }
                        } else {
                            doesSubfilesExist = Path.of("/home/gabriel/Documentos/2sem2/dados/dados_juntos/L" + conc +"_0" + j + "-" + year + ".xlsx");
                        }



                        if(Files.exists(doesSubfilesExist)) {
                            System.out.println("existe");

                        } else {

                            System.out.println("nao");

                        }
                    }


                }
                year ++;
                System.out.println(i);
            }
        }

        dadosEvasaoHandler.carregarPlanilha("/home/gabriel/Documentos/2sem2/dados/L01-AUTOBAN/2025/L01_01-2025.xlsx");
        dadosEvasaoHandler.processarDados();

        dadosEvasaoHandler.inserirDadosEvasao(dadosEvasaoHandler.getDadosEvasaos());

    }
}