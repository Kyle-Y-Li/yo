package com.kyle.excel;

import java.lang.annotation.*;

/**
 * Excel Sheet 和 Excel Bean 的关系
 *
 * @author Kyle.Y.Li
 * @since 1.0.0 06/06/2025 15:30:42 15:30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelSheet {
    /**
     * Sheet Name
     *
     * @return Sheet Name
     */
    String name() default "";
    
    /**
     * Sheet Index
     *
     * @return Sheet Index
     */
    int index() default -1;
    
    /**
     * Column Header Row Index
     *
     * @return Column Header Row Index
     */
    int headerRowIndex() default 0;
    
    /**
     * Body Content Row Index
     *
     * @return Body Content Row Index
     */
    int contentRowIndex() default 1;
}
