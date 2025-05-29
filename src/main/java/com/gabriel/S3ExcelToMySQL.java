package com.gabriel;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.model.StylesTable;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class S3ExcelToMySQL {

    public static void main(String[] args) throws Exception {

        Logger logger = LoggerFactory.getLogger(S3ExcelToMySQL.class);

        String bucketName = "s3-dataway-bucket";

        String jdbcURL = "jdbc:mysql://dataway-mysql:3306/dataway?allowPublicKeyRetrieval=true&useSSL=false&rewriteBatchedStatements=true";
        String user = "root";
        String password = "urubu100";

        S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        List<S3Object> objects = s3.listObjectsV2(listRequest).contents();

        Connection conn = DriverManager.getConnection(jdbcURL, user, password);
        conn.setAutoCommit(false);

        String sql = """
            INSERT INTO DadosPracaPedagio (praca, lote, data, hora, valor, sentido, tpCampo, quantidade, Categoria, Empresa_idEmpresa) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        PreparedStatement ps = conn.prepareStatement(sql);

        for (S3Object object : objects) {
            if (!object.key().endsWith(".xlsx")) continue;

            logger.info("Iniciando processamento do arquivo: " + object.key());

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(object.key())
                    .build();

            IOUtils.setByteArrayMaxOverride(400_000_000);

            try (InputStream s3InputStream = s3.getObject(getObjectRequest);
                 OPCPackage opcPackage = OPCPackage.open(s3InputStream)) {

                ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
                XSSFReader xssfReader = new XSSFReader(opcPackage);
                XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

                int sheetIndex = 1;
                while (iter.hasNext()) {
                    try (InputStream sheetStream = iter.next()) {
                        logger.info("Processando planilha #" + sheetIndex + " do arquivo " + object.key());
                        processSheet(strings, sheetStream, ps);
                        ps.executeBatch();
                        conn.commit();
                        logger.info("Inserção finalizada com sucesso.");
                        sheetIndex++;
                    }
                }

            } catch (Exception e) {
                logger.error("Erro ao processar arquivo: " + object.key(), e);
            }
        }
        ps.close();
        conn.close();
        logger.info("Todos os dados foram transferidos com sucesso.");
    }

    private static void processSheet(ReadOnlySharedStringsTable strings, InputStream sheetInputStream, PreparedStatement ps) throws Exception {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();

        SheetContentsHandler handler = new SheetContentsHandler() {
            List<String> rowValues = new ArrayList<>();

            @Override
            public void startRow(int rowNum) {
                rowValues.clear();
            }

            @Override
            public void endRow(int rowNum) {
                Logger logger = LoggerFactory.getLogger(S3ExcelToMySQL.class);

                try {
                    for (int i = 0; i <= 10; i++) {
                        String val = i < rowValues.size() ? rowValues.get(i) : null;
                        ps.setString(i + 1, val);
                    }
                    logger.info("Linha processada: " + rowValues);
                    ps.addBatch();
                } catch (Exception e) {
                    logger.error("Erro ao adicionar batch");
                    e.printStackTrace();
                }
            }

            @Override
            public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                rowValues.add(formattedValue);
            }

            @Override
            public void headerFooter(String text, boolean isHeader, String tagName) {}
        };


        XSSFSheetXMLHandler sheetHandler = new XSSFSheetXMLHandler(
                null, strings, handler, false);

        sheetParser.setContentHandler(sheetHandler);
        sheetParser.parse(new InputSource(sheetInputStream));
    }
}
