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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import javax.script.*;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.TaskScript;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import jsr223.scala.utils.ScalaStreamUtilities;
import jsr223.scala.utils.ScalaStringBindingsUtilities;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */

public class ScalaScriptEngine extends AbstractScriptEngine {

    private static final Logger log = Logger.getLogger(ScalaScriptEngine.class);

    public static final String DYNAMIC_WRAPPER_SCALA_FILE_NAME = "/KickDynamic.scala";

    private ScriptEngine engine;

    public ScalaScriptEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {

        this.engine.setBindings(ScalaStringBindingsUtilities.transformBindings(context.getBindings(ScriptContext.ENGINE_SCOPE)),
                                ScriptContext.ENGINE_SCOPE);

        /////////// BUILD SCRIPT

        String scriptToExecute = "";
        try {
            scriptToExecute += CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream(DYNAMIC_WRAPPER_SCALA_FILE_NAME),
                                                                          Charsets.UTF_8)) +
                               System.getProperty("line.separator");

        } catch (IOException e) {
            throw new ScriptException("Cannot find resource : " + DYNAMIC_WRAPPER_SCALA_FILE_NAME +
                                      ". Failed to execute Scala with exception: " + e);
        }

        scriptToExecute += ScalaStringBindingsUtilities.generateWrappingScalaInstructions(context.getBindings(ScriptContext.ENGINE_SCOPE)) +
                           System.getProperty("line.separator");
        scriptToExecute += script;

        /////////// EXECUTE

        // Attach streams
        ScalaStreamUtilities.attachStreams(this.engine.getContext(),
                                           context.getWriter(),
                                           context.getErrorWriter(),
                                           context.getReader());

        this.engine.eval(scriptToExecute);

        /////////// GET/UPDATE BINDINGS

        Bindings contextBindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        Object resultValue = this.getResultAndUpdateBindings(contextBindings);
        this.getValAndUpdateBindings(SelectionScript.RESULT_VARIABLE, contextBindings);
        this.getValAndUpdateBindings(FlowScript.loopVariable, contextBindings);
        this.getValAndUpdateBindings(FlowScript.branchSelectionVariable, contextBindings);
        this.getValAndUpdateBindings(FlowScript.replicateRunsVariable, contextBindings);
        this.getValAndUpdateBindings(SchedulerConstants.VARIABLES_BINDING_NAME, contextBindings);
        this.getValAndUpdateBindings(SchedulerConstants.VARIABLES_BINDING_NAME, contextBindings);
        this.getValAndUpdateBindings(SchedulerConstants.RESULT_METADATA_VARIABLE, contextBindings);

        return resultValue;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        StringWriter stringWriter = new StringWriter();

        try {
            ScalaStreamUtilities.pipe(reader, stringWriter);
        } catch (IOException e) {
            log.warn("Failed to convert Reader into StringWriter. Not possible to execute Scala script.");
            log.debug("Failed to convert Reader into StringWriter. Not possible to execute Scala script.", e);
        }

        return eval(stringWriter.toString(), context);
    }

    private Object getResultAndUpdateBindings(Bindings bindings) {

        Object resultValue = this.get(TaskScript.RESULT_VARIABLE);
        if (resultValue == null) {
            resultValue = true; // TaskResult.getResult() returns true by default
        }

        bindings.put(TaskScript.RESULT_VARIABLE, resultValue);
        return resultValue;
    }

    private void getValAndUpdateBindings(String variableName, Bindings bindings) {
        Object val = this.get(variableName);
        if (val != null) {
            bindings.put(variableName, val);
        }
    }

    @Override
    public Bindings createBindings() {
        return this.engine.createBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return this.engine.getFactory();
    }

    @Override
    public Object get(String key) {
        // First try to get value from bindings
        Object value = this.engine.get(key);

        // NB: Extracting values from Scala Script Engine are a little tricky.
        // Values (variables) initialised or computed in the script are
        // not added to the bindings of the CompiledScript AFAICT. Therefore
        // the only way to extract them is to evaluate the variable and
        // capture the return. If it evaluates to null or throws a
        // a ScriptException, we simply return null.
        if (value == null)
            try {
                value = this.engine.eval(key);
            } catch (ScriptException ignored) {
                // HACK: Explicitly ignore ScriptException, which arises if
                // key is not found. This feels bad because it fails silently
                // for the user, but it mimics behaviour in other script langs.
            }

        return value;
    }
}
