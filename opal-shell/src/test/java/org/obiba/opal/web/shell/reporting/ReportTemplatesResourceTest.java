/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.shell.reporting;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import junit.framework.Assert;

import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

import com.google.common.collect.Maps;

public class ReportTemplatesResourceTest {

  public static final String BASE_URI = "http://localhost:8888/ws";

  private OpalRuntime opalRuntimeMock;

  private OpalConfiguration opalConfiguration;

  private CommandRegistry commandRegistry;

  private CommandSchedulerService commandSchedulerServiceMock;

  Set<ReportTemplate> reportTemplates;

  @Before
  public void setUp() {
    opalRuntimeMock = createMock(OpalRuntime.class);
    opalConfiguration = new OpalConfiguration();
    commandRegistry = createMock(CommandRegistry.class);

    commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template1", "reports");
    commandSchedulerServiceMock.scheduleCommand("template1", "reports", "schedule");

    reportTemplates = new LinkedHashSet<ReportTemplate>();
    reportTemplates.add(getReportTemplate("template1"));
    reportTemplates.add(getReportTemplate("template2"));
    reportTemplates.add(getReportTemplate("template3"));
    reportTemplates.add(getReportTemplate("template4"));
    opalConfiguration.setReportTemplates(reportTemplates);

    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).anyTimes();
  }

  @Test
  public void testGetReportTemplates_RetrieveSetOfTemplates() {
    replay(opalRuntimeMock);

    ReportTemplatesResource reportTemplateResource = new ReportTemplatesResource(opalRuntimeMock, commandSchedulerServiceMock, commandRegistry);
    Set<ReportTemplateDto> reportTemplatesDtos = reportTemplateResource.getReportTemplates();

    Assert.assertEquals(4, reportTemplates.size());
    Assert.assertEquals(4, reportTemplatesDtos.size());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) reportTemplatesDtos.toArray()[0];
    Assert.assertEquals("template1", reportTemplateDto.getName());
    Assert.assertEquals("design", reportTemplateDto.getDesign());
    Assert.assertEquals("format", reportTemplateDto.getFormat());
    Assert.assertEquals("schedule", reportTemplateDto.getCron());
    Assert.assertEquals(2, reportTemplateDto.getParametersList().size());

    verify(opalRuntimeMock);
  }

  @Test
  public void testUpdateReportTemplate_NewReportTemplateCreated() {
    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getAbsolutePath()).andReturn(UriBuilderImpl.fromUri(BASE_URI).build("")).atLeastOnce();

    opalRuntimeMock.writeOpalConfiguration();
    expectLastCall().once();

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template9", "reports");
    commandSchedulerServiceMock.scheduleCommand("template9", "reports", "schedule");

    @SuppressWarnings("unchecked")
    Command<Object> commandMock = createMock(Command.class);
    commandSchedulerServiceMock.addCommand("template9", "reports", commandMock);

    expect(commandRegistry.newCommand("report")).andReturn(commandMock);

    replay(opalRuntimeMock, uriInfoMock, commandSchedulerServiceMock, commandRegistry);

    ReportTemplatesResource reportTemplatesResource = new ReportTemplatesResource(opalRuntimeMock, commandSchedulerServiceMock, commandRegistry);
    Response response = reportTemplatesResource.createReportTemplate(uriInfoMock, Dtos.asDto(getReportTemplate("template9")));

    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    Assert.assertEquals(BASE_URI, response.getMetadata().get("location").get(0).toString());

    verify(opalRuntimeMock, uriInfoMock, commandSchedulerServiceMock, commandRegistry);
  }

  private ReportTemplate getReportTemplate(String name) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(name);
    reportTemplate.setDesign("design");
    reportTemplate.setFormat("format");
    reportTemplate.setSchedule("schedule");
    Map<String, String> params = Maps.newHashMap();
    params.put("param1", "value1");
    params.put("param2", "value2");
    reportTemplate.setParameters(params);
    return reportTemplate;
  }

}