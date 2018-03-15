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
package jsr223.scala;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;


public class ScalaScriptEngineTest {

    public static final String TEST_SCALA_KICK_FILE_NAME = "/TestKickDynamic.scala";

    @Test
    public void testBindingsVariablesGetFromScript() throws Exception {

        // Create bindings with populated variables
        HashMap<String, Object> variablesMap = new HashMap<String, Object>(1);
        variablesMap.put("AA", "aa");
        variablesMap.put("BB", 3);
        HashMap<String, String> myMap = new HashMap<String, String>();
        myMap.put("cc_key", "cc_val");
        variablesMap.put("CC", myMap);
        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.VARIABLES_BINDING_NAME,
                                                                 (Object) variablesMap);

        // On the script side, display variables
        String scalaScript = "";
        scalaScript += "println(variables.get(\"AA\"))" + System.getProperty("line.separator");
        scalaScript += "println(variables.get(\"BB\"))" + System.getProperty("line.separator");
        scalaScript += "println(variables.get(\"CC\").get(\"cc_key\"))" + System.getProperty("line.separator");

        // Script execution
        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                                                            new PrintStream(output),
                                                            new PrintStream(output));

        if (res.getResult() == null) {
            fail("The result is null, the Script Engine is not executed correctly!");
        }

        // Check outputs
        String[] output_arr = output.toString().split(System.getProperty("line.separator"));
        assertEquals(output_arr[0], "aa");
        assertEquals(output_arr[1], "3");
        assertEquals(output_arr[2], "cc_val");
    }

    @Test
    public void testBindingsVariablesPutFromScript() throws Exception {

        // On the script side, put some variables
        String scalaScript = "";
        scalaScript += "variables.put(\"AA\",\"aa\")" + System.getProperty("line.separator");
        scalaScript += "variables.put(\"BB\",3)" + System.getProperty("line.separator");
        scalaScript += "var myMap = new java.util.HashMap[String, Object]();" + System.getProperty("line.separator");
        scalaScript += "myMap.put(\"cc_key\", \"cc_val\")" + System.getProperty("line.separator");
        scalaScript += "variables.put(\"CC\",myMap)" + System.getProperty("line.separator");

        HashMap<String, Object> variablesMap = new HashMap<String, Object>(1);

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.VARIABLES_BINDING_NAME,
                                                                 (Object) variablesMap);

        // Script execution
        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                                                            new PrintStream(output),
                                                            new PrintStream(output));

        if (res.getResult() == null) {
            fail("The result is null, the Script Engine is not executed correctly!");
        }

        // Check binding variables
        assertEquals(variablesMap.get("AA"), "aa");
        assertEquals(variablesMap.get("BB"), 3);
        assertEquals(((Map<String, String>) variablesMap.get("CC")).get("cc_key"), "cc_val");
    }

    @Test
    public void testBindingsResult() throws Exception {

        String scalaScript = "val result = 123";
        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);

        ScriptResult<Serializable> res = taskScript.execute();

        if (res.getResult() == null) {
            fail("The result is null, the Script Engine is not executed correctly!");
        }
        assertEquals("The result is returned correctly", 123, res.getResult());
    }

    @Test
    public void testBindingsSelected() throws Exception {

        Map<String, Object> aBindings = new HashMap<>();
        String scalaScript = "val selected = false";
        SelectionScript selectionScript = new SelectionScript(scalaScript,
                                                              ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                              true);

        ScriptResult<Boolean> res = selectionScript.execute(aBindings, System.out, System.err);

        if (res.getResult() == null) {
            fail("The result is null, the Script Engine is not executed correctly!");
        }
        assertEquals("The result should be false", false, res.getResult());
    }

    //public void testSelected() throws Exception {

    // RESULT METADATA

    @Test
    public void testScalaScriptFile() throws Exception {
        HashMap<String, Object> variablesMap = new HashMap<String, Object>(1);

        String scalaScript = "";

        scalaScript += CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream(TEST_SCALA_KICK_FILE_NAME),
                                                                  Charsets.UTF_8)) +
                       System.getProperty("line.separator");

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.VARIABLES_BINDING_NAME,
                                                                 (Object) variablesMap);

        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                                                            new PrintStream(output),
                                                            new PrintStream(output));

        System.out.println("Script output:");
        System.out.println(output.toString());

        System.out.println("Script Exception:");
        System.out.println(res.getException());

        if (res.getResult() == null) {
            fail("The result is null, the Script Engine is not executed correctly!");
        }
    }
}
