package com.gabriel;

import com.gabriel.services.DadosEvasaoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class main {
    public static void main(String[] args) throws IOException {
        String NOME_BUCKET = System.getenv("NOME_BUCKET");

        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();

        Integer year = 2024;

        //this is actually terrible, but it works
        for (int conc = 1; conc < 33; conc++) {

            year = 2024;
            for (int i = 1; i <= 10; i++) {

                Path doesFileExist;
                String filePath = "";
                if(conc < 10) {
                    doesFileExist = Path.of("D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L0" + conc + "_" + year + ".xlsx"); // L0" + conc + "_" + year + ".xlsx"
                } else {
                    doesFileExist = Path.of("D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L" + conc + "_" + year + ".xlsx"); // L" + conc + "_" + year + ".xlsx"
                }

                if(Files.exists(doesFileExist)) {
                    filePath = doesFileExist.toString();

                    dadosEvasaoService.carregarPlanilha(filePath);
                    System.out.println("35 -> existe");


                    dadosEvasaoService.processarDados();
                    dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);

                } else {

                    System.out.println("não");

                    for (int j = 1; j <= 12; j++) {
                        Path doesSubfilesExist;

                        if(conc < 10) {
                            if (j < 10) {
                                doesSubfilesExist = Path.of("D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L0" + conc +"_0" + j + "-" + year + ".xlsx"); //L0" + conc +"_0" + j + "-" + year + ".xlsx"
                            } else {
                                doesSubfilesExist = Path.of("D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L0" + conc +"_" + j + "-" + year + ".xlsx");//L0" + conc +"_" + j + "-" + year + ".xlsx"
                            }


                        } else {
                            doesSubfilesExist = Path.of("D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L" + conc +"_0" + j + "-" + year + ".xlsx");
                        }


                        if(Files.exists(doesSubfilesExist)) {
                            System.out.println("62 -> existe");

                            filePath = doesSubfilesExist.toString();

                            dadosEvasaoService.carregarPlanilha(filePath);

                            dadosEvasaoService.processarDados();
                            dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);
                        } else {

                            System.out.println("nao");

                        }

                    }


                }

                year ++;
                System.out.println(i);

            }


        }


    }
}