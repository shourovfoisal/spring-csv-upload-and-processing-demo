package com.shourov.csvProcessingDemo.controller;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.shourov.csvProcessingDemo.service.FileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    
    private final FileService service;
    
    public FileController(FileService service) {
        this.service = service;
    }

    @PostMapping("/parse-csv")
    public String parseCsv(@RequestParam MultipartFile file) {

        try {
            Reader reader = new InputStreamReader(file.getInputStream());

            try (CSVReader csvReader = new CSVReaderBuilder(reader).build()) {
                
                service.saveFile(file);
                
                List<String[]> rows = csvReader.readAll();
                return "Processed " + rows.size() + " rows...";

            } catch (CsvMalformedLineException e) {
                throw new RuntimeException("Bad CSV format: " + e.getMessage(), e);
            } catch (CsvException e) {
                throw new RuntimeException("CSV parsing error", e);
            } catch (IOException e) {
                throw new RuntimeException("I/O error while reading file", e);
            }

        } catch (IOException e) {
            throw new RuntimeException("File could not be read", e);
        }
    }

    @PostMapping("/parse-excel")
    public String parseExcel(@RequestParam MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook;

            String originalFileName = file.getOriginalFilename();

            if (StringUtils.isNotBlank(originalFileName) && originalFileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);   // Office 2007+ XML
                service.saveFile(file);
            } else {
                workbook = new HSSFWorkbook(inputStream); // .xls files
                service.saveFile(file);
            }

            for (Sheet sheet : workbook) {
                log.info(sheet.getSheetName());

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        System.out.print(cell + " ");
                    }
                    System.out.println();
                }
            }

        } catch (NotOLE2FileException | OLE2NotOfficeXmlFileException e) {
            throw new RuntimeException("The uploaded file is not a valid Excel document", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not get the file stream.", e);
        }

        return null;
    }

}
