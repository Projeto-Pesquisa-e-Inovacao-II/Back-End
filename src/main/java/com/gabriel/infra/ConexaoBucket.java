package com.gabriel.infra;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;

public class ConexaoBucket {
    private static AmazonS3 s3Client;

    static {
        Dotenv dotenv = Dotenv.load();
        String accessKey = dotenv.get("AWS_ACCESS_KEY_ID");
        String secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY");
        String sessionToken = dotenv.get("AWS_SESSION_TOKEN");
        String region = dotenv.get("AWS_REGION");

        if (accessKey == null || secretKey == null || region == null || sessionToken == null) {
            throw new IllegalStateException("As variáveis de ambiente AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_DEFAULT_REGION e AWS_SESSION_TOKEN devem estar definidas.");
        }

        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(accessKey, secretKey, sessionToken);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withRegion(region)
                .build();
    }

    public static AmazonS3 getS3Client() {
        return s3Client;
    }

}
