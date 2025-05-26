package com.gabriel;

import com.gabriel.enums.FilePath;
import com.gabriel.services.DadosEvasaoService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();

//        if (dadosEvasaoService.dadosJaInseridos()) {
//            System.out.println("Dados já inseridos. Encerrando execução.");
//            return;
//        }

        Integer year = 2024;

        String base = FilePath.FILE_PATH.getFilePath();

        //this is actually terrible, but it works
        for (int conc = 1; conc < 33; conc++) {

//            year = 2024;
//            for (int i = 1; i <= 10; i++) {

//                Path doesFileExist;
                String filePath = "";
//                if(conc < 10) {
//                    doesFileExist = Path.of(base + "L0" + conc + "_" + year + ".xlsx"); // L0" + conc + "_" + year + ".xlsx"
//                } else {
//                    doesFileExist = Path.of(base + "L" + conc + "_" + year + ".xlsx"); // L" + conc + "_" + year + ".xlsx"
//                }
//
//                if(Files.exists(doesFileExist)) {
//                    filePath = doesFileExist.toString();
//
//                    dadosEvasaoService.carregarPlanilha(filePath);
//
//                    dadosEvasaoService.processarDados();
//                    dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);
//                    dadosEvasaoService.sendFileToS3(filePath);
//
//                } else {
//
//                    System.out.println("não");

                    for (int j = 1; j <= 12; j++) {
                        Path doesSubfilesExist;

                        if(conc < 10) {
                            if (j < 10) {
                                doesSubfilesExist = Path.of(base + "L0" + conc +"_0" + j + "-" + year + ".xlsx"); //L0" + conc +"_0" + j + "-" + year + ".xlsx"
                            } else {
                                doesSubfilesExist = Path.of(base + "L0" + conc +"_" + j + "-" + year + ".xlsx");//L0" + conc +"_" + j + "-" + year + ".xlsx"
                            }


                        } else {
                            doesSubfilesExist = Path.of(base + "L" + conc +"_0" + j + "-" + year + ".xlsx");
                        }


                        if(Files.exists(doesSubfilesExist)) {
                            filePath = doesSubfilesExist.toString();

                            try (InputStream inputStream = Files.newInputStream(Path.of(filePath))) {
                                dadosEvasaoService.carregarPlanilha(inputStream, filePath);
                            }


                            dadosEvasaoService.processarDados();
                            dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);
                            dadosEvasaoService.sendFileToS3(filePath);
                        }
                    }


//                }

//                year ++;

//            }


        }


    }
}