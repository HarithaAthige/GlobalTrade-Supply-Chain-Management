package com.globaltrade.scm.ejb.remote;

import com.globaltrade.scm.ejb.dto.VendorDTO;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface VendorServiceRemote {

    Long createVendor(String vendorName, String vendorCode, String country,
                      String contactEmail, String contactPhone);

    VendorDTO getVendor(Long vendorId) throws VendorNotFoundException;

    List<VendorDTO> getAllVendors();

    void updateVendorStatus(Long vendorId, String status)
            throws VendorNotFoundException, InvalidVendorStatusException;

    void deleteVendor(Long vendorId) throws VendorNotFoundException;

    String recalculateVendorScoreBMT(Long vendorId);

    String deleteVendorSecureCheck(Long vendorId);

}
