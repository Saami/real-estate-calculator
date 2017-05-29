package com.saami.realestate.controller;

import com.saami.realestate.service.api.PropertyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by sasiddi on 5/1/17.
 */
@RestController
public class RealestateController {

    @Autowired
    PropertyReportService propertyReportService;

    @RequestMapping(path = "/report", method = RequestMethod.GET)
    public void getCharlotteProperties(HttpServletResponse response) {
        try {
            String csvFileName = "report.csv";

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"",
                    csvFileName);
            response.setHeader(headerKey, headerValue);
            response.setContentType("text/csv; charset=UTF-8");

            ServletOutputStream outputStream = response.getOutputStream();

            String csvString = propertyReportService.generateReport();
            byte[] bytes = csvString.getBytes("UTF-8");
            outputStream.write(bytes);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
