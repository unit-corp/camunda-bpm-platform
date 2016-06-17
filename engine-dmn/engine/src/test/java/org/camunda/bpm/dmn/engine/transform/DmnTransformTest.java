/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.engine.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.FirstHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.UniqueHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.dmn.engine.impl.type.DefaultTypeDefinition;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.junit.Test;

public class DmnTransformTest extends DmnEngineTest {

  public static final String TRANSFORM_DMN = "org/camunda/bpm/dmn/engine/transform/DmnTransformTest.dmn";
  public static final String REQUIRED_DECISIONS_DMN = "org/camunda/bpm/dmn/engine/api/RequiredDecision.dmn";
  public static final String MULTIPLE_REQUIRED_DECISIONS_DMN = "org/camunda/bpm/dmn/engine/api/MultipleRequiredDecisions.dmn";
  public static final String MULTI_LEVEL_MULTIPLE_REQUIRED_DECISIONS_DMN = "org/camunda/bpm/dmn/engine/api/MultiLevelMultipleRequiredDecisions.dmn";
  public static final String LOOP_REQUIRED_DECISIONS_DMN = "org/camunda/bpm/dmn/engine/api/LoopInRequiredDecision.dmn";
  public static final String SELF_REQUIRED_DECISIONS_DMN = "org/camunda/bpm/dmn/engine/api/SelfRequiredDecision.dmn";

  @Test
  public void shouldTransformDecisions() {
    List<DmnDecision> decisions = parseDecisionsFromFile(TRANSFORM_DMN);
    assertThat(decisions).hasSize(2);

    DmnDecision decision = decisions.get(0);
    assertThat(decision).isNotNull();
    assertThat(decision.getKey()).isEqualTo("decision1");
    assertThat(decision.getName()).isEqualTo("camunda");

    // decision2 should be ignored as it isn't supported by the DMN engine

    decision = decisions.get(1);
    assertThat(decision).isNotNull();
    assertThat(decision.getKey()).isEqualTo("decision3");
    assertThat(decision.getName()).isEqualTo("camunda");
  }

  @Test
  public void shouldTransformDecisionTables() {
    List<DmnDecision> decisions = parseDecisionsFromFile(TRANSFORM_DMN);
    DmnDecision decision = decisions.get(0);
    assertThat(decision.isDecisionTable()).isTrue();
    assertThat(decision).isInstanceOf(DmnDecisionImpl.class);

    DmnDecisionImpl decisionEntity = (DmnDecisionImpl) decision;
    DmnDecisionTableImpl decisionTable = decisionEntity.getDecisionTable();
    assertThat(decisionTable.getHitPolicyHandler()).isInstanceOf(UniqueHitPolicyHandler.class);

    decision = decisions.get(1);
    assertThat(decision.isDecisionTable()).isTrue();
    assertThat(decision).isInstanceOf(DmnDecisionImpl.class);

    decisionTable = ((DmnDecisionImpl) decision).getDecisionTable();
    assertThat(decisionTable.getHitPolicyHandler()).isInstanceOf(FirstHitPolicyHandler.class);
  }

  @Test
  public void shouldTransformInputs() {
    DmnDecisionImpl decisionEntity = (DmnDecisionImpl) parseDecisionFromFile("decision1", TRANSFORM_DMN);
    List<DmnDecisionTableInputImpl> inputs = decisionEntity.getDecisionTable().getInputs();
    assertThat(inputs).hasSize(2);

    DmnDecisionTableInputImpl input = inputs.get(0);
    assertThat(input.getId()).isEqualTo("input1");
    assertThat(input.getName()).isEqualTo("camunda");
    assertThat(input.getInputVariable()).isEqualTo("camunda");

    DmnExpressionImpl inputExpression = input.getExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getId()).isEqualTo("inputExpression");
    assertThat(inputExpression.getName()).isNull();
    assertThat(inputExpression.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(inputExpression.getExpression()).isEqualTo("camunda");

    assertThat(inputExpression.getTypeDefinition()).isNotNull();
    assertThat(inputExpression.getTypeDefinition().getTypeName()).isEqualTo("string");

    input = inputs.get(1);
    assertThat(input.getId()).isEqualTo("input2");
    assertThat(input.getName()).isNull();
    assertThat(input.getInputVariable()).isEqualTo(DmnDecisionTableInputImpl.DEFAULT_INPUT_VARIABLE_NAME);

    inputExpression = input.getExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getId()).isNull();
    assertThat(inputExpression.getName()).isNull();
    assertThat(inputExpression.getExpressionLanguage()).isNull();
    assertThat(inputExpression.getExpression()).isNull();

    assertThat(inputExpression.getTypeDefinition()).isNotNull();
    assertThat(inputExpression.getTypeDefinition()).isEqualTo(new DefaultTypeDefinition());
  }

  @Test
  public void shouldTransformOutputs() {
    DmnDecisionImpl decisionEntity = (DmnDecisionImpl) parseDecisionFromFile("decision1", TRANSFORM_DMN);
    List<DmnDecisionTableOutputImpl> outputs = decisionEntity.getDecisionTable().getOutputs();
    assertThat(outputs).hasSize(2);

    DmnDecisionTableOutputImpl output = outputs.get(0);
    assertThat(output.getId()).isEqualTo("output1");
    assertThat(output.getName()).isEqualTo("camunda");
    assertThat(output.getOutputName()).isEqualTo("camunda");
    assertThat(output.getTypeDefinition()).isNotNull();
    assertThat(output.getTypeDefinition().getTypeName()).isEqualTo("string");

    output = outputs.get(1);
    assertThat(output.getId()).isEqualTo("output2");
    assertThat(output.getName()).isNull();
    assertThat(output.getOutputName()).isEqualTo("out2");
    assertThat(output.getTypeDefinition()).isNotNull();
    assertThat(output.getTypeDefinition()).isEqualTo(new DefaultTypeDefinition());
  }

  @Test
  public void shouldTransformRules() {
    DmnDecisionImpl decisionEntity = (DmnDecisionImpl) parseDecisionFromFile("decision1", TRANSFORM_DMN);
    List<DmnDecisionTableRuleImpl> rules = decisionEntity.getDecisionTable().getRules();
    assertThat(rules).hasSize(1);

    DmnDecisionTableRuleImpl rule = rules.get(0);

    List<DmnExpressionImpl> conditions = rule.getConditions();
    assertThat(conditions).hasSize(2);

    DmnExpressionImpl condition = conditions.get(0);
    assertThat(condition.getId()).isEqualTo("inputEntry");
    assertThat(condition.getName()).isEqualTo("camunda");
    assertThat(condition.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(condition.getExpression()).isEqualTo("camunda");

    condition = conditions.get(1);
    assertThat(condition.getId()).isNull();
    assertThat(condition.getName()).isNull();
    assertThat(condition.getExpressionLanguage()).isNull();
    assertThat(condition.getExpression()).isNull();

    List<DmnExpressionImpl> conclusions = rule.getConclusions();
    assertThat(conclusions).hasSize(2);

    DmnExpressionImpl dmnOutputEntry = conclusions.get(0);
    assertThat(dmnOutputEntry.getId()).isEqualTo("outputEntry");
    assertThat(dmnOutputEntry.getName()).isEqualTo("camunda");
    assertThat(dmnOutputEntry.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(dmnOutputEntry.getExpression()).isEqualTo("camunda");

    dmnOutputEntry = conclusions.get(1);
    assertThat(dmnOutputEntry.getId()).isNull();
    assertThat(dmnOutputEntry.getName()).isNull();
    assertThat(dmnOutputEntry.getExpressionLanguage()).isNull();
    assertThat(dmnOutputEntry.getExpression()).isNull();
  }

  @Test
  public void shouldParseDecisionWithRequiredDecisions() {
    InputStream inputStream = IoUtil.fileAsStream(REQUIRED_DECISIONS_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    DmnDecision buyProductDecision = dmnEngine.parseDecision("buyProduct", modelInstance);
    assertDecision(buyProductDecision, "buyProduct");
    
    List<DmnDecision> buyProductrequiredDecisions = buyProductDecision.getRequiredDecisions();
    assertThat(buyProductrequiredDecisions.size()).isEqualTo(1);

    DmnDecision buyComputerDecision = buyProductrequiredDecisions.get(0);
    assertThat(buyComputerDecision.getKey()).isEqualTo("buyComputer");
    
    List<DmnDecision> buyComputerRequiredDecision = buyComputerDecision.getRequiredDecisions();
    assertThat(buyComputerRequiredDecision.size()).isEqualTo(1);
    
    DmnDecision buyElectronicDecision = buyComputerRequiredDecision.get(0);
    assertThat(buyElectronicDecision.getKey()).isEqualTo("buyElectronic");
    
    assertThat(buyElectronicDecision.getRequiredDecisions().size()).isEqualTo(0);
  }

  @Test
  public void shouldParseDecisionsWithRequiredDecisions() {
    InputStream inputStream = IoUtil.fileAsStream(REQUIRED_DECISIONS_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    List<DmnDecision> decisions = dmnEngine.parseDecisions(modelInstance);
    
    DmnDecision buyProductDecision = decisions.get(0);
    assertDecision(buyProductDecision, "buyProduct");
    List<DmnDecision> requiredProductDecisions = buyProductDecision.getRequiredDecisions();
    assertThat(requiredProductDecisions.size()).isEqualTo(1);
    assertThat(requiredProductDecisions.get(0).getKey()).isEqualTo("buyComputer");
    
    DmnDecision buyComputerDecision = decisions.get(1);
    assertDecision(buyComputerDecision, "buyComputer");
    List<DmnDecision> buyComputerRequiredDecisions = buyComputerDecision.getRequiredDecisions();
    assertThat(buyComputerRequiredDecisions.size()).isEqualTo(1);
    assertThat(buyComputerRequiredDecisions.get(0).getKey()).isEqualTo("buyElectronic");
    
    DmnDecision buyElectronicDecision = decisions.get(2);
    assertDecision(buyElectronicDecision, "buyElectronic");
    List<DmnDecision> buyElectronicRequiredDecisions = buyElectronicDecision.getRequiredDecisions();
    assertThat(buyElectronicRequiredDecisions.size()).isEqualTo(0);
  }

  @Test
  public void shouldParseDecisionWithMultipleRequiredDecisions() {
    InputStream inputStream = IoUtil.fileAsStream(MULTIPLE_REQUIRED_DECISIONS_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);
    DmnDecision decision = dmnEngine.parseDecision("car",modelInstance);
    List<DmnDecision> requiredDecisions = decision.getRequiredDecisions();
    assertThat(requiredDecisions.size()).isEqualTo(2);
    assertThat(requiredDecisions.get(0).getKey()).isEqualTo("carPrice");
    assertThat(requiredDecisions.get(1).getKey()).isEqualTo("carSpeed");
  }

  @Test
  public void shouldDetectLoopInParseDecisionWithRequiredDecision() {
    InputStream inputStream = IoUtil.fileAsStream(LOOP_REQUIRED_DECISIONS_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    try {
      decision = dmnEngine.parseDecision("buyProduct", modelInstance);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    } catch(DmnTransformException e) {
      Assertions.assertThat(e)
      .hasMessageStartingWith("DMN-02004")
      .hasMessageContaining("DMN-02015")
      .hasMessageContaining("decision 'buyElectronic' created a loop");
    }
  }

  @Test
  public void shouldDetectLoopInParseDecisionWithSelfRequiredDecision() {
    InputStream inputStream = IoUtil.fileAsStream(SELF_REQUIRED_DECISIONS_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    try {
      decision = dmnEngine.parseDecision("buyProduct", modelInstance);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    } catch(DmnTransformException e) {
      Assertions.assertThat(e)
      .hasMessageStartingWith("DMN-02004")
      .hasMessageContaining("DMN-02015")
      .hasMessageContaining("decision 'buyProduct' created a loop");
    }
  }

  @Test
  public void shouldNotDetectLoopInMultiLevelDecisionWithMultipleRequiredDecision() {
    InputStream inputStream = IoUtil.fileAsStream(MULTI_LEVEL_MULTIPLE_REQUIRED_DECISIONS_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    List<DmnDecision> decisions = dmnEngine.parseDecisions(modelInstance);
    assertThat(decisions.size()).isEqualTo(8);
  }
  
  protected void assertDecision(DmnDecision decision, String key) {
    assertThat(decision).isNotNull();
    assertThat(decision.getKey()).isEqualTo(key);
  }

}
