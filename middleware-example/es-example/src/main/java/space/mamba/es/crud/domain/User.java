package space.mamba.es.crud.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 *
 * @author pannkui
 *
 *  Elasticsearch字段名与Java实体类不一致 @Document解决
 */
@Document(indexName = "user")
@Getter
@Setter
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

}
