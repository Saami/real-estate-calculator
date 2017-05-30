package com.saami.realestate.controller;

import com.saami.realestate.enums.City;
import com.saami.realestate.model.Address;
import com.saami.realestate.model.Listing;
import com.saami.realestate.model.ZillowData;
import com.saami.realestate.service.api.PropertyReportService;
import com.saami.realestate.service.api.ZillowService;
import com.saami.realestate.util.RealEstateCalculator;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import static com.saami.realestate.util.RealEstateUtils.formatPrice;

/**
 * Created by sasiddi on 5/1/17.
 */
@RestController
public class RealestateController {

    @Autowired
    PropertyReportService propertyReportService;

    @Autowired
    ZillowService zillowService;

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

    @RequestMapping(path = "/house", method = RequestMethod.GET)
    public String getPropertyData(@RequestParam(value = "address", required = true) String addressParam, @RequestParam(value = "city", required = true) String city,
                                  @RequestParam(value = "state", required = true) String state, @RequestParam(value = "zip", required = false) Long zip,
                                  @RequestParam(value = "price", required = false) Double price, @RequestParam(value = "rentZestimate", required = false) Double rentZestimate) {

        ZillowData zillowData = new ZillowData();
        if (price == null || rentZestimate == null) {
            zillowData = zillowService.getZillowData(addressParam, city, state);
        } else {
            zillowData.setRentZestimate(rentZestimate);
        }

        Address address = new Address()
                .setAddress(addressParam)
                .setCity(city)
                .setState(state);

        Listing listing = new Listing()
                .setAddress(address)
                .setPrice(price != null ? price : zillowData.getZestimate())
                .setZillowData(zillowData);

        double rent = listing.getZillowData().getRentZestimate();
        double management = RealEstateCalculator.calculateMonthlyManagementFees(listing.getZillowData().getRentZestimate());
        double tax = RealEstateCalculator.calculateAnnualTax(City.CHARLOTTE.getTaxRate(), listing.getPrice()) / 12;
        double insurance = City.CHARLOTTE.getHomeInsurance() /12;
        double mortgage = RealEstateCalculator.calculateMonthlyPayment(listing.getPrice() - RealEstateCalculator.calculateDownPayment(listing.getPrice()));
        double wearTear = RealEstateCalculator.calculateMonthlWearTear(rent);
        double expense  = management+ tax + insurance + mortgage + wearTear;
        double cashFlow = rent - expense;

        JSONObject result = new JSONObject();
        try {
            result.put("rent", formatPrice(rent));
            result.put("management", formatPrice(management));
            result.put("tax", formatPrice(tax));
            result.put("insurane", formatPrice(insurance));
            result.put("mortgage", formatPrice(mortgage));
            result.put("wearTear", formatPrice(wearTear));
            result.put("cashFlow", formatPrice(rent - expense));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result.toString();

    }


}
