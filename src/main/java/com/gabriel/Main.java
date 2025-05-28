package com.gabriel;

import com.gabriel.enums.FilePath;
import com.gabriel.services.DadosEvasaoService;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();

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

                            String nomePlanilha = doesSubfilesExist.getFileName().toString();
                            String nomeConcessao = dadosEvasaoService.obterNomeConcessaoPorId(conc);

                            MDC.put("concessao", nomeConcessao);
                            MDC.put("planilha", nomePlanilha);
                            MDC.put("status", "INICIANDO_PROCESSAMENTO");

                            dadosEvasaoService.carregarPlanilha(filePath);
                            dadosEvasaoService.configurarContexto(conc, nomePlanilha);
                            dadosEvasaoService.processarDados();
                            dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);
                            dadosEvasaoService.sendFileToS3(filePath);

                            MDC.clear(); // importante para não manter valores antigos
                        }

                    }


//                }

//                year ++;

//            }


        }


    }
}