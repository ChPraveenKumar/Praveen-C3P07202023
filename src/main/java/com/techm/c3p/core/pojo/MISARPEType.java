//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.11 at 03:49:05 PM IST 
//


package com.techm.c3p.core.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MIS_AR_PE_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MIS_AR_PE_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="routerVrfVpnDIp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="routerVrfVpnDGateway" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fastEthernetIp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MIS_AR_PE_Type", propOrder = {
    "routerVrfVpnDIp",
    "routerVrfVpnDGateway",
    "fastEthernetIp"
})
public class MISARPEType {

    @XmlElement(name="routerVrfVpnDIp")
    protected String routerVrfVpnDIp;
    @XmlElement(name="routerVrfVpnDGateway")
    protected String routerVrfVpnDGateway;
    @XmlElement(name="fastEthernetIp")
    protected String fastEthernetIp;

    /**
     * Gets the value of the routerVrfVpnDIp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRouterVrfVpnDIp() {
        return routerVrfVpnDIp;
    }

    /**
     * Sets the value of the routerVrfVpnDIp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRouterVrfVpnDIp(String value) {
        this.routerVrfVpnDIp = value;
    }

    /**
     * Gets the value of the routerVrfVpnDGateway property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRouterVrfVpnDGateway() {
        return routerVrfVpnDGateway;
    }

    /**
     * Sets the value of the routerVrfVpnDGateway property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRouterVrfVpnDGateway(String value) {
        this.routerVrfVpnDGateway = value;
    }

    /**
     * Gets the value of the fastEthernetIp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFastEthernetIp() {
        return fastEthernetIp;
    }

    /**
     * Sets the value of the fastEthernetIp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFastEthernetIp(String value) {
        this.fastEthernetIp = value;
    }

}
