package lt.pskurimas.ptvs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PtvsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PtvsApplication.class, args);
	}

}
