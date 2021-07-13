package space.mamba.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author pankui
 * @date 2021/7/12 
 * <pre>
 *
 * </pre>  
 */
@Getter
@Setter
@ToString
public class Teacher implements Serializable {

    private Long id;

    private String teacherName;
}
