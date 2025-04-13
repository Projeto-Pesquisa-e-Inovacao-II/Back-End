package com.gabriel.entities.handlers;

import com.gabriel.entities.DadosEvasao;
import com.gabriel.infra.ConexaoBanco;
import com.gabriel.services.LeitorPlanilha;
import org.apache.poi.ss.usermodel.DataFormatter;
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
        DataFormatter formatter = new DataFormatter();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                System.out.println("cabecalho");
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
                Integer colTipo =  Integer.parseInt(formatter.formatCellValue(row.getCell(5)));
                Integer colCategoria =  Integer.parseInt(formatter.formatCellValue(row.getCell(6)));
                Integer colTipoPagamento =  Integer.parseInt(formatter.formatCellValue(row.getCell(7)));
                Integer colTipoCampo =  Integer.parseInt(formatter.formatCellValue(row.getCell(8)));
                Integer colQuantidade =  Integer.parseInt(formatter.formatCellValue(row.getCell(9)));
                Double colValor = Double.parseDouble(formatter.formatCellValue(row.getCell(10)).replace(',', '.'));

                DadosEvasao dadosEvasao = new DadosEvasao(colLote, colPraca, colSentido, colData, colHora, colTipo, colCategoria, colTipoPagamento, colTipoCampo, colQuantidade, colValor);
                dadosEvasaos.add(dadosEvasao);
                System.out.println("dadosEvasao finalizou");
            }  catch (Exception rowException) {
                System.out.println("Erro ao processar a linha: " + row.getRowNum() + " | " + rowException);
            }

        }
    }

    public void inserirDadosEvasao(List<DadosEvasao> dadosEvasao) {
        System.out.println("iniciando novo dadosEvasao");
        String sql = """
        INSERT INTO DadosPracaPedagio (lote, praca, sentido, data, hora, tipo, categoria, tipoPagamento, tipoCampo, quantidade, valor) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        System.out.println("inserindo novo dadosEvasao");
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

                System.out.println("foi adicionado novo dadosEvasao" + contador);
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
