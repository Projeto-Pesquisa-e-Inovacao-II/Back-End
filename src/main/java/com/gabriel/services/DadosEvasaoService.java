package com.gabriel.services;

import com.gabriel.entities.DadosEvasao;
import com.gabriel.infra.ConexaoBanco;
import com.gabriel.infra.LeitorPlanilha;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DadosEvasaoService extends LeitorPlanilha {
    private static final Logger logger = LoggerFactory.getLogger(DadosEvasaoService.class);
    List<DadosEvasao> dadosEvasaos = new ArrayList<>();

    @Override
    public void processarDados() {
        logger.info("Iniciando processamento de dados de evasão");
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
                Integer colPraca =  Integer.parseInt(formatter.formatCellValue(row.getCell(1)));
                Integer colSentido =  Integer.parseInt(formatter.formatCellValue(row.getCell(2)));

                String dateStr = formatter.formatCellValue(row.getCell(3)).replace('-', '/');
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date colData = sdf.parse(dateStr);

                Integer colHora =  Integer.parseInt(formatter.formatCellValue(row.getCell(4)));
                Integer colCategoria =  Integer.parseInt(formatter.formatCellValue(row.getCell(6)));
                Integer colTipoCampo =  Integer.parseInt(formatter.formatCellValue(row.getCell(8)));
                Integer colQuantidade =  Integer.parseInt(formatter.formatCellValue(row.getCell(9)));
                Double colValor = Double.parseDouble(formatter.formatCellValue(row.getCell(10)).replace(',', '.'));

                DadosEvasao dadosEvasao = new DadosEvasao(colLote, colPraca, colSentido, colData, colHora, colCategoria, colTipoCampo, colQuantidade, colValor);
                dadosEvasaos.add(dadosEvasao);
                linhasProcessadas++;
                logger.trace("Linha {} processada: {}", row.getRowNum(), dadosEvasao);
            }  catch (Exception e) {
                logger.error("Erro ao processar linha {} – ignorando (motivo: {})", row.getRowNum(), e.getMessage());
            }

        }
        logger.info("Processamento concluído: {} linhas válidas",
                linhasProcessadas);

    }

    public void inserirDadosEvasao(List<DadosEvasao> dadosEvasao, Integer concessionaria, String arquivo) {
        logger.info("Iniciando inserção de {} registros no banco (arquivo: {})", dadosEvasao.size(), arquivo);
        String sql = """
        INSERT INTO DadosPracaPedagio (lote, praca, sentido, data, hora, categoria, tpCampo, quantidade, valor, Empresa_idEmpresa) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection con = ConexaoBanco.getConnection();
             PreparedStatement stmtInserir = con.prepareStatement(sql)) {
            Integer contador = 0;

            for (DadosEvasao d : dadosEvasao) {
                stmtInserir.setInt(1, d.getLote());
                stmtInserir.setInt(2, d.getPraca());
                stmtInserir.setInt(3, d.getSentido());
                stmtInserir.setDate(4, new java.sql.Date(d.getDataEvasao().getTime()));
                stmtInserir.setInt(5, d.getHoras());
                stmtInserir.setInt(6, d.getCategoria());
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

            logger.info("Inserção concluída com sucesso! Total de registros inseridos: {}", contador);
            dadosEvasaos.clear();

        } catch (SQLException e) {
            logger.error("Erro ao inserir dados de evasão no banco", e);
            throw new RuntimeException(e);
        }
    }



    public List<DadosEvasao> getDadosEvasaos() {
        return dadosEvasaos;
    }
}
