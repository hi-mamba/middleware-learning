package space.mamba.config;

import org.springframework.cloud.stream.annotation.EnableBinding;
import space.mamba.channel.GreetingsStreams;

/**
 * @author pankui
 * @date 2021/9/6 
 * <pre>
 *  使用传递接口的@EnableBinding注释来完成绑定到流GreetingsService（见下文)。
 * </pre>  
 */
@EnableBinding(GreetingsStreams.class)
public class StreamsConfig {}
