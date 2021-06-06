package com.veeja.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * t_plate_reco_debug
 * @author veeja
 * 2020-04-30 16:17:58.795
 */
@Data
@NoArgsConstructor
public class PlateRecoDebugEntity implements Serializable {
    /**
     * id
     */
    private Integer id;

    /**
     * parentId
     */
    private Integer parentId;

    /**
     * fileName
     */
    private String fileName;

    /**
     * filePath
     */
    private String filePath;

    /**
     * debugType
     */
    private String debugType;

    /**
     * fileLength
     */
    private Integer fileLength;

    /**
     * lastRecoTime
     */
    private String lastRecoTime;

    /**
     * recoPlate
     */
    private String recoPlate;

    /**
     * plateColor
     */
    private String plateColor;

    /**
     * sort
     */
    private Integer sort;

    private static final long serialVersionUID = 1L;
}