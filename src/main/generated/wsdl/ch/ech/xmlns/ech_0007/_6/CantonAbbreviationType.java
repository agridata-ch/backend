
package ch.ech.xmlns.ech_0007._6;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * &lt;p&gt;Java-Klasse für cantonAbbreviationType.&lt;/p&gt;
 * 
 * &lt;p&gt;Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.&lt;/p&gt;
 * &lt;pre&gt;{&#064;code
 * &lt;simpleType name="cantonAbbreviationType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ZH"/&gt;
 *     &lt;enumeration value="BE"/&gt;
 *     &lt;enumeration value="LU"/&gt;
 *     &lt;enumeration value="UR"/&gt;
 *     &lt;enumeration value="SZ"/&gt;
 *     &lt;enumeration value="OW"/&gt;
 *     &lt;enumeration value="NW"/&gt;
 *     &lt;enumeration value="GL"/&gt;
 *     &lt;enumeration value="ZG"/&gt;
 *     &lt;enumeration value="FR"/&gt;
 *     &lt;enumeration value="SO"/&gt;
 *     &lt;enumeration value="BS"/&gt;
 *     &lt;enumeration value="BL"/&gt;
 *     &lt;enumeration value="SH"/&gt;
 *     &lt;enumeration value="AR"/&gt;
 *     &lt;enumeration value="AI"/&gt;
 *     &lt;enumeration value="SG"/&gt;
 *     &lt;enumeration value="GR"/&gt;
 *     &lt;enumeration value="AG"/&gt;
 *     &lt;enumeration value="TG"/&gt;
 *     &lt;enumeration value="TI"/&gt;
 *     &lt;enumeration value="VD"/&gt;
 *     &lt;enumeration value="VS"/&gt;
 *     &lt;enumeration value="NE"/&gt;
 *     &lt;enumeration value="GE"/&gt;
 *     &lt;enumeration value="JU"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * }&lt;/pre&gt;
 * 
 */
@XmlType(name = "cantonAbbreviationType", namespace = "http://www.ech.ch/xmlns/eCH-0007/6")
@XmlEnum
public enum CantonAbbreviationType {

    ZH,
    BE,
    LU,
    UR,
    SZ,
    OW,
    NW,
    GL,
    ZG,
    FR,
    SO,
    BS,
    BL,
    SH,
    AR,
    AI,
    SG,
    GR,
    AG,
    TG,
    TI,
    VD,
    VS,
    NE,
    GE,
    JU;

    public String value() {
        return name();
    }

    public static CantonAbbreviationType fromValue(String v) {
        return valueOf(v);
    }

}
