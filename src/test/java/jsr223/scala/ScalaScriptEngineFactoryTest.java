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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import javax.script.ScriptEngine;

import org.hamcrest.Matchers;
import org.junit.Test;


/**
 * @author ActiveEon Team
 * @since 18/10/2017
 */
public class ScalaScriptEngineFactoryTest {
    @Test
    public void getExtensions() throws Exception {
        assertThat(scalaScriptEngineFactory.getExtensions(),
                   hasItems(containsString(".scala"), containsString(".scalaw")));
    }

    @Test
    public void getMimeTypes() throws Exception {
        assertThat(scalaScriptEngineFactory.getMimeTypes(), hasItem(containsString("scala")));
    }

    @Test
    public void getNames() throws Exception {
        assertThat(scalaScriptEngineFactory.getNames(), hasItem(containsString("scala")));
        assertThat(scalaScriptEngineFactory.getNames(), hasItem(containsString("Scala")));
    }

    @Test
    public void getParameter() throws Exception {

        assertThat(scalaScriptEngineFactory.getParameter(ScriptEngine.NAME),
                   Matchers.<Object> is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME)));

        assertThat(scalaScriptEngineFactory.getParameter(ScriptEngine.ENGINE),
                   Matchers.<Object> is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE)));

        assertThat(scalaScriptEngineFactory.getParameter(ScriptEngine.ENGINE_VERSION),
                   Matchers.<Object> is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE_VERSION)));

        assertThat(scalaScriptEngineFactory.getParameter(ScriptEngine.LANGUAGE),
                   Matchers.<Object> is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE)));

        assertThat(scalaScriptEngineFactory.getParameter(ScriptEngine.LANGUAGE_VERSION),
                   Matchers.<Object> is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION)));
    }

    private ScalaScriptEngineFactory scalaScriptEngineFactory = new ScalaScriptEngineFactory();

    @Test
    public void getEngineName() throws Exception {
        assertThat(scalaScriptEngineFactory.getEngineName(),
                   is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME)));
    }

    @Test
    public void getEngineVersion() throws Exception {
        assertThat(scalaScriptEngineFactory.getEngineVersion(),
                   is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE_VERSION)));
    }

    @Test
    public void getLanguageName() throws Exception {
        assertThat(scalaScriptEngineFactory.getLanguageName(),
                   is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE)));
    }

    @Test
    public void getLanguageVersion() throws Exception {
        assertThat(scalaScriptEngineFactory.getLanguageVersion(),
                   is(scalaScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION)));
    }

    @Test
    public void getScriptEngine() throws Exception {
        assertThat(scalaScriptEngineFactory.getScriptEngine() instanceof ScalaScriptEngine, is(true));
    }

}
