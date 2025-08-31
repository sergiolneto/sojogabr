package com.br.sojogabr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SojogabrApplication {

	private static final Logger log = LoggerFactory.getLogger(SojogabrApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SojogabrApplication.class, args);
	}

	/**
	 * Este Bean é executado na inicialização e é útil para logar informações do ambiente.
	 */
	@Bean
	public CommandLineRunner commandLineRunner(Environment env) {
		return args -> {
			String appName = env.getProperty("spring.application.name", "SojogabrApplication");
			String port = env.getProperty("server.port", "8787");
			String contextPath = env.getProperty("server.servlet.context-path", "");
			String profiles = String.join(", ", env.getActiveProfiles());

			log.info("""
                            
                            ----------------------------------------------------------
                            \t\
                            Aplicação '{}' iniciada com sucesso!
                            \t\
                            Perfis ativos: {}
                            \t\
                            Acesso local: http://localhost:{}{}
                            \t\
                            Health Check: http://localhost:{}{}/actuator/health
                            ----------------------------------------------------------""",
					appName, profiles, port, contextPath, port, contextPath);
		};
	}
}
