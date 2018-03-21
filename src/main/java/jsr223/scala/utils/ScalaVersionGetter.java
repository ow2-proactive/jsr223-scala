/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package jsr223.scala.utils;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import scala.tools.nsc.interpreter.Scripted;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
public class ScalaVersionGetter {

    private static final Logger log = Logger.getLogger(ScalaVersionGetter.class);

    public static final String SCALA_VERSION_IF_NOT_INSTALLED = "Could not determine version";

    public static String getScalaVersion() {

        String result = SCALA_VERSION_IF_NOT_INSTALLED;

        log.debug("Retrieving the scala version from the jar manifest file");
        String classPath = Scripted.class.getResource(Scripted.class.getSimpleName() + ".class").toString();
        String libPath = classPath.substring(0, classPath.lastIndexOf("!"));
        String filePath = libPath + "!/META-INF/MANIFEST.MF";

        log.debug("Retrieving the scala version from " + filePath);
        Manifest manifest = null;
        try {
            manifest = new Manifest(new URL(filePath).openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Attributes attr = manifest.getMainAttributes();
        String bundle_version = attr.getValue("Bundle-Version");

        log.debug("Retrieving version from " + bundle_version);
        Pattern pattern = Pattern.compile(".*(\\d+[.]\\d+[.]\\d+).*");
        Matcher matcher = pattern.matcher(bundle_version);
        if (matcher.find())
            result = matcher.group(1);

        return result;
    }

}
