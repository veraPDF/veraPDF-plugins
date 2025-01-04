/**
 * This file is part of metsMetadata-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * metsMetadata-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with metsMetadata-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * metsMetadata-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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
