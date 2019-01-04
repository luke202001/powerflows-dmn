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

package org.powerflows.dmn.engine.reader;


import org.powerflows.dmn.engine.model.decision.Decision;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface DecisionReader {

    Optional<Decision> read(InputStream inputStream);

    Optional<Decision> read(InputStream inputStream, String decisionId);

    List<Decision> readAll(InputStream inputStream);

}
