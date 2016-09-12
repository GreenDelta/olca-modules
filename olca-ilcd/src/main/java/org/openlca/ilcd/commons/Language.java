package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Languages")
@XmlEnum
public enum Language {

	/**
	 * Afar
	 * 
	 */
	@XmlEnumValue("aa")
	AA("aa"),

	/**
	 * Abkhazian
	 * 
	 */
	@XmlEnumValue("ab")
	AB("ab"),

	/**
	 * Avestan
	 * 
	 */
	@XmlEnumValue("ae")
	AE("ae"),

	/**
	 * Afrikaans
	 * 
	 */
	@XmlEnumValue("af")
	AF("af"),

	/**
	 * Akan
	 * 
	 */
	@XmlEnumValue("ak")
	AK("ak"),

	/**
	 * Amharic
	 * 
	 */
	@XmlEnumValue("am")
	AM("am"),

	/**
	 * Aragonese
	 * 
	 */
	@XmlEnumValue("an")
	AN("an"),

	/**
	 * Arabic
	 * 
	 */
	@XmlEnumValue("ar")
	AR("ar"),

	/**
	 * Assamese
	 * 
	 */
	@XmlEnumValue("as")
	AS("as"),

	/**
	 * Avaric
	 * 
	 */
	@XmlEnumValue("av")
	AV("av"),

	/**
	 * Aymara
	 * 
	 */
	@XmlEnumValue("ay")
	AY("ay"),

	/**
	 * Azerbaijani
	 * 
	 */
	@XmlEnumValue("az")
	AZ("az"),

	/**
	 * Bashkir
	 * 
	 */
	@XmlEnumValue("ba")
	BA("ba"),

	/**
	 * Belarusian
	 * 
	 */
	@XmlEnumValue("be")
	BE("be"),

	/**
	 * Bulgarian
	 * 
	 */
	@XmlEnumValue("bg")
	BG("bg"),

	/**
	 * Bihari
	 * 
	 */
	@XmlEnumValue("bh")
	BH("bh"),

	/**
	 * Bislama
	 * 
	 */
	@XmlEnumValue("bi")
	BI("bi"),

	/**
	 * Bambara
	 * 
	 */
	@XmlEnumValue("bm")
	BM("bm"),

	/**
	 * Bengali
	 * 
	 */
	@XmlEnumValue("bn")
	BN("bn"),

	/**
	 * Tibetan
	 * 
	 */
	@XmlEnumValue("bo")
	BO("bo"),

	/**
	 * Breton
	 * 
	 */
	@XmlEnumValue("br")
	BR("br"),

	/**
	 * Bosnian
	 * 
	 */
	@XmlEnumValue("bs")
	BS("bs"),

	/**
	 * Catalan; Valencian
	 * 
	 */
	@XmlEnumValue("ca")
	CA("ca"),

	/**
	 * Chechen
	 * 
	 */
	@XmlEnumValue("ce")
	CE("ce"),

	/**
	 * Chamorro
	 * 
	 */
	@XmlEnumValue("ch")
	CH("ch"),

	/**
	 * Corsican
	 * 
	 */
	@XmlEnumValue("co")
	CO("co"),

	/**
	 * Cree
	 * 
	 */
	@XmlEnumValue("cr")
	CR("cr"),

	/**
	 * Czech
	 * 
	 */
	@XmlEnumValue("cs")
	CS("cs"),

	/**
	 * Church Slavic; Slavonic; Church Slavonic; Old Bulgarian; Old Church
	 * Slavonic
	 * 
	 */
	@XmlEnumValue("cu")
	CU("cu"),

	/**
	 * Chuvash
	 * 
	 */
	@XmlEnumValue("cv")
	CV("cv"),

	/**
	 * Welsh
	 * 
	 */
	@XmlEnumValue("cy")
	CY("cy"),

	/**
	 * Danish
	 * 
	 */
	@XmlEnumValue("da")
	DA("da"),

	/**
	 * German
	 * 
	 */
	@XmlEnumValue("de")
	DE("de"),

	/**
	 * Divehi
	 * 
	 */
	@XmlEnumValue("dv")
	DV("dv"),

	/**
	 * Dzongkha
	 * 
	 */
	@XmlEnumValue("dz")
	DZ("dz"),

	/**
	 * Ewe
	 * 
	 */
	@XmlEnumValue("ee")
	EE("ee"),

	/**
	 * Greek, Modern (1453-)
	 * 
	 */
	@XmlEnumValue("el")
	EL("el"),

	/**
	 * English
	 * 
	 */
	@XmlEnumValue("en")
	EN("en"),

	/**
	 * Esperanto
	 * 
	 */
	@XmlEnumValue("eo")
	EO("eo"),

	/**
	 * Castilian; Spanish
	 * 
	 */
	@XmlEnumValue("es")
	ES("es"),

	/**
	 * Estonian
	 * 
	 */
	@XmlEnumValue("et")
	ET("et"),

	/**
	 * Basque
	 * 
	 */
	@XmlEnumValue("eu")
	EU("eu"),

	/**
	 * Persian
	 * 
	 */
	@XmlEnumValue("fa")
	FA("fa"),

	/**
	 * Fulah
	 * 
	 */
	@XmlEnumValue("ff")
	FF("ff"),

	/**
	 * Finnish
	 * 
	 */
	@XmlEnumValue("fi")
	FI("fi"),

	/**
	 * Fijian
	 * 
	 */
	@XmlEnumValue("fj")
	FJ("fj"),

	/**
	 * Faroese
	 * 
	 */
	@XmlEnumValue("fo")
	FO("fo"),

	/**
	 * French
	 * 
	 */
	@XmlEnumValue("fr")
	FR("fr"),

	/**
	 * Frisian
	 * 
	 */
	@XmlEnumValue("fy")
	FY("fy"),

	/**
	 * Irish
	 * 
	 */
	@XmlEnumValue("ga")
	GA("ga"),

	/**
	 * Gaelic; Scottish Gaelic
	 * 
	 */
	@XmlEnumValue("gd")
	GD("gd"),

	/**
	 * Galician
	 * 
	 */
	@XmlEnumValue("gl")
	GL("gl"),

	/**
	 * Guarani
	 * 
	 */
	@XmlEnumValue("gn")
	GN("gn"),

	/**
	 * Gujarati
	 * 
	 */
	@XmlEnumValue("gu")
	GU("gu"),

	/**
	 * Manx
	 * 
	 */
	@XmlEnumValue("gv")
	GV("gv"),

	/**
	 * Hausa
	 * 
	 */
	@XmlEnumValue("ha")
	HA("ha"),

	/**
	 * Hebrew
	 * 
	 */
	@XmlEnumValue("he")
	HE("he"),

	/**
	 * Hindi
	 * 
	 */
	@XmlEnumValue("hi")
	HI("hi"),

	/**
	 * Hiri Motu
	 * 
	 */
	@XmlEnumValue("ho")
	HO("ho"),

	/**
	 * Croatian
	 * 
	 */
	@XmlEnumValue("hr")
	HR("hr"),

	/**
	 * Haitian; Haitian Creole
	 * 
	 */
	@XmlEnumValue("ht")
	HT("ht"),

	/**
	 * Hungarian
	 * 
	 */
	@XmlEnumValue("hu")
	HU("hu"),

	/**
	 * Armenian
	 * 
	 */
	@XmlEnumValue("hy")
	HY("hy"),

	/**
	 * Herero
	 * 
	 */
	@XmlEnumValue("hz")
	HZ("hz"),

	/**
	 * Interlingua (International
	 * 
	 */
	@XmlEnumValue("ia")
	IA("ia"),

	/**
	 * Indonesian
	 * 
	 */
	@XmlEnumValue("id")
	ID("id"),

	/**
	 * Interlingue
	 * 
	 */
	@XmlEnumValue("ie")
	IE("ie"),

	/**
	 * Igbo
	 * 
	 */
	@XmlEnumValue("ig")
	IG("ig"),

	/**
	 * Sichuan Yi
	 * 
	 */
	@XmlEnumValue("ii")
	II("ii"),

	/**
	 * Inupiaq
	 * 
	 */
	@XmlEnumValue("ik")
	IK("ik"),

	/**
	 * Ido
	 * 
	 */
	@XmlEnumValue("io")
	IO("io"),

	/**
	 * Icelandic
	 * 
	 */
	@XmlEnumValue("is")
	IS("is"),

	/**
	 * Italian
	 * 
	 */
	@XmlEnumValue("it")
	IT("it"),

	/**
	 * Inuktitut
	 * 
	 */
	@XmlEnumValue("iu")
	IU("iu"),

	/**
	 * Japanese
	 * 
	 */
	@XmlEnumValue("ja")
	JA("ja"),

	/**
	 * Javanese
	 * 
	 */
	@XmlEnumValue("jv")
	JV("jv"),

	/**
	 * Georgian
	 * 
	 */
	@XmlEnumValue("ka")
	KA("ka"),

	/**
	 * Kongo
	 * 
	 */
	@XmlEnumValue("kg")
	KG("kg"),

	/**
	 * Kikuyu; Gikuyu
	 * 
	 */
	@XmlEnumValue("ki")
	KI("ki"),

	/**
	 * Kuanyama; Kwanyama
	 * 
	 */
	@XmlEnumValue("kj")
	KJ("kj"),

	/**
	 * Kazakh
	 * 
	 */
	@XmlEnumValue("kk")
	KK("kk"),

	/**
	 * Greenlandic; Kalaallisut
	 * 
	 */
	@XmlEnumValue("kl")
	KL("kl"),

	/**
	 * Khmer
	 * 
	 */
	@XmlEnumValue("km")
	KM("km"),

	/**
	 * Kannada
	 * 
	 */
	@XmlEnumValue("kn")
	KN("kn"),

	/**
	 * Korean
	 * 
	 */
	@XmlEnumValue("ko")
	KO("ko"),

	/**
	 * Kanuri
	 * 
	 */
	@XmlEnumValue("kr")
	KR("kr"),

	/**
	 * Kashmiri
	 * 
	 */
	@XmlEnumValue("ks")
	KS("ks"),

	/**
	 * Kurdish
	 * 
	 */
	@XmlEnumValue("ku")
	KU("ku"),

	/**
	 * Komi
	 * 
	 */
	@XmlEnumValue("kv")
	KV("kv"),

	/**
	 * Cornish
	 * 
	 */
	@XmlEnumValue("kw")
	KW("kw"),

	/**
	 * Kirghiz
	 * 
	 */
	@XmlEnumValue("ky")
	KY("ky"),

	/**
	 * Latin
	 * 
	 */
	@XmlEnumValue("la")
	LA("la"),

	/**
	 * Luxembourgish; Letzeburgesch
	 * 
	 */
	@XmlEnumValue("lb")
	LB("lb"),

	/**
	 * Ganda
	 * 
	 */
	@XmlEnumValue("lg")
	LG("lg"),

	/**
	 * Limburgan; Limburger; Limburgish
	 * 
	 */
	@XmlEnumValue("li")
	LI("li"),

	/**
	 * Lingala
	 * 
	 */
	@XmlEnumValue("ln")
	LN("ln"),

	/**
	 * Lao
	 * 
	 */
	@XmlEnumValue("lo")
	LO("lo"),

	/**
	 * Lithuanian
	 * 
	 */
	@XmlEnumValue("lt")
	LT("lt"),

	/**
	 * Luba-Katanga
	 * 
	 */
	@XmlEnumValue("lu")
	LU("lu"),

	/**
	 * Latvian
	 * 
	 */
	@XmlEnumValue("lv")
	LV("lv"),

	/**
	 * Malagasy
	 * 
	 */
	@XmlEnumValue("mg")
	MG("mg"),

	/**
	 * Marshallese
	 * 
	 */
	@XmlEnumValue("mh")
	MH("mh"),

	/**
	 * Maori
	 * 
	 */
	@XmlEnumValue("mi")
	MI("mi"),

	/**
	 * Macedonian
	 * 
	 */
	@XmlEnumValue("mk")
	MK("mk"),

	/**
	 * Malayalam
	 * 
	 */
	@XmlEnumValue("ml")
	ML("ml"),

	/**
	 * Mongolian
	 * 
	 */
	@XmlEnumValue("mn")
	MN("mn"),

	/**
	 * Moldavian
	 * 
	 */
	@XmlEnumValue("mo")
	MO("mo"),

	/**
	 * Marathi
	 * 
	 */
	@XmlEnumValue("mr")
	MR("mr"),

	/**
	 * Malay
	 * 
	 */
	@XmlEnumValue("ms")
	MS("ms"),

	/**
	 * Maltese
	 * 
	 */
	@XmlEnumValue("mt")
	MT("mt"),

	/**
	 * Burmese
	 * 
	 */
	@XmlEnumValue("my")
	MY("my"),

	/**
	 * Nauru
	 * 
	 */
	@XmlEnumValue("na")
	NA("na"),

	/**
	 * Norwegian, Norwegian
	 * 
	 */
	@XmlEnumValue("nb")
	NB("nb"),

	/**
	 * Ndebele, North
	 * 
	 */
	@XmlEnumValue("nd")
	ND("nd"),

	/**
	 * Nepali
	 * 
	 */
	@XmlEnumValue("ne")
	NE("ne"),

	/**
	 * Ndonga
	 * 
	 */
	@XmlEnumValue("ng")
	NG("ng"),

	/**
	 * Dutch; Flemish
	 * 
	 */
	@XmlEnumValue("nl")
	NL("nl"),

	/**
	 * Norwegian Nynorsk; Nynorsk, Norwegian
	 * 
	 */
	@XmlEnumValue("nn")
	NN("nn"),

	/**
	 * Norwegian
	 * 
	 */
	@XmlEnumValue("no")
	NO("no"),

	/**
	 * Ndebele, South
	 * 
	 */
	@XmlEnumValue("nr")
	NR("nr"),

	/**
	 * Navajo; Navaho
	 * 
	 */
	@XmlEnumValue("nv")
	NV("nv"),

	/**
	 * Nyanja; Chichewa; Chewa
	 * 
	 */
	@XmlEnumValue("ny")
	NY("ny"),

	/**
	 * Proven�al; Occitan (post 1500)
	 * 
	 */
	@XmlEnumValue("oc")
	OC("oc"),

	/**
	 * Ojibwa
	 * 
	 */
	@XmlEnumValue("oj")
	OJ("oj"),

	/**
	 * Oromo
	 * 
	 */
	@XmlEnumValue("om")
	OM("om"),

	/**
	 * Oriya
	 * 
	 */
	@XmlEnumValue("or")
	OR("or"),

	/**
	 * Ossetian; Ossetic
	 * 
	 */
	@XmlEnumValue("os")
	OS("os"),

	/**
	 * Punjabi; Panjabi
	 * 
	 */
	@XmlEnumValue("pa")
	PA("pa"),

	/**
	 * Pali
	 * 
	 */
	@XmlEnumValue("pi")
	PI("pi"),

	/**
	 * Polish
	 * 
	 */
	@XmlEnumValue("pl")
	PL("pl"),

	/**
	 * Pushto
	 * 
	 */
	@XmlEnumValue("ps")
	PS("ps"),

	/**
	 * Portuguese
	 * 
	 */
	@XmlEnumValue("pt")
	PT("pt"),

	/**
	 * Quechua
	 * 
	 */
	@XmlEnumValue("qu")
	QU("qu"),

	/**
	 * Raeto-Romance
	 * 
	 */
	@XmlEnumValue("rm")
	RM("rm"),

	/**
	 * Rundi
	 * 
	 */
	@XmlEnumValue("rn")
	RN("rn"),

	/**
	 * Romanian
	 * 
	 */
	@XmlEnumValue("ro")
	RO("ro"),

	/**
	 * Russian
	 * 
	 */
	@XmlEnumValue("ru")
	RU("ru"),

	/**
	 * Kinyarwanda
	 * 
	 */
	@XmlEnumValue("rw")
	RW("rw"),

	/**
	 * Sanskrit
	 * 
	 */
	@XmlEnumValue("sa")
	SA("sa"),

	/**
	 * Sardinian
	 * 
	 */
	@XmlEnumValue("sc")
	SC("sc"),

	/**
	 * Sindhi
	 * 
	 */
	@XmlEnumValue("sd")
	SD("sd"),

	/**
	 * Northern Sami
	 * 
	 */
	@XmlEnumValue("se")
	SE("se"),

	/**
	 * Sango
	 * 
	 */
	@XmlEnumValue("sg")
	SG("sg"),

	/**
	 * Sinhala; Sinhalese
	 * 
	 */
	@XmlEnumValue("si")
	SI("si"),

	/**
	 * Slovak
	 * 
	 */
	@XmlEnumValue("sk")
	SK("sk"),

	/**
	 * Slovenian
	 * 
	 */
	@XmlEnumValue("sl")
	SL("sl"),

	/**
	 * Samoan
	 * 
	 */
	@XmlEnumValue("sm")
	SM("sm"),

	/**
	 * Shona
	 * 
	 */
	@XmlEnumValue("sn")
	SN("sn"),

	/**
	 * Somali
	 * 
	 */
	@XmlEnumValue("so")
	SO("so"),

	/**
	 * Albanian
	 * 
	 */
	@XmlEnumValue("sq")
	SQ("sq"),

	/**
	 * Serbian
	 * 
	 */
	@XmlEnumValue("sr")
	SR("sr"),

	/**
	 * Swati
	 * 
	 */
	@XmlEnumValue("ss")
	SS("ss"),

	/**
	 * Sotho, Southern
	 * 
	 */
	@XmlEnumValue("st")
	ST("st"),

	/**
	 * Sundanese
	 * 
	 */
	@XmlEnumValue("su")
	SU("su"),

	/**
	 * Swedish
	 * 
	 */
	@XmlEnumValue("sv")
	SV("sv"),

	/**
	 * Swahili
	 * 
	 */
	@XmlEnumValue("sw")
	SW("sw"),

	/**
	 * Tamil
	 * 
	 */
	@XmlEnumValue("ta")
	TA("ta"),

	/**
	 * Telugu
	 * 
	 */
	@XmlEnumValue("te")
	TE("te"),

	/**
	 * Tajik
	 * 
	 */
	@XmlEnumValue("tg")
	TG("tg"),

	/**
	 * Thai
	 * 
	 */
	@XmlEnumValue("th")
	TH("th"),

	/**
	 * Tigrinya
	 * 
	 */
	@XmlEnumValue("ti")
	TI("ti"),

	/**
	 * Turkmen
	 * 
	 */
	@XmlEnumValue("tk")
	TK("tk"),

	/**
	 * Tagalog
	 * 
	 */
	@XmlEnumValue("tl")
	TL("tl"),

	/**
	 * Tswana
	 * 
	 */
	@XmlEnumValue("tn")
	TN("tn"),

	/**
	 * Tonga (Tonga Islands)
	 * 
	 */
	@XmlEnumValue("to")
	TO("to"),

	/**
	 * Turkish
	 * 
	 */
	@XmlEnumValue("tr")
	TR("tr"),

	/**
	 * Tsonga
	 * 
	 */
	@XmlEnumValue("ts")
	TS("ts"),

	/**
	 * Tatar
	 * 
	 */
	@XmlEnumValue("tt")
	TT("tt"),

	/**
	 * Twi
	 * 
	 */
	@XmlEnumValue("tw")
	TW("tw"),

	/**
	 * Tahitian
	 * 
	 */
	@XmlEnumValue("ty")
	TY("ty"),

	/**
	 * Uighur; Uyghur
	 * 
	 */
	@XmlEnumValue("ug")
	UG("ug"),

	/**
	 * Ukrainian
	 * 
	 */
	@XmlEnumValue("uk")
	UK("uk"),

	/**
	 * Urdu
	 * 
	 */
	@XmlEnumValue("ur")
	UR("ur"),

	/**
	 * Uzbek
	 * 
	 */
	@XmlEnumValue("uz")
	UZ("uz"),

	/**
	 * Venda
	 * 
	 */
	@XmlEnumValue("ve")
	VE("ve"),

	/**
	 * Vietnamese
	 * 
	 */
	@XmlEnumValue("vi")
	VI("vi"),

	/**
	 * Volap�k
	 * 
	 */
	@XmlEnumValue("vo")
	VO("vo"),

	/**
	 * Walloon
	 * 
	 */
	@XmlEnumValue("wa")
	WA("wa"),

	/**
	 * Wolof
	 * 
	 */
	@XmlEnumValue("wo")
	WO("wo"),

	/**
	 * Xhosa
	 * 
	 */
	@XmlEnumValue("xh")
	XH("xh"),

	/**
	 * Yiddish
	 * 
	 */
	@XmlEnumValue("yi")
	YI("yi"),

	/**
	 * Yoruba
	 * 
	 */
	@XmlEnumValue("yo")
	YO("yo"),

	/**
	 * Chuang; Zhuang
	 * 
	 */
	@XmlEnumValue("za")
	ZA("za"),

	/**
	 * Chinese
	 * 
	 */
	@XmlEnumValue("zh")
	ZH("zh"),

	/**
	 * Zulu
	 * 
	 */
	@XmlEnumValue("zu")
	ZU("zu");
	private final String value;

	Language(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static Language fromValue(String v) {
		for (Language c : Language.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
