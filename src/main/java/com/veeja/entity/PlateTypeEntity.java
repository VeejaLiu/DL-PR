package com.veeja.entity;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * t_plate_type
 *
 * @author veeja
 * 2020-09-30 16:54:41.815
 */
@Data
@NoArgsConstructor
public class PlateTypeEntity implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(example = "Integer-id")
    private Integer id;

    /**
     * typeName
     */
    @ApiModelProperty(example = "String-typeName")
    private String typeName;

    /**
     * plateColor
     */
    @ApiModelProperty(example = "String-plateColor")
    private String plateColor;

    /**
     * charColor
     */
    @ApiModelProperty(example = "String-charColor")
    private String charColor;

    /**
     * charCount
     */
    @ApiModelProperty(example = "Integer-charCount")
    private Integer charCount;

    /**
     * chIndex
     */
    @ApiModelProperty(example = "String-chIndex")
    private String chIndex;

    /**
     * lineCount
     */
    @ApiModelProperty(example = "Integer-lineCount")
    private Integer lineCount;

    /**
     * heightPx
     */
    @ApiModelProperty(example = "Integer-heightPx")
    private Integer heightPx;

    /**
     * widthPx
     */
    @ApiModelProperty(example = "Integer-widthPx")
    private Integer widthPx;

    /**
     * heightGb
     */
    @ApiModelProperty(example = "Integer-heightGb")
    private Integer heightGb;

    /**
     * widthGb
     */
    @ApiModelProperty(example = "Integer-widthGb")
    private Integer widthGb;

    /**
     * plateMinH
     */
    @ApiModelProperty(example = "Integer-plateMinH")
    private Integer plateMinH;

    /**
     * plateMaxH
     */
    @ApiModelProperty(example = "Integer-plateMaxH")
    private Integer plateMaxH;

    /**
     * charMinH
     */
    @ApiModelProperty(example = "Integer-charMinH")
    private Integer charMinH;

    /**
     * charMaxH
     */
    @ApiModelProperty(example = "Integer-charMaxH")
    private Integer charMaxH;

    /**
     * remark
     */
    @ApiModelProperty(example = "String-remark")
    private String remark;

    /**
     * createTime
     */
    @ApiModelProperty(example = "String-createTime")
    private String createTime;

    /**
     * creatorId
     */
    @ApiModelProperty(example = "Integer-creatorId")
    private Integer creatorId;

    /**
     * version
     */
    @ApiModelProperty(example = "Integer-version")
    private Integer version;

    /**
     * delFlag
     */
    @ApiModelProperty(example = "Integer-delFlag")
    private Integer delFlag;

    private static final long serialVersionUID = 1L;
}