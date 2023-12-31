package com.techm.c3p.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techm.c3p.core.service.CategoryDropDownService;

@RestController
@RequestMapping
public class CategoryDropDownController {
	@Autowired
	CategoryDropDownService service;

	/**
	 *This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	@GET
	@RequestMapping(value = "/dropdownValue",method = RequestMethod.GET, produces = "application/json")
	public Response getDropDownValue() {
		return Response.status(200).entity(service.getAll()).build();
	}

	/**
	 *This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	@GET
	@RequestMapping(value = "/dropdownValue/{categoryName}", method = RequestMethod.GET,produces = "application/json")
	public Response getValueByCategoryName(@PathVariable String categoryName) {
		return Response.status(200)
				.entity(service.getAllByCategoryName(categoryName)).build();

	}
}
