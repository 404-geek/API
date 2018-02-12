package com.aptus.blackbox;

import java.io.IOException;
import com.aptus.blackbox.index.Parser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlackBoxReloadedApp {

	public static void main(String[] args)throws IOException {
		SpringApplication.run(BlackBoxReloadedApp.class, args);
	}
}
