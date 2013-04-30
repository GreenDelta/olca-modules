
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Languages.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Languages">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="aa"/>
 *     &lt;enumeration value="ab"/>
 *     &lt;enumeration value="ae"/>
 *     &lt;enumeration value="af"/>
 *     &lt;enumeration value="ak"/>
 *     &lt;enumeration value="am"/>
 *     &lt;enumeration value="an"/>
 *     &lt;enumeration value="ar"/>
 *     &lt;enumeration value="as"/>
 *     &lt;enumeration value="av"/>
 *     &lt;enumeration value="ay"/>
 *     &lt;enumeration value="az"/>
 *     &lt;enumeration value="ba"/>
 *     &lt;enumeration value="be"/>
 *     &lt;enumeration value="bg"/>
 *     &lt;enumeration value="bh"/>
 *     &lt;enumeration value="bi"/>
 *     &lt;enumeration value="bm"/>
 *     &lt;enumeration value="bn"/>
 *     &lt;enumeration value="bo"/>
 *     &lt;enumeration value="br"/>
 *     &lt;enumeration value="bs"/>
 *     &lt;enumeration value="ca"/>
 *     &lt;enumeration value="ce"/>
 *     &lt;enumeration value="ch"/>
 *     &lt;enumeration value="co"/>
 *     &lt;enumeration value="cr"/>
 *     &lt;enumeration value="cs"/>
 *     &lt;enumeration value="cu"/>
 *     &lt;enumeration value="cv"/>
 *     &lt;enumeration value="cy"/>
 *     &lt;enumeration value="da"/>
 *     &lt;enumeration value="de"/>
 *     &lt;enumeration value="dv"/>
 *     &lt;enumeration value="dz"/>
 *     &lt;enumeration value="ee"/>
 *     &lt;enumeration value="el"/>
 *     &lt;enumeration value="en"/>
 *     &lt;enumeration value="eo"/>
 *     &lt;enumeration value="es"/>
 *     &lt;enumeration value="et"/>
 *     &lt;enumeration value="eu"/>
 *     &lt;enumeration value="fa"/>
 *     &lt;enumeration value="ff"/>
 *     &lt;enumeration value="fi"/>
 *     &lt;enumeration value="fj"/>
 *     &lt;enumeration value="fo"/>
 *     &lt;enumeration value="fr"/>
 *     &lt;enumeration value="fy"/>
 *     &lt;enumeration value="ga"/>
 *     &lt;enumeration value="gd"/>
 *     &lt;enumeration value="gl"/>
 *     &lt;enumeration value="gn"/>
 *     &lt;enumeration value="gu"/>
 *     &lt;enumeration value="gv"/>
 *     &lt;enumeration value="ha"/>
 *     &lt;enumeration value="he"/>
 *     &lt;enumeration value="hi"/>
 *     &lt;enumeration value="ho"/>
 *     &lt;enumeration value="hr"/>
 *     &lt;enumeration value="ht"/>
 *     &lt;enumeration value="hu"/>
 *     &lt;enumeration value="hy"/>
 *     &lt;enumeration value="hz"/>
 *     &lt;enumeration value="ia"/>
 *     &lt;enumeration value="id"/>
 *     &lt;enumeration value="ie"/>
 *     &lt;enumeration value="ig"/>
 *     &lt;enumeration value="ii"/>
 *     &lt;enumeration value="ik"/>
 *     &lt;enumeration value="io"/>
 *     &lt;enumeration value="is"/>
 *     &lt;enumeration value="it"/>
 *     &lt;enumeration value="iu"/>
 *     &lt;enumeration value="ja"/>
 *     &lt;enumeration value="jv"/>
 *     &lt;enumeration value="ka"/>
 *     &lt;enumeration value="kg"/>
 *     &lt;enumeration value="ki"/>
 *     &lt;enumeration value="kj"/>
 *     &lt;enumeration value="kk"/>
 *     &lt;enumeration value="kl"/>
 *     &lt;enumeration value="km"/>
 *     &lt;enumeration value="kn"/>
 *     &lt;enumeration value="ko"/>
 *     &lt;enumeration value="kr"/>
 *     &lt;enumeration value="ks"/>
 *     &lt;enumeration value="ku"/>
 *     &lt;enumeration value="kv"/>
 *     &lt;enumeration value="kw"/>
 *     &lt;enumeration value="ky"/>
 *     &lt;enumeration value="la"/>
 *     &lt;enumeration value="lb"/>
 *     &lt;enumeration value="lg"/>
 *     &lt;enumeration value="li"/>
 *     &lt;enumeration value="ln"/>
 *     &lt;enumeration value="lo"/>
 *     &lt;enumeration value="lt"/>
 *     &lt;enumeration value="lu"/>
 *     &lt;enumeration value="lv"/>
 *     &lt;enumeration value="mg"/>
 *     &lt;enumeration value="mh"/>
 *     &lt;enumeration value="mi"/>
 *     &lt;enumeration value="mk"/>
 *     &lt;enumeration value="ml"/>
 *     &lt;enumeration value="mn"/>
 *     &lt;enumeration value="mo"/>
 *     &lt;enumeration value="mr"/>
 *     &lt;enumeration value="ms"/>
 *     &lt;enumeration value="mt"/>
 *     &lt;enumeration value="my"/>
 *     &lt;enumeration value="na"/>
 *     &lt;enumeration value="nb"/>
 *     &lt;enumeration value="nd"/>
 *     &lt;enumeration value="ne"/>
 *     &lt;enumeration value="ng"/>
 *     &lt;enumeration value="nl"/>
 *     &lt;enumeration value="nn"/>
 *     &lt;enumeration value="no"/>
 *     &lt;enumeration value="nr"/>
 *     &lt;enumeration value="nv"/>
 *     &lt;enumeration value="ny"/>
 *     &lt;enumeration value="oc"/>
 *     &lt;enumeration value="oj"/>
 *     &lt;enumeration value="om"/>
 *     &lt;enumeration value="or"/>
 *     &lt;enumeration value="os"/>
 *     &lt;enumeration value="pa"/>
 *     &lt;enumeration value="pi"/>
 *     &lt;enumeration value="pl"/>
 *     &lt;enumeration value="ps"/>
 *     &lt;enumeration value="pt"/>
 *     &lt;enumeration value="qu"/>
 *     &lt;enumeration value="rm"/>
 *     &lt;enumeration value="rn"/>
 *     &lt;enumeration value="ro"/>
 *     &lt;enumeration value="ru"/>
 *     &lt;enumeration value="rw"/>
 *     &lt;enumeration value="sa"/>
 *     &lt;enumeration value="sc"/>
 *     &lt;enumeration value="sd"/>
 *     &lt;enumeration value="se"/>
 *     &lt;enumeration value="sg"/>
 *     &lt;enumeration value="si"/>
 *     &lt;enumeration value="sk"/>
 *     &lt;enumeration value="sl"/>
 *     &lt;enumeration value="sm"/>
 *     &lt;enumeration value="sn"/>
 *     &lt;enumeration value="so"/>
 *     &lt;enumeration value="sq"/>
 *     &lt;enumeration value="sr"/>
 *     &lt;enumeration value="ss"/>
 *     &lt;enumeration value="st"/>
 *     &lt;enumeration value="su"/>
 *     &lt;enumeration value="sv"/>
 *     &lt;enumeration value="sw"/>
 *     &lt;enumeration value="ta"/>
 *     &lt;enumeration value="te"/>
 *     &lt;enumeration value="tg"/>
 *     &lt;enumeration value="th"/>
 *     &lt;enumeration value="ti"/>
 *     &lt;enumeration value="tk"/>
 *     &lt;enumeration value="tl"/>
 *     &lt;enumeration value="tn"/>
 *     &lt;enumeration value="to"/>
 *     &lt;enumeration value="tr"/>
 *     &lt;enumeration value="ts"/>
 *     &lt;enumeration value="tt"/>
 *     &lt;enumeration value="tw"/>
 *     &lt;enumeration value="ty"/>
 *     &lt;enumeration value="ug"/>
 *     &lt;enumeration value="uk"/>
 *     &lt;enumeration value="ur"/>
 *     &lt;enumeration value="uz"/>
 *     &lt;enumeration value="ve"/>
 *     &lt;enumeration value="vi"/>
 *     &lt;enumeration value="vo"/>
 *     &lt;enumeration value="wa"/>
 *     &lt;enumeration value="wo"/>
 *     &lt;enumeration value="xh"/>
 *     &lt;enumeration value="yi"/>
 *     &lt;enumeration value="yo"/>
 *     &lt;enumeration value="za"/>
 *     &lt;enumeration value="zh"/>
 *     &lt;enumeration value="zu"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
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
     * Church Slavic; Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic
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
     * Norwegian Bokmål; Bokmål, Norwegian
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
     * Provençal; Occitan (post 1500) 
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
     * Volapük
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
        for (Language c: Language.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
