package space.mamba.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author pankui
 * @date 2021/9/6 
 * <pre>
 *
 * </pre>  
 */

@Getter
@Setter
@ToString
@Builder
public class Greetings {
    private long timestamp;
    private String message;

    public Greetings(long timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public Greetings() {

    }
}
