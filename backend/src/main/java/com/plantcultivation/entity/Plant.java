package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("plant")
public class Plant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String commonName;
    private String aliases;
    private String scientificName;
    private String slug;
    private String family;
    private String genusName;
    private String plantType;
    private String growthCycle;
    private String ornamentalType;
    private String origin;
    private String description;
    private String shortDescription;

    // Morphology
    private String leafShape;
    private String leafColor;
    private String stemFeature;
    private String flowerShape;
    private String flowerColor;
    private String bloomMonth;
    private String fruitPeriod;
    private String overallShape;

    // Light
    private String lightLevel;
    private Integer lightHoursMin;
    private Integer lightHoursMax;
    private String lightDescription;

    // Water
    private String waterFrequency;
    private Integer waterIntervalDaysMin;
    private Integer waterIntervalDaysMax;
    private String waterDescription;
    private String waterPrinciple;
    private String waterSpring;
    private String waterSummer;
    private String waterFall;
    private String waterWinter;
    private String waterTaboo;
    private String waterQuality;

    // Humidity
    private String humidityLevel;
    private String humidityDescription;

    // Temperature
    private Integer tempMinCelsius;
    private Integer tempMaxCelsius;
    private Integer tempColdMin;
    private Integer tempHeatMax;
    private Boolean summerDormancy;
    private String suitablePosition;
    private String growthHabit;
    private String tempDescription;

    // Fertilizer
    private String fertilizerType;
    private String fertilizerFrequency;
    private String fertilizerDescription;
    private String fertilizerBestSeason;
    private String fertilizerGrow;
    private String fertilizerBloom;
    private Boolean fertilizerStopDormancy;
    private String fertilizerTaboo;
    private String deficiencySymptom;

    // Soil
    private String soilType;
    private String soilRecipe;
    private String potType;
    private String potSizeSuggestion;
    private String repotCycle;
    private BigDecimal soilPhMin;
    private BigDecimal soilPhMax;

    // Pruning
    private String pruneBestTime;
    private String pruneParts;
    private String pruneMethod;
    private String pruneTaboo;

    // Seasonal care, pest, toxicity, problems, value
    private String seasonalCare;   // JSON string
    private String pestDisease;    // JSON string
    private String toxicityLevel;
    private String toxicParts;
    private String toxicitySymptom;
    private String petKidWarning;
    private String commonProblems; // JSON string
    private String ornamentalValue;
    private String airPurifyDetail;
    private String edibleValue;
    private String medicinalValue;
    private String fengShui;
    private String suitableScene;

    // Characteristics
    private String difficulty;
    private String growthRate;
    private Integer maxHeightCm;
    private Boolean isIndoor;
    private Boolean isOutdoor;
    private Boolean isPetSafe;
    private Boolean isAirPurifying;

    // Media
    private String imageUrl;
    private String imageAlt;
    private String galleryUrls;  // JSON string
    @TableField("model_3d_url")
    private String model3dUrl;
    private String lottieUrl;

    // SEO
    private String metaTitle;
    private String metaDescription;

    // Status
    private Boolean isFeatured;
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Category> categories;

    @TableField(exist = false)
    private List<CareGuide> careGuides;
}
