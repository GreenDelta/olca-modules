package org.openlca.ecospold.internal.process;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.ILanguageCode;

/**
 * <p>
 * Java class for TISOLanguageCode.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="TISOLanguageCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;length value="2"/>
 *     &lt;enumeration value="ab"/>
 *     &lt;enumeration value="aa"/>
 *     &lt;enumeration value="af"/>
 *     &lt;enumeration value="sq"/>
 *     &lt;enumeration value="am"/>
 *     &lt;enumeration value="ar"/>
 *     &lt;enumeration value="hy"/>
 *     &lt;enumeration value="as"/>
 *     &lt;enumeration value="ay"/>
 *     &lt;enumeration value="az"/>
 *     &lt;enumeration value="ba"/>
 *     &lt;enumeration value="eu"/>
 *     &lt;enumeration value="bn"/>
 *     &lt;enumeration value="dz"/>
 *     &lt;enumeration value="bh"/>
 *     &lt;enumeration value="bi"/>
 *     &lt;enumeration value="br"/>
 *     &lt;enumeration value="bg"/>
 *     &lt;enumeration value="my"/>
 *     &lt;enumeration value="be"/>
 *     &lt;enumeration value="km"/>
 *     &lt;enumeration value="ca"/>
 *     &lt;enumeration value="zh"/>
 *     &lt;enumeration value="co"/>
 *     &lt;enumeration value="hr"/>
 *     &lt;enumeration value="cs"/>
 *     &lt;enumeration value="da"/>
 *     &lt;enumeration value="nl"/>
 *     &lt;enumeration value="en"/>
 *     &lt;enumeration value="eo"/>
 *     &lt;enumeration value="et"/>
 *     &lt;enumeration value="fo"/>
 *     &lt;enumeration value="fa"/>
 *     &lt;enumeration value="fj"/>
 *     &lt;enumeration value="fi"/>
 *     &lt;enumeration value="fr"/>
 *     &lt;enumeration value="fy"/>
 *     &lt;enumeration value="gl"/>
 *     &lt;enumeration value="ka"/>
 *     &lt;enumeration value="de"/>
 *     &lt;enumeration value="el"/>
 *     &lt;enumeration value="kl"/>
 *     &lt;enumeration value="gn"/>
 *     &lt;enumeration value="gu"/>
 *     &lt;enumeration value="ha"/>
 *     &lt;enumeration value="iw"/>
 *     &lt;enumeration value="he"/>
 *     &lt;enumeration value="hi"/>
 *     &lt;enumeration value="hu"/>
 *     &lt;enumeration value="is"/>
 *     &lt;enumeration value="in"/>
 *     &lt;enumeration value="id"/>
 *     &lt;enumeration value="ia"/>
 *     &lt;enumeration value="ie"/>
 *     &lt;enumeration value="iu"/>
 *     &lt;enumeration value="ik"/>
 *     &lt;enumeration value="ga"/>
 *     &lt;enumeration value="it"/>
 *     &lt;enumeration value="ja"/>
 *     &lt;enumeration value="jw"/>
 *     &lt;enumeration value="kn"/>
 *     &lt;enumeration value="ks"/>
 *     &lt;enumeration value="kk"/>
 *     &lt;enumeration value="rw"/>
 *     &lt;enumeration value="ky"/>
 *     &lt;enumeration value="rn"/>
 *     &lt;enumeration value="ko"/>
 *     &lt;enumeration value="ku"/>
 *     &lt;enumeration value="lo"/>
 *     &lt;enumeration value="la"/>
 *     &lt;enumeration value="lv"/>
 *     &lt;enumeration value="ln"/>
 *     &lt;enumeration value="lt"/>
 *     &lt;enumeration value="mk"/>
 *     &lt;enumeration value="mg"/>
 *     &lt;enumeration value="ms"/>
 *     &lt;enumeration value="ml"/>
 *     &lt;enumeration value="mt"/>
 *     &lt;enumeration value="gv"/>
 *     &lt;enumeration value="mi"/>
 *     &lt;enumeration value="mr"/>
 *     &lt;enumeration value="mo"/>
 *     &lt;enumeration value="mn"/>
 *     &lt;enumeration value="na"/>
 *     &lt;enumeration value="ne"/>
 *     &lt;enumeration value="no"/>
 *     &lt;enumeration value="oc"/>
 *     &lt;enumeration value="or"/>
 *     &lt;enumeration value="om"/>
 *     &lt;enumeration value="ps"/>
 *     &lt;enumeration value="pl"/>
 *     &lt;enumeration value="pt"/>
 *     &lt;enumeration value="pa"/>
 *     &lt;enumeration value="qu"/>
 *     &lt;enumeration value="rm"/>
 *     &lt;enumeration value="ro"/>
 *     &lt;enumeration value="ru"/>
 *     &lt;enumeration value="sm"/>
 *     &lt;enumeration value="sg"/>
 *     &lt;enumeration value="sa"/>
 *     &lt;enumeration value="gd"/>
 *     &lt;enumeration value="sr"/>
 *     &lt;enumeration value="sh"/>
 *     &lt;enumeration value="st"/>
 *     &lt;enumeration value="tn"/>
 *     &lt;enumeration value="sn"/>
 *     &lt;enumeration value="sd"/>
 *     &lt;enumeration value="si"/>
 *     &lt;enumeration value="ss"/>
 *     &lt;enumeration value="sk"/>
 *     &lt;enumeration value="sl"/>
 *     &lt;enumeration value="so"/>
 *     &lt;enumeration value="es"/>
 *     &lt;enumeration value="su"/>
 *     &lt;enumeration value="sw"/>
 *     &lt;enumeration value="sv"/>
 *     &lt;enumeration value="tl"/>
 *     &lt;enumeration value="tg"/>
 *     &lt;enumeration value="ta"/>
 *     &lt;enumeration value="tt"/>
 *     &lt;enumeration value="te"/>
 *     &lt;enumeration value="th"/>
 *     &lt;enumeration value="bo"/>
 *     &lt;enumeration value="ti"/>
 *     &lt;enumeration value="to"/>
 *     &lt;enumeration value="ts"/>
 *     &lt;enumeration value="tr"/>
 *     &lt;enumeration value="tk"/>
 *     &lt;enumeration value="tw"/>
 *     &lt;enumeration value="ug"/>
 *     &lt;enumeration value="uk"/>
 *     &lt;enumeration value="ur"/>
 *     &lt;enumeration value="uz"/>
 *     &lt;enumeration value="vi"/>
 *     &lt;enumeration value="vo"/>
 *     &lt;enumeration value="cy"/>
 *     &lt;enumeration value="wo"/>
 *     &lt;enumeration value="xh"/>
 *     &lt;enumeration value="ji"/>
 *     &lt;enumeration value="yi"/>
 *     &lt;enumeration value="yo"/>
 *     &lt;enumeration value="zu"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TISOLanguageCode")
@XmlEnum
enum LanguageCode implements ILanguageCode {

	@XmlEnumValue("ab")
	AB("ab"),
	@XmlEnumValue("aa")
	AA("aa"),
	@XmlEnumValue("af")
	AF("af"),
	@XmlEnumValue("sq")
	SQ("sq"),
	@XmlEnumValue("am")
	AM("am"),
	@XmlEnumValue("ar")
	AR("ar"),
	@XmlEnumValue("hy")
	HY("hy"),
	@XmlEnumValue("as")
	AS("as"),
	@XmlEnumValue("ay")
	AY("ay"),
	@XmlEnumValue("az")
	AZ("az"),
	@XmlEnumValue("ba")
	BA("ba"),
	@XmlEnumValue("eu")
	EU("eu"),
	@XmlEnumValue("bn")
	BN("bn"),
	@XmlEnumValue("dz")
	DZ("dz"),
	@XmlEnumValue("bh")
	BH("bh"),
	@XmlEnumValue("bi")
	BI("bi"),
	@XmlEnumValue("br")
	BR("br"),
	@XmlEnumValue("bg")
	BG("bg"),
	@XmlEnumValue("my")
	MY("my"),
	@XmlEnumValue("be")
	BE("be"),
	@XmlEnumValue("km")
	KM("km"),
	@XmlEnumValue("ca")
	CA("ca"),
	@XmlEnumValue("zh")
	ZH("zh"),
	@XmlEnumValue("co")
	CO("co"),
	@XmlEnumValue("hr")
	HR("hr"),
	@XmlEnumValue("cs")
	CS("cs"),
	@XmlEnumValue("da")
	DA("da"),
	@XmlEnumValue("nl")
	NL("nl"),
	@XmlEnumValue("en")
	EN("en"),
	@XmlEnumValue("eo")
	EO("eo"),
	@XmlEnumValue("et")
	ET("et"),
	@XmlEnumValue("fo")
	FO("fo"),
	@XmlEnumValue("fa")
	FA("fa"),
	@XmlEnumValue("fj")
	FJ("fj"),
	@XmlEnumValue("fi")
	FI("fi"),
	@XmlEnumValue("fr")
	FR("fr"),
	@XmlEnumValue("fy")
	FY("fy"),
	@XmlEnumValue("gl")
	GL("gl"),
	@XmlEnumValue("ka")
	KA("ka"),
	@XmlEnumValue("de")
	DE("de"),
	@XmlEnumValue("el")
	EL("el"),
	@XmlEnumValue("kl")
	KL("kl"),
	@XmlEnumValue("gn")
	GN("gn"),
	@XmlEnumValue("gu")
	GU("gu"),
	@XmlEnumValue("ha")
	HA("ha"),
	@XmlEnumValue("iw")
	IW("iw"),
	@XmlEnumValue("he")
	HE("he"),
	@XmlEnumValue("hi")
	HI("hi"),
	@XmlEnumValue("hu")
	HU("hu"),
	@XmlEnumValue("is")
	IS("is"),
	@XmlEnumValue("in")
	IN("in"),
	@XmlEnumValue("id")
	ID("id"),
	@XmlEnumValue("ia")
	IA("ia"),
	@XmlEnumValue("ie")
	IE("ie"),
	@XmlEnumValue("iu")
	IU("iu"),
	@XmlEnumValue("ik")
	IK("ik"),
	@XmlEnumValue("ga")
	GA("ga"),
	@XmlEnumValue("it")
	IT("it"),
	@XmlEnumValue("ja")
	JA("ja"),
	@XmlEnumValue("jw")
	JW("jw"),
	@XmlEnumValue("kn")
	KN("kn"),
	@XmlEnumValue("ks")
	KS("ks"),
	@XmlEnumValue("kk")
	KK("kk"),
	@XmlEnumValue("rw")
	RW("rw"),
	@XmlEnumValue("ky")
	KY("ky"),
	@XmlEnumValue("rn")
	RN("rn"),
	@XmlEnumValue("ko")
	KO("ko"),
	@XmlEnumValue("ku")
	KU("ku"),
	@XmlEnumValue("lo")
	LO("lo"),
	@XmlEnumValue("la")
	LA("la"),
	@XmlEnumValue("lv")
	LV("lv"),
	@XmlEnumValue("ln")
	LN("ln"),
	@XmlEnumValue("lt")
	LT("lt"),
	@XmlEnumValue("mk")
	MK("mk"),
	@XmlEnumValue("mg")
	MG("mg"),
	@XmlEnumValue("ms")
	MS("ms"),
	@XmlEnumValue("ml")
	ML("ml"),
	@XmlEnumValue("mt")
	MT("mt"),
	@XmlEnumValue("gv")
	GV("gv"),
	@XmlEnumValue("mi")
	MI("mi"),
	@XmlEnumValue("mr")
	MR("mr"),
	@XmlEnumValue("mo")
	MO("mo"),
	@XmlEnumValue("mn")
	MN("mn"),
	@XmlEnumValue("na")
	NA("na"),
	@XmlEnumValue("ne")
	NE("ne"),
	@XmlEnumValue("no")
	NO("no"),
	@XmlEnumValue("oc")
	OC("oc"),
	@XmlEnumValue("or")
	OR("or"),
	@XmlEnumValue("om")
	OM("om"),
	@XmlEnumValue("ps")
	PS("ps"),
	@XmlEnumValue("pl")
	PL("pl"),
	@XmlEnumValue("pt")
	PT("pt"),
	@XmlEnumValue("pa")
	PA("pa"),
	@XmlEnumValue("qu")
	QU("qu"),
	@XmlEnumValue("rm")
	RM("rm"),
	@XmlEnumValue("ro")
	RO("ro"),
	@XmlEnumValue("ru")
	RU("ru"),
	@XmlEnumValue("sm")
	SM("sm"),
	@XmlEnumValue("sg")
	SG("sg"),
	@XmlEnumValue("sa")
	SA("sa"),
	@XmlEnumValue("gd")
	GD("gd"),
	@XmlEnumValue("sr")
	SR("sr"),
	@XmlEnumValue("sh")
	SH("sh"),
	@XmlEnumValue("st")
	ST("st"),
	@XmlEnumValue("tn")
	TN("tn"),
	@XmlEnumValue("sn")
	SN("sn"),
	@XmlEnumValue("sd")
	SD("sd"),
	@XmlEnumValue("si")
	SI("si"),
	@XmlEnumValue("ss")
	SS("ss"),
	@XmlEnumValue("sk")
	SK("sk"),
	@XmlEnumValue("sl")
	SL("sl"),
	@XmlEnumValue("so")
	SO("so"),
	@XmlEnumValue("es")
	ES("es"),
	@XmlEnumValue("su")
	SU("su"),
	@XmlEnumValue("sw")
	SW("sw"),
	@XmlEnumValue("sv")
	SV("sv"),
	@XmlEnumValue("tl")
	TL("tl"),
	@XmlEnumValue("tg")
	TG("tg"),
	@XmlEnumValue("ta")
	TA("ta"),
	@XmlEnumValue("tt")
	TT("tt"),
	@XmlEnumValue("te")
	TE("te"),
	@XmlEnumValue("th")
	TH("th"),
	@XmlEnumValue("bo")
	BO("bo"),
	@XmlEnumValue("ti")
	TI("ti"),
	@XmlEnumValue("to")
	TO("to"),
	@XmlEnumValue("ts")
	TS("ts"),
	@XmlEnumValue("tr")
	TR("tr"),
	@XmlEnumValue("tk")
	TK("tk"),
	@XmlEnumValue("tw")
	TW("tw"),
	@XmlEnumValue("ug")
	UG("ug"),
	@XmlEnumValue("uk")
	UK("uk"),
	@XmlEnumValue("ur")
	UR("ur"),
	@XmlEnumValue("uz")
	UZ("uz"),
	@XmlEnumValue("vi")
	VI("vi"),
	@XmlEnumValue("vo")
	VO("vo"),
	@XmlEnumValue("cy")
	CY("cy"),
	@XmlEnumValue("wo")
	WO("wo"),
	@XmlEnumValue("xh")
	XH("xh"),
	@XmlEnumValue("ji")
	JI("ji"),
	@XmlEnumValue("yi")
	YI("yi"),
	@XmlEnumValue("yo")
	YO("yo"),
	@XmlEnumValue("zu")
	ZU("zu");
	private final String value;

	LanguageCode(String v) {
		value = v;
	}

	@Override
	public String value() {
		return value;
	}

	public static LanguageCode fromValue(String v) {
		if (v == null)
			return null;
		String trimmed = v.trim().toLowerCase();
		for (LanguageCode c : LanguageCode.values()) {
			if (c.value.equals(trimmed)) {
				return c;
			}
		}
		return null;
	}

}
