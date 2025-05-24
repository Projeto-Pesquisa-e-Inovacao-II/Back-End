package com.gabriel;

import com.gabriel.enums.FilePath;
import com.gabriel.infra.S3Provider;
import com.gabriel.services.DadosEvasaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();
        Logger logger = LoggerFactory.getLogger(Main.class);

//        Integer year = 2024;

//        String base = FilePath.FILE_PATH.getFilePath();

        //this is actually terrible, but it works
//        for (int conc = 1; conc < 33; conc++) {

//            year = 2024;
//            for (int i = 1; i <= 10; i++) {

//                Path doesFileExist;
//                String filePath = "";
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
//
//
//
//                    for (int j = 1; j <= 12; j++) {
//                        Path doesSubfilesExist;
//
//                        if(conc < 10) {
//                            if (j < 10) {
//                                doesSubfilesExist = Path.of(base + "L0" + conc +"_0" + j + "-" + year + ".xlsx"); //L0" + conc +"_0" + j + "-" + year + ".xlsx"
//                            } else {
//                                doesSubfilesExist = Path.of(base + "L0" + conc +"_" + j + "-" + year + ".xlsx");//L0" + conc +"_" + j + "-" + year + ".xlsx"
//                            }
//
//
//                        } else {
//                            if (j < 10) {
//                                doesSubfilesExist = Path.of(base + "L" + conc +"_0" + j + "-" + year + ".xlsx"); //L0" + conc +"_0" + j + "-" + year + ".xlsx"
//                            } else {
//                                doesSubfilesExist = Path.of(base + "L" + conc +"_" + j + "-" + year + ".xlsx");//L0" + conc +"_" + j + "-" + year + ".xlsx"
//                            }
//                            System.out.println(doesSubfilesExist);
//                        }
//
//
//                        if(Files.exists(doesSubfilesExist)) {
//                            System.out.println("existe");
//                            filePath = doesSubfilesExist.toString();
//
//                            dadosEvasaoService.carregarPlanilha(filePath);
//
//                            dadosEvasaoService.processarDados();
//                            dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);
//                            dadosEvasaoService.sendFileToS3(filePath);
//                        }
//                    }


        S3Client s3Client = new S3Provider().getS3Client();
        String bucketName = "s3-dataway-bucket";
        File flag = new File("/app/flag/flag.txt");

        // ⛔ Verifica se já foi executado
        if (flag.exists()) {
            logger.info("Dados já inseridos anteriormente. Encerrando.");
            return;
        }

        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();

        List<S3Object> objects = s3Client.listObjects(listObjects).contents();
        for (S3Object object : objects) {
            String key = object.key();
            logger.info("Baixando e processando o arquivo: {}", key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            InputStream objectContent = s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
            dadosEvasaoService.carregarPlanilha(objectContent, key);
            dadosEvasaoService.processarDados();
            dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), objectContent);
        }

        // ✅ Cria flag apenas após todo processamento
        Files.write(Paths.get("/app/flag/flag.txt"), "inserido".getBytes());


//                }

//                year ++;

//            }


    }
}
