/*******************************************************************************
 * Copyright (C) 2017 nicmus inc. (jivko.sabev@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.nicmus.jpoweradmin.service;

import java.io.Serializable;
import java.net.IDN;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Record.TYPE;

public class DNSValidator implements Serializable {
	private static final long serialVersionUID = -8162972050313878159L;


	//This monstrosity came from:
	//http://www.schlitt.net/spf/tests/spf_record_regexp-03.txt
	private static final Pattern SPF_PATTERN = Pattern.compile(
			"^[Vv]=[Ss][Pp][Ff]1( +([-+?~]?([Aa][Ll][Ll]|[Ii][Nn][Cc][Ll][Uu][Dd]" +
			"[Ee]:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?" +
			"[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*" +
			"[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|" +
			"12[0-8])?[Rr]?[+-/=_]*\\})|[Aa](:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]" +
			"?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*" +
			"(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))?" +
			"((/([1-9]|1[0-9]|2[0-9]|3[0-2]))?(//([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8]))?)?" +
			"|[Mm][Xx](:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?" +
			"[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|%" +
			"\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))?" +
			"((/([1-9]|1[0-9]|2[0-9]|3[0-2]))?(//([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8]))?)?|[Pp]" +
			"[Tt][Rr](:(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?" +
			"[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|" +
			"%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))" +
			"?|[Ii][Pp]4:([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|" +
			"1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
			"\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(/([1-9]|1[0-9]|2[0-9]|3[0-2]))" +
			"?|[Ii][Pp]6:(::|([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|([0-9A-Fa-f]{1,4}:){1,8}:|" +
			"([0-9A-Fa-f]{1,4}:){7}:[0-9A-Fa-f]{1,4}|([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}){1,2}|" +
			"([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4}){1,3}|([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4})" +
			"{1,4}|([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){1,5}|([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]" +
			"{1,4}){1,6}|[0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,7}|:(:[0-9A-Fa-f]{1,4}){1,8}|([0-9A-Fa-f]" +
			"{1,4}:){6}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4]" +
			"[0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}" +
			"|2[0-4][0-9]|25[0-5])|([0-9A-Fa-f]{1,4}:){6}:([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
			"\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|([0-9A-Fa-f]{1,4}:){5}:([0-9A-Fa-f]{1,4}:)?" +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|" +
			"([0-9A-Fa-f]{1,4}:){4}:([0-9A-Fa-f]{1,4}:){0,2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|([0-9A-Fa-f]{1,4}:){3}:([0-9A-Fa-f]{1,4}:){0,3}([0-9]|" +
			"[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|" +
			"([0-9A-Fa-f]{1,4}:){2}:([0-9A-Fa-f]{1,4}:){0,4}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|[0-9A-Fa-f]{1,4}::([0-9A-Fa-f]{1,4}:)" +
			"{0,5}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
			"\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|::" +
			"([0-9A-Fa-f]{1,4}:){0,6}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
			"([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))" +
			"(/([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8]))?|" +
			"[Ee][Xx][Ii][Ss][Tt][Ss]:(%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*" +
			"(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}))|[Rr][Ee][Dd][Ii][Rr][Ee][Cc][Tt]=" +
			"(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*" +
			"(\\.([A-Za-z]|[A-Za-z]([-0-9A-Za-z]?)*[0-9A-Za-z])|" +
			"%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\})" +
			"|[Ee][Xx][Pp]=(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]" +
			"|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*(\\.([A-Za-z]|[A-Za-z]" +
			"([-0-9A-Za-z]?)*[0-9A-Za-z])|%\\{[CDHILOPR-Tcdhilopr-t]" +
			"([1-9][0-9]?|10[0-9]|11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\})|[A-Za-z]" +
			"[-.0-9A-Z_a-z]*=(%\\{[CDHILOPR-Tcdhilopr-t]([1-9][0-9]?|10[0-9]|" +
			"11[0-9]|12[0-8])?[Rr]?[+-/=_]*\\}|%%|%_|%-|[!-$&-~])*))* *$");


	@Inject
	private FacesContext facesContext;
	
	@Inject
	private RecordServiceDAO recordServiceDAO;
	
	
	/**
	 * Validate that the given record is valid within the list of records in the 
	 * zone
	 * @param record The record to validate
	 * @return true on successful validation, false otherwise
	 */
	public boolean isValid(Record record){

		//see if the record exists
		String asciiName = IDN.toASCII(record.getName());
		String asciiContent = IDN.toASCII(record.getContent());
		if(this.recordServiceDAO.exists(record.getName(), record.getContent(), record.getType())){
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Record already exists", "");
			this.facesContext.addMessage(null, fc);
			return false;
		}

		
		switch(record.getType()){
			case A:{
				if(!this.isValidFQDN(asciiName)) return false;
				if(!this.isValidIPV4(record.getContent())) return false;
				if(this.recordServiceDAO.exists(record.getName(), TYPE.CNAME)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "There already exists a CNAME matching " + record.getName(),"");
					this.facesContext.addMessage(null, fc);
					return false;
				}
				return true;
			}
			case AAAA: {
				if(!this.isValidFQDN(asciiName)) return false;
				if(!this.isValidIPV6(record.getContent())) return false;
				if(this.recordServiceDAO.exists(record.getName(), TYPE.CNAME)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "There already exists a CNAME matching " + record.getName(),"");
					this.facesContext.addMessage(null, fc);
					return false;
				}
				return true;
			}
			case CNAME: {
				if(!this.isValidFQDN(asciiName)) return false;
				if(!this.isValidFQDN(asciiContent)) return false;
				if(this.recordServiceDAO.contentExists(record.getName(), TYPE.NS)) {
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not a valid CNAME. Check for MX or NS record", "");
					this.facesContext.addMessage(null, fc);
					return false;
				}
				
				if(this.recordServiceDAO.contentExists(record.getName(), TYPE.MX)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not a valid CNAME. Check for MX or NS record", "");
					this.facesContext.addMessage(null, fc);
					return false;
				}

				if(this.recordServiceDAO.exists(record.getName(), TYPE.A)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "This is not a valid CNAME. There is already exists an A, AAAA or CNAME with such a name", "");
					this.facesContext.addMessage(null, fc);
					return false;
				}
				if(this.recordServiceDAO.exists(record.getName(), TYPE.AAAA)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "This is not a valid CNAME. There is already exists an A, AAAA or CNAME with such a name", "");
					this.facesContext.addMessage(null, fc);
					return false;
				}

				if(this.recordServiceDAO.exists(record.getName(), TYPE.CNAME)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "This is not a valid CNAME. There is already exists an A, AAAA or CNAME with such a name", "");
					this.facesContext.addMessage(null, fc);
					return false;
				}

				return true;
			}
			case MX:{
				if(!this.isValidFQDN(asciiName)) return false;
				if(!this.isValidFQDN(asciiContent)) return false;
				if(this.recordServiceDAO.contentExists(record.getContent(), TYPE.CNAME)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "There already exists a CNAME matching " + record.getName(),"");
					this.facesContext.addMessage(null, fc);
					return false;
				}
				return true;
			}
			case NS: {
				if(!this.isValidFQDN(asciiName)) return false;
				if(!this.isValidFQDN(asciiContent)) return false;
				if(this.recordServiceDAO.contentExists(record.getContent(), TYPE.CNAME)){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR, "There already exists a CNAME matching " + record.getName(),"");
					this.facesContext.addMessage(null, fc);
					return false;
				}
				return true;
			}
			case PTR: {
				if(!this.isValidFQDN(asciiName)) return false;
				if(!this.isValidFQDN(asciiContent)) return false;
				return true;
			}
			case TXT: {
				if(!this.isPrintable(record.getName())) return false;
				if(!this.isPrintable(record.getContent())) return false;
				record.setName(this.quoteRecordContent(record.getName()));
				record.setContent(this.quoteRecordContent(record.getContent()));
				return true;
			}
			case SPF: {
				if(!this.isValidSPF(asciiContent)) return false;
				record.setName(this.quoteRecordContent(record.getName()));
				record.setContent(this.quoteRecordContent(record.getContent()));
				return true;
			}
			default:
				return true;
		}
	}

	/**
	 * 
	 * @param hostname
	 * @return
	 */
	public boolean isValidFQDN(String hostname){
		boolean valid = true;
		if(hostname.length() > 255){
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Hostname too long - maximum length allowed is 255 characters","");
			this.facesContext.addMessage(null, fc);
			valid = false;
		}
		if(hostname.indexOf('.') == -1){
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid hostname","");
			this.facesContext.addMessage(null, fc);
			valid = false;
		}
		String[] parts= hostname.split("\\.");
		int position = 0;
		for(String part : parts){
			if(position++ == 0){
				//we are at the first hostname part
				//could start with '*'
				if(!part.matches("^(\\*|[\\w-]+)$")){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid part","");
					this.facesContext.addMessage(null, fc);
					valid = false;
				}
			} else {
				//no '*' is allowed
				if(!part.matches("[\\w-]+$")){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid character " + part,"");
					this.facesContext.addMessage(null, fc);
					valid = false;
				}
			}
			//starts or ends in '-';
			if(part.startsWith("-") || part.endsWith("-")){
				FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid character " + part,"");
				this.facesContext.addMessage(null, fc);
				valid = false;
			}
			if(part.length() < 1){
				return false;
			}
			
			if(part.length() > 63){
				FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Hostname part too big " + part,"");
				this.facesContext.addMessage(null, fc);
				valid = false;
			}
		}
		return valid;
	}
	
	/**
	 * Validate IPV4
	 * @param ip the ip address
	 * @return true on valid ipv4 address, false otherwise
	 */
	public boolean isValidIPV4(String ip){
		boolean isValid = true;
		if(!ip.matches("^[0-9\\.]{7,15}$")){
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid format","");
			this.facesContext.addMessage(null, fc);
			isValid = false;
		}
		
		String[] ipQuads = ip.split("\\.");
		if(ipQuads != null && ipQuads.length !=4){
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IP part","");
			this.facesContext.addMessage(null, fc);
			isValid = false;
		}
		
		if(ipQuads != null){
			for(String quad : ipQuads){
				try {
					int parseInt = Integer.parseInt(quad);
					if(parseInt > 255){
						FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IP quad " + quad,"");
						this.facesContext.addMessage(null, fc);
						isValid = false;
					}
				} catch (NumberFormatException e){
					FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IP character","");
					this.facesContext.addMessage(null, fc);
					isValid = false;
				}
			}
		} else {
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IP","");
			this.facesContext.addMessage(null, fc);
			isValid = false;
		}
		
		return isValid;
	}
	

	/**
	 * 
	 * @param ip
	 * @return
	 */
	public boolean isValidIPV6(String ip){
		boolean isValid = true;
		if(!ip.matches("(?i)^[0-9a-f]{0,4}:([0-9a-f]{0,4}:){0,6}[0-9a-f]{0,4}$")){
			isValid = false;
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IPv6 address","");
			this.facesContext.addMessage(null, fc);
		}
		
		String[] hexParts = ip.split(":");
		if(hexParts == null){
			isValid = false;
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IPv6 address","");
			this.facesContext.addMessage(null, fc);
		}
		
		if(hexParts.length > 8 || hexParts.length < 3){
			isValid = false;
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid hex part","");
			this.facesContext.addMessage(null, fc);
		}
		
		int emptyHexParts = 0;
		for(String hexPart : hexParts){
			if(hexPart.isEmpty()){
				emptyHexParts++;
			}
		}
		
		if(emptyHexParts == 0 && hexParts.length != 8){
			isValid = false;
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid IPv6 format","");
			this.facesContext.addMessage(null, fc);

		}
		
		return isValid;
	}
		
	/**
	 * 
	 * @param string
	 * @return
	 */
	public boolean isPrintable(String string){
		boolean pritable = string.matches("^[\\p{Print}]+$");
		if(!pritable){
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Non printable characters found","");
			this.facesContext.addMessage(null, fc);
		}
		return pritable;
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public boolean isValidSPF(String content){
		if(SPF_PATTERN.matcher(content).matches()){
			return true;
		}
		FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid SRV target","");
		this.facesContext.addMessage(null, fc);
		return false;
	}

	/**
	 * Enclose the record content in quotation marks if there are spaces found. 
	 * Applicable for TXT and SRV records
	 * @param text 
	 * @return 
	 */
	private String quoteRecordContent(String text){
		if(text != null){
			if(text.split("\\s+").length > 1 && !(text.startsWith("\"") && text.endsWith("\""))){
				text = "\"" + text + "\"";
			}
		}
		return text;
	}

	
}
