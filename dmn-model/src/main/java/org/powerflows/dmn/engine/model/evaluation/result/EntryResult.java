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

package org.powerflows.dmn.engine.model.evaluation.result;


import lombok.ToString;
import org.powerflows.dmn.engine.model.builder.AbstractBuilder;

import java.io.Serializable;

/**
 * Represents entry evaluation result.
 */
@ToString
public class EntryResult implements Serializable {

    private static final long serialVersionUID = 1;

    private String name;
    private Serializable value;

    private EntryResult() {
    }

    public String getName() {
        return name;
    }

    public Serializable getValue() {
        return value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<EntryResult> {

        private Builder() {
        }

        @Override
        protected void initProduct() {
            this.product = new EntryResult();
        }

        public Builder name(String name) {
            this.product.name = name;

            return this;
        }

        public Builder value(Serializable value) {
            this.product.value = value;

            return this;
        }
    }
}
