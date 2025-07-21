package com.flick.business.core.entity;

import com.flick.business.core.enums.settings.StockControlType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "general_settings")
public class GeneralSettings {
    @Id
    private Long id;

    /**
     * Defines the global strategy for stock management
     * Defaults to PER_ITEM
     * 
     * @see StockControlType
     */
    @Enumerated
    @Column(name = "stock_control_type", nullable = false)
    private StockControlType stockControlType = StockControlType.PER_ITEM;

    /**
     * 
     */
    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_field")
    private String businessField;
}
