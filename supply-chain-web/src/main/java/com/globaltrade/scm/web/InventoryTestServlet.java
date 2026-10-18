package com.globaltrade.scm.web;

import com.globaltrade.scm.ejb.exception.InsufficientInventoryException;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import com.globaltrade.scm.ejb.remote.InventoryServiceRemote;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.InventoryItemNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/inventory-test")
public class InventoryTestServlet extends HttpServlet {

    @EJB
    private InventoryServiceRemote inventoryService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String deleteId = req.getParameter("delete");
        if (deleteId != null) {
            try {
                inventoryService.deleteItem(Long.parseLong(deleteId));
                out.println("Deleted inventory item with ID: " + deleteId);
            } catch (InventoryItemNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("Delete failed: " + e.getMessage());
            }
            return;
        }

        String adjustId = req.getParameter("adjust");
        if (adjustId != null) {
            String deltaParam = req.getParameter("delta");
            int delta = (deltaParam != null) ? Integer.parseInt(deltaParam) : 0;

            try {
                inventoryService.adjustQuantity(Long.parseLong(adjustId), delta);
                out.println("Adjusted item " + adjustId + " by " + delta);
            } catch (InventoryItemNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("Adjust failed: " + e.getMessage());
            } catch (InsufficientInventoryException e) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.println("Adjust failed: " + e.getMessage());
            }
            return;
        }

        // Default vendor is 1L (ACTIVE). Pass ?vendorId=2 to test the SUSPENDED rejection path.
        String vendorParam = req.getParameter("vendorId");
        Long vendorId = (vendorParam != null) ? Long.parseLong(vendorParam) : 1L;

        try {
            Long itemId = inventoryService.createItem(
                    "TEST-SKU-" + System.currentTimeMillis(),
                    "Test Item",
                    vendorId,
                    100,
                    20,
                    "Warehouse A",
                    new BigDecimal("9.99")
            );
            out.println("Created test inventory item with ID: " + itemId + " for vendor " + vendorId);
        } catch (VendorNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("Create failed: " + e.getMessage());
        } catch (InvalidVendorStatusException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.println("Create failed: " + e.getMessage());
        }

    }
}