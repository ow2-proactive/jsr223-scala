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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
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
        scalaScript += "println(" + SchedulerConstants.VARIABLES_BINDING_NAME + ".get(\"AA\"))" +
                       System.getProperty("line.separator");
        scalaScript += "println(" + SchedulerConstants.VARIABLES_BINDING_NAME + ".get(\"BB\"))" +
                       System.getProperty("line.separator");
        scalaScript += "println(" + SchedulerConstants.VARIABLES_BINDING_NAME + ".get(\"CC\").get(\"cc_key\"))" +
                       System.getProperty("line.separator");

        // Script execution
        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                                                            new PrintStream(output),
                                                            new PrintStream(output));

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
        scalaScript += SchedulerConstants.VARIABLES_BINDING_NAME + ".put(\"AA\",\"aa\")" +
                       System.getProperty("line.separator");
        scalaScript += SchedulerConstants.VARIABLES_BINDING_NAME + ".put(\"BB\",3)" +
                       System.getProperty("line.separator");
        scalaScript += "var myMap = new java.util.HashMap[String, Object]();" + System.getProperty("line.separator");
        scalaScript += "myMap.put(\"cc_key\", \"cc_val\")" + System.getProperty("line.separator");
        scalaScript += SchedulerConstants.VARIABLES_BINDING_NAME + ".put(\"CC\",myMap)" +
                       System.getProperty("line.separator");

        HashMap<String, Object> variablesMap = new HashMap<String, Object>(1);

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.VARIABLES_BINDING_NAME,
                                                                 (Object) variablesMap);

        // Script execution
        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

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

        assertEquals("The result is returned correctly", 123, res.getResult());
    }

    @Test
    public void testBindingsSelected() throws Exception {

        String scalaScript = "val selected = false";
        SelectionScript selectionScript = new SelectionScript(scalaScript,
                                                              ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                              true);

        ScriptResult<Boolean> res = selectionScript.execute();

        assertEquals("The result should be false", false, res.getResult());
    }

    @Test
    public void testError() throws Exception {

        String errorInstruction = "notExistingFunction";
        String messageAfter = "Must not be displayed";

        String scalaScript = errorInstruction + System.getProperty("line.separator") + "println(\"" + messageAfter +
                             "\")";

        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute();

        Assert.assertNotNull("The script exception must not be null", res.getException());
        Assert.assertTrue("The script exception must contain the error statement",
                          res.getException().getMessage().contains(errorInstruction));
        Assert.assertFalse("The script output must not contain the message after the error",
                           output.toString().contains(messageAfter));
    }

    @Test
    public void testLoop() throws Exception {
        String loopScalaScript = "val loop = true";

        FlowScript loopScript = FlowScript.createLoopFlowScript(loopScalaScript,
                                                                ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                                "aTarget");
        ScriptResult<FlowAction> res = loopScript.execute();

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      FlowActionType.LOOP,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      "aTarget",
                                      res.getResult().getTarget());

    }

    @Test
    public void testReplicate() throws Exception {
        String replicateRScript = "val runs = 2";

        FlowScript replicateScript = FlowScript.createReplicateFlowScript(replicateRScript,
                                                                          ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        ScriptResult<FlowAction> res = replicateScript.execute();

        org.junit.Assert.assertEquals("The result should contain the replicate runs",
                                      2,
                                      res.getResult().getDupNumber());

    }

    @Test
    public void testBranch() throws Exception {
        String branchRScript = "val branch=\"if\"";

        FlowScript loopScript = FlowScript.createIfFlowScript(branchRScript,
                                                              ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                              "ifTarget",
                                                              "elseTarget",
                                                              "continuationTarget");
        ScriptResult<FlowAction> res = loopScript.execute();

        org.junit.Assert.assertEquals("The result should contain the branch decision",
                                      FlowActionType.IF,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the if target",
                                      "ifTarget",
                                      res.getResult().getTarget());
    }

    @Test
    public void testDefaultResult() throws Exception {
        String rScript = "val a = 3";

        SimpleScript ss = new SimpleScript(rScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();

        org.junit.Assert.assertEquals("The default script result is true if result is not set in the script",
                                      Boolean.TRUE,
                                      (Boolean) res.getResult());
    }

    @Test
    public void testResults() throws Exception {

        int NB_TASKS = 10;

        String[] taskNames = new String[NB_TASKS];
        TaskResult[] results = new TaskResult[NB_TASKS];
        double[] resValues = new double[NB_TASKS];
        String scalaScript = "val result = Array (";

        for (int i = 1; i <= NB_TASKS; i++) {
            taskNames[i - 1] = "task" + i;
            resValues[i - 1] = i;
            TaskId id = new MockedTaskId(taskNames[i - 1]);
            results[i - 1] = new MockedTaskResult(id, resValues[i - 1]);
            scalaScript += "results(" + (i - 1) + ").getValue().value" + (i < NB_TASKS ? "," : "");
        }
        scalaScript += ")";

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.RESULTS_VARIABLE, results);

        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        Object[] value = (Object[]) res.getResult();
        assertEquals(Arrays.toString(value), Arrays.toString(resValues));
    }

    @Test
    public void testEmptyBindingsMetadata() throws Exception {

        HashMap<String, Object> metadata = new HashMap<String, Object>(1);
        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.RESULT_METADATA_VARIABLE,
                                                                 (Object) metadata);

        String scalaScript = "";
        scalaScript += SchedulerConstants.RESULT_METADATA_VARIABLE + ".put(\"AA\",\"aa\")" +
                       System.getProperty("line.separator");
        scalaScript += "val result = true";

        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        org.junit.Assert.assertEquals("The result should be true", true, res.getResult());
        org.junit.Assert.assertEquals("The metadata map should contain the metadata defined in the script",
                                      "aa",
                                      metadata.get("AA"));
    }

    @Test
    public void testBindingsMetadata() throws Exception {

        // Create bindings with populated variables
        HashMap<String, Object> metadata = new HashMap<String, Object>(1);
        metadata.put("AA", "aa");
        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.RESULT_METADATA_VARIABLE,
                                                                 (Object) metadata);

        // On the script side, display variables
        String scalaScript = "";
        scalaScript += SchedulerConstants.RESULT_METADATA_VARIABLE + ".put(\"BB\",\"bb\")" +
                       System.getProperty("line.separator");
        scalaScript += "val result = true";

        // Script execution
        SimpleScript ss = new SimpleScript(scalaScript, ScalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        org.junit.Assert.assertEquals("The result should be true", true, res.getResult());
        org.junit.Assert.assertEquals("The metadata map should contain the metadata defined in the script",
                                      "aa",
                                      metadata.get("AA"));
        org.junit.Assert.assertEquals("The metadata map should contain the metadata defined in the script",
                                      "bb",
                                      metadata.get("BB"));
    }

    public final class MockedTaskResult implements TaskResult {
        private TaskId taskId;

        private Serializable value;

        public MockedTaskResult(TaskId taskId, Serializable value) {
            this.taskId = taskId;
            this.value = value;
        }

        @Override
        public FlowAction getAction() {
            return null;
        }

        @Override
        public Throwable getException() {
            return null;
        }

        @Override
        public TaskLogs getOutput() {
            return null;
        }

        @Override
        public Map<String, byte[]> getPropagatedVariables() {
            return null;
        }

        @Override
        public byte[] getSerializedValue() {
            return null;
        }

        @Override
        public Map<String, String> getMetadata() {
            return null;
        }

        @Override
        public TaskId getTaskId() {
            return this.taskId;
        }

        @Override
        public boolean hadException() {
            return false;
        }

        public boolean isRaw() {
            return false;
        }

        @Override
        public Map<String, Serializable> getResultMap() {
            throw new UnsupportedOperationException();
        }

        public Map<String, Serializable> getVariables() {
            return null;
        }

        public Serializable getValue() {
            return this.value;
        }

        @Override
        public Serializable value() throws Throwable {
            return this.value;
        }
    }

    final class MockedTaskId implements TaskId {
        private String name;

        public MockedTaskId(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(TaskId o) {
            return 0;
        }

        @Override
        public int getIterationIndex() {
            return 0;
        }

        @Override
        public JobId getJobId() {
            return null;
        }

        @Override
        public String getReadableName() {
            return this.name;
        }

        @Override
        public int getReplicationIndex() {
            return 0;
        }

        @Override
        public String value() {
            return this.name;
        }

        @Override
        public long longValue() {
            return 0;
        }

        public String getTag() {
            return "";
        }
    }
}
