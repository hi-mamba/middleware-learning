package space.mamba.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author pankui
 */
@ApiModel(value = "space-mamba-Student")
@Getter
@Setter
@ToString
@TableName(value = "student")
public class Student {
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    private Integer id;

    @TableField(value = "`name`")
    @ApiModelProperty(value = "")
    private String name;

    public static final String COL_ID = "id";

    public static final String COL_NAME = "name";
}