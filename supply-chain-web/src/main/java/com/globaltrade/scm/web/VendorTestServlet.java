package com.globaltrade.scm.web;

import com.globaltrade.scm.ejb.dto.VendorDTO;

import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.remote.VendorServiceRemote;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/vendor-test")
public class VendorTestServlet extends HttpServlet {

    @EJB
    private VendorServiceRemote vendorService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String updateId = req.getParameter("updateStatus");
        if (updateId != null) {
            resp.setContentType("text/plain; charset=UTF-8");
            PrintWriter plainOut = resp.getWriter();
            String status = req.getParameter("status");
            try {
                vendorService.updateVendorStatus(Long.parseLong(updateId), status);
                plainOut.println("Updated vendor " + updateId + " status to " + status);
            } catch (VendorNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                plainOut.println("Update failed: " + e.getMessage());
            } catch (InvalidVendorStatusException e) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                plainOut.println("Update failed: " + e.getMessage());
            }
            return;
        }


        String recalcId = req.getParameter("recalcBMT");
        if (recalcId != null) {
            resp.setContentType("text/plain; charset=UTF-8");
            PrintWriter plainOut = resp.getWriter();
            String result = vendorService.recalculateVendorScoreBMT(Long.parseLong(recalcId));
            plainOut.println(result);
            return;
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h2>Vendor Service Test</h2>");

        // Create a test vendor on every hit, just to prove the write path works
        Long newId = vendorService.createVendor(
                "Pacific Rim Freight Co",
                "PRF-" + System.currentTimeMillis(),
                "Singapore",
                "ops@pacificrimfreight.com",
                "+65-5550-1234"
        );
        out.println("<p>Created vendor with ID: " + newId + "</p>");

        out.println("<h3>All Vendors:</h3><ul>");
        List<VendorDTO> vendors = vendorService.getAllVendors();
        for (VendorDTO v : vendors) {
            out.println("<li>" + v.getVendorId() + " - " + v.getVendorName()
                    + " (" + v.getVendorCode() + ") - " + v.getCountry()
                    + " - Status: " + v.getStatus() + "</li>");
        }
        out.println("</ul>");

        String bmtResult = vendorService.recalculateVendorScoreBMT(1L);
        out.println("<h3>BMT Recalculation Test:</h3>");
        out.println("<p>" + bmtResult + "</p>");

        String secureDeleteResult = vendorService.deleteVendorSecureCheck(999L);
        out.println("<h3>isCallerInRole() Test:</h3>");
        out.println("<p>" + secureDeleteResult + "</p>");

        out.println("</body></html>");
    }
}