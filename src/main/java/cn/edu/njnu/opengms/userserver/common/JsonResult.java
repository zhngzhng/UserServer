package cn.edu.njnu.opengms.userserver.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName JsonResult
 * @Description 响应体
 * @Author zhngzhng
 * @Date 2021/3/24
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Response Body")
public class JsonResult<T> {
    @ApiModelProperty(value = "response code")
    private Integer code;

    @ApiModelProperty(value = "response message")
    private String msg;

    @ApiModelProperty(value = "response payload")
    private T data;
}
