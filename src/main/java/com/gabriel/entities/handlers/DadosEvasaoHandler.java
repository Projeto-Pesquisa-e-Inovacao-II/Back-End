package com.gabriel.entities.handlers;

import com.gabriel.entities.DadosEvasao;
import com.gabriel.infra.ConexaoBanco;
import com.gabriel.services.LeitorPlanilha;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DadosEvasaoHandler extends LeitorPlanilha {
    List<DadosEvasao> dadosEvasaos = new ArrayList<>();

    @Override
    public void processarDados() {
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                System.out.println("cabecalho");
                continue;
            }

            try {
                Integer colLote = Integer.parseInt(row.getCell(0).getStringCellValue());
                Integer colPraca = Integer.parseInt(row.getCell(1).getStringCellValue());
                Integer colSentido = Integer.parseInt(row.getCell(2).getStringCellValue());
                Date colData = new SimpleDateFormat("dd/MM/yyyy").parse(row.getCell(3).getStringCellValue());
                Integer colHora = Integer.parseInt(row.getCell(4).getStringCellValue());
                Integer colTipo = Integer.parseInt(row.getCell(5).getStringCellValue());
                Integer colCategoria = Integer.parseInt(row.getCell(6).getStringCellValue());
                Integer colTipoPagamento = Integer.parseInt(row.getCell(7).getStringCellValue());
                Integer colTipoCampo = Integer.parseInt(row.getCell(8).getStringCellValue());
                Integer colQuantidade = Integer.parseInt(row.getCell(9).getStringCellValue());
                Double colValor = Double.parseDouble(row.getCell(10).getStringCellValue());

                DadosEvasao dadosEvasao = new DadosEvasao(colLote, colPraca, colSentido, colData, colHora, colTipo, colCategoria, colTipoPagamento, colTipoCampo, colQuantidade, colValor);
                dadosEvasaos.add(dadosEvasao);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }  catch (Exception rowException) {
                System.out.printf("Erro ao processar a linha: {}", row.getRowNum(), rowException);
            }

        }
    }

    public void inserirDadosEvasao(List<DadosEvasao> dadosEvasao) {
        System.out.println("iniciando novo dadosEvasao");
        String sql = """
        INSERT INTO DadosEvasao (lote, praca, sentido, dataEvasao, horas, tipo, categoria, tipoPagamento, tipoCampo, quantidade, valor) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                stmtInserir.setInt(6, d.getTipo());
                stmtInserir.setInt(7, d.getCategoria());
                stmtInserir.setInt(8, d.getTipoPagamento());
                stmtInserir.setInt(9, d.getTipoCampo());
                stmtInserir.setInt(10, d.getQuantidade());
                stmtInserir.setDouble(11, d.getValor());

                stmtInserir.executeUpdate();
                contador++;
            }

            System.out.println("Inserção concluída! Total: " + contador + " registros");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public List<DadosEvasao> getDadosEvasaos() {
        return dadosEvasaos;
    }
}
