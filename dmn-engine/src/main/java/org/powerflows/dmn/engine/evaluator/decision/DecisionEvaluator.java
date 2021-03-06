/*
 * Copyright (c) 2018-present PowerFlows.org - all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.powerflows.dmn.engine.evaluator.decision;


import lombok.extern.slf4j.Slf4j;
import org.powerflows.dmn.engine.evaluator.context.EvaluationContext;
import org.powerflows.dmn.engine.evaluator.exception.EvaluationException;
import org.powerflows.dmn.engine.evaluator.rule.RuleEvaluator;
import org.powerflows.dmn.engine.model.decision.Decision;
import org.powerflows.dmn.engine.model.decision.HitPolicy;
import org.powerflows.dmn.engine.model.decision.expression.Expression;
import org.powerflows.dmn.engine.model.decision.expression.ExpressionType;
import org.powerflows.dmn.engine.model.decision.field.Input;
import org.powerflows.dmn.engine.model.decision.field.Output;
import org.powerflows.dmn.engine.model.decision.rule.Rule;
import org.powerflows.dmn.engine.model.evaluation.result.DecisionResult;
import org.powerflows.dmn.engine.model.evaluation.result.RuleResult;
import org.powerflows.dmn.engine.model.evaluation.variable.DecisionVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Evaluates decisions.
 */
@Slf4j
public class DecisionEvaluator {

    private static final Set<HitPolicy> UNSUPPORTED_HIT_POLICIES = Collections.unmodifiableSet(EnumSet.of(HitPolicy.OUTPUT_ORDER, HitPolicy.PRIORITY));
    private final RuleEvaluator ruleEvaluator;

    /**
     *
     * @param ruleEvaluator rule evaluator to use.
     */
    public DecisionEvaluator(RuleEvaluator ruleEvaluator) {
        this.ruleEvaluator = ruleEvaluator;
    }

    /**
     * Evaluates decision using given variables context.
     *
     * @param decision decision to evaluate
     * @param decisionVariables variables to use during evaluation
     * @return decision evaluation result
     */
    public DecisionResult evaluate(final Decision decision, final DecisionVariables decisionVariables) {
        if (decision == null) {
            throw new NullPointerException("Decision can not be null");
        }

        if (decisionVariables == null) {
            throw new NullPointerException("Decision variables can not be null");
        }

        if (isUnsupportedSupportedHitPolicy(decision.getHitPolicy())) {
            throw new UnsupportedOperationException("HitPolicy " + decision.getHitPolicy() + " is not supported");
        }

        log.info("Starting evaluation of decision: {} with decision variables: {}", decision, decisionVariables);

        validateDecisionVariables(decision.getInputs(), decisionVariables);

        final List<RuleResult> ruleResults = new ArrayList<>();
        final boolean singleNonUniqueRuleResultExpected = isSingleNonUniqueRuleResultExpected(decision);

        final Map<String, Input> inputs = decision
                .getInputs()
                .stream()
                .collect(Collectors.toMap(Input::getName, Function.identity()));

        final Map<String, Output> outputs = decision
                .getOutputs()
                .stream()
                .collect(Collectors.toMap(Output::getName, Function.identity()));

        final EvaluationContext evaluationContext = new EvaluationContext(decisionVariables);

        for (Rule rule : decision.getRules()) {
            final RuleResult ruleResult = ruleEvaluator.evaluate(rule, inputs, outputs, evaluationContext);

            if (ruleResult != null) {
                ruleResults.add(ruleResult);

                if (singleNonUniqueRuleResultExpected) {
                    break;
                }
            }
        }

        if (isUniqueRuleResultExpected(decision) && isNonUniqueRuleResult(ruleResults)) {
            throw new EvaluationException("Unique result is expected");
        }

        final DecisionResult decisionResult = DecisionResult.builder().ruleResults(ruleResults).build();

        log.info("Evaluated decision result: {}", decisionResult);

        return decisionResult;
    }

    private boolean isUnsupportedSupportedHitPolicy(HitPolicy hitPolicy) {
        return UNSUPPORTED_HIT_POLICIES.contains(hitPolicy);
    }

    private void validateDecisionVariables(final List<Input> inputs, final DecisionVariables decisionVariables) {
        final String invalidInputNames = inputs
                .stream()
                .filter(input -> !isLiteral(input.getExpression()))
                .filter(input -> decisionVariables.isPresent(input.getName()))
                .map(Input::getName)
                .collect(Collectors.joining(","));

        if (!invalidInputNames.isEmpty()) {
            throw new EvaluationException("Can not apply decision variables to inputs '" + invalidInputNames + "'. Only to inputs with literal expression possible.");
        }
    }

    private boolean isLiteral(final Expression expression) {
        return ExpressionType.LITERAL.equals(expression.getType()) || ExpressionType.FEEL.equals(expression.getType());
    }

    private boolean isSingleNonUniqueRuleResultExpected(final Decision decision) {
        return HitPolicy.FIRST.equals(decision.getHitPolicy()) || HitPolicy.ANY.equals(decision.getHitPolicy());
    }

    private boolean isUniqueRuleResultExpected(final Decision decision) {
        return HitPolicy.UNIQUE.equals(decision.getHitPolicy());
    }

    private boolean isNonUniqueRuleResult(final List<RuleResult> ruleResults) {
        return ruleResults.size() > 1;
    }
}
