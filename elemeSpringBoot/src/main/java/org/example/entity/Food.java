package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
public class Food {
    @Id
    @NotNull(message = "商品ID不能为空")
    private Integer id;
    
    //名称
    @NotBlank(message = "商品名称不能为空")
    @Length(min = 1, max = 50, message = "商品名称长度必须在1-50字符之间")
    private String name;
    
    //评价
    @Length(max = 200, message = "商品评价不能超过200字符")
    private String text;
    
    //销量
    @Pattern(regexp = "^\\d+$", message = "销量必须是数字")
    private String amount;
    
    //打折
    @Length(max = 100, message = "打折信息不能超过100字符")
    private String discount;
    
    //现价
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格不能小于0.01元")
    @DecimalMax(value = "9999.99", message = "商品价格不能大于9999.99元")
    private Double redPrice;
    
    //原价
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "原价格式不正确")
    private String grayPrice;
    
    //商家ID
    @NotNull(message = "商家ID不能为空")
    @Min(value = 1, message = "商家ID必须大于0")
    private Integer business;
    
    //图片路径
    @Length(max = 500, message = "图片路径不能超过500字符")
    private String img;
    
    //是否上架(0:下架 1:上架)
    @NotNull(message = "上架状态不能为空")
    @Min(value = 0, message = "上架状态只能是0或1")
    @Max(value = 1, message = "上架状态只能是0或1")
    private Integer selling;
    //打折 列表
    @Transient
    private List<String> discountList;

    public void setDiscount(String discounts) {
        this.discount = discounts;
        if (discounts != null && !discounts.trim().isEmpty()) {
            this.discountList = Arrays.asList(discounts.split("-"));
        } else {
            this.discountList = new ArrayList<>();
        }
    }
}
