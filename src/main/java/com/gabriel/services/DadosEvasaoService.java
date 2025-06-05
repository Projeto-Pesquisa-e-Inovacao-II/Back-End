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


    public String obterNomeConcessaoPorId(Integer lote) {
        return switch (lote) {
            case 1 -> "AUTOBAN";

            case 3 -> "TEBE";

            case 6 -> "INTERVIAS";

            case 7 -> "ROTA";

            case 9 -> "TRIANGULO";

            case 11 -> "RENOVIAS";

            case 12 -> "VIA OESTE";

            case 13 -> "COLINAS";

            case 16 -> "CART";

            case 19 -> "VIA RONDOM";

            case 20 -> "SPVIAS";

            case 21 -> "TIETE";

            case 22 -> "ECOVIAS";

            case 23 -> "ECOPISTAS";

            case 24 -> "RODOANEL";

            case 25 -> "SPMAR";

            case 27 -> "TAMOIOS";

            case 28 -> "ENTREVIAS";

            case 29 -> "VIA PAULISTA";

            case 30 -> "EIXO";

            case 31 -> "ECONOROESTE";


            default -> "CONCESSAO_" + lote;
        };
    }

    public String descricaoCategoria(Integer categoria) {
        return switch (categoria) {
            // modificar segundo tabela
            case 1 -> "2 eixos";
            case 2 -> "2 eixos";
            case 3 -> "3 eixos";
            case 4 -> "4 eixos";
            case 5 -> "5 eixos";
            case 6 -> "6 eixos";
            case 7 -> "3 eixos";
            case 8 -> "4 eixos";
            case 9 -> "2 eixos";
            case 61 -> "7 eixos";
            case 62 -> "8 eixos";
            case 63 -> "9 eixos";
            case 64 -> "10 eixos";
            case 65 -> "11 eixos";
            case 66 -> "12 eixos";
            case 67 -> "13 eixos";
            case 68 -> "14 eixos";
            case 69 -> "15 eixos";
            case 16 -> "16 eixos";
            case 17 -> "17 eixos";
            case 18 -> "18 eixos";
            default -> "Categoria desconhecida";
        };
    }

    private String nomePraca(Integer praca) {
        return switch (praca) {
            case 2 -> "Caieiras";
            case 3 -> "Campo Limpo";
            case 4 -> "Itupeva Norte";
            case 87 -> "Itupeva Sul";
            case 8 -> "Limeira A (330) Norte";
            case 88 -> "Limeira A (330) Sul";
            case 9 -> "Limeira B (348) Norte";
            case 89 -> "Limeira B (348) Sul";
            case 7 -> "Nova Odessa Norte";
            case 90 -> "Nova Odessa Sul";
            case 1 -> "Perus Norte";
            case 91 -> "Perus Sul";
            case 92 -> "Sumaré Sul";
            case 6 -> "Sumaré Norte";
            case 5 -> "Valinhos Norte";
            case 85 -> "Valinhos Sul";
            case 63 -> "Colina";
            case 61 -> "Monte Alto";
            case 62 -> "Pirangi";
            case 30 -> "Araras";
            case 34 -> "Descalvado";
            case 28 -> "Eng. Coelho";
            case 27 -> "Iracemápolis";
            case 32 -> "Leme";
            case 29 -> "Mogi Mirim";
            case 33 -> "Pirassununga";
            case 31 -> "Rio Claro";
            case 35 -> "Sta Cruz das Palmeiras";
            case 125 -> "Atibaia";
            case 130 -> "Eng. Coelho";
            case 126 -> "Igaratá Norte";
            case 127 -> "Igaratá Sul";
            case 124 -> "Itatiba";
            case 131 -> "Jundiaí";
            case 132 -> "Louveira";
            case 128 -> "Paulínia A";
            case 129 -> "Paulínia B";
            case 167 -> "Pórtico Cosmópolis";
            case 165 -> "Pórtico Km 74";
            case 166 -> "Pórtico Paulínia Jd. Betel";
            case 65 -> "Agulha";
            case 64 -> "Araraquara";
            case 66 -> "Catiguá";
            case 67 -> "Dobrada";
            case 69 -> "Itápolis";
            case 70 -> "Jaboticabal";
            case 68 -> "Taiúva";
            case 39 -> "Aguaí";
            case 41 -> "Águas da Prata";
            case 42 -> "Casa Branca";
            case 38 -> "Estiva Gerbi";
            case 43 -> "Itobi";
            case 36 -> "Jaguariúna";
            case 44 -> "Mococa";
            case 37 -> "Pinhal";
            case 154 -> "Pórtico Sto. Ant. de Posse";
            case 40 -> "São João da Boa Vista";
            case 81 -> "Alumínio";
            case 80 -> "Araçoiaba Leste";
            case 141 -> "Araçoiaba Oeste";
            case 76 -> "Barueri";
            case 77 -> "Itapevi";
            case 78 -> "Itu";
            case 75 -> "Osasco";
            case 82 -> "São Roque";
            case 79 -> "Sorocaba";
            case 83 -> "Bloqueio de Boituva";
            case 84 -> "Bloqueio de Indaiatuba";
            case 48 -> "Boituva";
            case 46 -> "Indaiatuba";
            case 45 -> "Itupeva";
            case 163 -> "Pórtico Aeroporto";
            case 164 -> "Pórtico Campina";
            case 158 -> "Pórtico Itu - 1";
            case 159 -> "Pórtico Itu - 2";
            case 160 -> "Pórtico Salto - 1";
            case 161 -> "Pórtico Salto - 2";
            case 162 -> "Pórtico Salto - 3";
            case 47 -> "Porto Feliz";
            case 50 -> "Rio Claro";
            case 49 -> "Rio das Pedras";
            case 111 -> "Assis";
            case 113 -> "Caiuá";
            case 109 -> "Ourinhos";
            case 110 -> "Palmital";
            case 108 -> "Piratininga";
            case 112 -> "Pres. Bernardes";
            case 114 -> "Rancharia";
            case 107 -> "Regente Feijó";
            case 106 -> "Sta. Cruz R. Pardo";
            case 133 -> "Avaí";
            case 140 -> "Castilho";
            case 136 -> "Glicério";
            case 139 -> "Guaraçaí";
            case 138 -> "Lavínia";
            case 134 -> "Pirajuí";
            case 135 -> "Promissão";
            case 137 -> "Rubiácea";
            case 54 -> "Alambari";
            case 59 -> "Avaré";
            case 52 -> "Buri";
            case 53 -> "Gramadão";
            case 60 -> "Iaras";
            case 51 -> "Itararé";
            case 58 -> "Itatinga";
            case 56 -> "Morro I (Norte)";
            case 55 -> "Morro II (Sul)";
            case 57 -> "Quadra";
            case 118 -> "Anhembi";
            case 120 -> "Areiopolis";
            case 119 -> "Botucatu";
            case 117 -> "Conchas";
            case 122 -> "Elias Fausto";
            case 121 -> "Lençóis Paulista";
            case 115 -> "Monte Mor";
            case 116 -> "Rafard";
            case 123 -> "Rio das Pedras";
            case 19 -> "Batistini";
            case 21 -> "Diadema";
            case 20 -> "Eldorado";
            case 18 -> "Piratininga";
            case 17 -> "Riacho Grande";
            case 15 -> "Santos";
            case 16 -> "São Vicente";
            case 145 -> "Caçapava";
            case 143 -> "Guararema";
            case 142 -> "Itaquaquecetuba";
            case 144 -> "São José dos Campos";
            case 97 -> "Anhanguera Externa";
            case 98 -> "Anhanguera Interna Norte";
            case 99 -> "Anhanguera Interna Sul";
            case 95 -> "Bandeirantes Externa";
            case 96 -> "Bandeirantes Interna";
            case 100 -> "Castello Branco Externa";
            case 93 -> "Castello Branco Interna";
            case 101 -> "Padroeira Externa";
            case 102 -> "Padroeira Interna";
            case 94 -> "Raimundo Magalhães";
            case 103 -> "Raposo Tavares Externa";
            case 104 -> "Raposo Tavares Interna";
            case 105 -> "Regis Bittencourt";
            case 152 -> "Anchieta";
            case 157 -> "Ayrton Senna";
            case 168 -> "Dutra";
            case 151 -> "Imigrantes";
            case 150 -> "Imigrantes - Capital";
            case 149 -> "Imigrantes - Litoral";
            case 155 -> "P6E";
            case 156 -> "P6I";
            case 148 -> "Parelheiros";
            case 170 -> "Ribeirão Píres - A";
            case 169 -> "Ribeirão Píres - B";
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
                Integer colTipoCampo = Integer.parseInt(formatter.formatCellValue(row.getCell(8)));
                Integer colQuantidade = Integer.parseInt(formatter.formatCellValue(row.getCell(9)));
                Double colValor = Double.parseDouble(formatter.formatCellValue(row.getCell(10)).replace(',', '.'));

                if (colQuantidade < 0) {
                    throw new DadoInvalidoException("Quantidade negativa: " + colQuantidade);
                }


                String nomePraca = nomePraca(colPraca);

                String descricaoCategoria = descricaoCategoria(colCategoria);

                String obterNomeConcessaoPorId = obterNomeConcessaoPorId(colLote);


                DadosEvasao dadosEvasao = new DadosEvasao(
                        colLote, colPraca, colSentido, colData,
                        colHora, colTipoCampo, colQuantidade, colValor, nomePraca, descricaoCategoria, obterNomeConcessaoPorId
                );

                dadosEvasaos.add(dadosEvasao);
                linhasProcessadas++;
                logger.info("✔ Linha {} tratada com sucesso – {}", row.getRowNum(), nomePraca);

                if (linhasProcessadas % 1000 == 0) {
                    salvarLogNoBanco("INFO", "Processadas " + linhasProcessadas + " linhas até agora com sucesso.");
                }


            } catch (Exception rowException) {
                MDC.put("status", "FALHA_PROCESSAMENTO");
                logger.error("Erro ao processar linha {}: {}", row.getRowNum(), rowException.getMessage(), rowException);
                salvarLogNoBanco("ERROR", "Erro ao processar linha " + row.getRowNum() + ": " + rowException.getMessage());
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
                stmtInserir.setString(1, d.getNomeConcessao()); // concessao por nome
                stmtInserir.setString(2, d.getNomePraca());// praca por nome
                stmtInserir.setInt(3, d.getSentido());
                stmtInserir.setDate(4, new java.sql.Date(d.getDataEvasao().getTime()));
                stmtInserir.setInt(5, d.getHoras());
                stmtInserir.setString(6, d.getDescricaoCategoria()); // categoria por nome
                stmtInserir.setInt(7, d.getTipoCampo());
                stmtInserir.setInt(8, d.getQuantidade());
                stmtInserir.setDouble(9, d.getValor());
                stmtInserir.setInt(10, 1);  //dps alterar para "concessionaria"


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
