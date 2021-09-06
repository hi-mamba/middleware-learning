package space.mamba.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import space.mamba.entity.Greetings;
import space.mamba.service.GreetingsService;

/**
 * @author pankui
 * @date 2021/9/6 
 * <pre>
 *
 * </pre>  
 */
@RestController
public class GreetingsController {
    private final GreetingsService greetingsService;

    public GreetingsController(GreetingsService greetingsService) {
        this.greetingsService = greetingsService;
    }

    @GetMapping("/greetings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void greetings(@RequestParam("message") String message) {
        Greetings greetings = Greetings.builder().message(message).timestamp(System.currentTimeMillis()).build();
        greetingsService.sendGreeting(greetings);
    }
}
