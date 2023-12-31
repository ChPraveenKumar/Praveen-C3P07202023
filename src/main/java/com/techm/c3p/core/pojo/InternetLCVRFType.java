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
 * <p>Java class for internetLCVRFType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="internetLCVRFType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="networkIp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="neighbor1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="neighbor2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="neighbor1_remoteAS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="neighbor2_remoteAS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkIp_subnetMask" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="routingProtocol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "internetLCVRFType", propOrder = {
    "networkIp",
    "neighbor1",
    "neighbor2",
    "neighbor1_remoteAS",
    "neighbor2_remoteAS",
    "networkIp_subnetMask",
    "routingProtocol",
    "AS"
})
public class InternetLCVRFType {

    @XmlElement(name="networkIp")
    protected String networkIp;
    @XmlElement(name="AS")
    protected String AS;
    @XmlElement(name="neighbor1")
    protected String neighbor1;
    @XmlElement(name="neighbor2")
    protected String neighbor2;
    @XmlElement(name="neighbor1_remoteAS")
    protected String neighbor1_remoteAS;
    @XmlElement(name="neighbor2_remoteAS")
    protected String neighbor2_remoteAS;
    @XmlElement(name="networkIp_subnetMask")
    protected String networkIp_subnetMask;
    @XmlElement(name="routingProtocol")
    protected String routingProtocol;
    /**
     * Gets the value of the networkIp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetworkIp() {
        return networkIp;
    }

    /**
     * Sets the value of the networkIp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetworkIp(String value) {
        this.networkIp = value;
    }

    /**
     * Gets the value of the remotePort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAS() {
        return AS;
    }

    /**
     * Sets the value of the remotePort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAS(String value) {
        this.AS = value;
    }

    /**
     * Gets the value of the neighbor1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNeighbor1() {
        return neighbor1;
    }

    /**
     * Sets the value of the neighbor1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNeighbor1(String value) {
        this.neighbor1 = value;
    }

    /**
     * Gets the value of the neighbor2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNeighbor2() {
        return neighbor2;
    }

    /**
     * Sets the value of the neighbor2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNeighbor2(String value) {
        this.neighbor2 = value;
    }

    /**
     * Gets the value of the neighbor3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getneighbor1_remoteAS() {
        return neighbor1_remoteAS;
    }

    /**
     * Sets the value of the neighbor3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setneighbor1_remoteAS(String value) {
        this.neighbor1_remoteAS = value;
    }

    /**
     * Gets the value of the neighbor4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getneighbor2_remoteAS() {
        return neighbor2_remoteAS;
    }

    /**
     * Sets the value of the neighbor4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setneighbor2_remoteAS(String value) {
        this.neighbor2_remoteAS = value;
    }

    /**
     * Gets the value of the neighbor5 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getnetworkIp_subnetMask() {
        return networkIp_subnetMask;
    }

    /**
     * Sets the value of the neighbor5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setnetworkIp_subnetMask(String value) {
        this.networkIp_subnetMask = value;
    }

    /**
     * Gets the value of the neighbor6 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getroutingProtocol() {
        return routingProtocol;
    }

    /**
     * Sets the value of the neighbor6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setroutingProtocol(String value) {
        this.routingProtocol = value;
    }

}
