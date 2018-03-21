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

import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
public class ScalaStringBindingsUtilities {

    private static final Logger log = Logger.getLogger(ScalaStringBindingsUtilities.class);

    public static boolean isSystemVariable(String key) {
        switch (key) {
            case "javax.script.filename":
                return true;
            default:
                return false;
        }
    }

    public static Bindings transformBindings(Bindings bindings) {

        log.debug("Giving temporary names to bindings");
        if (bindings == null) {
            return null;
        }
        Bindings answer = new SimpleBindings();

        for (Map.Entry<String, Object> binding : bindings.entrySet()) {
            String key = binding.getKey();
            Object value = binding.getValue();
            if (isSystemVariable(key) || value == null) {
                answer.put(key, value);

            } else {
                answer.put("_" + key, value);
            }
        }
        return answer;
    }

    public static String generateWrappingScalaInstructions(Bindings bindings) {

        log.debug("Generating dynamic wrapping instructions");
        String answer = "";

        for (Map.Entry<String, Object> binding : bindings.entrySet()) {

            String key = binding.getKey();
            Object value = binding.getValue();

            if (!isSystemVariable(key) && value != null) {
                if (value.getClass().isArray()) {
                    answer += "val " + key + " = _" + key + ".asInstanceOf[Array[Object]].map(new DynamicWrapper(_))" +
                              System.getProperty("line.separator");
                } else
                    answer += "val " + key + " = new DynamicWrapper(_" + key + ")" +
                              System.getProperty("line.separator");
            }
        }

        log.debug(answer);
        return answer;
    }

}
