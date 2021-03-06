package com.project.web_service.wrappers.requests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.w3._2000._09.xmldsig.SignatureType;

@XmlRootElement(name = "getAgentReceivedMessages", namespace = "http://com.project/web_service/wrappers")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAgentReceivedMessages", namespace = "http://com.project/web_service/wrappers", propOrder = {"Signature"})
public class GetAgentReceivedMessages {

	@XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#")
	private SignatureType Signature;

	public SignatureType getSignature() {
		return Signature;
	}

	public void setSignature(SignatureType signature) {
		Signature = signature;
	}

}
