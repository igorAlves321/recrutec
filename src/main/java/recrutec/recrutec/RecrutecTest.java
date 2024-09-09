package recrutec.recrutec;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecrutecTest {
	
	@GetMapping("/testando")
	public String Testando(){
		return "olá, isso é um teste";
	}
}
