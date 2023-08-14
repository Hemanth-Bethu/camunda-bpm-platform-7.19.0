/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.optimize;

import io.restassured.response.Response;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OptimizeCompletedHistoricIncidentRestServiceTest extends AbstractRestServiceTest {

  public static final String OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH =
    TEST_RESOURCE_ROOT_PATH + "/optimize/incident/completed";

  protected OptimizeService mockedOptimizeService;
  protected ProcessEngine namedProcessEngine;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Before
  public void setUpRuntimeData() {
    mockedOptimizeService = mock(OptimizeService.class);
    ProcessEngineConfigurationImpl mockedConfig = mock(ProcessEngineConfigurationImpl.class);

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getProcessEngineConfiguration()).thenReturn(mockedConfig);
    when(mockedConfig.getOptimizeService()).thenReturn(mockedOptimizeService);
  }

  @Test
  public void testNoQueryParameters() {
    given()
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH);

    verify(mockedOptimizeService).getCompletedHistoricIncidents(null, null, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testFinishedAfterQueryParameter() {
    Date now = new Date();
    given()
      .queryParam("finishedAfter", DATE_FORMAT_WITH_TIMEZONE.format(now))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH);

    verify(mockedOptimizeService).getCompletedHistoricIncidents(now, null, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testFinishedAtQueryParameter() {
    Date now = new Date();
    given()
      .queryParam("finishedAt", DATE_FORMAT_WITH_TIMEZONE.format(now))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH);

    verify(mockedOptimizeService).getCompletedHistoricIncidents(null, now, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testMaxResultsQueryParameter() {
    given()
      .queryParam("maxResults", 10)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH);

    verify(mockedOptimizeService).getCompletedHistoricIncidents(null, null, 10);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testQueryParameterCombination() {
    Date now = new Date();
    given()
      .queryParam("finishedAfter", DATE_FORMAT_WITH_TIMEZONE.format(now))
      .queryParam("finishedAt", DATE_FORMAT_WITH_TIMEZONE.format(now))
      .queryParam("maxResults", 10)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH);

    verify(mockedOptimizeService).getCompletedHistoricIncidents(now, now, 10);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testPresenceOfProcessInstanceIdProperty() {
    final HistoricIncidentEntity mock = mock(HistoricIncidentEntity.class);
    when(mock.getProcessInstanceId()).thenReturn(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    when(mockedOptimizeService.getCompletedHistoricIncidents(null, null, Integer.MAX_VALUE))
      .thenReturn(Collections.singletonList(mock));

    final Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(MediaType.APPLICATION_JSON)
      .when()
        .get(OPTIMIZE_COMPLETED_HISTORIC_INCIDENT_PATH);

    String content = response.asString();
    String processInstanceId = from(content).getString("[0].processInstanceId");

    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, processInstanceId);
  }

}
