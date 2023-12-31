package com.techm.c3p.core.mapper;

import java.util.ArrayList;
import java.util.List;

import com.techm.c3p.core.entitybeans.RequestInfoEntity;
import com.techm.c3p.core.pojo.ServiceRequestPojo;
import com.techm.c3p.core.utility.WAFADateUtil;

public class RequestDetailsResponseMapper {	
	public List<ServiceRequestPojo> setEntityToPojo(List<RequestInfoEntity> allRequestDetails) {
		List<ServiceRequestPojo> serviceRequest = new ArrayList<>();
		if(allRequestDetails!=null) {
		allRequestDetails.forEach(request -> {
			WAFADateUtil dateutil=new WAFADateUtil();
			ServiceRequestPojo req = new ServiceRequestPojo();
			req.setInfoId(request.getInfoId());
			req.setAlpha_numeric_req_id(request.getAlphanumericReqId());
			req.setRequest_creator_name(request.getRequestCreatorName());
			req.setHostname(request.getHostName());
			req.setRegion(request.getRegion());
			req.setCustomer(request.getCustomer());
			if(request.getDateofProcessing()!=null)
			{
			req.setDateOfProcessing(dateutil.dateTimeInAppFormat(request.getDateofProcessing().toString()));
			}
			else
			{
			req.setDateOfProcessing(null);
			}
			req.setStatus(request.getStatus());
			req.setModel(request.getModel());
			req.setRequestVersion(request.getRequestVersion());
			req.setTemplateId(request.getTemplateUsed());
			if(request.getBatchId()!=null) {
				req.setBatchId(request.getBatchId());
			}
			req.setStartup(request.getStartUp());
			req.setExecutionMode(request.getRequestTypeFlag());
			req.setBaselinedFlag(request.isBaselineFlag());
			if(request.getBaselinedDate()!=null)
			{
				req.setBaselinedDate(dateutil.dateTimeInAppFormat(request.getBaselinedDate().toString()));
			}
			req.setHasDeltaFlag(request.isrHasDeltaWithBaseline());
			serviceRequest.add(req);
		});
		}
		return serviceRequest;
	}

}
