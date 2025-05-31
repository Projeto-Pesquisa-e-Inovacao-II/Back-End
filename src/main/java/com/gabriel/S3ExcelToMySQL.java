package com.gabriel;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

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
                INSERT INTO DadosPracaPedagio (
                    praca, lote, data, hora, valor, sentido, tpCampo, quantidade, Categoria, Empresa_idEmpresa
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                                processSheet(styles, strings, sheetStream, ps, logger);
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

    private static void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream, PreparedStatement ps, Logger logger) throws Exception {
        XMLReader sheetParser = XMLHelper.newXMLReader();

        SheetContentsHandler handler = new SheetContentsHandler() {
            List<String> rowValues = new ArrayList<>();

            @Override
            public void startRow(int rowNum) {
                rowValues.clear();
            }
            
            @Override
            public void endRow(int rowNum) {
                if (rowNum == 0) return;

                try {
                    ps.setString(1, getOrNull(rowValues, 0)); // praca
                    ps.setString(2, getOrNull(rowValues, 1)); // lote

                    // Data
                    try {
                        String dataStr = getOrNull(rowValues, 2);
                        ps.setDate(3, (dataStr != null && !dataStr.isEmpty()) ? Date.valueOf(dataStr) : null);
                    } catch (Exception e) {
                        logger.error("Data inválida: " + getOrNull(rowValues, 2));
                        ps.setNull(3, Types.DATE);
                    }

                    // Hora
                    try {
                        String horaStr = getOrNull(rowValues, 3);
                        ps.setTime(4, (horaStr != null && !horaStr.isEmpty()) ? Time.valueOf(horaStr) : null);
                    } catch (Exception e) {
                        logger.error("Hora inválida: " + getOrNull(rowValues, 3));
                        ps.setNull(4, Types.TIME);
                    }

                    // Valor
                    try {
                        String valorStr = getOrNull(rowValues, 4);
                        ps.setDouble(5, (valorStr != null && !valorStr.isEmpty()) ? Double.parseDouble(valorStr) : 0.0);
                    } catch (Exception e) {
                        logger.error("Valor inválido: " + getOrNull(rowValues, 4));
                        ps.setDouble(5, 0.0);
                    }

                    ps.setString(6, getOrNull(rowValues, 5)); // sentido

                    // tpCampo
                    try {
                        String tpCampoStr = getOrNull(rowValues, 6);
                        ps.setInt(7, (tpCampoStr != null && !tpCampoStr.isEmpty()) ? Integer.parseInt(tpCampoStr) : 0);
                    } catch (Exception e) {
                        logger.error("tpCampo inválido: " + getOrNull(rowValues, 6));
                        ps.setInt(7, 0);
                    }

                    // quantidade
                    try {
                        String qtdStr = getOrNull(rowValues, 7);
                        ps.setInt(8, (qtdStr != null && !qtdStr.isEmpty()) ? Integer.parseInt(qtdStr) : 0);
                    } catch (Exception e) {
                        logger.error("quantidade inválida: " + getOrNull(rowValues, 7));
                        ps.setInt(8, 0);
                    }

                    ps.setString(9, getOrNull(rowValues, 8)); // Categoria

                    // Empresa_idEmpresa
                    try {
                        String empresaStr = getOrNull(rowValues, 9);
                        ps.setInt(10, (empresaStr != null && !empresaStr.isEmpty()) ? Integer.parseInt(empresaStr) : 0);
                    } catch (Exception e) {
                        logger.error("Empresa_idEmpresa inválido: " + getOrNull(rowValues, 9));
                        ps.setInt(10, 0);
                    }

                    ps.addBatch();

                } catch (Exception e) {
                    logger.error("Erro ao adicionar linha ao batch. Conteúdo da linha: " + rowValues, e);
                }
            }


            @Override
            public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                rowValues.add(formattedValue != null ? formattedValue.trim() : null);
            }

            @Override
            public void headerFooter(String text, boolean isHeader, String tagName) {
                // Ignorar
            }
        };

        DataFormatter formatter = new DataFormatter();
        XSSFSheetXMLHandler sheetHandler = new XSSFSheetXMLHandler(styles, null, strings, handler, formatter, false);

        sheetParser.setContentHandler(sheetHandler);
        sheetParser.parse(new InputSource(sheetInputStream));
    }

    private static String getOrNull(List<String> values, int index) {
        return index < values.size() ? values.get(index) : null;
    }
}
