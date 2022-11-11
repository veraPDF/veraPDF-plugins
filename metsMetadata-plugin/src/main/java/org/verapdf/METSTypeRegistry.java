package org.verapdf;

import org.verapdf.xmp.impl.XMPSchemaRegistryImpl;

/**
 * @author Maksim Bezrukov
 */
public class METSTypeRegistry {

    public enum METSType {
        DMD_SEC,
        RIGHTS_MD,
        TECH_MD,
        SOURCE_MD,
        DIGIPROV_MD
    }

    public static METSType getTypeForProperty(String namespace, String name) {

        switch (namespace) {
            case XMPSchemaRegistryImpl.NS_DC:
                return getDCType(name);
            case XMPSchemaRegistryImpl.NS_XMP:
                return getXMPType(name);
            case XMPSchemaRegistryImpl.TYPE_IDENTIFIERQUAL:
                return METSType.TECH_MD;
            case XMPSchemaRegistryImpl.NS_XMP_RIGHTS:
                return METSType.RIGHTS_MD;
            case XMPSchemaRegistryImpl.NS_XMP_MM:
                return METSType.DIGIPROV_MD;
            case XMPSchemaRegistryImpl.NS_XMP_BJ:
            case XMPSchemaRegistryImpl.TYPE_PAGEDFILE:
            case XMPSchemaRegistryImpl.NS_PHOTOSHOP:
            case XMPSchemaRegistryImpl.NS_CAMERARAW:
            case XMPSchemaRegistryImpl.NS_EXIF:
            case XMPSchemaRegistryImpl.NS_EXIF_AUX:
                return METSType.TECH_MD;
            case XMPSchemaRegistryImpl.NS_DM:
                return getDMType(name);
            case XMPSchemaRegistryImpl.NS_PDF:
                return getPDFType(name);
            case XMPSchemaRegistryImpl.NS_TIFF:
                return getTIFFType(name);
            default:
                return METSType.TECH_MD;
        }
    }

    private static METSType getDCType(String name) {
        switch (name) {
            case "description":
            case "language":
            case "relation":
            case "source":
            case "subject":
            case "title":
            case "type":
                return METSType.DMD_SEC;
            case "contributor":
            case "creator":
            case "publisher":
            case "rights":
                return METSType.RIGHTS_MD;
            case "coverage":
            case "format":
            case "identifier":
                return METSType.TECH_MD;
            case "date":
                return METSType.DIGIPROV_MD;
            default:
                return METSType.TECH_MD;
        }
    }

    private static METSType getXMPType(String name) {
        switch (name) {
            case "Nickname":
                return METSType.DMD_SEC;
            case "BaseURL":
            case "Identifier":
            case "Label":
            case "Rating":
            case "Thumbnails":
                return METSType.TECH_MD;
            case "CreatorTool":
                return METSType.SOURCE_MD;
            case "Advisory":
            case "CreateDate":
            case "MetadataDate":
            case "ModifyDate":
                return METSType.DIGIPROV_MD;
            default:
                return METSType.TECH_MD;
        }
    }

    private static METSType getDMType(String name) {
        switch (name) {
            case "copyright":
                return METSType.RIGHTS_MD;
            case "projectRef":
                return METSType.SOURCE_MD;
            case "videoModDate":
            case "audioModDate":
            case "metadataModDate":
                return METSType.DIGIPROV_MD;
            default:
                return METSType.TECH_MD;
        }
    }

    private static METSType getPDFType(String name) {
        switch (name) {
            case "Keywords":
            case "PDFVersion":
                return METSType.TECH_MD;
            case "Producer":
                return METSType.SOURCE_MD;
            default:
                return METSType.TECH_MD;
        }
    }

    private static METSType getTIFFType(String name) {
        switch (name) {
            case "ImageDescription":
                return METSType.DMD_SEC;
            case "Artist":
            case "Copyright":
                return METSType.RIGHTS_MD;
            case "Software":
                return METSType.SOURCE_MD;
            case "DateTime":
                return METSType.DIGIPROV_MD;
            default:
                return METSType.TECH_MD;
        }
    }
}
