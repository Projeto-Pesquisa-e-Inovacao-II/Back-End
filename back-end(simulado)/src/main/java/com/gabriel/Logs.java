package com.gabriel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class Logs {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger counter = new AtomicInteger(1);
        String[] notficacoes = {
                "Notificação 1",
                "Notificação 2",
                "Notificação 3",
                "Notificação 4",
                "Notificação 5",
                "Notificação 6",
                "Notificação 7",
                "Notificação 8",
                "Notificação 9",
                "Notificação 10",
        };

        Runnable logTask = () -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            int index = counter.get() - 1;
            System.out.println("Notificação " + timestamp + " - " + notficacoes[index]);

            if (counter.incrementAndGet() > notficacoes.length) {
                scheduler.shutdown();
            }
        };

        scheduler.scheduleAtFixedRate(logTask, 0, 3, TimeUnit.SECONDS);
    }
}