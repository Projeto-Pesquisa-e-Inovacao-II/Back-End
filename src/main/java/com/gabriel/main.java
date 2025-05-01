package com.gabriel;

import com.gabriel.infra.S3Provider;
import com.gabriel.services.DadosEvasaoService;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class main {
    private static final Logger logger = LoggerFactory.getLogger(main.class);

    public static void main(String[] args) throws IOException {
        S3Client s3Client = new S3Provider().getS3Client();
        String bucketName = "dados-dataway-dev";
        String filePath = "D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\L27_02-2024.xlsx";

        try {
            logger.info("Iniciando upload de arquivo para S3...");
            logger.debug("Nome do bucket: {}", bucketName);
            String uniqueFileName = UUID.randomUUID().toString();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .build();

            File file = new File(filePath);
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            logger.info("Arquivo '" + file.getName() + "' enviado com sucesso com o nome: " + uniqueFileName);
        } catch (S3Exception e) {
            logger.error("Erro ao fazer upload do arquivo: " + e.getMessage());
        }


        logger.info("Iniciando aplicação...");

        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();


        logger.info("Carregando planilha: {}", filePath);
        dadosEvasaoService.carregarPlanilha(filePath);

        logger.info("Processando dados...");
        dadosEvasaoService.processarDados();

        logger.info("Inserindo dados de evasão...");
        dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), 3, filePath);


        logger.info("Processo concluído com sucesso.");


    }
}