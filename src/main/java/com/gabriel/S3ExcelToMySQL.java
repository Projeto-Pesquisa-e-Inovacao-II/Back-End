package com.gabriel;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
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

import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.ss.usermodel.DataFormatter;
import java.io.InputStream;
import java.sql.*;
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

        try (Connection conn = DriverManager.getConnection(jdbcURL, user, password)) {
            conn.setAutoCommit(false);

            String sql = """
                INSERT INTO DadosPracaPedagio 
                (praca, lote, data, hora, valor, sentido, tpCampo, quantidade, Categoria, Empresa_idEmpresa) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

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
                        StylesTable styles = xssfReader.getStylesTable();
                        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

                        int sheetIndex = 1;
                        while (iter.hasNext()) {
                            try (InputStream sheetStream = iter.next()) {
                                logger.info("Processando planilha #" + sheetIndex + " do arquivo " + object.key());
                                processSheet(styles, strings, sheetStream, ps);
                                sheetIndex++;
                            }
                        }

                        ps.executeBatch(); // Executa após processar todas as sheets
                        conn.commit();
                        logger.info("Inserção do arquivo concluída com sucesso: " + object.key());

                    } catch (Exception e) {
                        logger.error("Erro ao processar o arquivo: " + object.key(), e);
                        conn.rollback(); // Desfaz lote atual se necessário
                    }
                }
            }
        }

        logger.info("Todos os dados foram processados com sucesso.");
    }

    private static void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream, PreparedStatement ps) throws Exception {
        Logger logger = LoggerFactory.getLogger(S3ExcelToMySQL.class);
        logger.info("Entrou no método processSheet");

        XMLReader sheetParser = XMLHelper.newXMLReader();

        SheetContentsHandler handler = new SheetContentsHandler() {
            List<String> rowValues = new ArrayList<>();

            @Override
            public void startRow(int rowNum) {
                rowValues.clear();
            }

            @Override
            public void endRow(int rowNum) {
                try {
                    // Mapeamento com tratamento de tipo
                    ps.setString(1, getValue(rowValues, 0)); // praca
                    ps.setString(2, getValue(rowValues, 1)); // lote

                    // data
                    String dateStr = getValue(rowValues, 2);
                    if (dateStr != null && !dateStr.isEmpty()) {
                        ps.setDate(3, Date.valueOf(dateStr)); // yyyy-MM-dd
                    } else {
                        ps.setNull(3, Types.DATE);
                    }

                    // hora
                    String timeStr = getValue(rowValues, 3);
                    if (timeStr != null && !timeStr.isEmpty()) {
                        ps.setTime(4, Time.valueOf(timeStr)); // HH:mm:ss
                    } else {
                        ps.setNull(4, Types.TIME);
                    }

                    // valor
                    String valorStr = getValue(rowValues, 4);
                    if (valorStr != null && !valorStr.isEmpty()) {
                        ps.setBigDecimal(5, new java.math.BigDecimal(valorStr.replace(",", ".")));
                    } else {
                        ps.setNull(5, Types.DECIMAL);
                    }

                    ps.setString(6, getValue(rowValues, 5)); // sentido

                    // tpCampo
                    String tpCampoStr = getValue(rowValues, 6);
                    if (tpCampoStr != null && !tpCampoStr.isEmpty()) {
                        ps.setInt(7, Integer.parseInt(tpCampoStr));
                    } else {
                        ps.setNull(7, Types.INTEGER);
                    }

                    // quantidade
                    String qtdStr = getValue(rowValues, 7);
                    if (qtdStr != null && !qtdStr.isEmpty()) {
                        ps.setInt(8, Integer.parseInt(qtdStr));
                    } else {
                        ps.setNull(8, Types.INTEGER);
                    }

                    ps.setString(9, getValue(rowValues, 8)); // Categoria

                    // Empresa_idEmpresa
                    String empStr = getValue(rowValues, 9);
                    if (empStr != null && !empStr.isEmpty()) {
                        ps.setInt(10, Integer.parseInt(empStr));
                    } else {
                        ps.setNull(10, Types.INTEGER);
                    }

                    ps.addBatch();
                } catch (Exception e) {
                    logger.error("Erro ao adicionar linha ao batch", e);
                }
            }

            @Override
            public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                rowValues.add(formattedValue);
            }

            @Override
            public void headerFooter(String text, boolean isHeader, String tagName) {
                // Intencionalmente vazio
            }

            private String getValue(List<String> list, int index) {
                return index < list.size() ? list.get(index) : null;
            }
        };

        DataFormatter formatter = new DataFormatter();
        XSSFSheetXMLHandler sheetHandler = new XSSFSheetXMLHandler(styles, null, strings, handler, formatter, false);

        sheetParser.setContentHandler(sheetHandler);
        sheetParser.parse(new InputSource(sheetInputStream));
    }
}
