package com.gabriel;

import com.gabriel.entities.DadosEvasao;
import com.gabriel.entities.handlers.DadosEvasaoHandler;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class main {
    public static void main(String[] args) {
        String NOME_BUCKET = System.getenv("NOME_BUCKET");

        DadosEvasaoHandler dadosEvasaoHandler = new DadosEvasaoHandler();
        dadosEvasaoHandler.carregarPlanilha("/tmp/L23_01-2025(1).xlsx");
        dadosEvasaoHandler.processarDados();

        dadosEvasaoHandler.inserirDadosEvasao(dadosEvasaoHandler.getDadosEvasaos());

    }
}