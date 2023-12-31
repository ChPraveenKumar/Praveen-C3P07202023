
package com.techm.c3p.core.validator.config.service;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.techm.c3p.core.exception.InvalidValueException;
import com.techm.c3p.core.pojo.CreateConfigRequestDCM;

public class ValidatorConfigManagement {
	private static final Logger logger = LogManager.getLogger(ValidatorConfigManagement.class);
	private InetAddressValidator ipValidator = new InetAddressValidator();

	public String validate(CreateConfigRequestDCM configRequest) throws Exception {
		String result = "";

		try {

			if (!ipValidator.isValid(configRequest.getIp())) {
				result = "Interface Ip " + configRequest.getIp() + " for name : " + configRequest.getName()
						+ " is not a valid IP Address";
				throw new InvalidValueException(result);
			}
			if (!ipValidator.isValid(configRequest.getMask())) {
				result = "Interface Ip " + configRequest.getMask() + " for name : " + configRequest.getName()
						+ " is not a valid IP Address";
				throw new InvalidValueException(result);
			}

			if (!ipValidator.isValid(configRequest.getNetworkIp())) {
				result = "LC Vrf NetworkIp " + configRequest.getNetworkIp() + " is not a valid IP Address";
				throw new InvalidValueException(result);
			}
			/*
			 * if (!ipValidator.isValid(configRequest.getRouterBgp65k())) {
			 * result="LC Vrf Router Bgp 65k Ip " + configRequest.getRouterBgp65k() +
			 * " is not a valid IP Address"; throw new InvalidValueException(result); } if
			 * (!ipValidator.isValid(configRequest.getNeighbor1())) {
			 * result="LC Vrf Neighbour 1 Ip " + configRequest.getNeighbor1() +
			 * " is not a valid IP Address"; throw new InvalidValueException(result); } if
			 * (configRequest.getAS() == null || configRequest.getAS().equals("")) {
			 * result="LC Vrf Neighbour 1 Ip " + configRequest.getNeighbor1() +
			 * " is present but AS is not there."; throw new InvalidValueException(result);
			 * }
			 */

		} catch (Exception e) {
			logger.error("Exception in validate method "+e.getMessage());
		}

		return result;
	}

}
