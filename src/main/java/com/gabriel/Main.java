package com.gabriel;

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
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String FLAG_PATH = "/app/flag/flag.txt";
    private static final String BUCKET_NAME = "s3-dataway-bucket";

    public static void main(String[] args) {
        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();

        try {
            // Verifica se flag existe para evitar reprocessamento
            File flag = new File(FLAG_PATH);
            if (flag.exists()) {
                logger.info("Dados já inseridos anteriormente. Encerrando.");
                return;
            }

            S3Client s3Client = new S3Provider().getS3Client();

            logger.info("Listando arquivos no bucket S3: {}", BUCKET_NAME);
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                    .bucket(BUCKET_NAME)
                    .build();

            List<S3Object> objects = s3Client.listObjects(listObjectsRequest).contents();
            logger.info("Quantidade de arquivos encontrados: {}", objects.size());

            if (objects.isEmpty()) {
                logger.warn("Nenhum arquivo encontrado no bucket. Encerrando execução.");
                return;
            }

            for (S3Object s3Object : objects) {
                String key = s3Object.key();
                logger.info("Processando arquivo: {}", key);

                try (InputStream objectContent = s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(key)
                                .build(),
                        ResponseTransformer.toInputStream())) {

                    dadosEvasaoService.carregarPlanilha(objectContent, key);
                    dadosEvasaoService.processarDados();
                    dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), key);

                    logger.info("Arquivo {} processado com sucesso.", key);

                } catch (Exception e) {
                    logger.error("Erro ao processar arquivo {}: {}", key, e.getMessage(), e);
                    // Dependendo da lógica, pode continuar ou parar a execução.
                    // continue;
                }
            }

            // Cria flag para não reprocessar novamente
            Files.createDirectories(Paths.get("/app/flag"));
            Files.write(Paths.get(FLAG_PATH), "inserido".getBytes());
            logger.info("Flag criada para indicar processamento concluído.");

        } catch (IOException e) {
            logger.error("Erro de IO no processamento principal: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado: {}", e.getMessage(), e);
        }
    }
}
