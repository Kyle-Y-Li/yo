package com.kyle.excel;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Kyle.Y.Li
 * @since 1.0.0 06/06/2025 13:20:36 13:20
 */
public class ExcelUtils {
    /**
     * 读取Excel数据到目标对象(自定义字段映射关系)
     *
     * @param inputStream     输入流(可以是Excel文件流)
     * @param sheetIndex      sheet索引
     * @param contentRowIndex 正文开始行索引
     * @param rowMapper       行数据自定义转换函数
     * @param <T>             目标对象类型
     * @return 目标对象集合
     */
    public static <T extends BaseExcelEntity> List<T> readExcel2Obj(
            InputStream inputStream,
            int sheetIndex,
            int contentRowIndex,
            Function<Row, T> rowMapper) {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(rowMapper);
        
        List<T> excelDatas = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            for (int i = contentRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                T obj = rowMapper.apply(row);
                if (obj == null) {
                    continue;
                }
                excelDatas.add(obj);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return excelDatas;
    }
    
    /**
     * 读取Excel数据到目标对象(使用注解配置字段映射关系)
     *
     * @param inputStream 输入流(可以是Excel文件流)
     * @param clazz       目标对象类型
     * @param <T>         目标对象类型
     * @return 目标对象集合
     */
    public static <T extends BaseExcelEntity> List<T> readExcel2Obj(InputStream inputStream, Class<T> clazz) {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(clazz);
        
        List<T> excelDatas = new ArrayList<>();
        
        //0.获取Sheet位置
        ExcelSheet sheetAnnotation = clazz.getAnnotation(ExcelSheet.class);
        Objects.requireNonNull(sheetAnnotation, "The target object must have the annotation @ExcelSheet");
        String sheetName = sheetAnnotation.name();
        Integer sheetIndex = sheetAnnotation.index() < 0 ? null : sheetAnnotation.index();
        int headerRowIndex = sheetAnnotation.headerRowIndex();
        int contentRowIndex = sheetAnnotation.contentRowIndex();
        if (StringUtils.isBlank(sheetName) && sheetIndex == null) {
            throw new IllegalArgumentException("The target object must have the annotation @ExcelSheet");
        }
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = sheetIndex != null
                    ? workbook.getSheetAt(sheetIndex)
                    : workbook.getSheet(sheetName);
            Row headerRow = sheet.getRow(headerRowIndex);
            
            //1.获取类 字段，注解，excel列头 对应关系
            Map<Field, ExcelColumnEntity> fieldExcelColumnMap = getFieldColumnMap(clazz, headerRow);
            
            //2.开始读取数据
            for (int i = contentRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Map.Entry<Field, ExcelColumnEntity> entry : fieldExcelColumnMap.entrySet()) {
                    //目标字段
                    Field field = entry.getKey();
                    //格式化
                    ExcelColumnEntity format = entry.getValue();
                    //获取目标字段对应的单元格
                    Cell cell = row.getCell(entry.getValue().getIndex());
                    Object cellValue = getCellValue(cell);
                    Object convertedValue = convert2TargetValue(field.getType(), cellValue, format);
                    //设置值
                    field.set(instance, convertedValue);
                }
                excelDatas.add(instance);
            }
        } catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        
        return excelDatas;
    }
    
    /**
     * 写入 Excel 文件
     *
     * @param outputStream 输出流
     * @param datas 数据列表
     * @param sheetName sheet名
     * @param headerRowIndex 表头开始行索引
     * @param contentRowIndex 正文开始行索引
     * @param xssf 是否是高版本Excel
     * @param <T> 目标对象类型
     */
    public static <T extends BaseExcelEntity> void writeAsExcel(
            OutputStream outputStream,
            List<T> datas,
            String sheetName,
            int headerRowIndex,
            int contentRowIndex,
            boolean xssf) {
        ObjectUtils.requireNonEmpty(datas);
        
        T instance = datas.get(0);
        Objects.requireNonNull(instance);
        
        //1.获取类字段和注解对应关系
        List<Map.Entry<Field, ExcelColumnEntity>> fieldAnnotationMappings = getFieldAnnotationMappings(instance.getClass()).stream().sorted(Comparator.comparing(i -> i.getValue().getIndex())).collect(Collectors.toList());
        List<ExcelColumnEntity> excelColumnEntities = fieldAnnotationMappings.stream().map(Map.Entry::getValue).collect(Collectors.toList());
        
        try (Workbook workbook = WorkbookFactory.create(xssf)) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            //2.写表头
            Row headerRow = sheet.createRow(headerRowIndex);
            for (ExcelColumnEntity format : excelColumnEntities) {
                if (format == null) {
                    continue;
                }
                int index = format.getIndex();
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(format.getName());
                if (format.getWidth() != null && format.getWidth() > 0) {
                    sheet.setColumnWidth(index, format.getWidth());
                } else {
                    sheet.autoSizeColumn(index);
                }
            }
            
            //3.写数据行
            for (int i = 0; i < datas.size(); i++) {
                Row row = sheet.createRow(contentRowIndex + i);
                T obj = datas.get(i);
                
                for (Map.Entry<Field, ExcelColumnEntity> fieldMap : fieldAnnotationMappings) {
                    if (fieldMap == null) {
                        continue;
                    }
                    
                    Field field = fieldMap.getKey();
                    ExcelColumnEntity format = fieldMap.getValue();
                    int index = format.getIndex();
                    
                    //获取字段值
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    
                    //写入单元格值
                    Cell cell = row.createCell(index);
                    setCellValue(workbook, cell, fieldValue, format);
                }
            }
            
            workbook.write(outputStream);
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 获取类字段和注解对应关系
     *
     * @param clazz class
     * @param <T>   对象类型
     * @return 类字段和注解对应关系
     */
    private static <T extends BaseExcelEntity> List<Map.Entry<Field, ExcelColumnEntity>> getFieldAnnotationMappings(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        
        //所有字段
        Field[] fields = clazz.getDeclaredFields();
        
        //获取类字段和注解对应关系
        List<Map.Entry<Field, ExcelColumnEntity>> fieldExcelColumnMap = new ArrayList<>(fields.length);
        
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field == null) {
                continue;
            }
            
            ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
            if (columnAnnotation == null || (StringUtils.isBlank(columnAnnotation.name()) && columnAnnotation.index() < 0)) {
                continue;
            }
            field.setAccessible(true);
            
            Integer columnIndex = columnAnnotation.index() < 0 ? i : columnAnnotation.index();
            ExcelColumnEntity excelColumnEntity = new ExcelColumnEntity(
                    columnIndex,
                    columnAnnotation.name(),
                    columnAnnotation.width(),
                    columnAnnotation.dateFormat(),
                    columnAnnotation.cellStyles());
            fieldExcelColumnMap.add(new AbstractMap.SimpleEntry<>(field, excelColumnEntity));
        }
        
        return fieldExcelColumnMap;
    }
    
    /**
     * 获取类 字段，注解，excel列头 对应关系
     *
     * @param clazz     class
     * @param headerRow excel列头
     * @param <T>       对象类型
     * @return 类字段和注解对应关系
     */
    private static <T extends BaseExcelEntity> Map<Field, ExcelColumnEntity> getFieldColumnMap(Class<T> clazz, Row headerRow) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(headerRow);
        
        //所有字段
        Field[] fields = clazz.getDeclaredFields();
        
        //1.获取列头的 名 <-> 索引 的映射
        Map<String, Integer> headerMap = new HashMap<>(fields.length);
        Map<Integer, String> headerReversionMap = new HashMap<>(fields.length);
        for (Cell cell : headerRow) {
            if (cell == null) {
                continue;
            }
            String name = StringUtils.trim(cell.getStringCellValue());
            Integer index = cell.getColumnIndex();
            if (StringUtils.isBlank(name)) {
                continue;
            }
            headerMap.putIfAbsent(name, index);
            headerReversionMap.putIfAbsent(index, name);
        }
        
        //2.获取类字段和注解对应关系
        Map<Field, ExcelColumnEntity> fieldExcelColumnMap = new HashMap<>(fields.length);
        for (Field field : fields) {
            if (field == null) {
                continue;
            }
            ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
            if (columnAnnotation == null || (StringUtils.isBlank(columnAnnotation.name()) && columnAnnotation.index() < 0)) {
                continue;
            }
            field.setAccessible(true);
            String columnName = StringUtils.trim(columnAnnotation.name());
            Integer columnIndex = columnAnnotation.index() < 0 ? null : columnAnnotation.index();
            
            //如果目标实体只配置了name，则补偿index
            if (columnIndex == null && StringUtils.isNotBlank(columnName) && headerMap.containsKey(columnName)) {
                columnIndex = headerMap.get(columnName);
            }
            if (columnIndex == null || !headerReversionMap.containsKey(columnIndex)) {
                continue;
            }
            //如果目标实体只配置了index，则补偿name
            if (StringUtils.isBlank(columnName)) {
                columnName = headerReversionMap.get(columnIndex);
            }
            
            ExcelColumnEntity excelColumnEntity = new ExcelColumnEntity(
                    columnIndex,
                    columnName,
                    columnAnnotation.width(),
                    columnAnnotation.dateFormat(),
                    columnAnnotation.cellStyles());
            fieldExcelColumnMap.putIfAbsent(field, excelColumnEntity);
        }
        
        return fieldExcelColumnMap;
    }
    
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return DateUtil.isCellDateFormatted(cell)
                        ? cell.getDateCellValue()
                        : cell.getNumericCellValue();
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                cell.getBooleanCellValue();
            case FORMULA:
                cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    
    private static Object convert2TargetValue(Class<?> targetType, Object value, ExcelColumnEntity format) {
        if (value == null) {
            return null;
        }
        
        String defaultDateFormat = format != null && StringUtils.isNotBlank(format.getDateFormat())
                ? format.getDateFormat()
                : "MM/dd/yyyy HH:mm:ss";
        SimpleDateFormat defaultSimpleDateFormat = new SimpleDateFormat(defaultDateFormat);
        String strValue = value.toString().trim();
        if (targetType == String.class) {
            if (value instanceof Date) {
                return defaultSimpleDateFormat.format((Date) value);
            }
            return strValue;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(strValue);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(strValue);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(strValue);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(strValue);
        }
        if (targetType == Date.class) {
            if (value instanceof Date) {
                return value;
            }
            try {
                return defaultSimpleDateFormat.parse(strValue);
            } catch (ParseException e) {
                return null;
            }
        }
        return value;
    }
    
    private static void setCellValue(Workbook workbook, Cell cell, Object value, ExcelColumnEntity format) {
        if (cell == null) {
            return;
        }
        
        CellStyle cellStyle = cell.getCellStyle();
        //设置值
        if (value == null) {
            cell.setCellValue(StringUtils.EMPTY);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            if (format != null && StringUtils.isNotBlank(format.getDateFormat())) {
                DataFormat dataFormat = workbook.createDataFormat();
                cellStyle.setDataFormat(dataFormat.getFormat(format.getDateFormat()));
            }
        } else if (value instanceof LocalDateTime) {
            Date date = Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
            cell.setCellValue(date);
            if (format != null && StringUtils.isNotBlank(format.getDateFormat())) {
                DataFormat dataFormat = workbook.createDataFormat();
                cellStyle.setDataFormat(dataFormat.getFormat(format.getDateFormat()));
            }
        } else if (value instanceof LocalDate) {
            Date date = Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
            cell.setCellValue(date);
            if (format != null && StringUtils.isNotBlank(format.getDateFormat())) {
                DataFormat dataFormat = workbook.createDataFormat();
                cellStyle.setDataFormat(dataFormat.getFormat(format.getDateFormat()));
            }
        } else {
            cell.setCellValue(value.toString());
        }
        //设置样式
        cell.setCellStyle(cellStyle);
    }
}
