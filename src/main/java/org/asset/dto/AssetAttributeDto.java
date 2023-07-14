package org.asset.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetAttributeDto {

    private String name;

    private String type;

    private boolean required;

    private String minValue;

    private String maxValue;

}
