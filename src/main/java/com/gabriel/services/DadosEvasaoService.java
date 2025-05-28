package com.gabriel.services;

import com.gabriel.entities.DadosEvasao;
import com.gabriel.exceptions.DadoInvalidoException;
import com.gabriel.infra.ConexaoBanco;
import com.gabriel.infra.LeitorPlanilha;
import com.gabriel.infra.S3Provider;
import com.gabriel.Main;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DadosEvasaoService extends LeitorPlanilha {
    List<DadosEvasao> dadosEvasaos = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private Integer concessionariaId;
    private String nomeArquivo;


    public String obterNomeConcessaoPorId(Integer categoria) {
        return switch (categoria) {
            case 1 -> "ECOVIAS";
            case 2 -> "ECOPISTAS";
            case 3 -> "ECOSUL";
            case 4 -> "ECO101";
            case 5 -> "ECOVALE";
            case 6 -> "ECOPORTO";
            case 7 -> "ECORODOVIAS";
            default -> "CONCESSAO_" + categoria;
        };
    }

    public String descricaoCategoria(Integer categoria) {
        return switch (categoria) {
            // modificar segundo tabela
            case 1 -> "Motocicleta";
            case 2 -> "Passeio";
            case 3 -> "Comercial";
            default -> "Categoria desconhecida";
        };
    }

    private String nomePraca(Integer praca) {
        return switch (praca) {
            case 15 -> "Praça Anchieta";
            case 16 -> "Praça Imigrantes";
            case 17 -> "Praça Ecovias";
            default -> "Praça " + praca;
        };


    }

    public void configurarContexto(Integer concessionariaId, String nomeArquivo) {
        this.concessionariaId = concessionariaId;
        this.nomeArquivo = nomeArquivo;
    }


    @Override
    public void processarDados() {
        MDC.put("status", "INICIANDO_PROCESSAMENTO");
        MDC.put("concessao", obterNomeConcessaoPorId(this.concessionariaId));
        MDC.put("planilha", this.nomeArquivo);

        logger.info("Iniciando processamento de dados de evasão");
        salvarLogNoBanco("INFO", "Iniciando processamento de dados de evasão");
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();
        Integer linhasProcessadas = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                logger.debug("Ignorando cabeçalho");
                continue;
            }

            try {
                Integer colLote = Integer.parseInt(formatter.formatCellValue(row.getCell(0)));
                Integer colPraca = Integer.parseInt(formatter.formatCellValue(row.getCell(1)));
                Integer colSentido = Integer.parseInt(formatter.formatCellValue(row.getCell(2)));

                String dateStr = formatter.formatCellValue(row.getCell(3)).replace('-', '/');
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date colData = sdf.parse(dateStr);

                Integer colHora = Integer.parseInt(formatter.formatCellValue(row.getCell(4)));
                Integer colCategoria = Integer.parseInt(formatter.formatCellValue(row.getCell(6)));
                String descCategoria = descricaoCategoria(colCategoria);
                Integer colTipoCampo = Integer.parseInt(formatter.formatCellValue(row.getCell(8)));
                Integer colQuantidade = Integer.parseInt(formatter.formatCellValue(row.getCell(9)));
                Double colValor = Double.parseDouble(formatter.formatCellValue(row.getCell(10)).replace(',', '.'));

                if (colQuantidade < 0) {
                    throw new DadoInvalidoException("Quantidade negativa: " + colQuantidade);
                }

                if (colTipoCampo != 1) {
                    throw new DadoInvalidoException("Tipo de campo diferente de evasão: " + colTipoCampo);
                }

                String nomePraca = nomePraca(colPraca);

                DadosEvasao dadosEvasao = new DadosEvasao(
                        colLote, colPraca, colSentido, colData,
                        colHora, colCategoria, colTipoCampo, colQuantidade, colValor, nomePraca, descCategoria
                );

                dadosEvasaos.add(dadosEvasao);
                linhasProcessadas++;
                logger.info("✔ Linha {} tratada com sucesso – {}", row.getRowNum(), nomePraca);
                salvarLogNoBanco("INFO", "Linha " + row.getRowNum() + " tratada com sucesso – " + nomePraca);


            } catch (Exception rowException) {
                MDC.put("status", "FALHA_PROCESSAMENTO");
                logger.error("Erro ao processar linha {}", row.getRowNum());
                salvarLogNoBanco("ERROR", "Erro ao processar linha " + row.getRowNum());
            }

        }

        MDC.put("status", "INSERIDO_COM_SUCESSO");
        logger.info("Inserção concluída com sucesso! Total de registros inseridos: {}", linhasProcessadas);
        salvarLogNoBanco("INFO", "Inserção concluída com sucesso! Total de registros inseridos: " + linhasProcessadas);

        // Enviar o log para o S3
        sendFileToS3("logs/app.log");

    }


    public void salvarLogNoBanco(String nivel, String mensagem) {
        int nivelId = switch (nivel.toUpperCase()) {
            case "DEBUG" -> 1;
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 0; // ou lança exceção
        };

        int statusId = switch (MDC.get("status")) {
            case "INICIANDO_PROCESSAMENTO" -> 1;
            case "INSERINDO_DADOS" -> 2;
            case "INSERIDO_COM_SUCESSO" -> 3;
            case "FALHA_PROCESSAMENTO" -> 4;
            case "FALHA_INSERCAO" -> 5;
            default -> 0;
        };

        String sql = """
                    INSERT INTO LogJava (descricao, dataEnvio, fkNivelLog, fkStatus)
                    VALUES (?, ?, ?, ?)
                """;

        try (Connection con = ConexaoBanco.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, mensagem);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, nivelId);
            stmt.setInt(4, statusId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Erro ao salvar log no banco", e);
        }
    }


    public void inserirDadosEvasao(List<DadosEvasao> dadosEvasao, Integer concessionaria, String arquivo) {


        MDC.put("status", "INSERINDO_DADOS");
        MDC.put("concessao", obterNomeConcessaoPorId(this.concessionariaId));
        MDC.put("planilha", new File(arquivo).getName());
        logger.info("Iniciando inserção de {} registros no banco (arquivo: {})", dadosEvasao.size(), arquivo);

        String sql = """
                    INSERT INTO DadosPracaPedagio (lote, praca, sentido, data, hora, categoria, tpCampo, quantidade, valor, Empresa_idEmpresa) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        System.out.println("Inserindo novos dados de " + arquivo);
        try (Connection con = ConexaoBanco.getConnection();
             PreparedStatement stmtInserir = con.prepareStatement(sql)) {
            Integer contador = 0;

            for (DadosEvasao d : dadosEvasao) {
                stmtInserir.setInt(1, d.getLote());
                stmtInserir.setInt(2, d.getPraca());
                stmtInserir.setInt(3, d.getSentido());
                stmtInserir.setDate(4, new java.sql.Date(d.getDataEvasao().getTime()));
                stmtInserir.setInt(5, d.getHoras());
                stmtInserir.setString(6, d.getDescricaoCategoria());
                stmtInserir.setInt(7, d.getTipoCampo());
                stmtInserir.setInt(8, d.getQuantidade());
                stmtInserir.setDouble(9, d.getValor());
                stmtInserir.setInt(10, concessionaria);


                stmtInserir.executeUpdate();
                contador++;

                if (contador % 1000 == 0) {
                    logger.debug("{} registros inseridos até agora…", contador);
                }
            }

            MDC.put("status", "INSERIDO_COM_SUCESSO");
            logger.info("Inserção concluída com sucesso! Total de registros inseridos: {}", contador);
            dadosEvasaos.clear();

        } catch (SQLException e) {
            MDC.put("status", "FALHA_INSERCAO");
            logger.error("Erro ao inserir dados de evasão no banco", e);
            salvarLogNoBanco("ERROR", "Erro ao inserir dados de evasão no banco: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public void sendFileToS3(String filePath) {
        S3Client s3Client = new S3Provider().getS3Client();
        String bucketName = "dados-dataway-dev";
        try {
            logger.info("Iniciando upload de arquivo para S3...");
            logger.debug("Nome do bucket: {}", bucketName);
            File file = new File(filePath);

            String fileName = file.getName();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            logger.info("Arquivo '" + fileName + "' enviado com sucesso com o nome: " + fileName);
        } catch (S3Exception e) {
            logger.error("Erro ao fazer upload do arquivo: " + e.getMessage());
        }


    }


    public List<DadosEvasao> getDadosEvasaos() {
        return dadosEvasaos;
    }


}
