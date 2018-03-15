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

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;


@Log4j
@NoArgsConstructor
public class ScalaStringBindingsUtilities {

    public static final String VARIABLES_BINDING_NAME = "variables";

    public static boolean isSystemBinding(Map.Entry<String, Object> binding) {
        switch (binding.getKey()) {
            case "javax.script.filename":
                return true;
            default:
                return false;
        }
    }

    public static Bindings transformBindings(Bindings bindings) {
        if (bindings == null) {
            return null;
        }
        Bindings answer = new SimpleBindings();

        for (Map.Entry<String, Object> binding : bindings.entrySet()) {
            if (isSystemBinding(binding) || binding.getValue() == null) {
                answer.put(binding.getKey(), binding.getValue());

            } else {
                answer.put("_" + binding.getKey(), binding.getValue());
            }
        }
        return answer;
    }

    public static String generateWrappingScalaInstructions(Bindings bindings) {

        String answer = "";

        for (Map.Entry<String, Object> binding : bindings.entrySet()) {
            if (!isSystemBinding(binding) && binding.getValue() != null) {
                String bindingKey = binding.getKey();
                Object bindingValue = binding.getValue();

                answer += "val " + bindingKey + " = new DynamicWrapper(_" + bindingKey + ")" +
                          System.getProperty("line.separator");
            }
        }

        return answer;
    }

}
