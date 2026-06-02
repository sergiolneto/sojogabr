package com.br.sojogabr;

import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableCaching
@EnableAsync
@RegisterReflectionForBinding({User.class, CampeonatoItem.class})
public class SojogabrApplication {

	public static void main(String[] args) {
		// Desabilita o uso de LambdaMetafactory pelo SDK da AWS para compatibilidade com GraalVM
		System.setProperty("software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema.useUnderlyingInternalMethod", "true");
		SpringApplication.run(SojogabrApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
