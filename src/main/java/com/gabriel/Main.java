package com.gabriel;

import com.gabriel.enums.FilePath;
import com.gabriel.infra.ConexaoBanco;
import com.gabriel.services.DadosEvasaoService;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) throws IOException {

        // Testar conexão logo no início
        try (Connection conn = ConexaoBanco.getConnection()) {
            if (conn == null) {
                System.err.println("❌ Falha ao conectar ao banco. Encerrando o programa.");
                return;
            } else {
                System.out.println("✅ Banco conectado com sucesso!");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao tentar conectar no banco: " + e.getMessage());
            return;
        }

        DadosEvasaoService dadosEvasaoService = new DadosEvasaoService();
        Integer year = 2024;
        String base = FilePath.FILE_PATH.getFilePath();

        for (int conc = 1; conc < 33; conc++) {
            for (int j = 1; j <= 12; j++) {
                Path doesSubfilesExist;

                if (conc < 10) {
                    if (j < 10) {
                        doesSubfilesExist = Path.of(base + "L0" + conc + "_0" + j + "-" + year + ".xlsx");
                    } else {
                        doesSubfilesExist = Path.of(base + "L0" + conc + "_" + j + "-" + year + ".xlsx");
                    }
                } else {
                    doesSubfilesExist = Path.of(base + "L" + conc + "_0" + j + "-" + year + ".xlsx");
                }

                if (Files.exists(doesSubfilesExist)) {
                    String filePath = doesSubfilesExist.toString();
                    String nomePlanilha = doesSubfilesExist.getFileName().toString();
                    String nomeConcessao = dadosEvasaoService.obterNomeConcessaoPorId(conc);

                    MDC.put("concessao", nomeConcessao);
                    MDC.put("planilha", nomePlanilha);
                    MDC.put("status", "INICIANDO_PROCESSAMENTO");

                    dadosEvasaoService.carregarPlanilha(filePath);
                    dadosEvasaoService.configurarContexto(conc, nomePlanilha);
                    dadosEvasaoService.processarDados();
                    dadosEvasaoService.inserirDadosEvasao(dadosEvasaoService.getDadosEvasaos(), conc, filePath);
                    dadosEvasaoService.sendFileToS3(filePath);

                    MDC.clear();
                }
            }
        }
    }
}
