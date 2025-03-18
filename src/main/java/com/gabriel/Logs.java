package com.gabriel;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class Logs {
    public static void main(String[] args) {
        //delay
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger counter = new AtomicInteger(1);
        String[] notficacoes = {
                "INFO - Usuário 'fecaramico' iniciou sessão na aplicação.",
                "INFO - Carregamento da Dashboard: Interface renderizada com sucesso.",
                "INFO - Requisição HTTP iniciada: GET https://api.meuservidoraws.com/api/dashboard-data?user=fecaramico",
                "INFO - Resposta HTTP recebida: 200 OK - Dados gerais da dashboard.",
                "INFO - Requisição HTTP iniciada: GET https://api.meuservidoraws.com/api/charts-data?user=fecaramico",
                "INFO - Resposta HTTP recebida: 200 OK - Dados dos gráficos.",
                "INFO - Requisição HTTP iniciada: GET https://api.meuservidoraws.com/api/kpis?user=fecaramico",
                "INFO - Resposta HTTP recebida: 200 OK - Dados dos KPIs.",
                "INFO - Atualização da Dashboard: Gráficos e KPIs atualizados com os dados recebidos.",
                "INFO - Ação do usuário: Clique em 'Atualizar Dados' na Dashboard.",
                "INFO - Requisição HTTP iniciada: GET https://api.meuservidoraws.com/api/dashboard-data?user=fecaramico",
                "INFO - Resposta HTTP recebida: 200 OK - Dados atualizados da dashboard.",
                "INFO - Requisição HTTP iniciada: GET https://api.meuservidoraws.com/api/charts-data?user=fecaramico",
                "INFO - Resposta HTTP recebida: 200 OK - Dados atualizados dos gráficos.",
                "INFO - Requisição HTTP iniciada: GET https://api.meuservidoraws.com/api/kpis?user=fecaramico",
                "INFO - Resposta HTTP recebida: 200 OK - Dados atualizados dos KPIs.",
                "INFO - Atualização da Dashboard: Dados atualizados exibidos com sucesso.",
                "INFO - Encerramento da sessão: Usuário 'fecaramico' saiu da aplicação."
        };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Runnable logTask = () -> {
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(formatter);

            int index = counter.get() - 1;
            System.out.println("[" + formattedTime + "] - " + notficacoes[index]);

            if (counter.incrementAndGet() > notficacoes.length) {
                scheduler.shutdown();
            }
        };

        //definindo tempo de delay
        scheduler.scheduleAtFixedRate(logTask, 0, 3, TimeUnit.SECONDS);
    }
}