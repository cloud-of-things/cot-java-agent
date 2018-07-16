package com.telekom.cot.device.agent.platform.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AgentMobile implements AgentFragment {
	public final String imsi;
	public final String imei;
	public final String currentOperator;
	public final String currentBand;
	public final String connType;
	public final String rssi;
	public final String ecn0;
	public final String rcsp;
	public final String mnc;
	public final String lac;
	public final String cellId;
	public final String msisdn;
	public final String iccid;

	// This flag differentiates between the simple version (device management lib;
	// false) and the
	// complete, verbose version (sensor lib; true).
	private boolean isComplete = true;

	/**
	 * This is the partial constructor for c8y_Mobile as described in the device
	 * management library.
	 *
	 * @param imei
	 *            the International Mobile Equipment Number.
	 * @param cellId
	 *            the identifier of the current cell tower.
	 * @param iccid
	 *            the Integrated Curcuit Identifier.
	 */
	public AgentMobile(String imei, String cellId, String iccid) {
		this(null, imei, null, null, null, null, null, null, null, null, cellId, null, iccid);
		isComplete = false;
	}

	/**
	 * This is the full constructor for c8y_Mobile as described in the sensor
	 * library.
	 *
	 * @param imsi
	 *            the International Mobile Subscriber Identifier.
	 * @param imei
	 *            the International Mobile Equipment Number.
	 * @param currentOperator
	 *            the received mobile operator string, e.g. Telekom.
	 * @param currentBand
	 *            the current mobile band, e.g. WCDMA2100.
	 * @param connType
	 *            the current connection type, e.g. 3g
	 * @param rssi
	 *            the Receive Signal Strength Indicator value as text.
	 * @param ecn0
	 *            EcNo is the RSCP divided by the RSSI
	 * @param rcsp
	 *            Received Signal Code Power as text.
	 * @param mnc
	 *            the Mobile Network Code, e.g. 1 (for Telekom Deutschland).
	 * @param lac
	 *            Location Area Code, e.g. 38833 (for Bonn Area in Telekom
	 *            Deutschland network).
	 * @param cellId
	 *            the identifier of the current cell tower.
	 * @param msisdn
	 *            the Mobile Station Integrated Services Digital Network Number.
	 * @param iccid
	 *            the Integrated Curcuit Identifier.
	 */
	public AgentMobile(String imsi, String imei, String currentOperator, String currentBand, String connType,
			String rssi, String ecn0, String rcsp, String mnc, String lac, String cellId, String msisdn, String iccid) {

		this.imsi = imsi;
		this.imei = imei;
		this.currentOperator = currentOperator;
		this.currentBand = currentBand;
		this.connType = connType;
		this.rssi = rssi;
		this.ecn0 = ecn0;
		this.rcsp = rcsp;
		this.mnc = mnc;
		this.lac = lac;
		this.cellId = cellId;
		this.msisdn = msisdn;
		this.iccid = iccid;
	}

	/**
	 * Get IMEI
	 * 
	 * @return IMEI
	 */
	public String getImei() {
		return imei;
	}
	
	/**
	 * Get cell id
	 * 
	 * @return Cell id
	 */
	public String getCellId() {
		return cellId;
	}

	/**
	 * Get ICCID
	 * 
	 * @return ICCID
	 */
	public String getIccid() {
		return iccid;
	}

	@Override
	public String getId() {
		return "c8y_Mobile";
	}

	@Override
	public JsonElement getJson() {
		JsonObject object = new JsonObject();
		object.addProperty("imei", imei);
		object.addProperty("cellId", cellId);
		object.addProperty("iccid", iccid);

		if (isComplete) {
			object.addProperty("imsi", imsi);
			object.addProperty("currentOperator", currentOperator);
			object.addProperty("currentBand", currentBand);
			object.addProperty("connType", connType);
			object.addProperty("rssi", rssi);
			object.addProperty("ecn0", ecn0);
			object.addProperty("rcsp", rcsp);
			object.addProperty("mnc", mnc);
			object.addProperty("lac", lac);
			object.addProperty("msisdn", msisdn);
		}
		return object;
	}

}
