package com.globaltrade.scm.web;

import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import com.globaltrade.scm.ejb.remote.ShipmentServiceRemote;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.ShipmentNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/shipment-test")
public class ShipmentTestServlet extends HttpServlet {

    @EJB
    private ShipmentServiceRemote shipmentService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String deleteId = req.getParameter("delete");
        if (deleteId != null) {
            try {
                shipmentService.deleteShipment(Long.parseLong(deleteId));
                out.println("Deleted shipment with ID: " + deleteId);
                out.println("Now check the WildFly console for a 'Cancelled customs deadline timer' log line.");
            } catch (ShipmentNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("Delete failed: " + e.getMessage());
            }
            return;
        }

        try {
            Long shipmentId = shipmentService.createShipment(
                    "TEST-" + System.currentTimeMillis(),
                    1L,
                    "China",
                    "Sri Lanka",
                    "Test Carrier",
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(3)
            );

            out.println("Created test shipment with ID: " + shipmentId);
            out.println("Now check the WildFly console for a 'Scheduled customs deadline timer' log line.");
        } catch (VendorNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("Create failed: " + e.getMessage());
        } catch (InvalidVendorStatusException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.println("Create failed: " + e.getMessage());
        }

    }
}