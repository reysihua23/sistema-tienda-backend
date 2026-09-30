package com.reydi.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling   // ✅ habilita @Scheduled

public class TiendaApplication {

	public static void main(String[] args) {SpringApplication.run(TiendaApplication.class, args);}
}
