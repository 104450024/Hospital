package com.atguigu.yygh.model.hosp;

import com.atguigu.yygh.model.base.BaseEntity;
import com.atguigu.yygh.model.base.BaseMongoEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * <p>
 * Department
 * </p>
 *
 * @author qy
 */
@Data
@ApiModel(description = "Department")
public class Department extends BaseMongoEntity {
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "医院编号")
	private String hoscode;

	@ApiModelProperty(value = "科室编号")
	private String depcode;

	@ApiModelProperty(value = "科室名称")
	private String depname;

	@ApiModelProperty(value = "科室描述")
	private String intro;

	@ApiModelProperty(value = "大科室编号")
	private String bigcode;

	@ApiModelProperty(value = "大科室名称")
	private String bigname;

}

