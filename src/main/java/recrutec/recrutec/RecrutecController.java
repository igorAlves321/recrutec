package recrutec.recrutec;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecrutecController {

    @GetMapping("/teste")
    public String testarAplicacao() {
        return "olá, isso é um teste";
    }
}
