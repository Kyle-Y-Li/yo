package com.kyle.excel;

import java.lang.annotation.*;

/**
 * Excel Column 和 映射目标对象的映射关系
 *
 * @author Kyle.Y.Li
 * @since 1.0.0 06/06/2025 15:41:28 15:41
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {
    /**
     * Column Name
     *
     * @return Sheet Name
     */
    String name() default "";
    
    /**
     * Column Index
     *
     * @return Sheet Index
     */
    int index() default -1;
    
    /**
     * Column Width（不指定则自适应）
     *
     * @return Column Width
     */
    int width() default -1;
    
    /**
     * 时间格式
     *
     * @return 时间格式
     */
    String dateFormat() default "";
    
    /**
     * Column Styles
     *
     * @return Column Styles
     */
    ExcelCellStyleEnum[] cellStyles() default {ExcelCellStyleEnum.DEFAULT};
}
