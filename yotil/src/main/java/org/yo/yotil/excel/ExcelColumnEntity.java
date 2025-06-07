package com.kyle.excel;

/**
 * 读取 {@link ExcelColumn} 值
 *
 * @author Kyle.Y.Li
 * @since 1.0.0 06/06/2025 17:09:42 17:09
 */
public class ExcelColumnEntity {
    public ExcelColumnEntity(Integer index, String name, Integer width, String dateFormat, ExcelCellStyleEnum[] cellStyles) {
        this.index = index;
        this.name = name;
        this.width = width;
        this.dateFormat = dateFormat;
        this.cellStyles = cellStyles;
    }
    
    /**
     * Column Index
     */
    private final Integer index;
    
    /**
     * Column Name
     */
    private final String name;
    
    /**
     * Column Width（不指定则自适应）
     */
    private final Integer width;
    
    /**
     * 时间格式
     */
    private final String dateFormat;
    
    /**
     * Column Styles
     */
    private final ExcelCellStyleEnum[] cellStyles;
    
    public Integer getIndex() {
        return index;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public String getDateFormat() {
        return dateFormat;
    }
    
    public ExcelCellStyleEnum[] getCellStyles() {
        return cellStyles;
    }
}
